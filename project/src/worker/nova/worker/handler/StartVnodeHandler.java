package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.virt.Kvm;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

/**
 * Handler for "start new vnode" request.
 * 
 * @author santa
 * 
 */
public class StartVnodeHandler implements SimpleHandler<StartVnodeMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(StartVnodeHandler.class);

	/**
	 * Handle "start new vnode" request.
	 */
	@Override
	public void handleMessage(StartVnodeMessage msg, ChannelHandlerContext ctx,
			MessageEvent e, SimpleAddress xreply) {
		// TODO @future support both xen and kvm.
		final String virtService = "qemu:///system";
		Connect conn = null;
		try {
			// connect the qemu system
			conn = new Connect(virtService, false);
		} catch (LibvirtException ex) {
			// TODO @santa might need to restart libvirt deamon and retry
			log.error("Error connecting " + virtService, ex);
		}

		if (msg.isWakeupOnly()) {
			try {
				Domain testDomain = conn
						.domainLookupByUUIDString(msg.getUuid());
				testDomain.resume();
			} catch (LibvirtException ex) {
				log.error("Domain with UUID='" + msg.getUuid()
						+ "' can't be found!", ex);
			}
		} else {
			// create domain and show some info
			if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
				try {
					Domain testDomain = conn.domainCreateLinux(
							Kvm.emitDomain(msg.getHashMap()), 0);
					System.out.println("Domain:" + testDomain.getName()
							+ " id " + testDomain.getID() + " running "
							+ testDomain.getOSType());
					// Domain testDomain = conn.domainLookupByName("test");
					// System.out.println("xml desc\n" +
					// testDomain.getXMLDesc(0));
				} catch (LibvirtException ex) {
					log.error("Create domain failed", ex);
				}
			} else if (msg.getHyperVisor().equalsIgnoreCase("xen")) {
				// TODO @shayf add xen process
				log.error("xen not supported yet");
			} else {
				log.error("so such type hypervisor " + msg.getHyperVisor());
			}
		}

	}
}
