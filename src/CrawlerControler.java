
public class CrawlerControler {
	
	private static CrawlerControler instance = new CrawlerControler();
	
	ReportPerDomain m_ReportPerDomain;
	
	public static CrawlerControler getInstance() {
		return instance;
	}
	
	private CrawlerControler() {
		
		// TODO: get domain
		m_ReportPerDomain = new ReportPerDomain("domain");
	}

}
