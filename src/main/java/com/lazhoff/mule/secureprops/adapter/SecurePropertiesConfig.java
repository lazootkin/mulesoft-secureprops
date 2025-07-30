package com.lazhoff.mule.secureprops.adapter;

/**
 * Immutable configuration for SecurePropertiesTool.
 */
public final class SecurePropertiesConfig {

    public enum Type {
        STRING("string"),
        FILE("file"),
        WHOLE_FILE("file-level");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum Action {
        ENCRYPT("encrypt"),
        DECRYPT("decrypt");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private final Type type;
    private final Action action;
    private final String algorithm;
    private final String mode;
    private final String key;
    private final String inputString;
    private final String inputFile;
    private final String outputFile;
    private final boolean useRandomIV;

    public SecurePropertiesConfig(
            Type type,
            Action action,
            String algorithm,
            String mode,
            String key,
            String inputString,
            String inputFile,
            String outputFile,
            boolean useRandomIV
    ) {
        this.type = type;
        this.action = action;
        this.algorithm = algorithm;
        this.mode = mode;
        this.key = key;
        this.inputString = inputString;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.useRandomIV = useRandomIV;
    }

    public Type getType() {
        return type;
    }

    public Action getAction() {
        return action;
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

    public String getInputString() {
        return inputString;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public boolean isUseRandomIV() {
        return useRandomIV;
    }
}
