package nova.common.service;

import java.net.InetAddress;
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

	public static Xpacket createPacket(String name, Object obj)
			throws UnknownHostException {
		Xpacket packet = new Xpacket();
		packet.xfrom = InetAddress.getLocalHost().toString();
		packet.xid = xidCounter.incrementAndGet();
		packet.xtype = name;
		packet.xvalue = obj;
		return packet;
	}

}