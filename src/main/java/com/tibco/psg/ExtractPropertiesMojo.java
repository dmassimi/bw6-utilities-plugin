package com.tibco.psg;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Goal that extracts global variables from a BW6 substitution variable file 
 * (.substvar) and writes them to a standard Java .properties file.
 * It is invoked using the command: mvn bw6-utilities-plugin:extractProperties
 */
@Mojo(name = "extractProperties")
public class ExtractPropertiesMojo extends AbstractMojo {

    /**
     * The Maven project being built. Automatically injected.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Path to the input BW6 substitution variable file (.substvar).
     * E.g., -DextractProperties.sourceFile=META-INF/Tokenized.substvar
     */
    @Parameter(property = "extractProperties.sourceFile", 
               defaultValue = "${project.basedir}/META-INF/default.substvar",
               required = true)
    private File sourceFile;

    /**
     * Path where the output properties file should be written.
     * E.g., -DextractProperties.outputFile=target/extracted.properties
     */
    @Parameter(property = "extractProperties.outputFile", 
               defaultValue = "${project.build.directory}/extracted-config.properties")
    private File outputFile;

    /**
     * The main execution method for the Mojo.
     *
     * @throws MojoExecutionException if the plugin execution fails.
     */
    public void execute() throws MojoExecutionException {
        getLog().info("=========================================================================");
        getLog().info(" BW6 Utilities Plugin: Starting ExtractProperties Goal ");
        getLog().info("=========================================================================");

        getLog().info("Source substvar file: " + sourceFile.getAbsolutePath());
        getLog().info("Output properties file: " + outputFile.getAbsolutePath());
        
        if (!sourceFile.exists()) {
            throw new MojoExecutionException("Source file not found: " + sourceFile.getAbsolutePath() + 
                                             ". Please ensure the file path is correct.");
        }
        
        try {
            // 1. Parse the substvar file and extract properties
            Properties extractedProps = parseSubstvarFile(sourceFile);
            
            // 2. Write properties to the output file
            writePropertiesFile(extractedProps, outputFile);
            
        } catch (Exception e) {
            getLog().error("Extraction failed: " + e.getMessage(), e);
            throw new MojoExecutionException("Failed to extract properties from substvar file.", e);
        }

        getLog().info("=========================================================================");
        getLog().info(" Property Extraction Goal finished successfully. Output saved to: " + outputFile.getName());
        getLog().info("=========================================================================");
    }

    /**
     * Parses the BW6 .substvar XML file and extracts global variables into a Properties object.
     * * @param inputXmlFile The .substvar file to read.
     * @return A Properties object containing the extracted variables.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Properties parseSubstvarFile(File inputXmlFile) throws ParserConfigurationException, SAXException, IOException {
        Properties extractedProperties = new Properties();
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(inputXmlFile);
        Element docEle = dom.getDocumentElement();

        NodeList nl = docEle.getElementsByTagName("globalVariable");
        int length = nl.getLength();

        getLog().info("Found " + length + " global variables to process.");

        for (int i = 0; i < length; i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) node;

                // Extract Name
                String name = el.getElementsByTagName("name").item(0).getTextContent();
                
                // Extract Value (handle cases where 'value' tag might be missing, e.g., for password variables)
                String value = "";
                NodeList valueNodes = el.getElementsByTagName("value");
                if (valueNodes.getLength() > 0 && valueNodes.item(0) != null) {
                    value = valueNodes.item(0).getTextContent();
                } else {
                    getLog().debug("Variable '" + name + "' has no <value> tag. Using empty string.");
                }

                // Add to Properties object
                extractedProperties.setProperty(name, value);
                getLog().debug("Extracted: " + name + "=" + value);
            }
        }
        
        return extractedProperties;
    }

    /**
     * Writes the given Properties object to the specified output file.
     * * @param props The properties to write.
     * @param outputFile The destination file.
     * @throws IOException
     */
    private void writePropertiesFile(Properties props, File outputFile) throws IOException {
        // Ensure the parent directory exists
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        try (OutputStream output = new FileOutputStream(outputFile)) {
            props.store(output, "BW6 Global Variables extracted by bw6-utilities-plugin");
            getLog().info("Successfully wrote " + props.size() + " properties to " + outputFile.getAbsolutePath());
        }
    }
}