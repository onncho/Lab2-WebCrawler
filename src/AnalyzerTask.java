import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalyzerTask implements Runnable {

	LinkedList<String> m_externalAnchors;
	LinkedList<String> m_internalAnchors;
	LinkedList<String> m_images;
	LinkedList<String> m_videos;
	LinkedList<String> m_docs;
	LinkedList<String> m_allowedImageExt;
	LinkedList<String> m_allowedVideoExt;
	LinkedList<String> m_allowedDocExt;

	HTTPQuery query;
	String m_htmlSourceCode;
	URI m_uri;
	String m_pageAddress;
	int m_sizeAndTypeOfPage;

	public AnalyzerTask(String i_htmlSourceCode, String i_pageAddress) throws URISyntaxException {
		System.out.println("Analyzer constructed with " + i_pageAddress);
		if(i_pageAddress.equals("http://www.freebsd.org/./donations/donors.html")){
			System.out.println("lkl");
		}
		
		m_htmlSourceCode = i_htmlSourceCode.toLowerCase();
		m_htmlSourceCode = m_htmlSourceCode.replaceAll("(?s)<!--(.*?)-->", " 	");
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
	}

	@Override
	public void run() {
		findAllLinks();
		findAllImages();

		try {
			// add to Report
			addToDomainReport();
			System.out.println("Number of link Analyzer extracted from a given URL (and the URL itself):\t" + (m_internalAnchors.size() + m_externalAnchors.size() + 1));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void lookForImagesAndPopulate(){
//		getAllPropertiesValueByTagAndPopulateLists("<img", "src=");
//	}
	
	public void findAllLinks() {
		Pattern p = Pattern.compile("(href)\\s*=\\s*[\\\"\\']((http[s]?:\\/\\/\\S+)|([^\\s\\?\\'\\\"\\#\\@\\:]*))(\\#\\S*)?[\\\"\\']");
		Matcher m = p.matcher(m_htmlSourceCode);
		String match = "";
		while (m.find()) {
			match = m.group(2);
			match = adjustDomainUrl(m_pageAddress, match);
			while (match.contains("/../")) {
				match = match.replaceAll("(/\\w+/\\.\\./)", "/");
		    }
			if (!CrawlerControler.getInstance().isLinkCheck(match)) {
				String linkDomain = Utils.GetDomain(match);
				linkDomain = linkDomain.isEmpty() ? m_uri.getHost() : linkDomain;
				if (m_uri.getHost().equals(linkDomain)) {
					handleInternalLink(match);
				} else {
					// TODO - Handle external links statistics
					addIfNotExist(match, m_externalAnchors);
					CrawlerDB.getInstance().addConnectedDomain(CrawlerControler.getInstance().getDomain(), Utils.GetDomain(match));
					CrawlerControler.getInstance().addConnectedDomain(Utils.GetDomain(match));
				}
			}
		} 
	}
	
	private boolean addIfNotExist(String link, LinkedList<String> listToAddLink) {
		boolean wasExisted = true;
		
		if (!listToAddLink.contains(link)) {
			wasExisted = false;
			listToAddLink.add(link);
		}
		
		return wasExisted;
	}
	
	private void findAllImages() {
		Pattern p = Pattern.compile("<img[^>]+src ?= ?(?:\\\"|\\')(.[^\">]+?)(?=\\\"|\\')");
		Matcher m = p.matcher(m_htmlSourceCode);
		String imgMatch = "";
		while (m.find()) {
			imgMatch = m.group(1);
			if (imgMatch != null && !imgMatch.isEmpty()) {
				if (imgMatch.charAt(0) != '/' && imgMatch.charAt(0) != 'h') {
					imgMatch = "/" + imgMatch;
				}
				if(!CrawlerControler.getInstance().isLinkCheck(imgMatch)){
					if (!addIfNotExist(imgMatch, m_images)) {
						System.out.println("Image to fetch:  " + imgMatch);
					}
				}
			}
		}
	}
	
	 private void handleInternalLink(String link) {
			int dotIndex = link.lastIndexOf(".");
			if (dotIndex != -1) {
				String ext = link.substring(dotIndex + 1);
				if (ConfigurationObject.isImageExtension(ext)) {
					addIfNotExist(link, m_images);
					return;
				}
				else if (ConfigurationObject.isVideoExtension(ext)) {
					addIfNotExist(link, m_videos);
					return;
				}
				else if (ConfigurationObject.isDocumentExtension(ext)) {
					addIfNotExist(link, m_docs);
					return;
				} 
			}
	
			System.out.println("The URL has been added as an internal link -----:: " + link);
			addIfNotExist(link, m_internalAnchors);
			CrawlerControler.getInstance().addTaskToDownloaderQueue(new DownloaderTask(link));
	}

	private String adjustDomainUrl(String baseUrl, String link) {
	        
			if (link.startsWith("http")) {
	            return link;
	        }

	        boolean isRelative = !link.startsWith("/");
	        if (isRelative) {
	            int i = baseUrl.substring(8).lastIndexOf('/');
	            if (i == -1) {
	                baseUrl = baseUrl + "/" + link;
	            } else {
	                baseUrl = baseUrl.substring(0, i + 9) + link;
	            }
	        } else {
	            int indexOfSlash = baseUrl.indexOf('/', 8);
	            if (indexOfSlash == -1) {
	                baseUrl = baseUrl + link;
	            } else {
	                baseUrl = baseUrl.substring(0, indexOfSlash + 1) + link;
	            }
	        }

	        return baseUrl;
	    }

//	private void lookForAnchorsAndPopulate(){
//		getAllPropertiesValueByTagAndPopulateLists("<a", "href=");
//	}
//
//	//String subjectTag = "<a";
//	//String propertyToSearchFor = "href=";
//	private void getAllPropertiesValueByTagAndPopulateLists(String subjectTag, String propertyToSearchFor){
//
//		int currentIndex = m_htmlSourceCode.indexOf(subjectTag);
//
//		// while there are still anchors from currentIndex to end of the string..
//		while(currentIndex > -1){
//			String link = null;
//			char kindOfQuoteCharUsed;
//
//			//indexes of the link itself aka -> <a href='www.someLink.com'
//			int linkStartIndex, linkEndIndex;
//
//			//inside an "<a" tag there is the "href=" property that holds the link address
//			int hrefIndexInAnchor = m_htmlSourceCode.indexOf(propertyToSearchFor, currentIndex);
//
//			linkStartIndex = (hrefIndexInAnchor + propertyToSearchFor.length());
//
//			//can identify links with ' or " char, inorder to fecth it correctly 
//			kindOfQuoteCharUsed = m_htmlSourceCode.charAt(linkStartIndex);
//
//			//pointing to the closing quote char //TODO: check why +1
//			linkEndIndex = m_htmlSourceCode.indexOf(kindOfQuoteCharUsed, linkStartIndex + 1);
//
//			if(linkStartIndex > -1 && linkEndIndex > -1) {
//				link = m_htmlSourceCode.substring(linkStartIndex, linkEndIndex);
//				populateCorrectList(this.removeQuoteCharFromString(link));
//			}
//
//			currentIndex = m_htmlSourceCode.indexOf(subjectTag, currentIndex + subjectTag.length());
//		}
//	}

//	private int populateCorrectList(String linkToMap){
//		String ext = getExtensionFromString(linkToMap);
//		linkToMap = attachAbsoluteUrlToLink(linkToMap);
//		String domain = Utils.GetDomain(linkToMap);
//		domain = domain.isEmpty() ? m_uri.getHost() : domain;
//		System.out.println("The domain of: **" + linkToMap + "** is: " + domain);
//		int i = ext != null ? 0 : 3; //doesn't have an extension, mapping to anchors list stright away
//		while(i < 4) {
//			if(i == 0){
//				if(listContainsElement(m_allowedImageExt, ext)){
//					m_images.push(linkToMap);
//					break;
//				}
//			}
//			else if(i == 1) {
//				if(listContainsElement(m_allowedVideoExt, ext)){
//					m_videos.push(linkToMap);
//					break;
//				}
//			}
//			else if(i == 2){
//				if(listContainsElement(m_allowedDocExt, ext)){
//					m_docs.push(linkToMap);
//					break;
//				}
//			}
//			else {
//				populateAnchors(linkToMap, ext);
//				break;
//			}
//			i++;
//		}
//		
//		return i;
//	}

//
//	/**
//	 * The Link is internal or External
//	 * ASSUMING THIS METHOD WILL BE CALLED ONLY !!WITH!! attachAbsoluteUrlToLink() output as input
//	 */
//	private boolean isUrlInternal(String href){
//		
//		if((href.startsWith("http://") || href.startsWith("https://")) && href.indexOf(m_uri.getHost()) == -1){
//			//external so false for internal
//			System.out.println("href -> " + href + " was found as external :( -1");
//			return false;
//		} 
//		else if((href.startsWith("http://") || href.startsWith("https://")) && href.indexOf(m_uri.getHost()) > 4){
//			//internal with http , https with or without www
//			System.out.println("href -> " + href + " was found as internal :) 0 ");
//			return true;
//		} 
//		else if((! href.startsWith("http://") && !href.startsWith("https://")) && href.indexOf("www.") == -1) {
//			//internal
//			System.out.println("href -> " + href + " was found as internal :) 1");
//			return true;
//		}
//		System.out.println("href -> " + href + " was found as internal :( -2");
//		return true;
//	}
	
//	// fix links for future use
//	private String attachAbsoluteUrlToLink(String href){
//		if(href == null) {return null;}
//		String absoluteURL = href;
//		System.out.println("200 :: href -> " + href + " from pageAddress -> " + m_pageAddress);
//		int indexOfSolamitInLink = href.indexOf("#");
//		System.out.println("202 :: href passed --> " + href + " from pageAddress -> " + m_pageAddress);
//		if(indexOfSolamitInLink == 0){
//			//throw to garbage like this job fuk it
//			return null;
//		}
//		else if(indexOfSolamitInLink > 0){
//			href = href.substring(0, indexOfSolamitInLink);
//		}
//
//		if((href.startsWith("http://") || href.startsWith("https://")) && href.indexOf(m_uri.getHost()) == -1){
//			//external
//			absoluteURL = href;
//		} 
//		else if((href.startsWith("http://") || href.startsWith("https://")) && href.indexOf(m_uri.getHost()) > 4){
//			//internal with http , https with or without www
//			absoluteURL = href;
//		} 
//		else if((! href.startsWith("http://") && !href.startsWith("https://")) && href.indexOf("www.") == -1) {
//			//internal
//			if(!href.startsWith("/")){
//				href = "/" + href;
//			}
//			absoluteURL = m_uri.getHost() + href;
//		}
//		return absoluteURL;
//	}
//
//	/**
//	 * @param link -> anchor to be added to the external or internal lists if doesn't already exists
//	 * @return true on success
//	 */
//	private boolean populateAnchors(String link, String ext){
//		//String formattedLink = attachAbsoluteUrlToLink(link); // fix link if needed
//		String formattedLink = link;
//		ext = (ext == null) ? "" : ext;
//		boolean inserted = false;
//		
//		if (formattedLink != null) {
//			//TODO
//			//linkURI = new URI(formattedLink);
//			boolean isInternal = isUrlInternal(formattedLink);
//			//if(linkURI.getHost().equals(m_uri.getHost())){
//			if(isInternal && (ext.equals("") || ext.equals("html"))){
//				//m_internalAnchors.push(formattedLink);
//				inserted = pushIfNotExists(m_internalAnchors, formattedLink);
//			} else {
//				inserted = pushIfNotExists(m_externalAnchors, formattedLink);
//			}		
//		}
//
//		return inserted;
//	}

//	/**
//	 * 
//	 * @param LinkedList<String> set -> list to push element to
//	 * @param member -> string to push if not already in list
//	 * @return true if member was added , false otherwise
//	 */
//	private boolean pushIfNotExists(LinkedList<String> set, String member){
//		boolean exists = listContainsElement(set, member);
//		
//		if(!exists){
//			set.push(member);
//		}
//		
//		return !exists;
//	}
//
//	private String getExtensionFromString(String linkToMap) {
//		String ext = null;
//		int indexOfDotChar = linkToMap.lastIndexOf(".");
//		if(indexOfDotChar > -1){
//			ext = linkToMap.substring(indexOfDotChar + 1);
//		}
//		return ext;
//	}
//
//	private boolean listContainsElement(LinkedList<String> set, String member){
//		int i = 0;
//		while(i < set.size()){
//			if(set.get(i).trim().equals(member)){
//				return true;
//			}
//			i++;
//		}
//		return false;
//	}
//
//	private String removeQuoteCharFromString(String str){
//		return str.substring(1, str.length());
//	}

	private void addToDomainReport() throws IOException, Exception {
		fetchAllFromList(m_images, 0);
		fetchAllFromList(m_videos, 1);
		fetchAllFromList(m_docs, 2);
		fetchAllFromList(m_externalAnchors, 3);
	}

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
			if (!address.isEmpty()) {
				tryInsertToDB(address, listIdentifier);
			}
		} 
	}

	private void tryInsertToDB(String url, int identifier) {

		if (!CrawlerDB.getInstance().linkExist(url)) {
			CrawlerDB.getInstance().addDownloadLink(url);
			String response = "";

			try {
				String len = "";
				if(identifier != 3){
					response = query.sendHttpHeadRequestV2(url);

					try {
						if(response == null){
							System.out.println(" response for the following url -> " + url + "was null...");
							response = query.sendHttpGETRequestAndInterceptBeforeBody(url);
							if(response == null){
								System.out.println("response after GETintercept still null... = " + url);
							} else {
								System.out.println("response was recieved fine with getIntercept -->" + response + "\n----");
							}
						}
						len = query.getContentLengthFromResponse(response);
					} catch (ArrayIndexOutOfBoundsException error) {
						response = query.sendHttpGETRequestAndInterceptBeforeBody(url);
						len = query.getContentLengthFromResponse(response);
						len = len == null || len.isEmpty() ? "0" : len;
					}
				}
				// TODO: Some HEAD request don't provide Content-Length to their response
				// Solution A: send GET request only for the Response String, B: ignore this.
				// image
				if (identifier == 0) {
					CrawlerControler.getInstance().addNumOfImages();
					CrawlerControler.getInstance().sumSizeOfImages(Integer.parseInt(len));
				} 
				// video 
				else if (identifier == 1) {
					CrawlerControler.getInstance().addNumOfVideos();
					CrawlerControler.getInstance().sumSizeOfVideos(Integer.parseInt(len));
				}
				// doc
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
			catch (NumberFormatException e) {
				System.out.println("*********** NumberFormatException ************* --> " + url);
			}
		}
	}
}
