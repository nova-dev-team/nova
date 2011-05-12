package nova.test.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.handler.StartVnodeHandler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

/**
 * test for add vnode, run in linux, qemu required, remember to kill the
 * vmachine after running
 * 
 * @author shayf
 * 
 */
public class TestStartVnode {

	Logger log = Logger.getLogger(TestStartVnode.class);

	@Test
	public void test() {
		StartVnodeHandler svh = new StartVnodeHandler();
		StartVnodeMessage msg = new StartVnodeMessage("kvm", null, "false",
				null);
		// msg.setHyperVisor("kvm");
		msg.setName("vm");
		msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
		// msg.setMemSize("524288");
		// msg.setCpuCount("1");
		msg.setHdaImage("small.img");
		msg.setCdImage("");// /media/data/ubuntu-10.04-desktop-i386.iso
		msg.setRunAgent("false");

		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		svh.handleMessage(msg, ctx, e, xreply);

		StartVnodeMessage msg2 = new StartVnodeMessage("KVM", null, "false",
				"524288", "1", "small.img", "false");
		msg2.setName("vm2");
		msg2.setUuid("1f7c794b-2e17-45ef-3c55-ece004e76aef");
		msg2.setCdImage("");

		ChannelHandlerContext ctx2 = null;
		MessageEvent e2 = null;
		SimpleAddress xreply2 = null;
		svh.handleMessage(msg2, ctx2, e2, xreply2);
	}
}
