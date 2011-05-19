package nova.test.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.handler.StartVnodeHandler;

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

	@Test
	public void test() {
		StartVnodeHandler svh = new StartVnodeHandler();
		StartVnodeMessage msg = new StartVnodeMessage("kvm", null, "false",
				"524288", "1", "small.img", "false");
		msg.setName("vm");
		msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
		msg.setCdImage("");

		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		svh.handleMessage(msg, ctx, e, xreply);

		// StartVnodeMessage msg2 = new StartVnodeMessage("KVM", null, "false",
		// "100288", "1", "small.img", "false");
		// msg2.setName("vm2");
		// msg2.setUuid("1f7c794b-2e17-45ef-3c55-ece004e76aef");
		// msg2.setCdImage("");
		//
		// ChannelHandlerContext ctx2 = null;
		// MessageEvent e2 = null;
		// SimpleAddress xreply2 = null;
		// svh.handleMessage(msg2, ctx2, e2, xreply2);
		//
		// StartVnodeMessage msg3 = new StartVnodeMessage("kvm", null, "false",
		// "100288", "1", "small.img", "false");
		// msg3.setName("vm3");
		// msg3.setUuid("2f7c794b-2e17-45ef-3c55-ece004e76aef");
		// msg3.setCdImage("");
		//
		// ChannelHandlerContext ctx3 = null;
		// MessageEvent e3 = null;
		// SimpleAddress xreply3 = null;
		// svh.handleMessage(msg3, ctx3, e3, xreply3);
	}
}
