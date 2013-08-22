package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.StopUserMessage;
import nova.master.models.Users;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class StopUserHandler implements SimpleHandler<StopUserMessage> {
    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(StopUserHandler.class);

    @Override
    public void handleMessage(StopUserMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        Users ur = Users.findById(msg.id);
        if (ur != null) {
            ur.setActivated("false");
            ur.save();
            log.info("Stop user : " + ur.getName());
        } else {
            log.info("The user not exit!");
        }
    }

}