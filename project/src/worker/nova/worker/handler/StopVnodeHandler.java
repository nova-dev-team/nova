package nova.worker.handler;

import java.util.UUID;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;
import nova.worker.api.messages.StopVnodeMessage;
import nova.worker.daemons.VnodeStatusDaemon;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
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
		final String virtService;
		if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
			virtService = "qemu:///system";
		} else {
			// TODO @shayf get correct xen service address
			virtService = "some xen address";
		}

		try {
			Domain dom = null;
			synchronized (NovaWorker.getInstance().getConnLock()) {
				NovaWorker.getInstance().connectToKvm(virtService, false);
				dom = NovaWorker.getInstance().getConn()
						.domainLookupByUUIDString(msg.getUuid());
				// NovaWorker.getInstance().closeConnectToKvm();
			}
			if (dom == null) {
				VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
						Vnode.Status.CONNECT_FAILURE);
				log.error("cannot connect and close domain " + msg.getUuid());
				return;
			}
			String name = dom.getName();

			if (!msg.isSuspendOnly()) {
				VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
						Vnode.Status.SHUTTING_DOWN);
				dom.destroy();
				VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
						Vnode.Status.SHUT_OFF);
				NovaWorker.getInstance().getVnodeIP()
						.remove(UUID.fromString(msg.getUuid()));
				System.out.println("delete path = "
						+ Utils.pathJoin(Utils.NOVA_HOME, "run", name));
				Utils.delAllFile(Utils.pathJoin(Utils.NOVA_HOME, "run", name));
			} else {
				dom.suspend();
				VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
						Vnode.Status.PAUSED);
			}

		} catch (LibvirtException ex) {
			log.error("Error closing domain " + msg.getUuid(), ex);
		}

	}

}
