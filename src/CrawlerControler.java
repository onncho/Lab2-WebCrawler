import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CrawlerControler {
	
	private static CrawlerControler instance = new CrawlerControler();
	
	private ReportPerDomain m_ReportPerDomain;
	private DownloaderThreadPool m_DownloaderPool;
	private AnalyzerThreadPool m_AnalyzerPool;
	
	public static CrawlerControler getInstance() {
		return instance;
	}
	
	private CrawlerControler() {
		
		// TODO: get domain get from config.ini
		m_ReportPerDomain = new ReportPerDomain("http://www.naon-serv.co.il");
		m_DownloaderPool = new DownloaderThreadPool(10);
		m_AnalyzerPool = new AnalyzerThreadPool(2);
	}
	
	
	public void addTaskToDownloaderQueue(Runnable task) {
		m_DownloaderPool.putTaskInDownloaderQueue(task);
	}
	
	public void addTaskToAnalyzerQueue(Runnable task) {
		m_AnalyzerPool.putTaskInAnalyzerQueue(task);
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
				//String realValueOfLine = "";
				switch(lineFromReader){
				case "#_ROBOTS_#":
					htmlTemplate += report.isDisrespectRobot;
					break;
				case "#_AMOUNT$IMAGES_#":
					htmlTemplate += report.getNumOfImages();
					break;
				case "#_SIZE$IMAGES_#":
					htmlTemplate += report.getSizeOfImages();
					break;
				case "#_AMOUNT$VIDEOS_#":
					htmlTemplate += report.getNumOfVideos();
					break;
				case "#_SIZE$VIDEOS_#":
					htmlTemplate += report.getSizeOfVideos();
					break;
				case "#_AMOUNT$DOCUMENTS_#":
					htmlTemplate += report.getNumOfDocuments();
					break;
				case "#_SIZE$DOCUMENTS_#":
					htmlTemplate += report.getSizeOfDocuments();
					break;
				case "#_AMOUNT$PAGES_#":
					htmlTemplate += report.getNumOfOverallPages();
					break;
				case "#_SIZE$PAGES_#":
					htmlTemplate += report.getSizeOfOverAllPages();
					break;
				case "#_AMOUNT$INTERNAL_#":
					htmlTemplate += report.getNumOfInternalPages();
					break;
				case "#_AMOUNT$EXTERNAL_#":
					htmlTemplate += report.getNumOfExternalPages();
					break;
				case "#_AMOUNT$DOM$CONNECTED_#":
					htmlTemplate += report.getFileNamesOfConnectedDomains();
					
				}
				htmlTemplate += lineFromReader;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
