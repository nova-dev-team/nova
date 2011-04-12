package nova.common.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal class to wrap messages to be sent. DO NOT USE IT DIRECTLY!
 * 
 * @author santa
 * 
 */
class Xpacket {

	protected Xpacket() {

	}

	protected String xfrom;

	protected Integer xid;

	protected String xtype;

	protected Object xvalue;

	protected static AtomicInteger xidCounter = new AtomicInteger();

	public static Xpacket createPacket(String name, Object obj,
			InetSocketAddress replyAddr) {
		Xpacket packet = new Xpacket();
		if (replyAddr != null) {
			packet.xfrom = replyAddr.getAddress().getHostAddress() + ":"
					+ replyAddr.getPort();
		} else {
			try {
				packet.xfrom = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		packet.xid = xidCounter.incrementAndGet();
		packet.xtype = name;
		packet.xvalue = obj;
		return packet;
	}
}