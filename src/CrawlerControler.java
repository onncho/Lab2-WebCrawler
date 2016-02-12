import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CrawlerControler {
	
	private static CrawlerControler instance = new CrawlerControler();
	
	private ReportPerDomain m_ReportPerDomain;
	
	public static CrawlerControler getInstance() {
		return instance;
	}
	
	private CrawlerControler() {
		
		// TODO: get domain
		m_ReportPerDomain = new ReportPerDomain("domain");
	}
	
	// All Method need to be accessed
	public void addNumOfDocs() {
		m_ReportPerDomain.addNumOfDocs();
	}
		
	public void sumSizeOfDocs(int i_docsSize) {
		m_ReportPerDomain.sumSizeOfDocs(i_docsSize);
	}
	
	public void addNumOfPages() {
		m_ReportPerDomain.addNumOfPages();
	}
	
	public void sumSizeOfPages(int i_pagesSize) {
		m_ReportPerDomain.sumSizeOfPages(i_pagesSize);
	}
	
	public void addNumOfImages() {
		m_ReportPerDomain.addNumOfImages();
	}
	
	public void sumSizeOfImages(int i_imagesSize) {
		m_ReportPerDomain.sumSizeOfImages(i_imagesSize);
	}
	
	public void addNumOfVideos() {
		m_ReportPerDomain.addNumOfVideos();
	}
	
	public void sumSizeOfVideos(int i_videoSize) {
		m_ReportPerDomain.sumSizeOfVideos(i_videoSize);
	}
	
	public void addNumOfInternalLinks() {
		m_ReportPerDomain.addNumOfInternalLinks();
	}
	
	public void addNumOfExternalLinks() {
		m_ReportPerDomain.addNumOfExternalLinks();
	}
	
	public void saveReport(ReportPerDomain report){
		String pathToRoot = System.getProperty("user.dir") + "//serverroot//";
		File fileToOpen = new File(pathToRoot + "reportTemplate.txt");
		String htmlTemplate = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileToOpen));
			String lineFromReader;
			while((lineFromReader = reader.readLine()) != null){
				htmlTemplate += lineFromReader;
			}
		} catch (FileNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
