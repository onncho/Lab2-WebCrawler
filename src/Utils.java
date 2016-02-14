

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public static byte[] readFile(File file)
	{
		try
		{
			FileInputStream fis = new FileInputStream(file);
			byte[] bFile = new byte[(int)file.length()];
			// read until the end of the stream.
			while(fis.available() != 0)
			{
				fis.read(bFile, 0, bFile.length);
			}
			fis.close();
			return bFile;
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Error: File Not Found");
		}
		catch(IOException e)
		{
			System.out.println("Error: IO Exception");
		}
		return null;
	}


	public static HashMap<String, String> readConfFile()
	{
		Properties prop = new Properties();
		InputStream input = null;
		HashMap<String, String> confList = new HashMap<>();


		String pathToConfIniFile = System.getProperty("user.dir") + "\\config.ini";

		try {
			input = new FileInputStream(pathToConfIniFile);

			// load a properties file
			prop.load(input);

			// get the property value and store them in hashmap
			confList.put("port", prop.getProperty("port"));
			confList.put("root", prop.getProperty("root"));
			confList.put("maxThreads", prop.getProperty("maxThreads"));
			confList.put("defaultPage", prop.getProperty("defaultPage"));

			//Lab2
			confList.put("maxDownloader", prop.getProperty("maxDownloader"));
			confList.put("maxAnalyzer", prop.getProperty("maxAnalyzer"));
			confList.put("imageExtensionsNotParsed", prop.getProperty("imageExtensions"));
			confList.put("videoExtensionsNotParsed", prop.getProperty("videoExtensions"));
			confList.put("documentExtensionsNotParsed", prop.getProperty("documentExtensions"));

		} catch (IOException ex) {
			System.err.println("Could not Read The Config File duo to bad Path or IOERROR");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("ERROR: IO Exception");
				}
			}
		}
		return confList;
	}
	
	public static String GetDomain(String url) {
		String toReturn = "";
		Pattern pattern = Pattern.compile("(http[s]?):\\/\\/[.*\\@]?(www\\.|.*@)?([\\w\\-]+\\.[\\w\\-]+[\\.[\\w\\-]+]*)(\\:\\d+)?(\\S*)");
		Matcher m = pattern.matcher(url);
		if (m.find()) {
			toReturn = "www." + m.group(3);
		}
	
		return toReturn;
	}
	
}
