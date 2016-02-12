import java.util.LinkedList;

public class CrawlerDB {
	
	private static CrawlerDB instance = new CrawlerDB();
	
	private static LinkedList<String> m_DownloadLinks;
	//TODO: decide on report object
	private static LinkedList<String> m_AnalysisReports;
	
	public static CrawlerDB getInstance() {
		return instance;
	}
	
	private CrawlerDB() {
		m_DownloadLinks = new LinkedList<>();
		m_AnalysisReports = new LinkedList<>();
	}
	
	public synchronized LinkedList<String> getDownloadedLinks() {
		return m_DownloadLinks;
	}
	
	public synchronized void addDownloadLink(String i_link) {
		m_DownloadLinks.add(i_link);
	}
	
	public synchronized boolean linkExist(String i_link) {
		boolean res = false;
		if (m_DownloadLinks.contains(i_link)) {
			res = true;
		}
		return res;
	}
}