
public class PortScannerLatch extends WorkerLatch {
	
	private static PortScannerLatch instance = new PortScannerLatch();
	
	protected PortScannerLatch() {
		super();
		this.counter = Integer.parseInt(ConfigurationObject.getMaxDownloaders());
	}
	
	public static PortScannerLatch getInstance() {
		return instance;
	}
	
	@Override
	public void finish() {
		System.out.println("Finished Port Scan");
		CrawlerControler.getInstance().switchPortScannerStatus();
	}
}
