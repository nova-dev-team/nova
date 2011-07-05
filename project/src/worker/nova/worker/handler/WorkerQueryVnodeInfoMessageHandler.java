package nova.worker.handler;

import java.util.UUID;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.MasterProxy;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;
import nova.worker.api.messages.QueryVnodeInfoMessage;
import nova.worker.daemons.VnodeStatusDaemon;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class WorkerQueryVnodeInfoMessageHandler implements
		SimpleHandler<QueryVnodeInfoMessage> {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(WorkerQueryVnodeInfoMessageHandler.class);

	@Override
	public void handleMessage(QueryVnodeInfoMessage msg,
			ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
		Vnode.Status vs = null;
		UUID uu = msg.getUuid();

		if (uu != null) {
			vs = VnodeStatusDaemon.allStatus.get(msg.getUuid());
			if (vs == null) {
				try {
					Connect conn = new Connect("qemu:///system", true);
					Domain dom = conn.domainLookupByUUID(msg.getUuid());
					if (dom != null) {
						String info = dom.getInfo().state.toString();
						if (info.equalsIgnoreCase("VIR_DOMAIN_BLOCKED")) {
							vs = Vnode.Status.PAUSED;
						} else if (info.equalsIgnoreCase("VIR_DOMAIN_CRASHED")) {
							vs = Vnode.Status.CONNECT_FAILURE;
						} else if (info.equalsIgnoreCase("VIR_DOMAIN_NOSTATE")) {
							vs = Vnode.Status.UNKNOWN;
						} else if (info.equalsIgnoreCase("VIR_DOMAIN_PAUSED")) {
							vs = Vnode.Status.PAUSED;
						} else if (info.equalsIgnoreCase("VIR_DOMAIN_RUNNING")) {
							vs = Vnode.Status.RUNNING;
						} else if (info.equalsIgnoreCase("VIR_DOMAIN_SHUTDOWN")) {
							vs = Vnode.Status.SHUTTING_DOWN;
						} else if (info.equalsIgnoreCase("VIR_DOMAIN_SHUTOFF")) {
							vs = Vnode.Status.SHUT_OFF;
						}
					} else {
						// vs remains null
						log.error("no such uuid exists");
					}
				} catch (LibvirtException e1) {
					log.error("libvirt connection fail", e1);
				}
			}
			MasterProxy master = NovaWorker.getInstance().getMaster();
			master.sendVnodeStatus(null, uu.toString(), vs);
		} else {
			try {
				Connect conn = new Connect("qemu:///system", true);
				if (conn.numOfDomains() > 0) {
					int[] ids = conn.listDomains();
					for (int i = 0; i < ids.length; i++) {
						Domain dom = conn.domainLookupByID(ids[i]);
						if (dom != null) {
							uu = UUID.fromString(dom.getUUIDString());
							String info = dom.getInfo().state.toString();
							if (info.equalsIgnoreCase("VIR_DOMAIN_BLOCKED")) {
								vs = Vnode.Status.PAUSED;
							} else if (info
									.equalsIgnoreCase("VIR_DOMAIN_CRASHED")) {
								vs = Vnode.Status.CONNECT_FAILURE;
							} else if (info
									.equalsIgnoreCase("VIR_DOMAIN_NOSTATE")) {
								vs = Vnode.Status.UNKNOWN;
							} else if (info
									.equalsIgnoreCase("VIR_DOMAIN_PAUSED")) {
								vs = Vnode.Status.PAUSED;
							} else if (info
									.equalsIgnoreCase("VIR_DOMAIN_RUNNING")) {
								vs = Vnode.Status.RUNNING;
							} else if (info
									.equalsIgnoreCase("VIR_DOMAIN_SHUTDOWN")) {
								vs = Vnode.Status.SHUTTING_DOWN;
							} else if (info
									.equalsIgnoreCase("VIR_DOMAIN_SHUTOFF")) {
								vs = Vnode.Status.SHUT_OFF;
							}
						}
					}
				}
				conn.close();
			} catch (LibvirtException ex) {
				log.error("libvirt connection fail", ex);
			}

			MasterProxy master = NovaWorker.getInstance().getMaster();
			for (UUID uuid : VnodeStatusDaemon.allStatus.keySet()) {
				Vnode.Status status = VnodeStatusDaemon.allStatus.get(uuid);
				master.sendVnodeStatus(null, uuid.toString(), status);
			}
		}
	}
}
