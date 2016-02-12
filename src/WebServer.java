import java.io.IOException;
import java.util.HashMap;

public class WebServer {
	
	public static final boolean TEST_PORT_SCAN = true;
	
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
		
		//DownloaderThreadPool threadP = new DownloaderThreadPool(10);
		//ServerListener webSrv = new ServerListener(threadP);
		
		//webSrv.start();
		CrawlerControler.getInstance();
		//if input port scan
		if (TEST_PORT_SCAN) {
			
			//TODO - make it another method
			Thread[] scanners = new Thread[10];
			int startScanPort = 0;
			int portsPerScanner = 100;  // TODO - change it
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
			
		} else {
			startCrawling();
		}
			
	}
	
	public static void startCrawling() {
		DownloaderTask task = new DownloaderTask("http://smallbasic.com");
		CrawlerControler.getInstance().addTaskToDownloaderQueue(task);
	}
}
