package nova.master.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.messages.UnregisterVdiskMessage;
import nova.master.models.Vdisk;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class UnregisterVdiskHandler implements
		SimpleHandler<UnregisterVdiskMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(UnregisterVdiskHandler.class);

	@Override
	public void handleMessage(UnregisterVdiskMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		// TODO Auto-generated method stub
		Vdisk vdisk = Vdisk.findById(msg.id);
		if (vdisk != null) {
			log.info("Unregistered vdisk: " + vdisk.getFileName());
			Vdisk.delete(vdisk);
		} else {
			log.info("Vdisk @ id: " + String.valueOf(msg.id) + "not exists.");
		}
	}

}
