import java.util.LinkedList;

public class SynchronizedQueueLL {
	
	private final LinkedList<Runnable> m_buffer;
	
	public SynchronizedQueueLL() {
		m_buffer = new LinkedList<Runnable>();
	}
	
	// dequeue operation
	public synchronized Runnable dequeue() throws InterruptedException {
		
		// in case the queue is empty
		while (m_buffer.isEmpty())
		{
			wait();	
		}
		
		// dequeue - pull the first task from the queue
		Runnable task = m_buffer.remove();
		return task;
	}
	
	// queue operation
	public synchronized void enqueue(Runnable i_item) {
		// add to queue to the end of the list
		m_buffer.add(i_item);
		notifyAll();
	}
	
	// return the number of items in queue
	public synchronized int getCapacity() {
		return m_buffer.size();
	}
	
	public synchronized boolean isEmpty() {
		return m_buffer.isEmpty();
	}
	
	public void clear() {
		m_buffer.clear();
	}
}