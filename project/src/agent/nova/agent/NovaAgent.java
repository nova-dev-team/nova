package nova.agent;

import java.net.InetSocketAddress;

import nova.common.service.SimpleServer;
import nova.common.service.handler.HeartbeatMessageHandler;
import nova.common.service.message.HeartbeatMessage;

public class NovaAgent {

	public static void main(String[] args) {
		Thread t = new Thread(new Runnable() {

			static final int BIND_PORT = 9876;
			Object sem = new Object();

			@Override
			public void run() {

				SimpleServer svr = new SimpleServer();
				svr.registerHandler(HeartbeatMessage.class,
						new HeartbeatMessageHandler());

				svr.bind(new InetSocketAddress(BIND_PORT));

				synchronized (sem) {
					sem.notify();
				}

			}

		});

		t.start();
	}
}
