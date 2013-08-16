package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.AddUserMessage;
import nova.master.models.UserRelations;
import nova.master.models.Users;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class AddUserHandler implements SimpleHandler<AddUserMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(AddUserMessage.class);

    @Override
    public void handleMessage(AddUserMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        String email = msg.user_email;
        email = email.replace("%40", "@");
        Users user = Users.findByName(msg.user_name);
        if (user == null) {
            if (msg.create_userid == 0 || msg.user_privilege.equals("admin")) {
                Users ur = new Users(msg.user_name, email, msg.user_password,
                        msg.user_privilege, msg.user_actived);
                ur.save();
                log.info("Added new admin user: " + ur.getName());
            } else if (msg.user_privilege.equals("normal")) {
                Users ur = new Users(msg.user_name, email, msg.user_password,
                        msg.user_privilege, msg.user_actived);

                ur.save();
                ur = Users.findByName(msg.user_name);
                UserRelations urre = new UserRelations(ur.getId(),
                        msg.create_userid);
                urre.save();
                log.info("Added new normal user: " + ur.getName());
            }
        } else {
            log.info("The user already exist");
        }
    }
}
