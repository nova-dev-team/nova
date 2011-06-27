package nova.test.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.StopVnodeMessage;
import nova.worker.handler.StopVnodeHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

/**
 * test for suspend vnode
 * 
 * @author shayf
 * 
 */
public class TestSuspendVnode {
	@Test
	public void test() {
		StopVnodeHandler svh = new StopVnodeHandler();
		StopVnodeMessage msg = new StopVnodeMessage("kvm",
				"0f7c794b-2e17-45ef-3c55-ece004e76aef", true);
		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		svh.handleMessage(msg, ctx, e, xreply);
	}

}