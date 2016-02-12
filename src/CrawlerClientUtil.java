import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class CrawlerClientUtil {
	boolean isCrawlerTaken;
	LinkedList<String> linksToPastReports;
	
	
	
	
	public CrawlerClientUtil(){
		isCrawlerTaken = false;
	}
	
	public boolean CrawlerTaken(){
		if(isCrawlerTaken){
			return true;
		}
		return false;
	}
	
	
	private String replaceCrawlerFormInIndexHtml(String html){
		String findInHtml = "<form id = 'crawlerForm' method = 'POST' action = '/execResult.html'>";
		String whereToStop = "</form>";
		
		String replaceWithString = "<h1>crawler already running…</h1>";
		
		int indexStart = html.indexOf(findInHtml);
		int indexEnd = html.indexOf(whereToStop, indexStart);
		return html.substring(0,indexStart) + replaceWithString + html.substring(indexEnd);
	}
	
	public String generateHtmlIfCrawlerIsAlreadyInExecution(){
		String userDir = System.getProperty("user.dir");
		String html = "";
		File htmlFile = new File(userDir + "\\GUI\\index.html");
		try {
			FileReader fileReader = new FileReader(htmlFile);
			BufferedReader reader = new BufferedReader(fileReader);
			String line;
			
			while((line = reader.readLine()) != null){
				html += line;
			}
			
			return null;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return html;
		
	}
	
	
}
