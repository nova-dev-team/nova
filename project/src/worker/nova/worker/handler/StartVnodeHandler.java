package nova.worker.handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.worker.api.messages.StartVnodeMessage;

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
			this.vAddr = vAddr;
		}

		/**
		 * Basic information required to start a new vnode.
		 */
		public SimpleAddress vAddr;

	}

	/**
	 * Params for new vnode config
	 * 
	 * @author shayf
	 * 
	 */
	public static class Params {
		static String hyperVisor;
		static String name;
		static String machine;
		static String uuid;
		static String memSize;
		static String cpuCount;
		static String arch;
		static String bootDevice;
		static String cdImage;
		static String hdaImage;
		static String runAgent;
		static String emulatorPath;
		static String cdromPath;
		static String interfaceType;
		static String sourcebridge;
		static String macAddress;
		static String sourceNetwork;
		static String inputType;

		public static String getInputType() {
			return inputType;
		}

		public static void setInputType(String inputType) {
			Params.inputType = inputType;
		}

		public static String getBus() {
			return bus;
		}

		public static void setBus(String bus) {
			Params.bus = bus;
		}

		static String bus;

		public static String getInterfaceType() {
			return interfaceType;
		}

		public static void setInterfaceType(String interfaceType) {
			Params.interfaceType = interfaceType;
		}

		public static String getSourceNetwork() {
			return sourceNetwork;
		}

		public static void setSourceNetwork(String sourceNetwork) {
			Params.sourceNetwork = sourceNetwork;
		}

		public static String getMacAddress() {
			return macAddress;
		}

		public static void setMacAddress(String macAddress) {
			Params.macAddress = macAddress;
		}

		public static String getSourcebridge() {
			return sourcebridge;
		}

		public static void setSourcebridge(String sourcebridge) {
			Params.sourcebridge = sourcebridge;
		}

		public static String getCdromPath() {
			return cdromPath;
		}

		public static void setCdromPath(String cdromPath) {
			Params.cdromPath = cdromPath;
		}

		public static String getEmulatorPath() {
			return emulatorPath;
		}

		public static void setEmulatorPath(String emulatorPath) {
			Params.emulatorPath = emulatorPath;
		}

		public static String getHyperVisor() {
			return hyperVisor;
		}

		public static void setHyperVisor(String hyperVisor) {
			Params.hyperVisor = hyperVisor;
		}

		public static String getMachine() {
			return machine;
		}

		public static void setMachine(String machine) {
			Params.machine = machine;
		}

		public static String getName() {
			return name;
		}

		public static void setName(String name) {
			Params.name = name;
		}

		public static String getUuid() {
			return uuid;
		}

		public static void setUuid(String uuid) {
			Params.uuid = uuid;
		}

		public static String getMemSize() {
			return memSize;
		}

		public static void setMemSize(String memSize) {
			Params.memSize = memSize;
		}

		public static String getCpuCount() {
			return cpuCount;
		}

		public static void setCpuCount(String cpuCount) {
			Params.cpuCount = cpuCount;
		}

		public static String getArch() {
			return arch;
		}

		public static void setArch(String arch) {
			Params.arch = arch;
		}

		public static String getBootDevice() {
			return bootDevice;
		}

		public static void setBootDevice(String bootDevice) {
			Params.bootDevice = bootDevice;
		}

		public static String getCdImage() {
			return cdImage;
		}

		public static void setCdImage(String cdImage) {
			Params.cdImage = cdImage;
		}

		public static String getHdaImage() {
			return hdaImage;
		}

		public static void setHdaImage(String hdaImage) {
			Params.hdaImage = hdaImage;
		}

		public static String getRunAgent() {
			return runAgent;
		}

		public static void setRunAgent(String runAgent) {
			Params.runAgent = runAgent;
		}

		public static String getConfigString(String str) {
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("hyperVisor", hyperVisor);
			values.put("name", name);
			values.put("uuid", uuid);
			values.put("memSize", memSize);
			values.put("cpuCount", cpuCount);
			values.put("arch", arch);
			values.put("bootDevice", bootDevice);
			values.put("cdImage", cdImage);
			values.put("hdaImage", hdaImage);
			values.put("runAgent", runAgent);
			values.put("emulatorPath", emulatorPath);
			values.put("cdromPath", cdromPath);
			values.put("interfaceType", interfaceType);
			values.put("sourcebridge", sourcebridge);
			values.put("macAddress", macAddress);
			values.put("sourceNetwork", sourceNetwork);
			values.put("inputType", inputType);

			return Utils.expandTemplate(str, values);
		}

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

		// find conf file, currently using test-domain-template.xml
		BufferedReader br;
		String tmp = null;
		try {
			String filePath = Utils.pathJoin(Utils.NOVA_HOME, "conf", "virt",
					"kvm-domain-template.xml");
			br = new BufferedReader(new FileReader(filePath));

			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			tmp = sb.toString();
			br.close();
		} catch (FileNotFoundException ex) {
			log.error("Error loading vnode domain template file", ex);
		} catch (IOException ex) {
			log.error("Error loading vnode domain template file", ex);
		}

		// TODO @shayf get real config value from files
		Params.setHyperVisor("kvm");
		Params.setName("vm1");
		Params.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
		Params.setMemSize("524288");
		Params.setCpuCount("1");
		Params.setArch("i686");
		Params.setCdImage("");
		if ((Params.getCdImage() != null) && (Params.getCdImage() != "")) {
			Params.setBootDevice("cdrom");
		} else {
			Params.setBootDevice("hd");
		}
		if (Params.getHyperVisor() == "kvm") {
			Params.setEmulatorPath("/usr/bin/kvm");
		} else if (Params.getHyperVisor() == "xen") {
			// TODO @shayf update path
			Params.setEmulatorPath("some other path");
		} else {
			// TODO @shayf update path
			Params.setEmulatorPath("some other path");
		}
		Params.setRunAgent("false");
		if (Params.getRunAgent() == "true") {
			Params.setCdromPath(Utils.pathJoin(Utils.NOVA_HOME,
					Params.getName(), "agent-cd.iso"));
		} else if ((Params.getCdImage() != null) && (Params.getCdImage() != "")) {
			Params.setCdromPath(Utils.pathJoin(Utils.NOVA_HOME,
					Params.getName(), Params.getCdImage()));
		}

		// TODO @shayf read these values from some conf file
		String vmNetworkInterface = "";
		String vmNetworkBridge = "";
		String fixVncMousePointer = "true";
		if ((vmNetworkInterface != "") && (vmNetworkBridge != "")) {
			Params.setInterfaceType("bridge");
			Params.setSourcebridge(vmNetworkBridge);
			Params.setMacAddress(Integer.toHexString((int) (Math.random() * 256))
					+ ":"
					+ Integer.toHexString((int) (Math.random() * 256))
					+ ":"
					+ Integer.toHexString((int) (Math.random() * 256))
					+ ":"
					+ Integer.toHexString((int) (Math.random() * 256))
					+ ":"
					+ Integer.toHexString((int) (Math.random() * 256))
					+ ":" + Integer.toHexString((int) (Math.random() * 256)));
		} else {
			Params.setInterfaceType("network");
			Params.setSourceNetwork("default");
		}
		if (fixVncMousePointer == "true") {
			Params.setInputType("tablet");
			Params.setBus("usb");
		} else {
			// TODO @shayf set correct default value
			Params.setInputType("tablet");
			Params.setBus("usb");
		}

		System.out.println(Params.getConfigString(tmp));

		// create domain and show some info
		try {
			// TODO @shayf support both windows and linux
			Domain testDomain = conn.domainCreateLinux(
					Params.getConfigString(tmp), 0);
			System.out
					.println("Domain:" + testDomain.getName() + " id "
							+ testDomain.getID() + " running "
							+ testDomain.getOSType());
		} catch (LibvirtException ex) {
			log.error("Create domain failed", ex);
		}

	}
}
