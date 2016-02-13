
public class WorkerT extends Thread {

	SynchronizedQueueLL m_tasks;

	public WorkerT(SynchronizedQueueLL i_tasks) {
		m_tasks = i_tasks;
	}

	public void run() {
		
		while(!Thread.currentThread().isInterrupted()) {

			// try to get a task from taskQueue, if the Queue is empty wait if not take it and remove it from the queue
			Runnable taskToExecute;

			try {
				taskToExecute = (Runnable) m_tasks.dequeue();
				System.out.println("Downloader or Analyzer dequeues from their queue, number of items left: \t" + m_tasks.getCapacity());
				if (taskToExecute != null) {
					taskToExecute.run();
					WorkerLatch.getInstance().down();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
