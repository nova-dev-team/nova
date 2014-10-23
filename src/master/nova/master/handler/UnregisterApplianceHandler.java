package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.UnregisterApplianceMessage;
import nova.master.models.Appliance;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class UnregisterApplianceHandler implements
        SimpleHandler<UnregisterApplianceMessage> {

    /**
     * Log4i logger.
     */
    Logger log = Logger.getLogger(UnregisterApplianceHandler.class);

    @Override
    public void handleMessage(UnregisterApplianceMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub
        Appliance appliance = Appliance.findById(msg.id);
        if (appliance != null) {
            log.info("Unregistered appliance: " + appliance.getFileName());
            Appliance.delete(appliance);
        } else {
            log.info("Appliance @ id: " + String.valueOf(msg.id)
                    + "not exists.");
        }

    }

}
