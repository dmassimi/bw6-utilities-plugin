package com.tibco.psg;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.commons.io.FileUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.Properties;

/**
 * Goal that performs token replacement within BW6 substitution variable files (.substvar).
 * It is invoked using the command: mvn bw6-utilities-plugin:tokenizer
 */
@Mojo(name = "tokenizer")
public class TokenizerMojo extends AbstractMojo {

    private Properties commonTokens;
    private Properties appTokens;
    private final String GITLAB_JKS = "src/main/resources/gitlab.jks"; // Hardcoded path from original source

    // Replaces args[0]
    @Parameter(property = "tokenizer.applicationFolder", defaultValue = "${project.basedir}")
    private File applicationFolder;

    // Replaces args[1]
    @Parameter(property = "tokenizer.profileSource", defaultValue = "default")
    private String profileSource;

    // Replaces args[2]
    @Parameter(property = "tokenizer.profileTarget", defaultValue = "token")
    private String profileTarget;

    // Replaces args[3] (local common properties file)
    @Parameter(property = "tokenizer.commonTokensLocalPath", defaultValue = "${project.basedir}/resources/common-tokens.properties")
    private File commonTokensLocalPath;

    // Replaces args[4] (app properties file)
    @Parameter(property = "tokenizer.appTokensLocalPath", defaultValue = "${project.basedir}/resources/app-tokens.properties")
    private File appTokensLocalPath;

    // Replaces args[5]
    @Parameter(property = "tokenizer.isCommonRemote", defaultValue = "false")
    private boolean isCommonRemote;

    // Replaces args[6]
    @Parameter(property = "tokenizer.commonTokensPropertiesLink")
    private String commonTokensPropertiesLink;

    // Replaces args[7]
    @Parameter(property = "tokenizer.destinationLocalPathCommonLink", defaultValue = "${project.build.directory}/downloaded-common.properties")
    private File destinationLocalPathCommonLink;
    
    /**
     * The main execution method for the Mojo.
     *
     * @throws MojoExecutionException if the plugin execution fails.
     */
    public void execute() throws MojoExecutionException {
        getLog().info("=========================================================================");
        getLog().info(" BW6 Utilities Plugin: Starting Tokenizer Goal ");
        getLog().info("=========================================================================");

        // --- 1. Load Tokens ---
        try {
            loadTokens();
        } catch (Exception ex) {
            getLog().error("Error during loading tokens!", ex);
            throw new MojoExecutionException("Failed to load token properties.", ex);
        }

        // --- 2. Tokenize File ---
        String metaInfPath = applicationFolder.getAbsolutePath() + File.separator + "META-INF";
        getLog().info("Application Folder: " + applicationFolder.getAbsolutePath());
        getLog().info("Creating tokenized profile " + profileTarget + " from profile " + profileSource + " in " + metaInfPath);

        try {
            tokenizeValues(
                metaInfPath + File.separator + profileSource + ".substvar",
                metaInfPath + File.separator + profileTarget + ".substvar"
            );
        } catch (Exception e) {
            getLog().error("Error during tokenization process: " + e.getMessage(), e);
            throw new MojoExecutionException("Tokenization failed.", e);
        }
        
        getLog().info("=========================================================================");
        getLog().info(" Tokenizer Goal finished successfully. ");
        getLog().info("=========================================================================");
    }

    /**
     * Logic from the original main method to load properties.
     */
    private void loadTokens() throws Exception {
        
        // --- Load Common Tokens (Remote or Local) ---
        InputStream commonTokensPropertiesFile = null;
        if (isCommonRemote) {
            getLog().info("Attempting to load Common tokens from remote URL: " + commonTokensPropertiesLink);
            
            // Note: This security setup is copied directly from the original source.
            // In a real-world scenario, this JKS handling should be done more robustly.
            InputStream trustStream = new FileInputStream(GITLAB_JKS);
            char[] trustPassword = "changeit".toCharArray();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(trustStream, trustPassword);
            
            TrustManagerFactory trustFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(trustStore);
            TrustManager[] trustManagers = trustFactory.getTrustManagers();
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, null);
            SSLContext.setDefault(sslContext);
            
            URL url = new URL(commonTokensPropertiesLink);
            URLConnection connection = url.openConnection();
            connection.connect();

            // Use FileUtils from Apache Commons IO to save the remote file locally
            FileUtils.copyURLToFile(connection.getURL(), destinationLocalPathCommonLink);
            commonTokensPropertiesFile = new FileInputStream(destinationLocalPathCommonLink);
            getLog().info("Common tokens successfully downloaded to: " + destinationLocalPathCommonLink.getAbsolutePath());
            
        } else {
            if (!commonTokensLocalPath.exists()) {
                getLog().warn("Local common tokens file not found: " + commonTokensLocalPath.getAbsolutePath() + ". Skipping common tokens load.");
                commonTokens = new Properties(); // Initialize empty properties
            } else {
                commonTokensPropertiesFile = new FileInputStream(commonTokensLocalPath);
                getLog().info("Common tokens loaded from local path: " + commonTokensLocalPath.getAbsolutePath());
            }
        }
        
