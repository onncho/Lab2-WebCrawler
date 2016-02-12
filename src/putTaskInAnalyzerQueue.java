
public class putTaskInAnalyzerQueue {

	SynchronizedQueueLL m_DownloaderQueue;
	
	// create collection of workers
	int m_NumOfDownloaders;
	WorkerT[] m_DownloadersWorkersThreads;
	
	//TODO: maybe to Add List of Links
	
	public putTaskInAnalyzerQueue(int i_NumOfDownloaders) {
		m_NumOfDownloaders = i_NumOfDownloaders;
		m_DownloaderQueue = new SynchronizedQueueLL();
		m_DownloadersWorkersThreads = new WorkerT[m_NumOfDownloaders];
		
		for (WorkerT thread : m_DownloadersWorkersThreads) {
			thread = new WorkerT(m_DownloaderQueue);
			thread.start();
		}
	}
	
	// add task in downloader queue
	public void putTaskInDownloaderQueue(Runnable task) {
		synchronized (m_DownloaderQueue) {
			//up the letch counter
			WorkerLatch.getInstance().up();
			m_DownloaderQueue.enqueue(task);
		}
	}
	
}
