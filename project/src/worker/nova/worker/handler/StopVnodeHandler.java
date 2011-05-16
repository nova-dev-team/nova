package nova.worker.handler;

import java.io.File;
import java.util.UUID;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.master.models.Vnode;
import nova.worker.api.messages.StopVnodeMessage;
import nova.worker.daemons.VnodeStatusDaemon;

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
		final String virtService;
		if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
			virtService = "qemu:///system";
		} else {
			// TODO @shayf get correct xen service address
			virtService = "some xen address";
		}
		Connect conn = null;
		try {
			// connect the qemu system
			conn = new Connect(virtService, false);
		} catch (LibvirtException ex) {
			log.error("Error connecting " + virtService, ex);
		}

		try {
			Domain dom = conn.domainLookupByUUIDString(msg.getUuid());
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
				System.out.println("delete path = "
						+ Utils.pathJoin(Utils.NOVA_HOME, "run", name));
				delAllFile(Utils.pathJoin(Utils.NOVA_HOME, "run", name));
			} else {
				dom.suspend();
				VnodeStatusDaemon.putStatus(UUID.fromString(msg.getUuid()),
						Vnode.Status.PAUSED);
			}

		} catch (LibvirtException ex) {
			log.error("Error closing domain " + msg.getUuid(), ex);
		}

	}

	private void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			temp = new File(Utils.pathJoin(path, tempList[i]));
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(Utils.pathJoin(path, tempList[i]));
				delFolder(Utils.pathJoin(path, tempList[i]));
			}
		}
		delFolder(path);
		return;
	}

	private void delFolder(String folderPath) {
		try {
			java.io.File myFilePath = new java.io.File(folderPath);
			myFilePath.delete();
		} catch (Exception e) {
			log.error("del folder " + folderPath + " error", e);
		}

	}
}