        commonTokens = new Properties();
        if (commonTokensPropertiesFile != null) {
            commonTokens.load(commonTokensPropertiesFile);
        }

        // --- Load Application Tokens (Local) ---
        if (!appTokensLocalPath.exists()) {
            getLog().error("Application tokens file not found: " + appTokensLocalPath.getAbsolutePath());
            throw new IOException("Mandatory application tokens file not found.");
        }
        
        InputStream appTokensPropertiesFile = new FileInputStream(appTokensLocalPath);
        appTokens = new Properties();
        appTokens.load(appTokensPropertiesFile);
        getLog().info("Application tokens loaded from: " + appTokensLocalPath.getAbsolutePath());
    }


    /**
     * Logic from the original searchToken method.
     */
    private String searchToken(String key, String value) {

        if (commonTokens.getProperty(key) != null) {
            value = commonTokens.getProperty(key);
        }
        if (appTokens.getProperty(key) != null) {
            value = appTokens.getProperty(key);
        }
        
        if (value.length() > 0)
            getLog().debug("Value found for key " + key + ": " + value);
        else
            getLog().warn("Key " + key + " has an empty value");

        return value;
    }


    /**
     * Logic from the original tokenizeValues method.
     */
    private void tokenizeValues(String inputProfile, String outputProfile)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        getLog().info("Tokenizing substvar file: " + inputProfile);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(inputProfile);
        Element docEle = dom.getDocumentElement();

        NodeList nl = docEle.getElementsByTagName("globalVariable");

        int length = nl.getLength();

        getLog().info("Processing " + length + " global variables for token replacement.");
        for (int i = 0; i < length; i++) {

            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) nl.item(i);

                if (el.getNodeName().contains("globalVariable")) {

                    String name = el.getElementsByTagName("name").item(0).getTextContent();
                    String value = "";

                    if (el.getElementsByTagName("value").item(0) != null)
                        value = el.getElementsByTagName("value").item(0).getTextContent();

                    String type = el.getElementsByTagName("type").item(0).getTextContent();

                    String tokenValue = searchToken(name, value);
                    getLog().info("  Name: " + name + ", Original Value: " + value + ", Tokenized Value: " + tokenValue);

                    boolean isTokenized = !value.equals(tokenValue);

                    if (!value.equals("")) {
                        el.getElementsByTagName("value").item(0).setTextContent(tokenValue);
                        
                        if (isTokenized) {
                            getLog().debug("    Change isOverride to true");
                            el.getElementsByTagName("isOverride").item(0).setTextContent("true");
                            
                            if (el.getElementsByTagName("lookupValue").item(0) != null) {
                                getLog().debug("    Remove lookupValue");
                                el.removeChild(el.getElementsByTagName("lookupValue").item(0));
                            }
                        }
                    }
                    
                    // Special handling for Password type variables
                    if (type.equals("Password") && isTokenized) {
                        getLog().info("    Password management: applying lookup for tokenized password.");
                        
                        // remove value
                        if (el.getElementsByTagName("value").item(0) != null) {
                            getLog().debug("    Password management: remove value tag");
                            el.removeChild(el.getElementsByTagName("value").item(0));
                        }

                        // remove old useLookupValue
                        if (el.getElementsByTagName("useLookupValue").item(0) != null) {
                            getLog().debug("    Password management: remove old useLookupValue");
                            el.removeChild(el.getElementsByTagName("useLookupValue").item(0));
                        }

                        // add useLookupValue = true
                        Element useLookupValueElem = dom.createElement("useLookupValue");
                        useLookupValueElem.setTextContent("true");
                        el.appendChild(useLookupValueElem);

                        // add lookupValue with the token
                        Element lookupValueElem = dom.createElement("lookupValue");
                        lookupValueElem.setTextContent(tokenValue);
                        el.appendChild(lookupValueElem);
                    }
                }
            }
        }
        
        // --- Write the DOM back to the new file ---
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(dom);
        StreamResult result = new StreamResult(new File(outputProfile));
        transformer.transform(source, result);
        
        getLog().info("Tokenized profile successfully written to: " + outputProfile);
    }
}