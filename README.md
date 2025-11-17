# BW6 Utilities Maven Plugin

The `bw6-utilities-plugin` is designed to simplify common configuration management tasks for TIBCO BusinessWorks Container Edition (BWCE) and BusinessWorks 6 (BW6) projects. It provides goals for automatic token replacement in substitution variable files (`.substvar`) and for extracting configuration variables into standard `.properties` files. 

## 🚀 Goals Overview

This plugin defines two custom goals:

1. **`tokenizer`**: Replaces values in a source `.substvar` file using tokens defined in local or remote properties files, and correctly configures password variables for lookup. 

2. **`extractProperties`**: Parses a `.substvar` file and extracts all global variables (name and value) into a standard Java `.properties` file. 

## 🛠️ Installation and Usage

### 1. Installation

Since this is a custom plugin, you must **build and install** it into your local Maven repository before use.

**In the `bw6-utilities-plugin` directory:**

```bash
# Compile, run tests, package, and install the plugin locally
mvn clean install
```

### 2. Basic Command Syntax

The plugin uses the simplified prefix **`bw6-utilities`** (as configured in the `pom.xml`):

```bash
mvn bw6-utilities:<goal-name> -D<parameter-name>=<value>
```

---

## 🎯 Goal: `tokenizer`

**(Prefix: `bw6-utilities:tokenizer`)**

This goal tokenizes a source substitution variable file and writes the result to a new file, setting `isOverride` to `true` for tokenized variables and handling `Password` types correctly (converting them to use `lookupValue`).

| Parameter | Type | Default Value | Description | 
| ----- | ----- | ----- | ----- | 
| `tokenizer.profileSource` | `String` | `default` | The name of the input `.substvar` profile (e.g., reads `default.substvar`). | 
| `tokenizer.profileTarget` | `String` | `token` | The name of the output `.substvar` profile (e.g., writes `token.substvar`). | 
| `tokenizer.commonTokensLocalPath` | `File` | `${project.basedir}/resources/common-tokens.properties` | Path to the common tokens file. | 
| `tokenizer.appTokensLocalPath` | `File` | `${project.basedir}/resources/app-tokens.properties` | Path to the application-specific tokens file. | 
| `tokenizer.isCommonRemote` | `boolean` | `false` | Set to `true` to download common tokens from a remote URL. | 
| `tokenizer.commonTokensPropertiesLink` | `String` | *None* | The full URL for remote common properties (if `isCommonRemote` is `true`). | 

### Command Example (Local Tokens)

Reads from `META-INF/default.substvar`, applies tokens from `resources/common-tokens.properties` and `resources/app-tokens.properties`, and writes the output to `META-INF/token.substvar`. If the value has been set 2 times, the value applied will be the one in `app-tokens.properties`.

```bash
mvn bw6-utilities:tokenizer \
    -Dtokenizer.profileSource=default \
    -Dtokenizer.profileTarget=token
```

Console Output

