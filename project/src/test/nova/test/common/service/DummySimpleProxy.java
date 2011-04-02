package nova.test.common.service;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.MonitorMessage;

public class DummySimpleProxy {
	public void run() throws UnknownHostException {
		MessageProxy hp = new MessageProxy();
		hp.connect(new InetSocketAddress("localhost", 9876)); // Connect to
																// server and
																// establish a
																// channel
		// Send many messages through this channel
		for (int j = 0; j < 1000; j++) {
			hp.sendHeartbeatMessage();
		}
		for (int j = 0; j < 1000; j++) {
			hp.sendMonitorMessage();
		}

		hp.sendCloseChannelMessage(); // Close message
		hp.close();

		try {
			// wait a few milliseconds for packets to be handled
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException {
		DummySimpleProxy proxy = new DummySimpleProxy();
		proxy.run();
	}
}

class MessageProxy extends SimpleProxy { // A client example

	public void sendHeartbeatMessage() throws UnknownHostException {
		this.sendRequest(new HeartbeatMessage());
	}

	public void sendMonitorMessage() throws UnknownHostException {
		this.sendRequest(new MonitorMessage());
	}

	public void sendCloseChannelMessage() throws UnknownHostException {
		this.sendRequest(new CloseChannelMessage());
	}

}