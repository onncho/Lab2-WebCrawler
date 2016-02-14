import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import org.omg.Messaging.SyncScopeHelper;






public class HandleRequest implements Runnable {

	private final Socket m_Connection;
	private HTTPResponse m_Response;
	BufferedReader m_ClientInput;
	String m_line, m_FullRequest;
	String m_messageBodyString;
	int m_contentLength;
	char[] m_MsgBodyCharBuffer;
	StringBuilder m_MessageBodyBuilder;

	CrawlerClientUtil crawlerUtil = new CrawlerClientUtil();

	private final String ContentLengthHeader = "Content-Length: ";

	public HandleRequest(Socket i_connection)
	{
		this.m_Connection = i_connection;
		m_line = "";
		m_FullRequest = "";
		m_messageBodyString = null;
		m_contentLength = -1;
		m_MessageBodyBuilder = null;
		m_Response = null;
	}

	@Override
	public void run() {			

		try {
			if (m_Connection.isClosed()) {
				return;
			} 
			m_ClientInput = new BufferedReader(new InputStreamReader(m_Connection.getInputStream()));
			m_line = m_ClientInput.readLine();				

			// Read Request According to Http Protocol
			while(m_line != null && !m_line.equals(""))
			{
				// Check For Request With A Body Message
				if(m_line.indexOf(ContentLengthHeader) > -1){
					String bodyContentLengthAsString = m_line.substring(ContentLengthHeader.length());
					m_contentLength = Integer.parseInt(bodyContentLengthAsString);
				}
				m_FullRequest += (m_line + "\n");
				m_line = m_ClientInput.readLine();
			}

			// Handle With Request that Contain Body Message
			if(m_contentLength > 0){
				m_MsgBodyCharBuffer = new char[m_contentLength];
				m_ClientInput.read(m_MsgBodyCharBuffer);
				m_MessageBodyBuilder = new StringBuilder();

				for(int i = 0; i < m_MsgBodyCharBuffer.length; i++)
				{
					m_MessageBodyBuilder.append(m_MsgBodyCharBuffer[i]);
				}
				m_messageBodyString = m_MessageBodyBuilder.toString();
			}

			//TRACE: Request Headers
			System.out.println(m_FullRequest);
			HTTPResponse http_response = this.handleRequest(m_FullRequest, m_messageBodyString, m_contentLength);

			if (m_Connection.isConnected()) {
				handleResponse(http_response, m_Connection);					
			}
		} catch (IOException e) {
			System.err.println("ERROR: IO Exception");
		} 
	}

	//TODO: simply write data + response code according to the request data --> no logic
	public void handleResponse(HTTPResponse res, Socket connection){
		// LAB 2 --> will enter with GET /executeResult.html
		String response = res.GenerateResponse();
		DataOutputStream writer;

		try {
			if (connection.getOutputStream() != null ) {
				writer = new DataOutputStream(connection.getOutputStream());
				System.out.println(response);

				if(!connection.isClosed()){
					writer.writeBytes(response);
					writer.flush();
				}

				// Send The File and Close Response As Http protocol request
				// TODO: enter here also when ExecResult request and alsp HTMLTEMPLATE is none
				if(res.getPathToFile() != null && res.fileIsExpected()){
					File file= new File(res.getPathToFile());
					
					System.out.println(file.getName());//TODO: delete 
					//serving without chunked transfer
					if(!res.v_isChunked){
						byte[] fileToSend;
						System.out.println("crawler state : " + CrawlerControler.getInstance().getState().toString());
						if(file.getName().equals("params_info.html")){
							fileToSend = res.templatedHTML;
						}
						else if((file.getName().equals("index.html")|| file.getName().equals("/")) && CrawlerControler.getInstance().getState().equals(CrawlerControler.State.RUNNING)){
							fileToSend = res.templatedHTML;
						}
						
						else if ((file.getName().equals("/") || file.getName().equals("index.html") || file.getName().equals("/index.html")) &&
								!CrawlerControler.getInstance().CrawlerIsWorking()){
							String indexHTML = CrawlerClientUtil.getIndexHtmlAndAddRecentReportsToPage();
							if(indexHTML == null){
								fileToSend = Utils.readFile(file);
							} else {								
								fileToSend = res.templatedHTML;
							}
							
						}
						
						else if (file.getName().equals("execResult.html") && !CrawlerControler.getInstance().CrawlerIsWorking()) {
							System.out.println("generating ExecResult Page");
							fileToSend = res.templatedHTML;
						}
						else {
							fileToSend = Utils.readFile(file);
						}

						if(!connection.isClosed()){
							writer.write(fileToSend, 0, fileToSend.length);
							writer.flush();
						}

						//serving as chunks
					} else {
						if(file.getName().equals("params_info.html")){
							writeChunkString(res.templatedHTML, writer);
						} else {
							writeChunkData(new File(res.getPathToFile()),writer);
						}
					}
				}
				writer.close();
			}
		} catch (IOException e) {
			System.err.println("Network Problem: Socket was Closed");
		}
	}

