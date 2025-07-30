package com.lazhoff.mule.secureprops.crypto;

import com.lazhoff.mule.secureprops.adapter.SecurePropertiesConfig;
import com.lazhoff.mule.secureprops.adapter.SecurePropertiesToolAdapter;
import com.lazhoff.mule.secureprops.adapter.SecurePropertiesToolRunner;
import com.lazhoff.mule.secureprops.util.TempFileManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.lazhoff.mule.secureprops.crypto.CryptoExecutionResult.Status.*;

public class DefaultCryptoServiceYaml implements ICryptoService, SupportsPreview {

    private static final Logger logger = LogManager.getLogger(DefaultCryptoServiceYaml.class);

    private final CryptoConfig config;
    private final SecurePropertiesToolRunner runner;
    private final TempFileManager tempFileManager;


    public DefaultCryptoServiceYaml(CryptoConfig config) {
        this.config = config;
        this.tempFileManager = new TempFileManager(config.getTempFolder());
        this.runner = new SecurePropertiesToolAdapter();
    }

    @Override
    public CryptoExecutionResult.Status encrypt() {
        Path file = Path.of(config.getFilePath());

        if (isAlreadyEncrypted(file)) {
            logger.warn("File {} appears to be already encrypted. Skipping encryption.", file);
            return UNCHANGED;
        }

        return process(SecurePropertiesConfig.Action.ENCRYPT);
    }

    @Override
    public CryptoExecutionResult.Status decrypt() {
        return process(SecurePropertiesConfig.Action.DECRYPT);
    }

    private CryptoExecutionResult.Status process(SecurePropertiesConfig.Action action) {
        Path file = Path.of(config.getFilePath());
        Path temp = null;

        try {
            //temp = Files.createTempFile("secure-yaml-", ".tmp.yaml");
            temp = tempFileManager.createTempSecureYamlFile();

            logger.info("{} started: {}", action, file);

            SecurePropertiesConfig secureConfig = new SecurePropertiesConfig(
                    SecurePropertiesConfig.Type.FILE,
                    action,
                    config.getAlgorithm(),
                    config.getMode(),
                    config.getKey(),
                    null,
                    file.toString(),
                    temp.toString(),
                    config.isUseRandomIV()
            );

            SecurePropertiesToolAdapter.ExecutionResult result = runner.run(secureConfig);

            if (result.exitCode != 0) {
                logger.error("{} failed: {}", action, result.error);
                return FAILED;
            }

            if (config.isDryRun()) {
                logger.info("Dry run enabled - original file was not modified: {}", file);
                String original = Files.readString(file);
                String processed = Files.readString(temp);
                return original.equals(processed) ? UNCHANGED : DRYRUN_CHANGED;
            }

            if (config.isBackup()) {
                Path backup = file.resolveSibling(file.getFileName() + ".bak");
                Files.copy(file, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("Backup created at: {}", backup);
            }

            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("{} succeeded. File updated: {}", action, file);
            return SUCCESS;

        } catch (Exception e) {
            logger.error("Exception during {}: {}", action, e.toString(), e);
            return FAILED;
        } finally {
            deleteTempFile(temp);
        }
    }

    private void deleteTempFile(Path temp) {
        if (temp != null) {
            try {
                Files.deleteIfExists(temp);
                logger.debug("Temporary file deleted: {}", temp);
            } catch (IOException e) {
                logger.warn("Could not delete temp file: {}", temp);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isAlreadyEncrypted(Path file) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Object root = mapper.readValue(file.toFile(), Object.class);
            return containsEncryptedValue(root);
        } catch (IOException e) {
            logger.warn("Failed to parse YAML file for encryption check: {}", e.toString());
            return false;
        }
    }

    private boolean containsEncryptedValue(Object node) {
        if (node instanceof String str) {
            logger.debug("yaml value found: {}", str);
            return str.trim().startsWith("![");
        } else if (node instanceof Map<?, ?> map) {
            return map.values().stream().anyMatch(this::containsEncryptedValue);
        } else if (node instanceof Iterable<?> list) {
            for (Object item : list) {
                if (containsEncryptedValue(item)) return true;
            }
        }
        return false;
    }

    // === SupportsPreview implementation ===

    @Override
    public String previewEncrypt(String input) throws Exception {
        return transformYamlContent(input, SecurePropertiesConfig.Action.ENCRYPT);
    }

    @Override
    public String previewDecrypt(String input) throws Exception {
        return transformYamlContent(input, SecurePropertiesConfig.Action.DECRYPT);
    }

    private String transformYamlContent(String input, SecurePropertiesConfig.Action action) throws Exception {
//        Path tempInput = Files.createTempFile("preview-yaml-", ".in.yaml");
//        Path tempOutput = Files.createTempFile("preview-yaml-", ".out.yaml");
        Path tempInput = tempFileManager.createTempPreviewInYamlFile();
        Path tempOutput = tempFileManager.createTempPreviewOutYamlFile();

        try {
            Files.writeString(tempInput, input);

            SecurePropertiesConfig secureConfig = new SecurePropertiesConfig(
                    SecurePropertiesConfig.Type.FILE,
                    action,
                    config.getAlgorithm(),
                    config.getMode(),
                    config.getKey(),
                    null,
                    tempInput.toString(),
                    tempOutput.toString(),
                    config.isUseRandomIV()
            );

            SecurePropertiesToolAdapter.ExecutionResult result = runner.run(secureConfig);

            if (result.exitCode != 0) {
                throw new IllegalStateException("Preview transformation failed: " + result.error);
            }

            return Files.readString(tempOutput);

        } finally {
            deleteTempFile(tempInput);
            deleteTempFile(tempOutput);
        }
    }
}
