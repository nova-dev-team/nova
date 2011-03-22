package nova.test.common.service;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import nova.common.service.SimpleProxy;
import nova.common.service.SimpleRequest;
import nova.common.service.SimpleServer;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

import com.google.gson.Gson;

public class TestSimpleService {

	static final int BIND_PORT = 9876;

	Object sem = new Object();

	@Test
	public void test() {

	}

	@Test
	public void testHeartbeat() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				HeartbeatServer svr = new HeartbeatServer();
				svr.bind(new InetSocketAddress(BIND_PORT));

				synchronized (sem) {
					sem.notify();
				}

			}

		}).start();

		try {
			synchronized (sem) {
				sem.wait();
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ArrayList<Thread> tList = new ArrayList<Thread>();

		for (int k = 0; k < 10; k++) {
			Thread t = new Thread() {

				@Override
				public void run() {
					for (int i = 0; i < 20; i++) {
						HeartbeatProxy hp = new HeartbeatProxy();
						hp.connect(new InetSocketAddress("localhost", BIND_PORT));
						for (int j = 0; j < 500; j++) {
							hp.getHeartbeat(j);
						}

						hp.close();
					}
				}

			};
			t.start();
			tList.add(t);

		}

		for (Thread t : tList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}

class HeartbeatServer extends SimpleServer {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		Gson gson = new Gson();
		HeartbeatRequest req = gson.fromJson((String) e.getMessage(),
				HeartbeatRequest.class);
		System.out.println(gson.toJson(req));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

}

class HeartbeatProxy extends SimpleProxy {

	public void getHeartbeat() {
		this.sendRequest(new HeartbeatRequest());
	}

	public void getHeartbeat(int id) {
		this.sendRequest(new HeartbeatRequest(id));
	}

}

class HeartbeatRequest extends SimpleRequest {
	public String xcall = "getHeartbeat";
	public int xid = xidCounter++;
	public int id = 0;

	private static int xidCounter = 0;

	public HeartbeatRequest() {
		this(0);
	}

	public HeartbeatRequest(int id) {
		this.id = id;
	}

}
