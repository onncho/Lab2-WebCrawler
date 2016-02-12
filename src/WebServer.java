import java.io.IOException;
import java.util.HashMap;

public class WebServer {
	
	public static final boolean TEST_PORT_SCAN = false;
	public static int Number_Of_Downloaders = 0;
	
	public static void main(String[] args) throws IOException
	{
		HashMap<String, String> m_confList = new HashMap<>();
		m_confList = Utils.readConfFile();

		// create configuration object
		ConfigurationObject.getConfigurationObject().setup(m_confList);

		if(ConfigurationObject.getPortNumber() == null){
			throw new IOException("Problem in Reading The Config File");
		}

		//ThreadPool threadPool = new ThreadPool(ConfigurationObject.getMaxThreads());
		//ServerListener webserver = new ServerListener(threadPool);

		//webserver.start();
		Number_Of_Downloaders = Integer.parseInt(ConfigurationObject.getMaxDownloaders());
		//DownloaderThreadPool threadP = new DownloaderThreadPool(Number_Of_Downloaders);
		//ServerListener webSrv = new ServerListener(threadP);
		//webSrv.start();
		
		CrawlerControler.getInstance();
		
		//if input port scan
		if (TEST_PORT_SCAN) {
			startPortScanner();
		} 
		// TODO: check how to get the indicatation to start the web crawler and to The Port Sccanner
		startCrawling();
			
	}
	
	//TODO: Domain transfer
	private static void startPortScanner() {
		Thread[] scanners = new Thread[Number_Of_Downloaders];
		int startScanPort = 0;
		int portsPerScanner = 65535 / Number_Of_Downloaders;
		
		PortScannerLatch scannerLatch = PortScannerLatch.getInstance();
		
		//Create threads
		for (int i = 0; i < scanners.length; i++) {
			int endPort = startScanPort + portsPerScanner;
			PortScanner scanner = new PortScanner("http://smallbasic.com",
					startScanPort, endPort, scannerLatch);
			scanners[i] = new Thread(scanner);
			startScanPort = endPort + 1;
		}
		
		//Start thread
		for (Thread thread : scanners) {
			thread.start();
		}
	}
	
	// get the input from the user of the requested domain
	public static void startCrawling() {
		DownloaderTask task = new DownloaderTask("http://smallbasic.com");
		CrawlerControler.getInstance().addTaskToDownloaderQueue(task);
	}
}
