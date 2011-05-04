package nova.test.worker;

import nova.common.service.SimpleAddress;
import nova.worker.handler.StartVnodeHandler;
import nova.worker.handler.StartVnodeHandler.Message;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

public class TestStartVnode {
	@Test
	public void test() {
		StartVnodeHandler svh = new StartVnodeHandler();
		Message msg = new Message(null);
		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		svh.handleMessage(msg, ctx, e, xreply);

	}
}
