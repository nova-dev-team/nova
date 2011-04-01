package nova.common.service.message;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RequestHeartbeatMessage {
	private String from = null;

	public RequestHeartbeatMessage() throws UnknownHostException {
		this.from = InetAddress.getLocalHost().getHostAddress().toString();
	}

	public String getFrom() {
		return this.from;
	}
}
