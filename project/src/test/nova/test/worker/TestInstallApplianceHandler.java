package nova.test.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.InstallApplianceMessage;
import nova.worker.handler.InstallApplianceHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

public class TestInstallApplianceHandler {

	@Test
	public void mytest() {
		String[] appNames = { "demo_appliance" };
		InstallApplianceMessage iam = new InstallApplianceMessage(appNames);
		InstallApplianceHandler iah = new InstallApplianceHandler();

		ChannelHandlerContext ctx = null;
		MessageEvent e = null;
		SimpleAddress xreply = null;
		iah.handleMessage(iam, ctx, e, xreply);
	}
}
