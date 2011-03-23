package nova.common.service;

import java.util.concurrent.atomic.AtomicInteger;

class Xpacket {

	private Xpacket() {

	}

	Integer xid;

	String xtype;

	Object xvalue;

	private static AtomicInteger xidCounter = new AtomicInteger();

	public static Xpacket createPacket(String name, Object obj) {
		Xpacket packet = new Xpacket();
		packet.xid = xidCounter.incrementAndGet();
		packet.xtype = name;
		packet.xvalue = obj;
		return packet;
	}

}
