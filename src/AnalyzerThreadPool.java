
public class AnalyzerThreadPool {

	SynchronizedQueueLL m_AnalyzerQueue;
	
	// create collection of workers
	int m_NumOfAnalyzers;
	WorkerT[] m_WorkersThreads;
	
	//TODO: maybe to Add List of Links
	
	public AnalyzerThreadPool(int i_NumOfAnalyzers) {
		m_NumOfAnalyzers = i_NumOfAnalyzers;
		m_AnalyzerQueue = new SynchronizedQueueLL();
		m_WorkersThreads = new WorkerT[m_NumOfAnalyzers];
		
		for (WorkerT thread : m_WorkersThreads) {
			thread = new WorkerT(m_AnalyzerQueue);
			thread.start();
		}
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
