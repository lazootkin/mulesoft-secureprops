
package com.lazhoff.mule.secureprops.ui;

public class UISettings {

    public String lastFolder = "";
    public boolean backup = true;
    public boolean dryRun = false;
    public boolean debug = false;
    public String secureAttributeRegex = ".*(password|secret).*";

    public String envKeyMappingPostman = "(local.postman_environment.json):(0000123400001234),((dev|uat|test|mock).postman_environment.json):(0000123400001DEV),(.*.postman_collection.json):(000012340000DEV),(prod.postman_environment.json):(000012340000PROD)";
    public String envKeyMappingProperties = "(secure-config-local.yaml):(0000123400001234),(secure-config-(dev|uat).yaml):(0000123400001DEV),(secure-config-prod.yaml):(000012340000PROD)";

    public String algorithm = "AES";
    public String mode = "CBC";
    public boolean useRandomIV = true;

    @Override
    public String toString() {
        return String.format("""
        {
          "lastFolder": "%s",
          "backup": %s,
          "dryRun": %s,
          "debug": %s,
          "secureAttributeRegex": "%s",
          "envKeyMappingPostman": "%s",
          "envKeyMappingProperties": "%s",
          "algorithm": "%s",
          "mode": "%s",
          "useRandomIV": %s
        }
        """,
                lastFolder, backup, dryRun, debug, secureAttributeRegex,
                envKeyMappingPostman, envKeyMappingProperties,
                algorithm, mode, useRandomIV);
    }

    public void copyFrom(UISettings other) {
        this.envKeyMappingPostman = other.envKeyMappingPostman;
        this.envKeyMappingProperties = other.envKeyMappingProperties;
        this.secureAttributeRegex = other.secureAttributeRegex;
        this.dryRun = other.dryRun;
        this.debug = other.debug;
        this.backup = other.backup;
        this.algorithm = other.algorithm;
        this.mode = other.mode;
        this.useRandomIV = other.useRandomIV;
        this.lastFolder = other.lastFolder;
    }

}
