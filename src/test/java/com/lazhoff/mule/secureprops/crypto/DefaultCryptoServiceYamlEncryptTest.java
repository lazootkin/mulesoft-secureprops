package com.lazhoff.mule.secureprops.crypto;


import com.lazhoff.mule.secureprops.util.TempFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCryptoServiceYamlEncryptTest {

    private static final Logger logger = LogManager.getLogger(DefaultCryptoServiceYamlEncryptTest.class);

    @Test
    void testEncryptYamlFile() throws IOException {
        // Given
        String yamlContent = "secure.password: mySecret123\n";
        Path file = Files.createTempFile("test-encrypt-", ".yaml");
        Files.writeString(file, yamlContent);

        logger.info("initial file content: {}", yamlContent);

        CryptoConfig config = new CryptoConfig(
                CryptoConfig.FileOrLine.FILE,
                file.toString(),
                "AES",
                "CBC",
                "0000y1230000yXYZ",
                false,  // useRandomIV
                TempFileManager.getSystemPathDir(),
                false,  // dryRun
                false,  // debug
                true,    // backup (ignored)
                "value"
        );

        ICryptoService service = new DefaultCryptoServiceYaml(config);

        // When
        service.encrypt();

        // Then
        String encrypted = Files.readString(file);

        logger.info("encrypted file content: {}", encrypted);

        assertNotEquals(yamlContent, encrypted);
        assertTrue(encrypted.contains("![") || encrypted.matches("(?s).*secure\\.password\\s*:\\s*!\\[.*"));

        // Cleanup
        Files.deleteIfExists(file);
    }
}
