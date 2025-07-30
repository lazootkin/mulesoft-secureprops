# mule-secureprops-extension

A command-line tool for secure encryption and decryption of YAML and JSON configuration files, extending MuleSoft's   
[MuleSoft's Secure Properties Tool](https://docs.mulesoft.com/mule-runtime/latest/secure-configuration-properties).

	
## Features

- Supports **YAML** and **JSON** formats
- Encrypts entire files or selected values
- Uses file name masks to dynamically assign encryption keys
- Supports recursive folder processing
- Regex filtering for JSON/YAML property names
- Compatible with Mule SecurePropertiesTool format

## Teaser

```sh
java -jar mule-secureprops-extension.jar encrypt ./src/main/resource/config \
  AES CBC \
  dummyKey true \
  --envKeyMapping="(*.dev.*):(devKey123),(*.uat.*):(uatKey456)" \
  --rex=".*(password|secret).*" \
  --dryRun
```

## How it works
For each file, the tool:

Determines the encryption key based on the file name

Selects the matching crypto engine (.yaml or .json)

Applies encryption only to values matching the provided regex

Creates .bak file unless --noBackup is used

Command-line Usage
```sh
java -jar mule-secureprops-extension.jar <encrypt|decrypt> <fileOrFolderPath> \
  <algorithm> <mode> <defaultKey> <useRandomIV> \
  [--envKeyMapping="(pattern):(key),(pattern2):(key2)"] \
  [--rex="regex"] \
  [--dryRun] \
  [--debug] \
  [--tmp] \
  [--noBackup]
```


## Examples
Encrypt JSON files in folder using regex:
```sh
java -jar mule-secureprops-extension.jar encrypt ./secrets AES CBC dummyKey false \
  --envKeyMapping="(*.uat.*):(uatKey456)" \
  --rex=".*password.*"
Decrypt a YAML file
```
```sh
java -jar mule-secureprops-extension.jar decrypt ./example.yaml AES CBC dummyKey false
```



## Requirements
- Java 17+
- Maven (for build)
- Maven dependencies must be available (e.g., via internet access or a
  pre-populated local repository) for the tests to run successfully.

Before building from source, install the bundled Secure Properties Tool
into your local Maven repository:

linux
```sh
mvn install:install-file \
  -Dfile=src/main/resources/lib/secure-properties-tool-v17/secure-properties-tool-j17.jar \
  -DgroupId=com.mulesoft.tools \
  -DartifactId=secure-properties-tool \
  -Dversion=1.0 \
  -Dpackaging=jar
```

windows
```sh
mvn install:install-file ^
  -Dfile=src/main/resources/lib/secure-properties-tool-v17/secure-properties-tool-j17.jar ^
  -DgroupId=com.mulesoft.tools ^
  -DartifactId=secure-properties-tool ^
  -Dversion=1.0 ^
  -Dpackaging=jar
```




This allows Maven to resolve the dependency declared in the `pom.xml`
without needing internet access.




## Installation
Download the release archive 📦 secureprops-v1.0.1.zip:
- https://github.com/lazootkin/mulesoft-secureprops/releases
   

Extract the archive to a utility folder of your choice (e.g., C:\tools\secureprops or ~/utils/secureprops):

```sh
C:\tools\secureprops\
  ├── decrypt-postman.bat
  ├── decrypt-props.bat
  ├── encrypt-postman.bat
  ├── encrypt-props.bat
  ├── mule-secureprops-extension-1.0-SNAPSHOT.jar
  ├── secure-properties-tool.jar
  └── secure-properties-ui.bat
```
(Optional) Add to PATH:
If you want to use the batch files (encrypt-*.bat, decrypt-*.bat, etc.) from any location, you can add the secureprops/ folder to your system PATH environment variable.

### Run the UI Tool (Graphical mode):

```sh
secure-properties-ui.bat
```

### Use the CLI (Script mode):

With a specified folder:

```sh
encrypt-postman.bat path\to\configs
```
Or using the current directory:

```sh
encrypt-props.bat
```




## 3. How to Use
- UI Mode (Recommended)
Run:

```sh
secure-properties-ui.bat
```

In the UI:

Choose a folder containing Postman JSON or Mule YAML/Properties files.

Click Encrypt or Decrypt for either Postman or Properties.

File preview appears on the right; logs are shown at the bottom.

Click Settings to configure keys, regex, and encryption options.

- CLI Mode (Advanced)
You can run the CLI batch files in two ways:

1. Provide a folder explicitly

```sh
encrypt-postman.bat path\to\your\folder
decrypt-props.bat   path\to\your\folder
```


2. Use current directory

```sh
encrypt-postman.bat
decrypt-props.bat
```

## 4. Configuration
When you save settings in the UI, the tool creates a config file:

```sh
.secureprops/config.json
```

This file is stored in the same folder where the .jar file is located.

It contains:

Algorithm and mode (e.g., AES/CBC)

Regex for detecting secure fields

Environment-to-key mapping

Last used folder

(Placeholder) Random IV option — not implemented yet



## General Notes
**Postman Encrypt/Decrypt** → maps to file-level encryption
Uses the Mule Secure Properties Tool in string mode, encrypting the entire JSON file as one block.
Output looks like:

```sh
"![ENCRYPTED-FILE]"
```
**Properties Encrypt/Decrypt** → maps to file mode
Uses the Mule Secure Properties Tool in file mode, encrypting secure field values line by line in YAML or .properties files.
Example:

```sh
authToken: "![ENCRYPTED-VALUE]"
```


## 5. FAQ

**Q: Where are temporary files stored?**  
**A:**  
By default, temporary files are created in the system's temp folder (e.g., `%TEMP%` on Windows).

However, the behavior varies depending on how you run the tool:

- **In CLI mode** (`*.bat` scripts):  
  - Temporary files are saved to the **current working directory** (`.`),  
    or  
  - To a **custom folder**, if provided as the **first argument** to the script.

- **In UI mode** (via `secure-properties-ui.bat`):  
  - The temporary directory is set to the folder selected in the **"Folder"** input field (at the top of the UI).

This allows you to **retain and inspect** encrypted/decrypted files in a predictable location during usage.

## License
MIT License

## Author
Developed by Leonard Lazutkin
Powered by the OpenAI platform(OpenAI, 2025).

 
 