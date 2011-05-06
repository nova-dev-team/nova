package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.worker.api.messages.StopVnodeMessage;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

/**
 * Handler for "stop an existing vnode" request
 * 
 * @author shayf
 * 
 */
public class StopVnodeHandler implements SimpleHandler<StopVnodeMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(StartVnodeHandler.class);

	@Override
	public void handleMessage(StopVnodeMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, SimpleAddress xreply) {
		final String virtService = "qemu:///system";
		Connect conn = null;
		try {
			// connect the qemu system
			conn = new Connect(virtService, false);
		} catch (LibvirtException ex) {
			log.error("Error connecting " + virtService, ex);
		}

		try {
			Domain dom = conn.domainLookupByUUIDString(msg.getUuid());
			dom.destroy();
		} catch (LibvirtException ex) {
			log.error("Error closing domain " + msg.getUuid(), ex);
		}

	}

}
