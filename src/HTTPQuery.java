import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class HTTPQuery {

	String _CRLF = "\r\n";

	// read chunks data from requests
	private String readChunksFromBufferedReader(BufferedReader reader){
		String chunkSizeAsString;
		String messageBody = "";
		try {
			chunkSizeAsString = reader.readLine();
			int currentChunk = Integer.parseInt(chunkSizeAsString, 16);

			while(currentChunk != 0){
				long index = 0;
				while(index < currentChunk){
					messageBody += (char) reader.read();
					index += 1;
				}
				reader.readLine(); //CRLF char so skipping
				currentChunk = Integer.parseInt(reader.readLine(), 16);
			}
			return messageBody;
		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}

	// send any request to specific target
	public String[] sendHttpRequestV2(String target, String requestType) throws IOException, UnknownHostException{
		String res[] = new String[2];

		try {
			target = target.replaceAll("\\./", "/");
			String host = Utils.GetDomain(target); 
			host = host.isEmpty() ? CrawlerControler.getInstance().getDomain() : host;
			String path = target.contains(host) ? target.substring(target.indexOf(host) + host.length()) : target;
			path = path.equals("") ? "/" : path;

			if (host.startsWith("http://")) {
				host = host.substring(host.indexOf(("http://")) + 7);
			}
			if(host.indexOf(("%2F")) > -1){
				host = host.substring(0 , host.indexOf("%2F"));
			}
			if(host.startsWith("http://")){
				host = "http://" + host.substring("http://".length()).replaceAll("//", "/");
			} else {
				host = host.replaceAll("//", "/");
			}
			String requestLine = requestType + " " + path + " " + "HTTP/1.0";
			String headers = "Host: " + host;

			Socket socket = new Socket(host, 80);
			socket.setSoTimeout(6000);
			PrintWriter writer = new PrintWriter(socket.getOutputStream());

			writer.write(requestLine);
			writer.write(_CRLF);
			writer.write(headers);
			writer.write(_CRLF);

			writer.write(_CRLF);
			writer.flush();

			res = readHttpResponseFromStream(socket);
			writer.close();
			socket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new UnknownHostException();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}

	public String[] readHttpResponseFromStream(Socket socket) throws IOException{
		String[] res = new String[2];
		StringBuilder headers2 = new StringBuilder("");
		StringBuilder body = new StringBuilder("");
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line;
		while (((line = reader.readLine()) != null) && (!line.isEmpty())) {
			headers2.append(line + "\n");
		}				

		int i = 0;
		while ((i = reader.read()) != -1) {
			body.append((char)i);
		}

		res[0] = headers2.toString();
		res[1] = body.toString();

		reader.close();
		
		return res;
	}

	/**
	 * @Desc 
	 * @param String response -> Response-String that has returned from a GET/HEAD request
	 * @return String -> Content-Length from the message body (octets and represented in decimal) || null if failed. 
	 */
	public String getContentLengthFromResponse(String response){
		String lengthValue = null;
		String _contentLength = "Content-Length: ";

		try{
			String[] responseLines = response.split("\n");
			for(int i = 0; i < responseLines.length; i++){
				String line = responseLines[i];
				if(line.indexOf(_contentLength) > -1 && line.indexOf(" ") > -1){
					lengthValue = (line.split(" "))[1];
				}
			}
		} catch (NullPointerException e){
			System.out.println("---\nexception trace back inside HTTPQuery.getContentLengthFromResponse("+ response +")");
			System.out.println(e);
			System.out.println("---");
		}

		return lengthValue;
	}

	public String parseContentLengthFromHttpResponse(String response){
		String[] responseLines = response.split("\n");
		String _contentLength = "Content-Length: ";
		String _contentType = "Content-Type: ";
		String _seperator = "#_#@#_#";

		String lengthValue = "";
		String typeValue = "";

		for(int i = 0; i < responseLines.length; i++){
			String line = responseLines[i];
			if(line.indexOf(_contentLength) > -1 && line.indexOf(" ") > -1){
				lengthValue = (line.split(" "))[1];
			}
			else if(line.indexOf(_contentType) > -1 && line.indexOf(" ") > -1){
				typeValue = (line.split(" "))[1];
			}
		}

		return typeValue + _seperator + lengthValue;
	}

	/**
	 * 
	 * @param target : link to communicate with
	 * @return Response
	 */
	public String sendHttpHeadRequestV2(String target) throws IOException, UnknownHostException{
		return (sendHttpRequestV2(target, "HEAD"))[0];
	}

	/**
	 * 
	 * @param target : link to communicate with
	 * @return String Array -> [Response, Response-Mesaage-Body]
	 */
	public String[] sendHttpGetRequest(String target) throws IOException, UnknownHostException{
		return sendHttpRequestV2(target, "GET");
	}

	/////////// INTERCEPT GET 
	// Trick for Get request to receive HEAD
	public String sendHttpGETRequestAndInterceptBeforeBody(String target) throws IOException, UnknownHostException{

		String response = "";
		String requestType = "GET";

		try {
			if(!target.startsWith("http")){
				target = "http://" + target;
			}
			
			String host = Utils.GetDomain(target); 
			host = host.isEmpty() ? CrawlerControler.getInstance().getDomain() : host;
			String path = target.contains(host) ? target.substring(target.indexOf(host) + host.length()) : target;
			path = path.equals("") ? "/" : path;

			if (host.startsWith("http://")) {
				host = host.substring(host.indexOf(("http://")) + 7);
			}
			if(host.indexOf(("%2F")) > -1){
				host = host.substring(0 , host.indexOf("%2F"));
			}
			if(host.startsWith("http://")){
				host = "http://" + host.substring("http://".length()).replaceAll("//", "/");
			} else {
				host = host.replaceAll("//", "/");
			}
			String requestLine = requestType + " " + path + " " + "HTTP/1.0";
			String headers = "Host: " + host;
/*
			URI uri = new URI(target);

			String host = uri.getHost();
			String path = uri.getPath();*/
			path = path.equals("") ? "/" : path;

			requestLine = requestType + " " + path + " " + "HTTP/1.0";
			headers = "Host: " + host;

			Socket socket = new Socket(InetAddress.getByName(host), 80);
			socket.setSoTimeout(6000);
			PrintWriter writer = new PrintWriter(socket.getOutputStream());

			writer.write(requestLine);
			writer.write(_CRLF.toCharArray());
			writer.flush();

			writer.write(headers);
			writer.write(_CRLF.toCharArray());
			writer.flush();

			writer.write(_CRLF.toCharArray());
			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = reader.readLine();

			// Read Request According to Http Protocol
			while (line != null && !line.equals("")) {
				response += (line + "\n");
				line = reader.readLine();
			}

			reader.close();
			writer.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new UnknownHostException();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return response;
	}
}
