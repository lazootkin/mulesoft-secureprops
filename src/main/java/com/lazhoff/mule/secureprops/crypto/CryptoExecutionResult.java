package com.lazhoff.mule.secureprops.crypto;

import java.nio.file.Path;

public class CryptoExecutionResult {
    public enum Status {
        SUCCESS,
        SKIPPED,
        FAILED,
        UNCHANGED,
        DRYRUN_CHANGED
    }

    private final Path file;
    private final Status status;
    private final String message;
    private final String diffPreview;

    public CryptoExecutionResult(Path file, Status status, String message, String diffPreview) {
        this.file = file;
        this.status = status;
        this.message = message;
        this.diffPreview = diffPreview;
    }

    public Path getFile() { return file; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public String getDiffPreview() { return diffPreview; }
}
