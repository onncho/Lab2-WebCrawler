import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
		m_ReportPerDomain = new ReportPerDomain("http://smallbasic.com");
		m_DownloaderPool = new DownloaderThreadPool(2);
		m_AnalyzerPool = new AnalyzerThreadPool(1);
	}
	
	
	public void addTaskToDownloaderQueue(Runnable task) {
		m_DownloaderPool.putTaskInDownloaderQueue(task);
	}
	
	public void addTaskToAnalyzerQueue(Runnable task) {
		m_AnalyzerPool.putTaskInAnalyzerQueue(task);
	}
	
	public void print() throws IllegalArgumentException, IllegalAccessException {
		m_ReportPerDomain.Print();
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
	
	public void addPorts(ArrayList<Integer> ports) {
		m_ReportPerDomain.addPorts(ports);
	}
	
	public void saveReport(){
		
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
					htmlTemplate += m_ReportPerDomain.isDisrespectRobot;
					break;
				case "#_AMOUNT$IMAGES_#":
					htmlTemplate += m_ReportPerDomain.getNumOfImages();
					break;
				case "#_SIZE$IMAGES_#":
					htmlTemplate += m_ReportPerDomain.getSizeOfImages();
					break;
				case "#_AMOUNT$VIDEOS_#":
					htmlTemplate += m_ReportPerDomain.getNumOfVideos();
					break;
				case "#_SIZE$VIDEOS_#":
					htmlTemplate += m_ReportPerDomain.getSizeOfVideos();
					break;
				case "#_AMOUNT$DOCUMENTS_#":
					htmlTemplate += m_ReportPerDomain.getNumOfDocuments();
					break;
				case "#_SIZE$DOCUMENTS_#":
					htmlTemplate += m_ReportPerDomain.getSizeOfDocuments();
					break;
				case "#_AMOUNT$PAGES_#":
					htmlTemplate += m_ReportPerDomain.getNumOfOverallPages();
					break;
				case "#_SIZE$PAGES_#":
					htmlTemplate += m_ReportPerDomain.getSizeOfOverAllPages();
					break;
				case "#_AMOUNT$INTERNAL_#":
					htmlTemplate += m_ReportPerDomain.getNumOfInternalPages();
					break;
				case "#_AMOUNT$EXTERNAL_#":
					htmlTemplate += m_ReportPerDomain.getNumOfExternalPages();
					break;
				case "#_AMOUNT$DOM$CONNECTED_#":
					htmlTemplate += m_ReportPerDomain.getFileNamesOfConnectedDomains();
					
				}
				htmlTemplate += lineFromReader;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
