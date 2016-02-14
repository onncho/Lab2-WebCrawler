
public class AnalyzerThreadPool {

	SynchronizedQueueLL m_AnalyzerQueue;
	
	// create collection of workers
	int m_NumOfAnalyzers;
	WorkerT[] m_WorkersThreads;
	
	//TODO: maybe to Add List of Links
	
	public AnalyzerThreadPool(int i_NumOfAnalyzers) {
		m_NumOfAnalyzers = i_NumOfAnalyzers;
		m_AnalyzerQueue = new SynchronizedQueueLL();
		createWorkers();
		
	}
	
	public void createWorkers() {
		
		m_WorkersThreads = new WorkerT[m_NumOfAnalyzers];
		for (WorkerT thread : m_WorkersThreads) {
			thread = new WorkerT(m_AnalyzerQueue);
			thread.start();
		}
	}
	
	public void stopWorker() {
		for (WorkerT thread : m_WorkersThreads) {
			thread.interrupt();
		}
		m_WorkersThreads = null;
		m_AnalyzerQueue.clear();
		createWorkers();
	}
	
	// add task in Analyzer queue
	public void putTaskInAnalyzerQueue(Runnable task) {
		synchronized (m_AnalyzerQueue) {
			//up the latch counter
			WorkerLatch.getInstance().up();
			m_AnalyzerQueue.enqueue(task);
		}
	}
	
}
