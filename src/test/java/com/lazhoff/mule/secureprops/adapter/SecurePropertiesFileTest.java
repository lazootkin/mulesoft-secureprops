package com.lazhoff.mule.secureprops.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SecurePropertiesFileTest {

    private static final Logger logger = LogManager.getLogger(SecurePropertiesFileTest.class);

    private static final String ALGORITHM = "AES";
    private static final String MODE = "CBC";
    private static final String KEY = "0000y1230000yXYZ";
    private static final boolean USE_RANDOM_IV = false;

    @Test
    void testEncryptAndDecryptFile() throws IOException {
        logger.info("Starting test: Encrypt and Decrypt FILE type");

        // Given: Create a YAML with sensitive properties
        String originalYaml = "http.port=8081\nsecure.key=mySecretValue\n";
        Path inputFile = Files.createTempFile("secure-test-props-", ".properties");
        Path encryptedFile = Files.createTempFile("secure-test-props-encrypted-", ".properties");
        Path decryptedFile = Files.createTempFile("secure-test-props-decrypted-", ".properties");

        Files.writeString(inputFile, originalYaml);
        logger.info("Original file created: {}", inputFile);

        SecurePropertiesToolRunner runner = new SecurePropertiesToolAdapter();

        // Encrypt
        logger.info("--- Encrypting file: {}", inputFile);
        SecurePropertiesConfig encryptConfig = new SecurePropertiesConfig(
                SecurePropertiesConfig.Type.FILE,
                SecurePropertiesConfig.Action.ENCRYPT,
                ALGORITHM,
                MODE,
                KEY,
                null,
                inputFile.toString(),
                encryptedFile.toString(),
                USE_RANDOM_IV
        );

        SecurePropertiesToolAdapter.ExecutionResult encryptResult = runner.run(encryptConfig);
        logger.info("Encryption completed with exitCode={}, error='{}'", encryptResult.exitCode, encryptResult.error);

        assertEquals(0, encryptResult.exitCode);
        assertTrue(Files.exists(encryptedFile));
        String encryptedContent = Files.readString(encryptedFile);
        assertNotNull(encryptedContent);
        assertNotEquals(originalYaml, encryptedContent);

        // Decrypt
        logger.info("--- Decrypting file: {}", encryptedFile);
        SecurePropertiesConfig decryptConfig = new SecurePropertiesConfig(
                SecurePropertiesConfig.Type.FILE,
                SecurePropertiesConfig.Action.DECRYPT,
                ALGORITHM,
                MODE,
                KEY,
                null,
                encryptedFile.toString(),
                decryptedFile.toString(),
                USE_RANDOM_IV
        );

        SecurePropertiesToolAdapter.ExecutionResult decryptResult = runner.run(decryptConfig);
        logger.info("Decryption completed with exitCode={}, error='{}'", decryptResult.exitCode, decryptResult.error);

        assertEquals(0, decryptResult.exitCode);
        assertTrue(Files.exists(decryptedFile));

        String decryptedContent = Files.readString(decryptedFile);
        assertEquals(originalYaml, decryptedContent);
        assertEquals("", decryptResult.error);

        logger.info("Test passed successfully. Cleaning up temp files...");

        // Cleanup
        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(encryptedFile);
        Files.deleteIfExists(decryptedFile);
    }
}
