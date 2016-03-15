package nova.test.functional.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.InstallApplianceMessage;
import nova.worker.handler.InstallApplianceHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

import sun.net.ftp.FtpProtocolException;

public class TestInstallApplianceHandler {

    @Test
    public void mytest() {
        String[] appNames = { "demo_appliance" };
        InstallApplianceMessage iam = new InstallApplianceMessage(appNames);
        InstallApplianceHandler iah = new InstallApplianceHandler();

        ChannelHandlerContext ctx = null;
        MessageEvent e = null;
        SimpleAddress xreply = null;
        try {
            iah.handleMessage(iam, ctx, e, xreply);
        } catch (FtpProtocolException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
