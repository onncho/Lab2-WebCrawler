
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
		try {
			CrawlerControler.getInstance().print();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
