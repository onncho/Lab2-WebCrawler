import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class PortScanner implements Runnable {

	String m_target;
	int m_StartPort;
	int m_EndPort;
	ArrayList<Integer> m_OpenPorts;
	private final String m_RegexDefinition; 
	private final String m_RegexHttp;
	private String m_TargetParsed;
	private PortScannerLatch m_latch;

	public PortScanner(String i_Target, int i_StartPort, int i_EndPort, PortScannerLatch i_latch) {
		m_target = i_Target;
		m_StartPort = i_StartPort;
		m_EndPort = i_EndPort;
		m_RegexDefinition = "^.[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}";
		m_RegexHttp = "http://";
		m_TargetParsed = null;
		m_latch = i_latch;
		m_OpenPorts = new ArrayList<>();
		getIpFromHost();
	}

	private void getIpFromHost() {

		// if target is ip version 4
		if (m_target.matches(m_RegexDefinition) == false) {		
			m_target = m_target.replaceFirst(m_RegexHttp, "");
			InetAddress address;

			try {
				address = InetAddress.getByName(m_target);
				m_TargetParsed = address.getHostAddress();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}
	}

	public ArrayList<Integer> runScan() {

		for (int port = m_StartPort ; port < m_EndPort; port++) {
			try {	
				Socket socket = new Socket();

				if (m_TargetParsed != null) {					
					socket.connect(new InetSocketAddress(m_TargetParsed, port), 200);
				}

				m_OpenPorts.add(port);
				socket.close();
			} 
			catch (Exception e) 
			{
				System.out.println("Port : " + port + " is Closed");
			}
		}

		return m_OpenPorts;
	}

	@Override
	public void run() {
		getIpFromHost();
		ArrayList<Integer> ports = runScan();
		CrawlerControler.getInstance().addPorts(ports);
		m_latch.down();
	}
}