```console
[INFO] Scanning for projects...
[INFO] Starting Maven Build for BW6 Project.................................
[INFO] Checking for In-Project JAR dependencies if any and Pushing them to Local Maven Repository
[INFO] 
[INFO] ----------------< com.tibco.psg:order-mgmt.application >----------------
[INFO] Building order-mgmt.application 1.0.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] -------------------------------[ bwear ]--------------------------------
[INFO] 
[INFO] --- bw6-utilities-plugin:1.0.0-SNAPSHOT:tokenizer (default-cli) @ order-mgmt.application ---
[INFO] =========================================================================
[INFO]  BW6 Utilities Plugin: Starting Tokenizer Goal 
[INFO] =========================================================================
[INFO] Common tokens loaded from local path: C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application\resources\common-tokens.properties
[INFO] Application tokens loaded from: C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application\resources\app-tokens.properties
[INFO] Application Folder: C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application
[INFO] Creating tokenized profile token from profile default in C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application\META-INF
[INFO] Tokenizing substvar file: C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application\META-INF\default.substvar
[INFO] Processing 16 global variables for token replacement.
[WARNING] Key //order-mgmt//BW.APPNODE.NAME has an empty value
[INFO]   Name: //order-mgmt//BW.APPNODE.NAME, Original Value: , Tokenized Value: 
[WARNING] Key //order-mgmt//BW.DEPLOYMENTUNIT.NAME has an empty value
[INFO]   Name: //order-mgmt//BW.DEPLOYMENTUNIT.NAME, Original Value: , Tokenized Value: 
[INFO]   Name: //order-mgmt//BW.HOST.NAME, Original Value: localhost, Tokenized Value: localhost
[WARNING] Key //order-mgmt//BW.DEPLOYMENTUNIT.VERSION has an empty value
[INFO]   Name: //order-mgmt//BW.DEPLOYMENTUNIT.VERSION, Original Value: , Tokenized Value: 
[WARNING] Key //order-mgmt//BW.MODULE.VERSION has an empty value
[INFO]   Name: //order-mgmt//BW.MODULE.VERSION, Original Value: , Tokenized Value: 
[INFO]   Name: //order-mgmt//BW.CLOUD.PORT, Original Value: 8080, Tokenized Value: 8080
[WARNING] Key //order-mgmt//BW.MODULE.NAME has an empty value
[INFO]   Name: //order-mgmt//BW.MODULE.NAME, Original Value: , Tokenized Value: 
[WARNING] Key //common-lib//BW.APPNODE.NAME has an empty value
[INFO]   Name: //common-lib//BW.APPNODE.NAME, Original Value: , Tokenized Value: 
[WARNING] Key //common-lib//BW.DEPLOYMENTUNIT.NAME has an empty value
[INFO]   Name: //common-lib//BW.DEPLOYMENTUNIT.NAME, Original Value: , Tokenized Value: 
[INFO]   Name: //common-lib//BW.HOST.NAME, Original Value: localhost, Tokenized Value: localhost
[WARNING] Key //common-lib//BW.DEPLOYMENTUNIT.VERSION has an empty value
[INFO]   Name: //common-lib//BW.DEPLOYMENTUNIT.VERSION, Original Value: , Tokenized Value: 
[INFO]   Name: //common-lib///log/env, Original Value: DEV, Tokenized Value: PROD
[WARNING] Key //common-lib//BW.MODULE.VERSION has an empty value
[INFO]   Name: //common-lib//BW.MODULE.VERSION, Original Value: , Tokenized Value: 
[INFO]   Name: //common-lib//BW.CLOUD.PORT, Original Value: 8080, Tokenized Value: 8080
[WARNING] Key //common-lib//BW.MODULE.NAME has an empty value
[INFO]   Name: //common-lib//BW.MODULE.NAME, Original Value: , Tokenized Value: 
[INFO]   Name: //order-mgmt///order-mgmt/componentName, Original Value: placeOrder, Tokenized Value: placeOrderMgmt
[INFO] Tokenized profile successfully written to: C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application\META-INF\token.substvar
[INFO] =========================================================================
[INFO]  Tokenizer Goal finished successfully. 
[INFO] =========================================================================
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.669 s
[INFO] Finished at: 2025-11-17T11:20:01+01:00
[INFO] ------------------------------------------------------------------------
```

---

## 🎯 Goal: `extractProperties`

**(Prefix: `bw6-utilities:extractProperties`)**

This goal reads a `.substvar` file and extracts all variables and their current values into a standard Java `.properties` file.

| Parameter | Type | Default Value | Description | 
| ----- | ----- | ----- | ----- | 
| `extractProperties.sourceFile` | `File` | `${project.basedir}/META-INF/default.substvar` | **Input:** Path to the `.substvar` file to be read. | 
| `extractProperties.outputFile` | `File` | `${project.build.directory}/extracted-default.properties` | **Output:** Path where the new `.properties` file will be created. | 

### Command Example

Extracts the variables from the `META-INF/default.substvar` profile and saves them to a file named `target/extracted-default.properties`.

```bash
mvn bw6-utilities:extractProperties \
    -DextractProperties.sourceFile=META-INF/default.substvar \
    -DextractProperties.outputFile=target/extracted-default.properties
```

Console output
```console
[INFO] Scanning for projects...
[INFO] Starting Maven Build for BW6 Project.................................
[INFO] Checking for In-Project JAR dependencies if any and Pushing them to Local Maven Repository
[INFO] 
[INFO] ----------------< com.tibco.psg:order-mgmt.application >----------------
[INFO] Building order-mgmt.application 1.0.0-SNAPSHOT
[INFO] 	from pom.xml
[INFO] -------------------------------[ bwear ]--------------------------------
[INFO] 
[INFO] --- bw6-utilities-plugin:1.0.0-SNAPSHOT:extractProperties (default-cli) @ order-mgmt.application ---
[INFO] =========================================================================
[INFO]  BW6 Utilities Plugin: Starting ExtractProperties Goal 
[INFO] =========================================================================
[INFO] Source substvar file: C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application\META-INF\Default.substvar
[INFO] Output properties file: C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application\target\extracted-default.properties
[INFO] Found 16 global variables to process.
[INFO] Successfully wrote 16 properties to C:\Users\dmassimi\workspace-612-plugins-source\order-mgmt.application\target\extracted-default.properties
[INFO] =========================================================================
[INFO]  Property Extraction Goal finished successfully. Output saved to: extracted-default.properties
[INFO] =========================================================================
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.807 s
[INFO] Finished at: 2025-11-17T11:03:57+01:00
[INFO] ------------------------------------------------------------------------
```
---

## 👨‍💻 Eclipse integration

You will find the Eclipse launcher to run maven goals in the `/src/main/resources/launchers` directory.

A sample BW6 application (used for testing maven goals) has been added to `/src/main/resources/bw-project-test`.

---

## 🗒️ Compatibility

The plugin is designed for use in projects leveraging TIBCO BusinessWorks configuration files.

| Plugin Version | TIBCO BusinessWorks Compatibility | 
| ----- | ----- | 
| **1.0.0** | BWCE 2.8.2+ / BW6.12+ | 


