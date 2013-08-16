package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.DeleteUserMessage;
import nova.master.models.UserRelations;
import nova.master.models.Users;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class DeleteUserHandler implements SimpleHandler<DeleteUserMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(DeleteUserMessage.class);

    @Override
    public void handleMessage(DeleteUserMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        Users user = Users.findById(msg.id);
        if (user.getPrivilege().equals("admin")) {
            for (UserRelations urre : UserRelations.getByAdminUserId(msg.id)) {
                UserRelations.delete(urre);
                Users.delete(Users.findById(urre.getNormalUserId()));
            }
        } else if (user.getPrivilege().equals("normal")) {
            UserRelations.delete(UserRelations.findByNormalId(msg.id));
        }
        Users.delete(user);
    }
}
