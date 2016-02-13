import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.event.TreeWillExpandListener;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

public class CrawlerControler {

	public enum State {
		WAITING, RUNNING, STOPPING
	}

	private static CrawlerControler instance = new CrawlerControler();

	private ReportPerDomain m_ReportPerDomain;
	private DownloaderThreadPool m_DownloaderPool;
	private AnalyzerThreadPool m_AnalyzerPool;
	private State m_CrawlerState;
	private String m_timeAndDate;
	private boolean m_PortscannerRunning;
	
	public static CrawlerControler getInstance() {
		return instance;
	}

	private CrawlerControler() {

		// TODO: get domain get from config.ini
		
		m_DownloaderPool = new DownloaderThreadPool(2);
		m_AnalyzerPool = new AnalyzerThreadPool(1);
		m_CrawlerState = State.WAITING;
		m_PortscannerRunning = false;
	}


	public void addTaskToDownloaderQueue(Runnable task) {
		m_DownloaderPool.putTaskInDownloaderQueue(task);
	}

	public void addTaskToAnalyzerQueue(Runnable task) {
		m_AnalyzerPool.putTaskInAnalyzerQueue(task);
	}
	
	public void switchPortScannerStatus() {
		m_PortscannerRunning = !m_PortscannerRunning;
	}
	
	public boolean CrawlerIsWorking() {
		if (this.getState() == State.RUNNING) {
			return true;
		} else {
			return false;
		}
	}

	// start crawling
	public synchronized void startCrawling( String domain,  boolean shouldFullTcp, 
			 boolean shouldDisrespectRobot) {
		m_CrawlerState  = State.RUNNING;
		Date date = new Date();
		m_timeAndDate = date.toString();
		m_ReportPerDomain = new ReportPerDomain(domain);
		
		if(shouldFullTcp){
			m_PortscannerRunning = true;
			startPortScanner();
		}
		
		while(m_PortscannerRunning) {
			try {
				Thread.currentThread().sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		DownloaderTask task = new DownloaderTask(domain);
		addTaskToDownloaderQueue(task);
	}

	public synchronized void startPortScanner() {
		int Number_Of_Downloaders = Integer.parseInt(ConfigurationObject.getMaxDownloaders());
		Thread[] scanners = new Thread[Number_Of_Downloaders];
		int startScanPort = 0;
		int portsPerScanner = 65535 / Number_Of_Downloaders;
		

		PortScannerLatch scannerLatch = PortScannerLatch.getInstance();

		//Create threads
		for (int i = 0; i < scanners.length; i++) {
			int endPort = startScanPort + portsPerScanner;
			PortScanner scanner = new PortScanner(m_ReportPerDomain.getDomain(),
					startScanPort, endPort, scannerLatch);
			scanners[i] = new Thread(scanner);
			startScanPort = endPort + 1;
		}

		//Start thread
		for (Thread thread : scanners) {
			thread.start();
		}
		
	}

	public void changeState(State state) {
		m_CrawlerState = state;
	}
	
	public State getState() {
		return m_CrawlerState;
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

public synchronized String[] saveReport(){
		String fileName = "";
		String pathToRoot = System.getProperty("user.dir") + "//serverroot//";
		File fileToOpen = new File(pathToRoot + "reportTemplate.txt");
		String htmlTemplate = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileToOpen));
			String lineFromReader;
			while((lineFromReader = reader.readLine()) != null){
				//String realValueOfLine = "";
				switch(lineFromReader.trim()){
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
					htmlTemplate += m_ReportPerDomain.getNumOfConnectedDomains();
					break;
				case "#_CRAWLED$DOMS$REPORTS_#":
					htmlTemplate += m_ReportPerDomain.getFileNamesOfConnectedDomains();
					break;
				case "#_PORTS$OPEN_#" : 
					htmlTemplate += m_ReportPerDomain.getNumOfOpenPorts();
					break;
				default :
					htmlTemplate += lineFromReader;
					break;
				}
			}
			reader.close();
			
			
			/// Finished reading and inserting data ///
			String domain = m_ReportPerDomain.getDomain().replaceAll("/", "");
			domain = domain.replace("http:", "_");
			domain = domain.replaceAll(":", "_");
			m_timeAndDate = m_timeAndDate.replaceAll(" ", "_");
			fileName = (domain.replaceAll("\\.", "_")+ "_"+ m_timeAndDate.replaceAll(":", "_") + ".html");
			System.out.println("---> Report File Name: \t" + fileName);
			
			File report = new File(pathToRoot + "reports\\"+ fileName);
			PrintWriter writer = new PrintWriter(new FileWriter(report, true));
			writer.print(htmlTemplate);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//return "\\reports\\" +fileName;
		String[] pathAndBody = new String[2];
		pathAndBody[0] = "\\reports\\" + fileName;
		pathAndBody[1] = htmlTemplate;
		return pathAndBody;
	}


}
