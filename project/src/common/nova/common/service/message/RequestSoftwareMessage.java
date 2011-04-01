package nova.common.service.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class RequestSoftwareMessage {
	private String from = null;
	private ArrayList<String> softList = new ArrayList<String>();

	public RequestSoftwareMessage(ArrayList<String> installSoftList)
			throws UnknownHostException {
		this.from = InetAddress.getLocalHost().getHostAddress().toString();
		this.softList = installSoftList;
	}

	public String getFrom() {
		return this.from;
	}

	public ArrayList<String> getInstallSoftList() {
		return this.softList;
	}
}
