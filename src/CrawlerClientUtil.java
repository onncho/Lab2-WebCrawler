import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class CrawlerClientUtil {
	boolean isCrawlerTaken;
	String crawlerTakenByDomain;
	HashMap<String,String> linksToPastReports;
	
	
	
	
	public CrawlerClientUtil(){
		isCrawlerTaken = false;
		crawlerTakenByDomain = null;
	}
	
	public boolean CrawlerTaken(){
		if(isCrawlerTaken){
			return true;
		}
		return false;
	}
	
	private boolean switchLock(){
		isCrawlerTaken = !isCrawlerTaken;
		return isCrawlerTaken;
	}
	
	public boolean lockCrawler(String domain){
		if(!CrawlerTaken()){
			crawlerTakenByDomain = domain;
			switchLock();
		}
	}
	
	public String getJStoAddToExecResults(String domain){
		String js = "<script>\n" +
            "var mayIStopChecking  = false;\n" +
            "var reportChecker = setInterval(function(){checkIfReportIsFinished();}, 3000);\n" + 
            "function checkIfReportIsFinished(){\n" + 
                "if(mayIStopChecking){\n" + 
                    "clearInterval(reportChecker);\n" + 
                "}\n" + 
                "var domainToCheckFor = '" + domain + "';\n" +
                "var ajax = new XMLHttpRequest();\n" +
                "var url = 'localhost:8080/getCrawlerReport.html?Domain=' + domainToCheckFor;\n" + 
                "ajax.onreadystatechange = function() {\n" +
                    "if (ajax.readyState == 4 && ajax.status == 200) {\n" +
                        "var res = ajax.responseText;\n" +
                    "if(res !== 'no'){\n"+
                        "document.getElementById('containerToReplace').innerHTML = res;\n" +
                        "mayIStopChecking = true;\n" +
                       "}\n"+
                    "}\n" +
                "};\n" +
                "ajax.open('GET', url, true);\n" +
                "ajax.send();\n" +
            "}\n" + 
        "</script>\n";
		
		return js;
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
			
			return replaceCrawlerFormInIndexHtml(html);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
}
