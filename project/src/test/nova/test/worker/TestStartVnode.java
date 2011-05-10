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
		StartVnodeMessage msg = new StartVnodeMessage(null);
		msg.setHyperVisor("kvm");
		msg.setName("vm");
		msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
		msg.setMemSize("524288");
		msg.setCpuCount("1");
		msg.setArch("i686");
		msg.setCdImage("");// /media/data/ubuntu-10.04-desktop-i386.iso
		msg.setEmulatorPath("/usr/bin/kvm");
		msg.setRunAgent("false");

		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		svh.handleMessage(msg, ctx, e, xreply);

		msg.setHyperVisor("KVM");
		msg.setName("vm2");
		msg.setUuid("1f7c794b-2e17-45ef-3c55-ece004e76aef");
		msg.setMemSize("524288");
		msg.setCpuCount("1");
		msg.setArch("i686");
		msg.setCdImage("");
		msg.setEmulatorPath("/usr/bin/kvm");
		msg.setRunAgent("false");

		ChannelHandlerContext ctx2 = null;
		MessageEvent e2 = null;
		SimpleAddress xreply2 = null;
		svh.handleMessage(msg, ctx2, e2, xreply2);
	}
}
