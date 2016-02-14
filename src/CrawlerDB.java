import java.util.HashMap;
import java.util.LinkedList;

public class CrawlerDB {

	private static CrawlerDB instance = new CrawlerDB();
	private static LinkedList<String> m_DownloadLinks;
	private LinkedList<String[]> m_ReportsIncludingPaths; 
	private static HashMap<String,LinkedList<String> >m_connectedDomain;
	public static CrawlerDB getInstance() {
		return instance;
	}

	private CrawlerDB() {
		m_DownloadLinks = new LinkedList<>();
		m_connectedDomain = new HashMap<>();
		m_ReportsIncludingPaths = new LinkedList<>();
		m_connectedDomain = new HashMap<>();
	}

	public void addReportAndPath(String[] i_reportAndPath) {
		m_DownloadLinks.clear();
		m_DownloadLinks = new LinkedList<>();
		m_ReportsIncludingPaths.addLast(i_reportAndPath);
	}

	public String[] getLastReportIncludingPath() {
		return m_ReportsIncludingPaths.getLast();
	}

	public LinkedList<String[]> getAllReports() {
		return m_ReportsIncludingPaths;
	}

	public synchronized LinkedList<String> getDownloadedLinks() {
		return m_DownloadLinks;
	}

	public synchronized void addDownloadLink(String i_link) {
		m_DownloadLinks.add(i_link);
	}
	
	public synchronized void addConnectedDomain(String domainCrawled, String domainFound){
		if(!m_connectedDomain.get(domainCrawled).contains(domainFound)){
			m_connectedDomain.get(domainCrawled).add(domainFound);
		}
	}
	public synchronized void initConnectedDomainList(String Domain){
		if(m_connectedDomain.containsKey(Domain)){
			if(m_connectedDomain.get(Domain) == null){
				m_connectedDomain.put(Domain, new LinkedList<String>());
			}
		} else {
			m_connectedDomain.put(Domain, new LinkedList<String>());
		}
	}

	public synchronized boolean linkExist(String i_link) {
		boolean res = false;
		if (m_DownloadLinks.contains(i_link)) {
			res = true;
		}
		return res;
	}
}
