package com.lazhoff.mule.secureprops.crypto;

public interface ICryptoService {
    CryptoExecutionResult.Status encrypt();
    CryptoExecutionResult.Status decrypt();
}
