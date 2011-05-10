package nova.worker.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.daemons.VdiskPoolDaemon;
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

		// mv img files from vdiskpool
		File stdFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
				"linux.img"));
		long stdLen = stdFile.length();
		boolean found = false;
		for (int i = VdiskPoolDaemon.getPOOL_SIZE(); i >= 1; i--) {
			File srcFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
					"vdiskpool", "linux.img.pool." + Integer.toString(i)));
			if (srcFile.exists() && (srcFile.length() == stdLen)) {
				System.out.println("file " + "linux.img.pool."
						+ Integer.toString(i) + "exists!");
				File foder = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
						msg.getName()));
				if (!foder.exists()) {
					foder.mkdirs();
				}
				File dstFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
						msg.getName(), "linux.img"));
				srcFile.renameTo(dstFile);
				found = true;
				break;
			} else {
				System.out.println("file " + "linux.img.pool."
						+ Integer.toString(i) + "not exist!");
			}
		}
		if (!found) {
			// copy img files
			File foder = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
					msg.getName()));
			File file = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
					msg.getName(), "linux.img"));
			if (!foder.exists()) {
				foder.mkdirs();
			} else {
				// TODO @santa rename or stop or what?
				log.error("vm name " + msg.getName() + " has been used!");
			}
			if (file.exists() == false) {
				try {
					System.out.println("copying file");
					String sourceUrl = Utils.pathJoin(Utils.NOVA_HOME, "run",
							"linux.img");
					String destUrl = Utils.pathJoin(Utils.NOVA_HOME, "run",
							msg.getName(), "linux.img");
					File sourceFile = new File(sourceUrl);
					if (sourceFile.isFile()) {
						FileInputStream input = new FileInputStream(sourceFile);
						FileOutputStream output = new FileOutputStream(destUrl);
						byte[] b = new byte[1024 * 5];
						int len;
						while ((len = input.read(b)) != -1) {
							output.write(b, 0, len);
						}
						output.flush();
						output.close();
						input.close();
					}
				} catch (IOException ex) {
					log.error("copy image fail", ex);
				}
			}
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
