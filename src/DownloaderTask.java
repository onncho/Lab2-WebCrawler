
public class DownloaderTask implements Runnable {
	
	String m_UrlToDownload;
	HTTPQuery m_QuerySite;
	//AnalyzerThreadPool m_AnalyzerThreadPool;
	String[] m_DownloadedHtmlWithBody;
	AnalyzerTask m_AnalyzerTask;
	String m_PageSizeAndType;

	public DownloaderTask(String i_UrlToDownload) {
		m_UrlToDownload = i_UrlToDownload;
		//m_AnalyzerThreadPool = i_threadPool;
		m_QuerySite = new HTTPQuery();
	}

	@Override
	public void run() {
		try {
			
			if (!CrawlerDB.getInstance().linkExist(m_UrlToDownload)) {
				
				System.out.println("Downloader starts downloading URL: \t" +  m_UrlToDownload);
				m_DownloadedHtmlWithBody = m_QuerySite.sendHttpGetRequest(m_UrlToDownload);
				System.out.println("Downloader ends downloading URL: \t" + m_UrlToDownload);
				
				//System.out.println("$$$$$$$$$$$$$$$$$$$$" + "Reponse For Downloader" + "\n" + m_DownloadedHtmlWithBody[0] + "$$$$$$$$$$$$$$$");
				
				// crawling only the 200 ok links.
				// TODO: deals with 301
				String response = m_DownloadedHtmlWithBody[0];
				boolean flagForResponse = response.contains("200 OK");
				
				if (flagForResponse) {
					
					CrawlerDB.getInstance().addDownloadLink(m_UrlToDownload);
					
					String body = m_DownloadedHtmlWithBody[1];

					m_PageSizeAndType = m_QuerySite.parseContentLengthFromHttpResponse(m_DownloadedHtmlWithBody[0]);

					//TODO: analyzing Task
					m_AnalyzerTask = new AnalyzerTask(body, m_UrlToDownload);
					//put in analyzerqueue
					CrawlerControler.getInstance().addTaskToAnalyzerQueue(m_AnalyzerTask);
					//m_AnalyzerThreadPool.putTaskInAnalyzersQueue((Runnable) m_AnalyzerTask);				
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
