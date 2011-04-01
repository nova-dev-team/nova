package nova.test.common.service;

import java.net.InetSocketAddress;

import nova.common.service.SimpleServer;
import nova.common.service.handler.CloseChannelMessageHandler;
import nova.common.service.handler.HeartbeatMessageHandler;
import nova.common.service.handler.MonitorMessageHandler;
import nova.common.service.message.CloseChannelMessage;
import nova.common.service.message.HeartbeatMessage;
import nova.common.service.message.MonitorMessage;

public class TestSimpleServer {

	static final int BIND_PORT = 9876;

	Object sem = new Object();

	public void testMessage() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println(CloseChannelMessage.class.getName()
						.toString());
				SimpleServer svr = new SimpleServer(); // Create a server

				// Register 3 type of message handler to server
				svr.registerHandler(MonitorMessage.class,
						new MonitorMessageHandler());
				svr.registerHandler(HeartbeatMessage.class,
						new HeartbeatMessageHandler());
				svr.registerHandler(CloseChannelMessage.class,
						new CloseChannelMessageHandler());

				svr.bind(new InetSocketAddress(BIND_PORT));

				synchronized (sem) {
					sem.notify();
				}

			}

		});

		t.start();

		try {
			synchronized (sem) {
				sem.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TestSimpleServer server = new TestSimpleServer();
		server.testMessage();
	}
}
