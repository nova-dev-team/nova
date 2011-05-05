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
	 * Message for "start new vnode" request.
	 * 
	 * @author santa
	 * 
	 */
	public static class Message {

		public Message(SimpleAddress vAddr) {
			Message.vAddr = vAddr;
		}

		/**
		 * Basic information required to start a new vnode.
		 */
		public static SimpleAddress vAddr;

	}

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
			//
			// connect the qemu system
			conn = new Connect(virtService, false);
		} catch (LibvirtException ex) {
			// TODO @santa might need to restart libvirt deamon and retry
			log.error("Error connecting " + virtService, ex);
		}

		StartVnodeMessage svm = new StartVnodeMessage(Message.vAddr);
		// TODO @shayf get real config value from files
		svm.setHyperVisor("kvm");
		svm.setName("vm1");
		svm.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
		svm.setMemSize("524288");
		svm.setCpuCount("1");
		svm.setArch("i686");
		svm.setCdImage("");
		if ((svm.getCdImage() != null) && (svm.getCdImage() != "")) {
			svm.setBootDevice("cdrom");
		} else {
			svm.setBootDevice("hd");
		}
		if (svm.getHyperVisor() == "kvm") {
			svm.setEmulatorPath("/usr/bin/kvm");
		} else if (svm.getHyperVisor() == "xen") {
			// TODO @shayf update path
			svm.setEmulatorPath("some other path");
		} else {
			// TODO @shayf update path
			svm.setEmulatorPath("some other path");
		}
		svm.setRunAgent("false");
		if (svm.getRunAgent() == "true") {
			svm.setCdromPath(Utils.pathJoin(Utils.NOVA_HOME, svm.getName(),
					"agent-cd.iso"));
		} else if ((svm.getCdImage() != null) && (svm.getCdImage() != "")) {
			svm.setCdromPath(Utils.pathJoin(Utils.NOVA_HOME, svm.getName(),
					svm.getCdImage()));
		}

		// TODO @shayf read these values from some conf file
		String vmNetworkInterface = "";
		String vmNetworkBridge = "";
		String fixVncMousePointer = "true";
		if ((vmNetworkInterface != "") && (vmNetworkBridge != "")) {
			svm.setInterfaceType("bridge");
			svm.setSourcebridge(vmNetworkBridge);
			svm.setMacAddress(Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256)));
		} else {
			svm.setInterfaceType("network");
			svm.setSourceNetwork("default");
		}
		if (fixVncMousePointer == "true") {
			svm.setInputType("tablet");
			svm.setBus("usb");
		} else {
			// TODO @shayf set correct default value
			svm.setInputType("tablet");
			svm.setBus("usb");
		}

		// create domain and show some info
		try {
			Domain testDomain = conn.domainCreateLinux(
					Kvm.emitDomain(svm.getHashMap()), 0);
			System.out
					.println("Domain:" + testDomain.getName() + " id "
							+ testDomain.getID() + " running "
							+ testDomain.getOSType());
		} catch (LibvirtException ex) {
			log.error("Create domain failed", ex);
		}

	}
}
