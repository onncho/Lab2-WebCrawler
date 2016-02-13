
public class DownloaderTask implements Runnable {

	String m_UrlToDownload;
	HTTPQuery m_QuerySite;
	String[] m_DownloadedHtmlWithBody;
	AnalyzerTask m_AnalyzerTask;
	String m_PageSizeAndType;

	public DownloaderTask(String i_UrlToDownload) {
		m_UrlToDownload = i_UrlToDownload;
		m_QuerySite = new HTTPQuery();
	}

	@Override
	public void run() {
		try {
			if(!m_UrlToDownload.trim().isEmpty() || m_UrlToDownload != null){
				if (!CrawlerDB.getInstance().linkExist(m_UrlToDownload)) {

					System.out.println("Downloader starts downloading URL: " +  m_UrlToDownload);
					m_DownloadedHtmlWithBody = m_QuerySite.sendHttpGetRequest(m_UrlToDownload);
					System.out.println("Downloader ends downloading URL: " + m_UrlToDownload);

					// crawling only the 200 ok links.
					String response = m_DownloadedHtmlWithBody[0];
					boolean flagForResponse = response.contains("200 OK");

					if (flagForResponse) {
						CrawlerDB.getInstance().addDownloadLink(m_UrlToDownload);
						String body = m_DownloadedHtmlWithBody[1];
						m_PageSizeAndType = m_QuerySite.getContentLengthFromResponse(m_DownloadedHtmlWithBody[0]);

						//put in analyzerqueue
						m_AnalyzerTask = new AnalyzerTask(body, m_UrlToDownload);
						CrawlerControler.getInstance().addTaskToAnalyzerQueue(m_AnalyzerTask);				
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
