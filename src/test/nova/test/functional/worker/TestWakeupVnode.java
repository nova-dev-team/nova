package nova.test.functional.worker;

import nova.common.service.SimpleAddress;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.handler.StartVnodeHandler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;

import sun.net.ftp.FtpProtocolException;

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
        StartVnodeMessage msg = new StartVnodeMessage("kvm", true, false, null);
        msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
        ChannelHandlerContext ctx = null;
        MessageEvent e = null;
        SimpleAddress xreply = null;
        try {
            svh.handleMessage(msg, ctx, e, xreply);
        } catch (FtpProtocolException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}