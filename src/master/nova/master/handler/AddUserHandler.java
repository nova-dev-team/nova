package nova.master.handler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.AddUserMessage;
import nova.master.models.Users;

public class AddUserHandler implements SimpleHandler<AddUserMessage>{

	/**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(AddUserMessage.class);
    
    @Override
    public void handleMessage(AddUserMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
    	Users user = Users.findByName(msg.user_name);
    	if(user == null) {
    		Users ur = new Users(msg.user_name, msg.user_email, msg.user_password, 
    				msg.user_privilege, msg.user_actived);
    		ur.save();
    		log.info("Added new user: " + ur.getName());
    	} else {
    		log.info("The user already exist");
    	}
    }
}
