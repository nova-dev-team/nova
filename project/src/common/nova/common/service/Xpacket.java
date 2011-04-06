package nova.common.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

class Xpacket {

	protected Xpacket() {

	}

	protected String xfrom;

	protected Integer xid;

	protected String xtype;

	protected Object xvalue;

	protected static AtomicInteger xidCounter = new AtomicInteger();

	public static Xpacket createPacket(String name, Object obj,
			InetSocketAddress replyAddr) throws UnknownHostException {
		Xpacket packet = new Xpacket();
		if (replyAddr != null) {
			packet.xfrom = replyAddr.getAddress().getHostAddress() + ":"
					+ replyAddr.getPort();
		} else {
			packet.xfrom = InetAddress.getLocalHost().getHostAddress();
		}
		packet.xid = xidCounter.incrementAndGet();
		packet.xtype = name;
		packet.xvalue = obj;
		return packet;
	}
}