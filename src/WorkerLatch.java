public class WorkerLatch {

	private static WorkerLatch instance = new WorkerLatch();
	private int counter = 0;

	public static WorkerLatch getInstance() {
		return instance;
	}
	
	private WorkerLatch() {
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
	}

	public synchronized void up() {
		counter++;
	}
}
