package com.lazhoff.mule.secureprops.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultCryptoServiceJson implements ICryptoService,SupportsPreview  {

    private static final Logger logger = LogManager.getLogger(DefaultCryptoServiceJson.class);

    private final CryptoConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    public DefaultCryptoServiceJson(CryptoConfig config) {
        this.config = config;
    }

    @Override
    public CryptoExecutionResult.Status encrypt() {
        return process(true);
    }

    @Override
    public CryptoExecutionResult.Status decrypt() {
        return process(false);
    }
    private CryptoExecutionResult.Status process(boolean encrypt) {
        Path path = Path.of(config.getFilePath());
        Path backupPath = path.resolveSibling(path.getFileName() + ".bak");

        try {
            JsonNode root = mapper.readTree(path.toFile());

            JsonCryptoTransformer transformer = new JsonCryptoTransformer(
                    config.getAlgorithm(),
                    config.getMode(),
                    config.getKey(),
                    config.isUseRandomIV(),
                    encrypt,
                    config.getSecureAttributeNameRegex()
            );

            JsonNode updated = transformer.transform(root);

            if (config.isDryRun()) {
                logger.info("Dry run enabled - original file was not modified.");
                return updated.equals(root)
                        ? CryptoExecutionResult.Status.UNCHANGED
                        : CryptoExecutionResult.Status.DRYRUN_CHANGED;
            }

            if (config.isBackup()) {
                Files.copy(path, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("Backup created: {}", backupPath);
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), updated);
            logger.info("{} completed: {}", encrypt ? "Encryption" : "Decryption", path);
            return CryptoExecutionResult.Status.SUCCESS;

        } catch (Exception e) {
            logger.error("Failed to process JSON file: {}", e.toString(), e);
            return CryptoExecutionResult.Status.FAILED;
        }
    }

    @Override
    public String previewEncrypt(String input) throws Exception {
        JsonNode root = mapper.readTree(input);

        JsonCryptoTransformer transformer = new JsonCryptoTransformer(
                config.getAlgorithm(),
                config.getMode(),
                config.getKey(),
                config.isUseRandomIV(),
                true, // encrypt
                config.getSecureAttributeNameRegex()
        );

        JsonNode updated = transformer.transform(root);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updated);
    }

    @Override
    public String previewDecrypt(String input) throws Exception {
        JsonNode root = mapper.readTree(input);

        JsonCryptoTransformer transformer = new JsonCryptoTransformer(
                config.getAlgorithm(),
                config.getMode(),
                config.getKey(),
                config.isUseRandomIV(),
                false, // decrypt
                config.getSecureAttributeNameRegex()
        );

        JsonNode updated = transformer.transform(root);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updated);
    }

}
