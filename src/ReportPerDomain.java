import java.util.LinkedList;

public class ReportPerDomain {
	
	private String m_Domain;
	private LinkedList<String> m_ConnectedDomains;
	private int m_NumOfDocuments;
	private int m_SizeOfDocuments;
	private int m_NumOfPages;
	private int m_SizeOfPages;
	private int m_NumOfImages;
	private int m_SizeOfImages;
	private int m_NumOfVideos;
	private int m_SizeOfVideos;
	
	private int m_NumInternalLinks;
	private int m_NumExternalLinks;
	
	//TODO:
	public boolean isDisrespectRobot;
	private LinkedList<Integer> ports;
	private String dateTime;
	private long sumRTT;
	private long numRTT;
	
	public ReportPerDomain(String i_Domain) {
		m_Domain = i_Domain;
		m_ConnectedDomains = new LinkedList<>();
		m_NumOfDocuments = 0;
		m_SizeOfDocuments = 0;
		m_NumOfPages = 0;
		m_SizeOfPages = 0;
		m_NumOfImages = 0;
		m_SizeOfImages = 0;
		m_NumOfVideos = 0;
		m_SizeOfVideos = 0;
		m_NumInternalLinks = 0;
		m_NumExternalLinks = 0;
	}
	
	public synchronized void addNumOfDocs() {
		m_NumOfDocuments++;
	}



}
