package nova.master.handler;

import java.io.IOException;
import java.net.ServerSocket;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.master.api.MasterVNCPM;
import nova.master.api.messages.PnodeCreateVnodeMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class PnodeCreateVnodeHandler implements
        SimpleHandler<PnodeCreateVnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(PnodeCreateVnodeMessage.class);

    public static int getFreePort() {
        ServerSocket s = null;
        int MINPORT = 5901;
        int MAXPORT = 6900;
        for (; MINPORT < MAXPORT; MINPORT++) {
            try {
                s = new ServerSocket(MINPORT);
                s.close();
                return MINPORT;
            } catch (IOException e) {
                continue;
            }
        }
        return -1;

    }

    @Override
    public void handleMessage(PnodeCreateVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        String masterIP = Conf.getString("master.bind_host");
        int masterPort = getFreePort();
        MasterVNCPM
                .addService(masterIP, masterPort, msg.PnodeIP, msg.VnodePort);
        Utils.MASTER_VNC_MAP.put(msg.uuid, String.valueOf(masterPort));
    }
}
