
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
			//State stop = State.STOPPING;
			//CrawlerControler.getInstance().changeState(State.STOPPING);
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