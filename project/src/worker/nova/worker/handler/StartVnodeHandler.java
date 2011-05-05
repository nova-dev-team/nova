package nova.worker.handler;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
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
		// TODO @shayf [future] support both xen and kvm.
		final String virtService = "qemu:///system";
		Connect conn = null;
		try {
			// connect the qemu system
			conn = new Connect(virtService, false);
		} catch (LibvirtException ex) {
			// TODO @santa might need to restart libvirt deamon and retry
			log.error("Error connecting " + virtService, ex);
		}

		// TODO @shayf get real config value from files
		msg.setHyperVisor("kvm");
		msg.setName("vm1");
		msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
		msg.setMemSize("524288");
		msg.setCpuCount("1");
		msg.setArch("i686");
		msg.setCdImage("");
		if ((msg.getCdImage() != null) && (msg.getCdImage() != "")) {
			msg.setBootDevice("cdrom");
		} else {
			msg.setBootDevice("hd");
		}
		if (msg.getHyperVisor() == "kvm") {
			msg.setEmulatorPath("/usr/bin/kvm");
		} else if (msg.getHyperVisor() == "xen") {
			// TODO @shayf update path
			msg.setEmulatorPath("some other path");
		} else {
			msg.setEmulatorPath("some other path");
		}
		msg.setRunAgent("false");
		if (msg.getRunAgent() == "true") {
			msg.setCdromPath(Utils.pathJoin(Utils.NOVA_HOME, msg.getName(),
					"agent-cd.iso"));
		} else if ((msg.getCdImage() != null) && (msg.getCdImage() != "")) {
			msg.setCdromPath(Utils.pathJoin(Utils.NOVA_HOME, msg.getName(),
					msg.getCdImage()));
		}

		// TODO @shayf read these values from some conf file
		String vmNetworkInterface = "";
		String vmNetworkBridge = "";
		String fixVncMousePointer = "true";
		if ((vmNetworkInterface != "") && (vmNetworkBridge != "")) {
			msg.setInterfaceType("bridge");
			msg.setSourcebridge(vmNetworkBridge);
			msg.setMacAddress(Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256)));
		} else {
			msg.setInterfaceType("network");
			msg.setSourceNetwork("default");
		}
		if (fixVncMousePointer == "true") {
			msg.setInputType("tablet");
			msg.setBus("usb");
		} else {
			// TODO @shayf set correct default value
			msg.setInputType("tablet");
			msg.setBus("usb");
		}

		// create domain and show some info
		try {
			Domain testDomain = conn.domainCreateLinux(
					Kvm.emitDomain(msg.getHashMap()), 0);
			System.out
					.println("Domain:" + testDomain.getName() + " id "
							+ testDomain.getID() + " running "
							+ testDomain.getOSType());
		} catch (LibvirtException ex) {
			log.error("Create domain failed", ex);
		}

	}
}
