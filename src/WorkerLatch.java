public class WorkerLatch {

	private static WorkerLatch instance = new WorkerLatch();
	protected int counter = 0;

	public static WorkerLatch getInstance() {
		return instance;
	}
	
	protected WorkerLatch() {
	}

	public synchronized void down() {
		counter--;
		if (counter == 0) {
			finish();
		}
	}

	public void finish() {
		//TODO - STOP ALL THREADS RUNNING
		System.out.println("Crawling needs to finish");
		try {
			CrawlerControler.getInstance().print();
			CrawlerControler.getInstance().saveReport();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void up() {
		counter++;
	}
}
