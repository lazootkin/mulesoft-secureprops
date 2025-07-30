package com.lazhoff.mule.secureprops.adapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SecurePropertiesWholeFileTest {

    private static final String ALGORITHM = "AES";
    private static final String MODE = "CBC";
    private static final String KEY = "0000y1230000yXYZ";
    private static final boolean USE_RANDOM_IV = false;

 //   @Test
    void testEncryptWholeFile() throws IOException {
        // Given: create temporary input and output files
        Path inputFile = Files.createTempFile("secure-test-input-", ".yaml");
        Path outputFile = Files.createTempFile("secure-test-output-", ".yaml");
        Files.writeString(inputFile, "myTestValue");

        SecurePropertiesConfig config = new SecurePropertiesConfig(
                SecurePropertiesConfig.Type.WHOLE_FILE,
                SecurePropertiesConfig.Action.ENCRYPT,
                ALGORITHM,
                MODE,
                KEY,
                null,
                inputFile.toString(),
                outputFile.toString(),
                USE_RANDOM_IV
        );

        SecurePropertiesToolRunner runner = new SecurePropertiesToolAdapter();

        // When
        SecurePropertiesToolAdapter.ExecutionResult result = runner.run(config);

        // Then
        assertEquals(0, result.exitCode);
        assertTrue(Files.exists(outputFile));
        String encrypted = Files.readString(outputFile);
        assertNotNull(encrypted);
        assertNotEquals("myTestValue", encrypted);
        assertEquals("", result.error);

        // Cleanup (BUG)
        // Mule Tool BUG, stream not closed
        // in  com.mulesoft.tools.SecurePropertiesTool
        //
        //   public static void applyHoleFile(String action, String algorithm, String mode, String key, boolean useRandomIVs, String inputFilePath, String outputFilePath) throws IOException, MuleEncryptionException
        //   {
        //       File inputFile = new File(inputFilePath);
        //       InputStream stream = new FileInputStream(inputFile)
        //

        // Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }

  //  @Test
    void testDecryptWholeFile() throws IOException {
        // Given: prepare encrypted file and decrypt back
        String original = "myTestValue";

        Path inputFile = Files.createTempFile("secure-test-enc-input-", ".yaml");
        Path encryptedFile = Files.createTempFile("secure-test-enc-output-", ".yaml");
        Path decryptedFile = Files.createTempFile("secure-test-dec-output-", ".yaml");

        // Encrypt it first
        Files.writeString(inputFile, original);

        SecurePropertiesConfig encryptConfig = new SecurePropertiesConfig(
                SecurePropertiesConfig.Type.WHOLE_FILE,
                SecurePropertiesConfig.Action.ENCRYPT,
                ALGORITHM,
                MODE,
                KEY,
                null,
                inputFile.toString(),
                encryptedFile.toString(),
                USE_RANDOM_IV
        );

        SecurePropertiesToolRunner runner = new SecurePropertiesToolAdapter();
        runner.run(encryptConfig);

        // Now decrypt
        SecurePropertiesConfig decryptConfig = new SecurePropertiesConfig(
                SecurePropertiesConfig.Type.WHOLE_FILE,
                SecurePropertiesConfig.Action.DECRYPT,
                ALGORITHM,
                MODE,
                KEY,
                null,
                encryptedFile.toString(),
                decryptedFile.toString(),
                USE_RANDOM_IV
        );

        SecurePropertiesToolAdapter.ExecutionResult result = runner.run(decryptConfig);

        // Then
        assertEquals(0, result.exitCode);
        assertTrue(Files.exists(decryptedFile));
        String decrypted = Files.readString(decryptedFile);
        assertEquals(original, decrypted);
        assertEquals("", result.error);

        // Cleanup
//        Files.deleteIfExists(inputFile); Mule Tool BUG, stream not closed
//        Files.deleteIfExists(encryptedFile); Mule Tool BUG, stream not closed
        Files.deleteIfExists(decryptedFile);
    }
}
