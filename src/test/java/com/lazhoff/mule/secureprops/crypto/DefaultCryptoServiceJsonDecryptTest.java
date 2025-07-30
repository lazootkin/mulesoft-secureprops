package com.lazhoff.mule.secureprops.crypto;

import com.lazhoff.mule.secureprops.util.TempFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCryptoServiceJsonDecryptTest {

    private static final Logger logger = LogManager.getLogger(DefaultCryptoServiceJsonDecryptTest.class);

    @Test
    void testDecryptJsonFile() throws Exception {
        // Given: JSON content that contains encrypted values (marked with ![...])
        String encryptedJson = """
            {
              "user": {"name": "!admin"},
              "value": "![U3HBzx9qDge/0ow9JnNKOg==]"
            }
            """;

        // Note: Replace "EncryptedNameHere" and "EncryptedPasswordHere" with real encrypted strings
        // if you're doing integration testing with real AES/CBC key.

        Path file = Files.createTempFile("crypto-json-decrypt-", ".json");
        Files.writeString(file, encryptedJson);

        CryptoConfig config = new CryptoConfig(
                CryptoConfig.FileOrLine.FILE,
                file.toString(),
                "AES",
                "CBC",
                "0000y1230000yXYZ",
                false, // useRandomIV
                TempFileManager.getSystemPathDir(),
                false, // dryRun
                false, // debug
                false, // backup
                "value"
        );

        ICryptoService service = new DefaultCryptoServiceJson(config);

        // When: decrypt the file
        service.decrypt();

        // Then: file content should be decrypted (no longer contains ![...])
        String decrypted = Files.readString(file);
        logger.debug("File content after decryption: {}", decrypted);

        assertFalse(decrypted.contains("!["));
        assertTrue(decrypted.contains("value"));
        assertTrue(decrypted.contains("user"));

        // Cleanup
        Files.deleteIfExists(file);
    }
}
