package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.ModifyUserPassMessage;
import nova.master.models.Users;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class ModifyUserPassHandler implements
        SimpleHandler<ModifyUserPassMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(ModifyUserPassMessage.class);

    @Override
    public void handleMessage(ModifyUserPassMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        Users user = Users.findById(msg.userid);
        if (user != null) {
            user.setPassword(msg.newpass);
            user.save();
            log.info("Modify user password: " + user.getName());
        } else {
            log.info("The user not exit!");
        }
    }

}
