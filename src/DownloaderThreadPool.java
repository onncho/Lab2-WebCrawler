
public class DownloaderThreadPool {
	
	SynchronizedQueueLL m_DownloaderQueue;
	
	// create collection of workers
	int m_NumOfDownloaders;
	WorkerT[] m_WorkersThreads;
	
	//TODO: maybe to Add List of Links
	
	public DownloaderThreadPool (int i_NumOfDownloaders) {
		m_NumOfDownloaders = i_NumOfDownloaders;
		m_DownloaderQueue = new SynchronizedQueueLL();
		m_WorkersThreads = new WorkerT[m_NumOfDownloaders];
		
		for (WorkerT thread : m_WorkersThreads) {
			thread = new WorkerT(m_DownloaderQueue);
			thread.start();
		}
	}
	
	// add task in Analyzer queue
	public void putTaskInDownloaderQueue(Runnable task) {
		synchronized (m_DownloaderQueue) {
			//up the latch counter
			WorkerLatch.getInstance().up();
			m_DownloaderQueue.enqueue(task);
		}
	}
	
}
