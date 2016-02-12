import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class AnalyzerTask implements Runnable {

	//TODO: why linkedList?
	LinkedList<String> m_externalAnchors;
	LinkedList<String> m_internalAnchors;
	LinkedList<String> m_images;
	LinkedList<String> m_videos;
	LinkedList<String> m_docs;

	LinkedList<String> m_allowedImageExt;
	LinkedList<String> m_allowedVideoExt;
	LinkedList<String> m_allowedDocExt;

	HTTPQuery query;

	//DownloaderThreadPool m_DownloaderThreadPool;

	String m_htmlSourceCode;
	URI m_uri;
	String m_pageAddress;
	int m_sizeAndTypeOfPage;

	//LinkReport m_report;

	public AnalyzerTask(String i_htmlSourceCode, String i_pageAddress) throws URISyntaxException {
		//m_DownloaderThreadPool = i_threadPool;
		m_htmlSourceCode = i_htmlSourceCode.toLowerCase();
		m_htmlSourceCode.replaceAll("(?s)<!--(.*?)-->", "");
		m_pageAddress = i_pageAddress;
		m_sizeAndTypeOfPage = 0;
		query = new HTTPQuery();

		if(m_pageAddress.toLowerCase().indexOf("http://") != 0 && m_pageAddress.toLowerCase().indexOf("https://") != 0){
			m_pageAddress = "http://" + m_pageAddress;
		}

		m_uri = new URI(i_pageAddress);

		m_allowedImageExt = ConfigurationObject.getImageExtensions();
		m_allowedVideoExt = ConfigurationObject.getVideoExtensions();
		m_allowedDocExt = ConfigurationObject.getDocumentExtensions();

		m_externalAnchors = new LinkedList<>();
		m_internalAnchors = new LinkedList<>();
		m_images = new LinkedList<>();
		m_videos = new LinkedList<>();
		m_docs = new LinkedList<>();

		//m_report = createReport();
	}

	@Override
	public void run() {
		lookForAnchorsAndPopulate();
		lookForImagesAndPopulate();

		try {
			
			// add to Report
			addToDomainReport();
			for(int i = 0; i < m_internalAnchors.size(); i++){
				System.out.println(String.format("Sending to downloader: %S", m_internalAnchors.pop()));
				
				DownloaderTask downloader = new DownloaderTask(m_internalAnchors.get(i));
				CrawlerControler.getInstance().addTaskToDownloaderQueue(downloader);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// send all internal link to downloader queue
		//LinkedList<String> internalLinksToDownload = getInternalAnchors();
		
		

		//m_DownloaderThreadPool.addReportAndCheckIfFinished(m_report, m_report.m_pageAddress);
	}

	/*private LinkedList<String> getInternalAnchors() {
		return m_internalAnchors;
	}*/

	private void lookForImagesAndPopulate(){
		getAllPropertiesValueByTagAndPopulateLists("<img", "src=");
	}

	private void lookForAnchorsAndPopulate(){
		getAllPropertiesValueByTagAndPopulateLists("<a", "href=");
	}

	private void getAllPropertiesValueByTagAndPopulateLists(String subjectTag, String propertyToSearchFor){

		//String subjectTag = "<a";
		//String propertyToSearchFor = "href=";

		int currentIndex = m_htmlSourceCode.indexOf(subjectTag);

		// while there are still anchors from currentIndex to end of the string..
		while(currentIndex > -1){
			String link = null;
			char kindOfQuoteCharUsed;

			//indexes of the link itself aka -> <a href='www.someLink.com'
			int linkStartIndex, linkEndIndex;

			//inside an "<a" tag there is the "href=" property that holds the link address
			int hrefIndexInAnchor = m_htmlSourceCode.indexOf(propertyToSearchFor, currentIndex);

			linkStartIndex = (hrefIndexInAnchor + propertyToSearchFor.length());

			//can identify links with ' or " char, inorder to fecth it correctly 
			kindOfQuoteCharUsed = m_htmlSourceCode.charAt(linkStartIndex);

			//pointing to the closing quote char //TODO: check why +1
			linkEndIndex = m_htmlSourceCode.indexOf(kindOfQuoteCharUsed, linkStartIndex + 1);

			if(linkStartIndex > -1 && linkEndIndex > -1){
				link = m_htmlSourceCode.substring(linkStartIndex, linkEndIndex);
				populateCorrectList(this.removeQuoteCharFromString(link));
			}

			currentIndex = m_htmlSourceCode.indexOf(subjectTag, currentIndex + subjectTag.length());
		}

	}

	private int populateCorrectList(String linkToMap){
		String ext = getExtensionFromString(linkToMap);
		linkToMap = attachAbsoluteUrlToLink(linkToMap);
		int i = ext != null ? 0 : 3;//doesn't have an extension, mapping to anchors list stright away
		while(i < 4) {
			if(i == 0){
				if(listContainsElement(m_allowedImageExt, ext)){
					m_images.push(linkToMap);
					break;
				}
			}
			else if(i == 1) {
				if(listContainsElement(m_allowedVideoExt, ext)){
					m_videos.push(linkToMap);
					break;
				}
			}
			else if(i == 2){
				if(listContainsElement(m_allowedDocExt, ext)){
					m_docs.push(linkToMap);
					break;
				}
			}
			else {

				populateAnchors(linkToMap, ext);
				
				break;
			}
			i++;
		}
		return i;
	}

	
	/**
	 * 
	 * ASSUMING THIS METHOD WILL BE CALLED ONLY !!WITH!! attachAbsoluteUrlToLink() output as input
	 */
	private boolean isUrlInternal(String href){
		System.out.println("check if this -> " + href + " is internal");
		
		if((href.startsWith("http://") || href.startsWith("https://")) && href.indexOf(m_uri.getHost()) == -1){
			//external so false for internal
			System.out.println("href -> " + href + " was found as external :( -1");
			return false;
		} 
		else if((href.startsWith("http://") || href.startsWith("https://")) && href.indexOf(m_uri.getHost()) > 4){
			//internal with http , https with or without www
			System.out.println("href -> " + href + " was found as internal :) 0 ");
			return true;
		} 
		else if((! href.startsWith("http://") && !href.startsWith("https://")) && href.indexOf("www.") == -1) {
			//internal
			System.out.println("href -> " + href + " was found as internal :) 1");
			return true;
		}
		System.out.println("href -> " + href + " was found as external :( -2");
		return false;
	}
	
	private String attachAbsoluteUrlToLink(String href){
		if(href == null) {return null;}
		String absoluteURL = "";
		System.out.println("200 :: href -> " + href + " from pageAddress -> " + m_pageAddress);
		int indexOfSolamitInLink = href.indexOf("#");
		System.out.println("202 :: href passed --> " + href + " from pageAddress -> " + m_pageAddress);
		if(indexOfSolamitInLink == 0){
			//throw to garbage like this job fuk it
			return null;
		}
		else if(indexOfSolamitInLink > 0){
			href = href.substring(0, indexOfSolamitInLink);
		}
		
		if((href.startsWith("http://") || href.startsWith("https://")) && href.indexOf(m_uri.getHost()) == -1){
			//external
			absoluteURL = href;
		} 
		else if((href.startsWith("http://") || href.startsWith("https://")) && href.indexOf(m_uri.getHost()) > 4){
			//internal with http , https with or without www
			absoluteURL = href;
		} 
		else if((! href.startsWith("http://") && !href.startsWith("https://")) && href.indexOf("www.") == -1) {
			//internal
			if(!href.startsWith("/")){
				href = "/" + href;
			}
			absoluteURL = m_uri.getHost() + href;
		}
		return absoluteURL;
	}
	
	
	
	// internal "/path../../somePage.html" , "path../../somePage.html", http://thisDomain/path/somePage.html, www.thisDomain
	//
	// h / # w ?
	// remove suffix #suffix -> "/text/internallinks.html#section-names"
	// drop
	private String reformatAnchorLink(String link){// buildCorrectLink(String link){
		String temp = "\n\n--> input link = " + link;
		String stringToReturn = "";
		String host = m_uri.getHost();
		
		char firstChar = link.charAt(0);
		int linkLength = link.length();
		
		if(firstChar == '#' || (firstChar == '/' && linkLength == 1)){
			return null;
		}
		
		link = (link.startsWith("/")) ? link.substring(1) : link;
		link = (link.endsWith("/")) ? link.substring(0, linkLength) : link;
		
		int indexOfSolamitInLink = link.indexOf('#');
		if(indexOfSolamitInLink > 0){
			link = link.substring(0, indexOfSolamitInLink);
		}
		link = link.startsWith("http://") == false ? (link) : (link.substring(7));
		stringToReturn = "http://" + host + link;
		if(link.startsWith("www.")){
			 stringToReturn = "http://" + link;
		}
		temp += "\n\n--> output link = " + stringToReturn;
		System.out.println(temp);
		return stringToReturn;
		
		
	}
	
	
	// TODO: rejecting any line formatted without "http"/s "/" 
	// http://
	private String buildCorrectLink(String link){//reformatAnchorLink
		String repsDELETE = "---> LINE 167 :: reformatAnchorLink ( " + link + " ) \n";
		String linkLowered = link.toLowerCase();
		String verifiedLink;

		repsDELETE += "---> LINE 171 :: pre 1st IF --> link = " + link + " \n";
		if (link.charAt(link.length() - 1) == '#' || (link.charAt(link.length() - 1) == '/' && link.length() > 1)) {
			System.out.println(repsDELETE);
			System.out.println("---> LINE 173 :: inside 1st IF --> returns NULL on link = " + link + "<--");
			return null; 
		}
		repsDELETE += "---> LINE 177 :: post 1st IF no change to link --> enters 2nd IF on line 179";
		//System.out.println(repsDELETE);
		// check if the link is internal
		if(linkLowered.indexOf("/") == 0) {
			verifiedLink = "http://" + m_uri.getPath() + link.toLowerCase();
		} else {
			if(linkLowered.indexOf("http://") != 0 && linkLowered.indexOf("https://") != 0){
				//TODO: deals with sub-domains
				verifiedLink = (linkLowered.indexOf(m_uri.getHost()) == -1) ? "http://" + m_uri.getHost() + "/" +linkLowered : "http://" + linkLowered; 
			} else {
				verifiedLink = linkLowered;
			}
			
		}
		repsDELETE += "---> LINE 191 :: ABOUT TO EXIT reformatAnchorLink ( " + link + " )  output --> " + verifiedLink + "\n------\n------";
		System.out.println(repsDELETE);
		return verifiedLink;
	}


	/**
	 * @param link -> anchor to be added to the external or internal lists if doesn't already exists
	 * @return true on success
	 */
	private boolean populateAnchors(String link, String ext){

		String formattedLink = attachAbsoluteUrlToLink(link);//reformatAnchorLink(link);
		ext = (ext == null) ? "" : ext;
		boolean inserted = false;
		if (formattedLink != null) {
			
				//linkURI = new URI(formattedLink);
				boolean isInternal = isUrlInternal(formattedLink);
				//if(linkURI.getHost().equals(m_uri.getHost())){
				if(isInternal && (ext.equals("") || ext.equals("html"))){
					//m_internalAnchors.push(formattedLink);
					inserted = pushIfNotExists(m_internalAnchors, formattedLink);
				} else {
					inserted = pushIfNotExists(m_externalAnchors, formattedLink);
				}
						
		}

		return inserted;
	}

	/**
	 * 
	 * @param LinkedList<String> set -> list to push element to
	 * @param member -> string to push if not already in list
	 * @return true if member was added , false otherwise
	 */
	private boolean pushIfNotExists(LinkedList<String> set, String member){

		boolean exists = listContainsElement(set, member);
		if(!exists){
			set.push(member);
		}
		return !exists;
	}


	private String getExtensionFromString(String linkToMap) {
		String ext = null;
		int indexOfDotChar = linkToMap.lastIndexOf(".");
		if(indexOfDotChar > -1){
			ext = linkToMap.substring(indexOfDotChar + 1);
		}
		return ext;
	}

	private boolean listContainsElement(LinkedList<String> set, String member){
		int i = 0;
		while(i < set.size()){
			if(set.get(i).trim().equals(member)){
				return true;
			}
			i++;
		}
		return false;
	}

	private String removeQuoteCharFromString(String str){
		return str.substring(1, str.length());
	}

	// 
	private void addToDomainReport() throws IOException, Exception {

		fetchAllFromList(m_images, 0);
		fetchAllFromList(m_videos, 1);
		fetchAllFromList(m_docs, 2);
		fetchAllFromList(m_externalAnchors, 3);
		//fetchFromInternalLinks();

	}

//	//TODO: del
//	private void fetchFromInternalLinks(){
//		for(int i = 0; i < m_internalAnchors.size(); i++){
//			String address = m_internalAnchors.get(i);
//			//Link link = new Link(address, "" , "", "0");
//			//m_report.addInternalPageLink(link);
//		}
//	}

	/**
	 * @TODO pages in links, are going to be downloaded anyway and will have own reports
	 * @param list to pop link from
	 * @param listIdentifier - 0 image , 1 videos , 2 documents, 3 external pages
	 * @throws Exception 
	 * @throws IOException 
	 */
	private void fetchAllFromList(LinkedList<String> list, int listIdentifier) throws IOException, Exception {
		for(int i = 0; i < list.size(); i++){
			String address = list.get(i);
			tryInsertToDB(address, listIdentifier);
		} 
	}

	private void tryInsertToDB(String url, int identifier) {
		
		if (!CrawlerDB.getInstance().linkExist(url)) {
			CrawlerDB.getInstance().addDownloadLink(url);
			String response = "";
			
			// TODO: check what's happens when response with exception
			try {
				response = query.sendHttpHeadRequest(url);
				
				String len = "";
				try {
					len = query.parseContentLengthFromHttpResponse(response).split("#_#@#_#")[1];
				} catch (ArrayIndexOutOfBoundsException error) {
					response = query.sendHttpHeadRequest(url);
					len = query.parseContentLengthFromHttpResponse(response).split("#_#@#_#")[1];
				}
				
				// image
				if (identifier == 0) {
					CrawlerControler.getInstance().addNumOfImages();
					CrawlerControler.getInstance().sumSizeOfImages(Integer.parseInt(len));
				} 
				//video 
				else if (identifier == 1) {
					CrawlerControler.getInstance().addNumOfVideos();
					CrawlerControler.getInstance().sumSizeOfVideos(Integer.parseInt(len));
				}
				//doc
				else if (identifier == 2) {
					CrawlerControler.getInstance().addNumOfDocs();
					CrawlerControler.getInstance().sumSizeOfDocs(Integer.parseInt(len));
				}
				// external link
				else if (identifier == 3) {
					CrawlerControler.getInstance().addNumOfExternalLinks();
				}
				
			} catch (UnknownHostException e) {
				System.out.println("failed send heads request -> link " + url);
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("failed send heads request -> link " + url);
			}
			
			
		}
	}

/*
	private LinkReport createReport(){
		String size = m_sizeAndTypeOfPage.split("#_#@#_#")[1];
		int sizeInBytes = Integer.parseInt(size);
		LinkReport report = new LinkReport(m_pageAddress, sizeInBytes);
		return report;
	}
 */


}
