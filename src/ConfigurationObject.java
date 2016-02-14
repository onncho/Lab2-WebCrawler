import java.util.HashMap;
import java.util.LinkedList;

// SingleTone Class
public class ConfigurationObject {

	String m_defaultPage;
	private static String m_Port;
	private static String m_MaxThreads;
	private static String m_defaultPageFullUrl;
	private static String m_rootFolder;
	
	//Added For Lab2
	private static String m_MaxDownloaders;
	private static String m_MaxAnalyzers;
	private static LinkedList<String> m_imageExtensions;
	private static LinkedList<String> m_videoExtensions;
	private static LinkedList<String> m_documentExtensions;
	
	
	private static ConfigurationObject m_Configuration = new ConfigurationObject();
	
	private ConfigurationObject() {
		
	}
	
	public static ConfigurationObject getConfigurationObject() {
		return m_Configuration;
	}
	
	public void setup(HashMap<String, String> i_confList) {
		m_Port = i_confList.get("port");
		m_rootFolder = i_confList.get("root");
		m_defaultPage = i_confList.get("defaultPage");
		m_MaxThreads = i_confList.get("maxThreads");
		m_defaultPageFullUrl = m_rootFolder + "/" + m_defaultPage;
		
		m_MaxDownloaders = i_confList.get("maxDownloader");
		m_MaxAnalyzers = i_confList.get("maxAnalyzer");
		m_imageExtensions = (LinkedList<String>) parseStringToList(i_confList.get("imageExtensionsNotParsed"));
		m_videoExtensions = (LinkedList<String>) parseStringToList(i_confList.get("videoExtensionsNotParsed"));
		m_documentExtensions = (LinkedList<String>) parseStringToList(i_confList.get("documentExtensionsNotParsed"));	
	}
	
	private LinkedList<String> parseStringToList(String i_stringToParse) {
		i_stringToParse.trim();
		String[] values = i_stringToParse.split(",");	
		LinkedList<String> res = new LinkedList<>();
		
		for(String val : values)
		{
			if (val != null) {
				res.add(val);				
			}
		}
		
		return res;
	}

	public static String getMaxDownloaders() {
		return m_MaxDownloaders;
	}

	public static String getMaxAnalyzers() {
		return m_MaxAnalyzers;
	}

	public static LinkedList<String> getImageExtensions() {
		return m_imageExtensions;
	}

	public static LinkedList<String> getVideoExtensions() {
		return m_videoExtensions;
	}

	public static LinkedList<String> getDocumentExtensions() {
		return m_documentExtensions;
	}

	public static String getRoot()
	{
		return m_rootFolder;
	}
	
	public static String getDefaultPage()
	{
		return m_defaultPageFullUrl;
	}
	
	public static String getPortNumber()
	{
		return m_Port;
	}
	
	public static int getMaxThreads()
	{
		return Integer.parseInt(m_MaxThreads);	
	}
	
	public static boolean isImageExtension(String ext) {
		for (String image : m_imageExtensions) {
			if (ext.equals(image)) return true;
		}
		return false;
	}
	
	public static boolean isDocumentExtension(String ext) {
		for (String doc : m_documentExtensions) {
			if (ext.equals(doc)) return true;
		}
		return false;
	}
	
	public static boolean isVideoExtension(String ext) {
		for (String vid : m_videoExtensions) {
			if (ext.equals(vid)) return true;
		}
		return false;
	}
}