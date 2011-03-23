package nova.test.common.service;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import nova.common.service.SimpleHandler;
import nova.common.service.SimpleProxy;
import nova.common.service.SimpleServer;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

public class TestSimpleService {

	static final int BIND_PORT = 9876;

	Object sem = new Object();

	@Test
	public void testHeartbeat() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				SimpleServer svr = new SimpleServer();
				svr.registerHandler(HeartbeatMessage.class,
						new SimpleHandler<HeartbeatMessage>() {

							AtomicLong counter = new AtomicLong();

							@Override
							public void handleMessage(HeartbeatMessage msg,
									ChannelHandlerContext ctx, MessageEvent e) {
								System.out.println(counter.incrementAndGet());
								System.out.println("Got a heartbeat request!");
								System.out.println(msg);
							}

						});

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

		HeartbeatProxy hp = new HeartbeatProxy();
		hp.connect(new InetSocketAddress("localhost", BIND_PORT));
		for (int j = 0; j < 1000; j++) {
			hp.sendHeartbeat();
		}
		hp.close();

		try {
			// wait a few milliseconds for packets to be handled
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class HeartbeatProxy extends SimpleProxy {

	public void sendHeartbeat() {
		this.sendRequest(new HeartbeatMessage());
	}

}

class HeartbeatMessage {
	// dummy request

	Integer id = counter++;

	static int counter = 0;

	String xtype = "dummy!";

	@Override
	public String toString() {
		return "heartbeat id: " + id + " ~ " + xtype;
	}
}
