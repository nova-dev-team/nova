package nova.worker.handler;

import java.io.File;
import java.io.IOException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.worker.api.messages.RevokeImageMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class RevokeImageHandler implements SimpleHandler<RevokeImageMessage> {

    Logger log = Logger.getLogger(RevokeImageHandler.class);

    @Override
    public void handleMessage(RevokeImageMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        File revokeFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                "vdiskpool", msg.getName() + ".revoke"));
        if (!revokeFile.exists()) {
            try {
                revokeFile.createNewFile();
            } catch (IOException e1) {
                log.error(
                        "create revokeFile " + revokeFile.getName() + " fail",
                        e1);
            }
        }
    }

}
