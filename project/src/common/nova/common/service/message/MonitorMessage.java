package nova.common.service.message;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MonitorMessage {
	private String from = null;
	private String monitorInfo = null;

	public MonitorMessage() throws UnknownHostException {
		this.from = InetAddress.getLocalHost().getHostAddress().toString();
		this.monitorInfo = "This is moniotr information!";
	}

	public String getMonitorInfo() {
		return this.monitorInfo;
	}

	public String getFrom() {
		return this.from;
	}
}
