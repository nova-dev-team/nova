package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.ActiveUserMessage;
import nova.master.models.Users;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class ActiveUserHandler implements SimpleHandler<ActiveUserMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(ActiveUserHandler.class);

    @Override
    public void handleMessage(ActiveUserMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {

        Users ur = Users.findById(msg.id);
        if (ur != null) {
            ur.setActivated("true");
            ur.save();
            log.info("Actived user : " + ur.getName());
        } else {
            log.info("The user not exit!");
        }
    }

}
