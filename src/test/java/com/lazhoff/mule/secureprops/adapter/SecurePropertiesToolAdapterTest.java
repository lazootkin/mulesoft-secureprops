package com.lazhoff.mule.secureprops.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurePropertiesToolAdapterTest {

    @Test
    void testRunWithStringEncryption() {
        // Given
        SecurePropertiesConfig config = new SecurePropertiesConfig(
                SecurePropertiesConfig.Type.STRING,
                SecurePropertiesConfig.Action.ENCRYPT,
                "AES",
                "CBC",
                "0000y1230000yXYZ",
                "myTestValue",
                null,  // inputFile
                null,  // outputFile
                false  // useRandomIV
        );

        SecurePropertiesToolRunner runner = new SecurePropertiesToolAdapter();

        // When
        SecurePropertiesToolAdapter.ExecutionResult result = runner.run(config);

        // Then
        assertEquals(0, result.exitCode);
        assertNotNull(result.output);
        assertTrue(result.output.length() > 0);
        assertEquals("", result.error);
        assertEquals("Jy9PWSLuUL040RvV3iZoJQ==", result.output);
    }

    @Test
    void testRunWithStringDecryption() {
        // Given
        SecurePropertiesConfig config = new SecurePropertiesConfig(
                SecurePropertiesConfig.Type.STRING,
                SecurePropertiesConfig.Action.DECRYPT,
                "AES",
                "CBC",
                "0000y1230000yXYZ",
                "Jy9PWSLuUL040RvV3iZoJQ==",
                null,
                null,
                false
        );

        SecurePropertiesToolRunner runner = new SecurePropertiesToolAdapter();

        // When
        SecurePropertiesToolAdapter.ExecutionResult result = runner.run(config);

        // Then
        assertEquals(0, result.exitCode);
        assertNotNull(result.output);
        assertEquals("myTestValue", result.output);
        assertEquals("", result.error);
    }

}
