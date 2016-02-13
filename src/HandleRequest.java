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
							System.out.println("running should be returned");
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
		if(checkForCrawler(req.getMap(), req.m_HttpRequestParams)){
			res = crawlerFlow(req.m_HttpRequestParams, contentLength);
		} else {res = new HTTPResponse(req.m_requestHeaders, req.m_HttpRequestParams);}
		
		return res;
		
	}

	private boolean checkForCrawler(HashMap<String,String> reqHeaders, HashMap<String, String> reqParams) {
		//System.out.println("175 -->" + reqParams.get("URI"));
		if(reqHeaders == null){return false;}

		boolean clientAsksForCrawler = reqHeaders.get("URI").equals("/execResult.html");
		if(clientAsksForCrawler == false) {return false;}//returning false since in this case we will go for normal response
		if(!reqParams.containsKey("domainToCrawl")){
			return false;
		}
		return true;
	}

	private HTTPResponse crawlerFlow(HashMap<String,String> reqParams, int contentLengthOfOriginalRequest){

		boolean checkForPortsParamExists = reqParams.containsKey("PortScanChecked");
		boolean respectRobotsTxtExists = reqParams.containsKey("RobotsChecked");


		String domainToCrawl = reqParams.get("domainToCrawl");
		boolean checkForPorts = checkForPortsParamExists ? reqParams.get("PortScanChecked").equals("Checked") : false;
		boolean respectRobotsTxt = respectRobotsTxtExists ? reqParams.get("RobotsChecked").equals("Checked") : false;
		if (checkForPorts) {
			doPortScan();
		}
		return doCrawl(domainToCrawl, checkForPorts, respectRobotsTxt, reqParams, contentLengthOfOriginalRequest);

	}

	private void doPortScan() {
		CrawlerControler.getInstance().startPortScanner();
	}

	private HTTPResponse doCrawl(String domainToCrawl, boolean checkForPorts, boolean respectRobotsTxt, HashMap<String,String> reqParams, int contentLengthOfOriginalRequest) {
		// TODO init crawler
		if(!domainToCrawl.startsWith("http://")){
			int www = domainToCrawl.indexOf("www.");
			domainToCrawl = "http://" + domainToCrawl.substring(www);
		}
		CrawlerControler.getInstance().startCrawling(domainToCrawl, checkForPorts, respectRobotsTxt);
		while(CrawlerControler.getInstance().getState().equals(CrawlerControler.State.RUNNING)){
			try {
				Thread.currentThread().sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// POST /execResult.html HTTP/1.1
		String newPath = CrawlerControler.getInstance().saveReport();

		String newRequestString = (reqParams.get("originalRequest")).replace("/execResult.html", newPath);
		System.out.println("new request \n---\n" + newRequestString + "\n---\n");
		HTTPRequest newReq = new HTTPRequest(newRequestString, reqParams.get("mzgBody"), contentLengthOfOriginalRequest);

		return new HTTPResponse(newReq.getMap(), newReq.m_HttpRequestParams);
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
