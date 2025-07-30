package com.lazhoff.mule.secureprops.crypto;

import java.nio.file.Path;

public class CryptoConfig {


    public enum FileOrLine {
        FILE("file"),
        WHOLE_FILE("file-level");

        private final String value;

        FileOrLine(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static FileOrLine fromString(String input) {
            for (FileOrLine type : FileOrLine.values()) {
                if (type.value.equalsIgnoreCase(input)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown FileOrLine value: " + input);
        }
    }

    private final String filePath;
    private final String algorithm;
    private final String mode;
    private final String key;
    private final boolean useRandomIV;
    private final Path tempFolder;

    private final boolean dryRun;
    private final boolean debug;
    private final boolean backup;


    private final String secureAttributeNameRegex;

    private final FileOrLine fileOrLine;

    public CryptoConfig(
            FileOrLine fileOrLine,
            String filePath,
            String algorithm,
            String mode,
            String key,
            boolean useRandomIV,
            Path tempFolder,
            boolean dryRun,
            boolean debug,
            boolean backup,
            String secureAttributeNameRegex
    ) {
        this.filePath = filePath;
        this.algorithm = algorithm;
        this.mode = mode;
        this.key = key;
        this.useRandomIV = useRandomIV;
        this.tempFolder = tempFolder;
        this.dryRun = dryRun;
        this.debug = debug;
        this.backup = backup;
        this.secureAttributeNameRegex = secureAttributeNameRegex;
        this.fileOrLine = fileOrLine;
    }

    public Path getTempFolder() {
        return tempFolder;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getMode() {
        return mode;
    }

    public String getKey() {
        return key;
    }

    public boolean isUseRandomIV() {
        return useRandomIV;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isBackup() {
        return backup;
    }

    public String getSecureAttributeNameRegex() {
        return secureAttributeNameRegex;
    }

    public FileOrLine getFileOrLine() {
        return fileOrLine;
    }

}
