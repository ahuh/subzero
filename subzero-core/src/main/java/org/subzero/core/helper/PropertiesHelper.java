package org.subzero.core.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * Properties file Helper
 * @author Julien
 *
 */
public class PropertiesHelper {	
	/**
	 * Constant : properties file location
	 */
	public final static String PROPERTIES_FILE_PATH = "/SubZero.properties";
	
	private final static String BASEFOLDER_TOKEN = "{basefolder}";
	private final static String WORKINGFOLDER_TOKEN = "{workingfolder}";
	private final static String SEPARATOR_TOKEN = ";";
		
    private static Properties properties;
    
    /**
     * Configure the logger file
     */
    public static void configureLogger() {    	
    	try {
    		resolveLoggerAppenderReportPath();
    		resolveLoggerAppenderFilePath();
			PropertyConfigurator.configure(getPropertiesSingleton());
		} catch (Exception e) {
			System.out.println(String.format("ERROR : impossible to initiliaze log4j with configuration file '%s'", PROPERTIES_FILE_PATH));			
			e.printStackTrace();
		}
    }

    /**
     * Get Properties Singleton
     * @return
     * @throws Exception
     */
    private static Properties getPropertiesSingleton() throws Exception
    {
    	if (properties == null) {
    		properties = new Properties();
    		//properties.load(PropertiesHelper.class.getResourceAsStream(PROPERTIES_FILE_PATH));
    		URL propertiesURL = PropertiesHelper.class.getResource(PROPERTIES_FILE_PATH);
    		InputStream stream = new FileInputStream(new File(URLDecoder.decode(propertiesURL.getFile(), "UTF-8")));
    		properties.load(stream);
    		stream.close();
    	}
    	return properties;
    }
    
    /**
     * Reset configuration
     * @throws Exception
     */
    public static void resetConfiguration() throws Exception {
    	// Reset properties
    	resetProperties();
    	// Reset logger configuration
    	configureLogger();
    }    
    
    /**
     * Reset Properties
     */
    public static void resetProperties() {
    	if (properties != null) {
    		properties = null;
    	}
    }

    /**
     * Get a property after getting the properties singleton
     * @param propertyName
     * @return
     * @throws Exception
     */
    private static String getPropertyValue(String propertyName) throws Exception
    {
    	return getPropertiesSingleton().getProperty(propertyName);
    }
    
    /**
     * Set a property after getting the properties singleton
     * @param propertyName
     * @param propertyValue
     * @throws Exception
     */
    private static void setPropertyValue(String propertyName, String propertyValue) throws Exception
    {
    	getPropertiesSingleton().setProperty(propertyName, propertyValue);
    }
    
