package com.lazhoff.mule.secureprops.crypto;

public interface SupportsPreview {
    String previewEncrypt(String input) throws Exception;
    String previewDecrypt(String input) throws Exception;
}
