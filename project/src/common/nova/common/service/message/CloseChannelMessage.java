package nova.common.service.message;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CloseChannelMessage {
	private String from = null;

	public CloseChannelMessage() throws UnknownHostException {
		this.from = InetAddress.getLocalHost().getHostAddress().toString();
	}

	public String getFrom() {
		return this.from;
	}
}
