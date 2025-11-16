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
| `tokenizer.appTokensLocalPath` | `File` | `${project.basedir}/resources/app-tokens.properties` | Path to the application-specific tokens file. | 
| `tokenizer.isCommonRemote` | `boolean` | `false` | Set to `true` to download common tokens from a remote URL. | 
| `tokenizer.commonTokensPropertiesLink` | `String` | *None* | The full URL for remote common properties (if `isCommonRemote` is `true`). | 

### Command Example (Local Tokens)

Reads from `META-INF/default.substvar`, applies tokens from `common-tokens.properties` and `app-tokens.properties`, and writes the output to `META-INF/token.substvar`. If the value has been set 2 times, the value applied will be the one in `app-tokens.properties`.

```bash
mvn bw6-utilities:tokenizer \
    -Dtokenizer.profileSource=default \
    -Dtokenizer.profileTarget=token
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

Extracts the variables from the `default` profile and saves them to a file named `target/extracted-default.properties`.

```bash
mvn bw6-utilities:extractProperties \
    -DextractProperties.sourceFile=META-INF/default.substvar \
    -DextractProperties.outputFile=target/extracted-default.properties
```

---

## 🗒️ Compatibility

The plugin is designed for use in projects leveraging TIBCO BusinessWorks configuration files.

| Plugin Version | TIBCO BusinessWorks Compatibility | 
| ----- | ----- | 
| **1.0.0** | BWCE 2.9.2+ / BW6.12+ | 
