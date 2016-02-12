import java.lang.reflect.Field;
import java.util.ArrayList;
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
		ports = new LinkedList<>();
	}
	
	//TODO: Debug
	public void Print() throws IllegalArgumentException, IllegalAccessException {
		for (Field field : this.getClass().getDeclaredFields()) {
		    field.setAccessible(true);
		    String name = field.getName();
		    Object value = field.get(this);
		    System.out.printf("Field name: %s, Field value: %s%n", name, value);
		}
	}
	
	public int getNumOfDocuments(){
		return m_NumOfDocuments;
	}
	public int getSizeOfDocuments(){
		return m_SizeOfDocuments;
	}
	public int getNumOfVideos(){
		return m_NumOfVideos;
	}
	public int getSizeOfVideos(){
		return m_SizeOfVideos;
	}
	
	public int getNumOfImages(){
		return m_NumOfImages;
	}
	public int getSizeOfImages(){
		return m_SizeOfImages;
	}
	
	public int getNumOfInternalPages(){
		return m_NumInternalLinks;
	}
	public int getNumOfExternalPages(){
		return m_NumExternalLinks;
	}
	
	public int getNumOfOverallPages(){
		return m_NumExternalLinks + m_NumInternalLinks;
	}
	
	public int getSizeOfOverAllPages(){
		return m_SizeOfPages;
	}
	
	
	public synchronized void addNumOfDocs() {
		m_NumOfDocuments++;
	}
	
	public synchronized void sumSizeOfDocs(int i_docsSize) {
		m_SizeOfDocuments += i_docsSize;
	}
	
	public synchronized void addNumOfPages() {
		m_NumOfPages++;
	}
	
	public synchronized void sumSizeOfPages(int i_pagesSize) {
		m_SizeOfPages += i_pagesSize;
	}
	
	public synchronized void addNumOfImages() {
		m_NumOfImages++;
	}
	
	public synchronized void sumSizeOfImages(int i_imagesSize) {
		m_SizeOfImages += i_imagesSize;
	}
	
	public synchronized void addNumOfVideos() {
		m_NumOfVideos++;
	}
	
	public synchronized void sumSizeOfVideos(int i_videoSize) {
		m_SizeOfVideos += i_videoSize;
	}
	
	public synchronized void addNumOfInternalLinks() {
		m_NumInternalLinks++;
	}
	
	public synchronized void addNumOfExternalLinks() {
		m_NumExternalLinks++;
	}
	
	public synchronized void addPorts(ArrayList<Integer> ports) {
		if (ports != null) {
			this.ports.addAll(ports);
		}
	}

	
	
	//TODO: implement and maintain
	
	public int getNumOfConnectedDomains(){
		return m_ConnectedDomains.size();
	}
	public String getFileNamesOfConnectedDomains(){
		String tableCol = "";
		for (String reportFileNameName : m_ConnectedDomains) {
			tableCol += "<a href ='/" + reportFileNameName + "'>FILE HARTA BARTA</a></br>"; 
		}
		return tableCol;
	}
	public int getNumOfOpenPorts(){
		return ports.size();
	}


}
