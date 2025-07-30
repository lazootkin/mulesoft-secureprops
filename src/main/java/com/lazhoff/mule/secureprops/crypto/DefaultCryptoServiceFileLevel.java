package com.lazhoff.mule.secureprops.crypto;

import com.mulesoft.tools.SecurePropertiesTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.lazhoff.mule.secureprops.crypto.CryptoExecutionResult.Status.*;

public class DefaultCryptoServiceFileLevel implements ICryptoService, SupportsPreview {

    private static final Logger logger = LogManager.getLogger(DefaultCryptoServiceFileLevel.class);

    private final CryptoConfig config;

    public DefaultCryptoServiceFileLevel(CryptoConfig config) {
        this.config = config;
    }

    @Override
    public CryptoExecutionResult.Status encrypt() {

        Path file = Path.of(config.getFilePath());

        if (isAlreadyEncrypted(file)) {
            logger.warn("File {} appears to be already encrypted. Skipping encryption.", file);
            return UNCHANGED;
        }

        return process(true);
    }

    @Override
    public CryptoExecutionResult.Status decrypt() {

        Path file = Path.of(config.getFilePath());

        if (!isAlreadyEncrypted(file)) {
            logger.warn("File {} appears to be already decrypted. Skipping decryption.", file);
            return UNCHANGED;
        }
        return process(false);
    }

    private CryptoExecutionResult.Status process(boolean isEncrypt) {
        Path file = Path.of(config.getFilePath());
        Path temp = null;

        try {
            String originalContent = Files.readString(file, StandardCharsets.UTF_8);
            logger.debug("Original content read from file: {}", file);

            String processedContent = isEncrypt
                    ? encryptValue(originalContent)
                    : decryptValue(originalContent);

            logger.debug("Processed content:\n{}", processedContent);

            if (config.isDryRun()) {
                if (!originalContent.equals(processedContent)) {
                    logger.info("Dry run: file would change: {}", file);
                    return DRYRUN_CHANGED;
                } else {
                    logger.info("Dry run: no changes needed: {}", file);
                    return UNCHANGED;
                }
            }

            if (originalContent.equals(processedContent)) {
                logger.info("No changes detected: {}", file);
                return UNCHANGED;
            }

            if (config.isBackup()) {
                Path backup = file.resolveSibling(file.getFileName() + ".bak");
                Files.copy(file, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("Backup created: {}", backup);
            }

            temp = Files.createTempFile("secure-file-", ".tmp");
            Files.writeString(temp, processedContent, StandardCharsets.UTF_8);
            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("{} completed and file updated: {}", isEncrypt ? "Encryption" : "Decryption", file);

            return SUCCESS;

        } catch (Exception e) {
            logger.error("Failed to {} file {}: {}", isEncrypt ? "encrypt" : "decrypt", file, e.toString(), e);
            return FAILED;
        } finally {
            deleteTempFile(temp);
        }
    }

    private String encryptValue(String plainText) throws Exception {
        String encrypted = SecurePropertiesTool.applyOverString(
                "encrypt",
                config.getAlgorithm(),
                config.getMode(),
                config.getKey(),
                config.isUseRandomIV(),
                plainText
        );
        return "![" + encrypted + "]";
    }

    private String decryptValue(String encryptedText) throws Exception {
        String raw = encryptedText.trim();
        if (raw.startsWith("![") && raw.endsWith("]")) {
            raw = raw.substring(2, raw.length() - 1);
        }

        return SecurePropertiesTool.applyOverString(
                "decrypt",
                config.getAlgorithm(),
                config.getMode(),
                config.getKey(),
                config.isUseRandomIV(),
                raw
        );
    }

    private void deleteTempFile(Path temp) {
        if (temp != null) {
            try {
                Files.deleteIfExists(temp);
                logger.debug("Temporary file deleted: {}", temp);
            } catch (IOException e) {
                logger.warn("Could not delete temp file: {}", temp, e);
            }
        }
    }

    @Override
    public String previewEncrypt(String input) throws Exception {
        return "![" + SecurePropertiesTool.applyOverString(
                "encrypt",
                config.getAlgorithm(),
                config.getMode(),
                config.getKey(),
                config.isUseRandomIV(),
                input
        ) + "]";
    }

    @Override
    public String previewDecrypt(String input) throws Exception {
        String raw = input.trim();
        if (raw.startsWith("![") && raw.endsWith("]")) {
            raw = raw.substring(2, raw.length() - 1);
        }
        return SecurePropertiesTool.applyOverString(
                "decrypt",
                config.getAlgorithm(),
                config.getMode(),
                config.getKey(),
                config.isUseRandomIV(),
                raw
        );
    }

    @SuppressWarnings("unchecked")
    private boolean isAlreadyEncrypted(Path file) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8).trim();
            return content.startsWith("![") && content.endsWith("]");
        } catch (IOException e) {
            logger.warn("Failed to read file for encryption check: {}", e.toString());
            return false;
        }
    }

}
