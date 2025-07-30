package com.lazhoff.mule.secureprops.crypto;

import com.lazhoff.mule.secureprops.util.TempFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCryptoServiceJsonEncryptTest {

    private static final Logger logger = LogManager.getLogger(DefaultCryptoServiceJsonEncryptTest.class);

    @Test
    void testEncryptJsonFile() throws Exception {
        // Given
        String originalJson = """
            {
              "user": {"name": "admin"},
              "values":{"value": "mySecret123"}
            }
            """;

        Path file = Files.createTempFile("crypto-json-test-", ".json");
        Files.writeString(file, originalJson);

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

        // When
        service.encrypt();

        // Then
        String encrypted = Files.readString(file);
        assertNotEquals(originalJson, encrypted);

        logger.debug("file content encrypted: {}",encrypted);

        assertTrue(encrypted.contains("![") || encrypted.matches("(?s).*\"value\"\\s*:\\s*\"!\\[.*"));

        // Cleanup
        Files.deleteIfExists(file);
    }
}
