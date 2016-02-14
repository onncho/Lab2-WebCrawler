
public class WorkerLatch {

	private static WorkerLatch instance = new WorkerLatch();
	protected int counter = 0;
	
	public static WorkerLatch getInstance() {
		return instance;
	}

	protected WorkerLatch() {
	}
	
	public synchronized int getCounter() {
		System.out.println(counter);
		return counter;
	}

	public synchronized void down() {
		counter--;
		if (counter == 0) {
			finish();
		}
	}

	public void finish() {
		System.out.println("Crawling needs to finish");
		try {
			CrawlerControler.getInstance().print();
			CrawlerControler.getInstance().changeState(CrawlerControler.State.STOPPING);
			CrawlerControler.getInstance().killThreadPool();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public synchronized void up() {
		counter++;
	}
}