	// GET execute --> CrawlerisRunning.... executeResult.html --> 
	// Refresh --> Get --> Running - NO --> executeNEW
	// create http request and response
	// TODO: can't debug only works when not stopping
	public HTTPResponse handleRequest(String i_fullRequest, String msgBody, int contentLength){
		HTTPRequest req = new HTTPRequest(i_fullRequest, msgBody, contentLength);
		HTTPResponse res;
		boolean goodCrawler = false;
		
		if(checkForCrawler(req.getMap(), req.m_HttpRequestParams)){
			 goodCrawler = crawlerFlow(req.m_HttpRequestParams, contentLength);
		}
		
		res = new HTTPResponse(req.m_requestHeaders, req.m_HttpRequestParams);
	
		return res;	
	}
	
	//receive parameters for crawler
	private boolean checkForCrawler(HashMap<String,String> reqHeaders, HashMap<String, String> reqParams) {
		//System.out.println("175 -->" + reqParams.get("URI"));
		if(reqHeaders == null){return false;}

		boolean clientAsksForCrawler = reqHeaders.get("URI").equals("/execResult.html");
		if(clientAsksForCrawler == false) {return false;}//returning false since in this case we will go for normal response
		
		if(!reqParams.containsKey("domainToCrawl")){
			System.out.println("Bad input!");
			return false;
		}
		return true;
	}

	private boolean crawlerFlow(HashMap<String,String> reqParams, int contentLengthOfOriginalRequest){

		boolean checkForPortsParamExists = reqParams.containsKey("PortScanChecked");
		boolean respectRobotsTxtExists = reqParams.containsKey("RobotsChecked");

		String domainToCrawl = reqParams.get("domainToCrawl");
		boolean checkForPorts = checkForPortsParamExists ? reqParams.get("PortScanChecked").equals("Checked") : false;
		boolean respectRobotsTxt = respectRobotsTxtExists ? reqParams.get("RobotsChecked").equals("Checked") : false;	
		boolean crawlFinishedSuceessfully = doCrawl(domainToCrawl, checkForPorts, respectRobotsTxt, reqParams, contentLengthOfOriginalRequest); 
		
		return crawlFinishedSuceessfully;
	}

	private boolean doCrawl(String domainToCrawl, boolean checkForPorts, boolean respectRobotsTxt, HashMap<String,String> reqParams, int contentLengthOfOriginalRequest) {
		// TODO init crawler
		boolean res = false;
		if(!domainToCrawl.startsWith("http://")){
			int www = domainToCrawl.indexOf("www.");
			domainToCrawl = "http://" + domainToCrawl.substring(www);
		}
		
		CrawlerControler.getInstance().startCrawling(domainToCrawl, checkForPorts, respectRobotsTxt);
		
		// waiting for crawling to finish
		while(CrawlerControler.getInstance().CrawlerIsWorking()){
			try {
				Thread.currentThread().sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("sleep got interrupted");
				e.printStackTrace();
			}
		}
		
		try {
			String[] newReport = CrawlerControler.getInstance().saveReport();
			CrawlerDB.getInstance().addReportAndPath(newReport); 
			res = true;
		} catch (Exception e) {
			System.out.println("exception in saving reprot yo");
		}
		
		return res;
	}

	private void writeChunkData(File file, DataOutputStream writer){

		try
		{
			FileInputStream fis = new FileInputStream(file);
			byte[] bFile = new byte[1024];
			int chunkSize = 0;
			// read until the end of the stream.
			while((chunkSize = fis.read(bFile)) != -1)
			{
				writer.writeBytes(Integer.toHexString(chunkSize));
				writer.writeBytes("\r\n");
				writer.flush();
				writer.write(bFile, 0, chunkSize);
				writer.writeBytes("\r\n");
				writer.flush();
			}

			fis.close();
			writer.writeBytes(Integer.toHexString(0));
			writer.writeBytes("\r\n");
			writer.flush();
			writer.writeBytes("\r\n");
			writer.flush();

		}

		catch(FileNotFoundException e)
		{
			System.err.println("FileNotFound While Writing Cuncked Data");
		} 
		catch (IOException e) 
		{
			System.err.println("ERROR: IO Exception");
		}
	}


	private void writeChunkString(byte[] string, DataOutputStream writer){

		try
		{
			ByteArrayInputStream fis = new ByteArrayInputStream(string);
			byte[] bFile = new byte[1024];
			int chunkSize = 0;

			// read until the end of the stream.
			while((chunkSize = fis.read(bFile)) != -1)
			{
				writer.writeBytes(Integer.toHexString(chunkSize));
				writer.writeBytes("\r\n");
				writer.flush();
				writer.write(bFile, 0, chunkSize);
				writer.writeBytes("\r\n");
				writer.flush();
			}

			fis.close();
			writer.writeBytes(Integer.toHexString(0));
			writer.writeBytes("\r\n");
			writer.flush();
			writer.writeBytes("\r\n");
			writer.flush();
		}

		catch(FileNotFoundException e)
		{
			System.err.println("ERROR: File Not Found");
		} catch (IOException e) {
			System.err.println("ERROR: IO Exception");
		}
	}
}
