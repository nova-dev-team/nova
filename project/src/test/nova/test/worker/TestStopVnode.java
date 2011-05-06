package nova.test.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.StopVnodeMessage;
import nova.worker.handler.StopVnodeHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

/**
 * test for stop vnode
 * 
 * @author shayf
 * 
 */
public class TestStopVnode {
	@Test
	public void test() {
		StopVnodeHandler svh = new StopVnodeHandler();
		StopVnodeMessage msg = new StopVnodeMessage(
				"0f7c794b-2e17-45ef-3c55-ece004e76aef");
		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		svh.handleMessage(msg, ctx, e, xreply);

		StopVnodeMessage msg2 = new StopVnodeMessage(
				"1f7c794b-2e17-45ef-3c55-ece004e76aef");
		ChannelHandlerContext ctx2 = null;
		MessageEvent e2 = null;
		SimpleAddress xreply2 = null;
		svh.handleMessage(msg2, ctx2, e2, xreply2);
	}
}