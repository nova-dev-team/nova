package nova.test.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.RevokeImageMessage;
import nova.worker.handler.RevokeImageHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

public class TestRevokeImage {
	@Test
	public void mytest() {
		RevokeImageHandler rih = new RevokeImageHandler();
		RevokeImageMessage rim = new RevokeImageMessage("del.img");
		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		rih.handleMessage(rim, ctx, e, xreply);
	}

}
