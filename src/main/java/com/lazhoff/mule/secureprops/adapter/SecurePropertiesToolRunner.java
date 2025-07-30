package com.lazhoff.mule.secureprops.adapter;

/**
 * Interface for executing SecurePropertiesTool with a fixed configuration.
 */
public interface SecurePropertiesToolRunner {

    /**
     * Executes the encryption/decryption operation.
     * @param config the immutable configuration
     * @return the result of the operation
     */
    SecurePropertiesToolAdapter.ExecutionResult run(SecurePropertiesConfig config);
}
