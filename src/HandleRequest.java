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

	public void handleResponse(HTTPResponse res, Socket connection){
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
				if(res.getPathToFile() != null && res.fileIsExpected()){
					File file= new File(res.getPathToFile());

					//serving without chunked transfer
					if(!res.v_isChunked){
						byte[] fileToSend;

						if(file.getName().equals("params_info.html")){
							fileToSend = res.templatedHTML;
						} else {
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

	// create http request and response
	public HTTPResponse handleRequest(String i_fullRequest, String msgBody, int contentLength){
		HTTPResponse res;
		HTTPRequest req = new HTTPRequest(i_fullRequest, msgBody, contentLength);
		
		if(!checkForCrawler(req)){
			
			if(req.m_RequestedPage.equals("/")){
				if(CrawlerControler.getInstance().getState().equals(CrawlerControler.State.RUNNING)){
					//working
					int indexOfRoot = i_fullRequest.indexOf("/");
					String newRequest = "";
					if(indexOfRoot > -1){
						newRequest = i_fullRequest.substring(0,indexOfRoot) + "/Running.html" + i_fullRequest.substring(indexOfRoot + 1);
						req = new HTTPRequest(newRequest, msgBody, contentLength);
						res = new HTTPResponse(req.m_requestHeaders, req.m_HttpRequestParams);
						return res;
					}
					
				}
				/*else if(CrawlerControler.getInstance().getState().equals(CrawlerControler.State.WAITING)){
					//waiting
				}*/
		
			}
			res = new HTTPResponse(req.m_requestHeaders, req.m_HttpRequestParams);
			return res;
			
		} else {
			// starting crawling and there for need to hold the entire flow until when the crawler will finish
			 
			 res = crawlerFlow(req); 
					// new HTTPResponse(req.m_requestHeaders, req.m_HttpRequestParams);
		}
		return res;
	}


	private boolean checkForCrawler(HTTPRequest req) {
		boolean clientAsksForCrawler = req.m_RequestedPage.equals("/execResult.html");
		if(req.m_HttpRequestParams == null || clientAsksForCrawler == false) {return false;}//returning false since in this case we will go for normal response
		if(!req.m_HttpRequestParams.containsKey("domainToCrawl")){
			return false;
		}
		return true;
	}

	private HTTPResponse crawlerFlow(HTTPRequest req){
		HashMap<String,String> copyOfParams = req.m_HttpRequestParams;
		boolean checkForPortsParamExists = copyOfParams.containsKey("PortScanChecked");
		boolean respectRobotsTxtExists = copyOfParams.containsKey("RobotsChecked");


		String domainToCrawl = copyOfParams.get("domainToCrawl");
		boolean checkForPorts = checkForPortsParamExists ? copyOfParams.get("PortScanChecked").equals("Checked") : false;
		boolean respectRobotsTxt = respectRobotsTxtExists ? copyOfParams.get("RobotsChecked").equals("Checked") : false;
		if (checkForPorts) {
			doPortScan();
		}
		return doCrawl(domainToCrawl, checkForPorts, respectRobotsTxt, req);

	}
	
	private void doPortScan() {
		CrawlerControler.getInstance().startPortScanner();
	}

	private HTTPResponse doCrawl(String domainToCrawl, boolean checkForPorts, boolean respectRobotsTxt, HTTPRequest req) {
		// TODO init crawler
	HTTPResponse res = new HTTPResponse(req.m_requestHeaders, req.m_HttpRequestParams);
		CrawlerControler.getInstance().startCrawling(domainToCrawl, checkForPorts, respectRobotsTxt);
		while(!CrawlerControler.getInstance().getState().equals(CrawlerControler.State.STOPPING)){
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
