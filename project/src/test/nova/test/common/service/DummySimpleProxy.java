package nova.test.common.service;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import nova.common.service.SimpleProxy;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.GeneralMonitorMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.RequestGeneralMonitorMessage;
import nova.common.service.message.RequestHeartbeatMessage;
import nova.common.service.message.RequestSoftwareMessage;

public class DummySimpleProxy {
	public void run() throws UnknownHostException {

		MessageProxy hp = new MessageProxy(new InetSocketAddress("10.0.1.236",
				9876));
		// Connect to server and establish a channel
		hp.connect(new InetSocketAddress("localhost", 9876));

		// // TestRequestSoftwareMessage in agent
		// LinkedList<String> installSoftList = new LinkedList<String>();
		// installSoftList.offer("test1.exe");
		// installSoftList.offer("test2.exe");
		// installSoftList.offer("test3.exe");
		// hp.sendRequestSoftwareMessage(installSoftList);
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// }
		// installSoftList.clear();
		// installSoftList.offer("test4.exe");
		// hp.sendRequestSoftwareMessage(installSoftList);
		//
		// installSoftList.clear();
		// installSoftList.offer("test5.exe");
		// installSoftList.offer("test6.exe");
		// hp.sendRequestSoftwareMessage(installSoftList);

		// Send many messages through this channel
		for (int j = 0; j < 1000; j++) {
			hp.sendHeartbeatMessage();
		}
		hp.sendGeneralMonitorMessage();

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

	public MessageProxy(InetSocketAddress replyAddr) {
		super(replyAddr);
	}

	public void sendHeartbeatMessage() throws UnknownHostException {
		this.sendRequest(new HeartbeatMessage());
	}

	public void sendGeneralMonitorMessage() throws UnknownHostException {
		this.sendRequest(new GeneralMonitorMessage());
	}

	public void sendRequestGeneralMonitorMessage() throws UnknownHostException {
		this.sendRequest(new RequestGeneralMonitorMessage());
	}

	public void sendCloseChannelMessage() throws UnknownHostException {
		this.sendRequest(new CloseChannelMessage());
	}

	public void sendRequestHeartBeatMessage() {
		this.sendRequest(new RequestHeartbeatMessage());
	}

	public void sendRequestSoftwareMessage(LinkedList<String> installSoftList) {
		this.sendRequest(new RequestSoftwareMessage(installSoftList));
	}

}