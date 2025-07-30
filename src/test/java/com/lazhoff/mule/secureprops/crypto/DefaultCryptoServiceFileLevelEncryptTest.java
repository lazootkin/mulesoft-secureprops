package com.lazhoff.mule.secureprops.crypto;

import com.lazhoff.mule.secureprops.util.TempFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCryptoServiceFileLevelEncryptTest {

    private static final Logger logger = LogManager.getLogger(DefaultCryptoServiceFileLevelEncryptTest.class);

    @Test
    void testEncryptEntireFileContent() throws IOException {
        // Given
        String originalContent = "username=admin\npassword=mySecret123\n";
        Path file = Files.createTempFile("test-filelevel-encrypt-", ".txt");
        Files.writeString(file, originalContent);

        logger.info("Initial file content:\n{}", originalContent);

        CryptoConfig config = new CryptoConfig(
                CryptoConfig.FileOrLine.WHOLE_FILE,
                file.toString(),
                "AES",
                "CBC",
                "0000y1230000yXYZ",
                false,  // useRandomIV
                TempFileManager.getSystemPathDir(),
                false,  // dryRun
                false,  // debug
                true,   // backup
                ".*"    // regex (not used here)
        );

        ICryptoService service = new DefaultCryptoServiceFileLevel(config);

        // When
        service.encrypt();

        // Then
        String encrypted = Files.readString(file);

        logger.info("Encrypted file content:\n{}", encrypted);

        assertNotEquals(originalContent, encrypted);
        assertTrue(encrypted.trim().startsWith("![") && encrypted.trim().endsWith("]"),
                "Encrypted output should be wrapped in ![ ... ]");

        // Cleanup
        Files.deleteIfExists(file);
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        Files.deleteIfExists(backup);
    }
}