    /**
     * Resolve the Logger Appender Report Path with the BASEFOLDER_TOKEN and BASEFOLDER_TOKEN and WORKINGFOLDER_TOKEN
     * @throws Exception 
     */
    private static void resolveLoggerAppenderReportPath() throws Exception
    {
    	String propertyName = "log4j.appender.report.File";
    	String resolvedPath = getPropertyValue(propertyName)
    			.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath())
    			.replace(WORKINGFOLDER_TOKEN, PropertiesHelper.getWorkingFolderPath());
    	setPropertyValue(propertyName, resolvedPath);    	
    }
    
    /**
     * Resolve the Logger Appender File Path with the BASEFOLDER_TOKEN and BASEFOLDER_TOKEN and WORKINGFOLDER_TOKEN
     * @throws Exception 
     */
    private static void resolveLoggerAppenderFilePath() throws Exception
    {
    	String propertyName = "log4j.appender.file.File";
    	String resolvedPath = getPropertyValue(propertyName)
    			.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath())
    			.replace(WORKINGFOLDER_TOKEN, PropertiesHelper.getWorkingFolderPath());
    	setPropertyValue(propertyName, resolvedPath);    	
    }
    
    /**
     * getLoggerAppenderFilePath : get the active logger report path 
     * @return
     * @throws Exception
     */
    public static String getLoggerAppenderReportPath() throws Exception {
    	return getPropertyValue("log4j.appender.report.File");
    }
    
    /**
     * getLoggerAppenderFilePath : get the active logger file path 
     * @return
     * @throws Exception
     */
    public static String getLoggerAppenderFilePath() throws Exception {
    	return getPropertyValue("log4j.appender.file.File");
    }
    
    /**
     * getLoggerAppenderFolderPath : get the parent folder path of logger files
     * @return
     * @throws Exception
     */
    public static String getLoggerAppenderFolderPath() throws Exception {
    	File file = new File(getPropertyValue("log4j.appender.file.File"));
    	return file.getParent();
    }
    
    /**
     * getBaseFolderPath
     * @return
     * @throws Exception
     */
    public static String getBaseFolderPath() throws Exception {
    	return getPropertyValue("subzero.basefolder.path");
    }
    
    /**
     * getWorkingFolderPath
     * @return
     * @throws Exception
     */
    public static String getWorkingFolderPath() throws Exception {
    	return getPropertyValue("subzero.workingfolder.path").replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath());
    }
    
    /**
     * getOutputFolderPath
     * @param serie
     * @param season
     * @return
     * @throws Exception
     */
    public static String getOutputFolderPath(String serie, String season) throws Exception {
    	return getPropertyValue("subzero.outputfolder.path")
    			.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath())
    			.replace(WORKINGFOLDER_TOKEN, PropertiesHelper.getWorkingFolderPath())
    			.replace("{serie}", serie)
    			.replace("{season}", season);
    }
    
    /**
     * getAutomaticProcessInitDelay : property value in file in seconds
     * @return Value in milliseconds
     * @throws Exception
     */
    public static int getAutomaticProcessInitDelay() throws Exception {
    	return Integer.parseInt(getPropertyValue("subzero.automaticprocess.initdelay"))*1000;
    }
    
    /**
     * getAutomaticProcessFrequency : property value in file in minutes
     * @return Value in milliseconds
     * @throws Exception
     */
    public static int getAutomaticProcessFrequency() throws Exception {
    	return Integer.parseInt(getPropertyValue("subzero.automaticprocess.frequency"))*60*1000;
    }
    
    /**
     * getMkvMergeMoveMkvMergeCommandLine
     * @param outputFilePath
     * @param language
     * @param subFilePath
     * @param inputVideoFilePath
     * @return
     * @throws Exception
     */
    public static String getMkvMergeMoveMkvMergeCommandLine(String outputFilePath, String language, String subFilePath, String inputVideoFilePath) throws Exception {
    	return getPropertyValue("plugin.mkvmergemove.mkvmerge.commandline")
    			.replace("{outputFilePath}", outputFilePath)
		    	.replace("{language}", language)
		    	.replace("{subFilePath}", subFilePath)
		    	.replace("{inputVideoFilePath}", inputVideoFilePath);
    }    
    
    /**
     * getMkvMergeMoveOriFilesKeep
     * @return
     * @throws Exception
     */
    public static boolean getMkvMergeMoveOriFilesKeep() throws Exception {
    	return Boolean.parseBoolean(getPropertyValue("plugin.mkvmergemove.orifiles.keep"));
    }
    
    /**
     * getMkvMergeMoveOriFilesFolderPath
     * @return
     * @throws Exception
     */
    public static String getMkvMergeMoveOriFilesFolderPath() throws Exception {
    	return getPropertyValue("plugin.mkvmergemove.orifiles.folderpath")
    			.replace(BASEFOLDER_TOKEN, PropertiesHelper.getBaseFolderPath())
    			.replace(WORKINGFOLDER_TOKEN, PropertiesHelper.getWorkingFolderPath());
    }
    
    /**
     * getSubLeecherPlugins : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubLeecherPlugins() throws Exception {
    	return getPropertyValue("subzero.subleecher.plugins").split(SEPARATOR_TOKEN);
    }
    
    /**
     * getSubLeecherLanguages : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubLeecherLanguages() throws Exception {
    	return getPropertyValue("subzero.subleecher.languages").split(SEPARATOR_TOKEN);
    }
    
    /**
     * getSubLeecherReleaseGroupRequired
     * @return
     * @throws Exception
     */
    public static boolean getSubLeecherReleaseGroupRequired() throws Exception {
    	return Boolean.parseBoolean(getPropertyValue("subzero.subleecher.releasegroup.required"));
    }    
    
    /**
     * getPostProcessPlugin : multiple properties value in file
     * @return
     * @throws Exception
     */
    public static String getPostProcessPlugin() throws Exception {
    	return getPropertyValue("subzero.postprocess.plugin");
    }
    
    /**
     * getVideoFileExtensions : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getVideoFileExtensions() throws Exception {
    	return getPropertyValue("subzero.videofile.extensions").split(SEPARATOR_TOKEN);
    }
    
    /**
     * getInputFileNoiseStrings : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getInputFileNoiseStrings() throws Exception {
    	return getPropertyValue("subzero.inputfile.noisestrings").split(SEPARATOR_TOKEN);
    }
    
    /**
     * getSubFileExtensions : multiple properties value in file, separator SEPARATOR_TOKEN
     * @return
     * @throws Exception
     */
    public static String[] getSubFileExtensions() throws Exception {
    	return getPropertyValue("subzero.subfile.extensions").split(SEPARATOR_TOKEN);
    }
    
    /**
     * getSubfileLanguageSuffix
     * @param language
     * @return
     * @throws Exception
     */
    public static String getSubfileLanguageSuffix(String language) throws Exception {
    	return getPropertyValue("subzero.subfile.languagesuffix")
		    	.replace("{language}", language.substring(0, 2).toUpperCase());
    }
    
    /**
     * getPluginPodnapisiIdLanguage : get language ID for specified language
     * @param language
     * @return ID of the language, null if mapping not found
     * @throws Exception
     */
    public static String getPluginPodnapisiIdLanguage(String language) throws Exception {
    	return getPropertyValue(String.format("plugin.podnapisi.idlanguage.%s", language.toLowerCase()));
    }
    
}

