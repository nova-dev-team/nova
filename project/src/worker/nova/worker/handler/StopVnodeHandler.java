package nova.worker.handler;

import java.io.File;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
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
			String name = dom.getName();
			dom.destroy();

			System.out.println("delete path = "
					+ Utils.pathJoin(Utils.NOVA_HOME, "run", name));
			delAllFile(Utils.pathJoin(Utils.NOVA_HOME, "run", name));

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
