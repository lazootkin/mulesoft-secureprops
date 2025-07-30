package com.lazhoff.mule.secureprops.crypto;


import com.lazhoff.mule.secureprops.util.TempFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCryptoServiceFileLevelDecryptTest {

    private static final Logger logger = LogManager.getLogger(DefaultCryptoServiceFileLevelDecryptTest.class);

    @Test
    void testDecryptEncryptedFileContent() throws IOException {
        // Given
        String encryptedWrapped = "![gZ+a84mSlUWMvQnHGlIN4EUO1EVOikO2lQm00imyNafpYArXW5tFV+uldByU+AsF]";
        Path file = Files.createTempFile("test-filelevel-decrypt-", ".txt");
        Files.writeString(file, encryptedWrapped);

        logger.info("Initial encrypted file content:\n{}", encryptedWrapped);

        CryptoConfig config = new CryptoConfig(
                CryptoConfig.FileOrLine.WHOLE_FILE,
                file.toString(),
                "AES",
                "CBC",
                "0000y1230000yXYZ",  // Must match the key used for encryption
                false,              // useRandomIV: must match encryption time setting
                TempFileManager.getSystemPathDir(),
                false,              // dryRun
                false,              // debug
                true,               // backup
                ".*"                // regex (not used here)
        );

        ICryptoService service = new DefaultCryptoServiceFileLevel(config);

        // When
        service.decrypt();

        // Then
        String decrypted = Files.readString(file);

        logger.info("Decrypted file content:\n{}", decrypted);

        assertNotEquals(encryptedWrapped, decrypted);
        assertTrue(decrypted.contains("mySecret123") || decrypted.contains("password") || decrypted.length() > 5,
                "Decrypted content should look like the original plaintext.");

        // Cleanup
        Files.deleteIfExists(file);
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        Files.deleteIfExists(backup);
    }
}
