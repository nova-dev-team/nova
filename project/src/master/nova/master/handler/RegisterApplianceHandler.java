package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.RegisterApplianceMessage;
import nova.master.models.Appliance;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class RegisterApplianceHandler implements
		SimpleHandler<RegisterApplianceMessage> {
	/**
	 * Log4j logger.
	 */

	Logger log = Logger.getLogger(RegisterApplianceHandler.class);

	@Override
	public void handleMessage(RegisterApplianceMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub
		Appliance appliance = Appliance.findByFileName(msg.fileName);
		if (appliance == null) {
			// new appliance
			appliance = new Appliance();
			appliance.setDisplayName(msg.displayName);
			appliance.setFileName(msg.fileName);
			appliance.setOsFamily(msg.osFamily);
			appliance.setDescription(msg.description);
			appliance.save();
			log.info("Registered new vdisk: " + appliance.getFileName());
		} else {
			// the appliance exists
			log.info("Vdisk @" + appliance.getFileName()
					+ " already registered");
		}

	}

}
