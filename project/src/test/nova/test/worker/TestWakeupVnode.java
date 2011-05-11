package nova.test.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.handler.StartVnodeHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

/**
 * test for suspend vnode
 * 
 * @author shayf
 * 
 */
public class TestWakeupVnode {
	@Test
	public void test() {
		StartVnodeHandler svh = new StartVnodeHandler();
		StartVnodeMessage msg = new StartVnodeMessage(null);
		msg.setWakeupOnly("true");
		msg.setUuid("1f7c794b-2e17-45ef-3c55-ece004e76aef");
		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		svh.handleMessage(msg, ctx, e, xreply);
	}

}