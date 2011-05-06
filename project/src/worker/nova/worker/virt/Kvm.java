package nova.worker.virt;

import java.util.HashMap;

import nova.common.util.Utils;
import nova.worker.NovaWorker;

import org.apache.log4j.Logger;

/**
 * Interfacing to the KVM hypervisor.
 * 
 * @author santa
 * 
 */
public class Kvm {

	/**
	 * Log4j logger.
	 */
	Logger log = Logger.getLogger(Kvm.class);

	/**
	 * Emit libvirt domain definitions.
	 * 
	 * @param params
	 *            VM parameters.
	 * @return Emitted XML domain definition.
	 */
	public static String emitDomain(HashMap<String, Object> params) {
		String templateFpath = Utils.pathJoin(Utils.NOVA_HOME, "conf", "virt",
				"kvm-domain-template.xml");
		if ((params.get("cdimg") != null)
				&& (!params.get("cdimg").toString().equals(""))) {
			params.put("bootDevice", "cdrom");
		} else {
			params.put("bootDevice", "hd");
		}

		if (params.get("runAgent").equals("true")) {
			params.put("cdromPath", Utils.pathJoin(Utils.NOVA_HOME,
					params.get("name").toString(), "agent-cd.iso"));
			params.put("determinCdrom", "<disk type='file' device='cdrom'>"
					+ "\n    <source file='"
					+ params.get("cdromPath").toString() + "'/>"
					+ "\n    <target dev='hdc'/>" + "\n  </disk>");
		} else if ((params.get("cdimg") != null)
				&& (!params.get("cdimg").toString().equals(""))) {
			params.put("cdromPath", Utils.pathJoin(Utils.NOVA_HOME,
					params.get("name").toString(), params.get("cdImage")
							.toString()));
			params.put("determinCdrom", "<disk type='file' device='cdrom'>"
					+ "\n    <source file='"
					+ params.get("cdromPath").toString() + "'/>"
					+ "\n    <target dev='hdc'/>" + "\n    <readonly/>"
					+ "\n  </disk>");
		} else {
			params.put("determinCdrom", "");
		}

		String vmNetworkInterface = NovaWorker.getInstance().getConf()
				.getString("vm_network_interface");
		String vmNetworkBridge = NovaWorker.getInstance().getConf()
				.getString("vm_network_bridge");
		String fixVncMousePointer = NovaWorker.getInstance().getConf()
				.getString("fix_vnc_mouse_pointer");

		if ((!vmNetworkInterface.equals("")) && (!vmNetworkBridge.equals(""))) {
			params.put("interfaceType", "bridge");
			params.put("sourceBridge", vmNetworkBridge);
			params.put(
					"macAddress",
					"54:7E:" + Integer.toHexString((int) (Math.random() * 256))
							+ ":"
							+ Integer.toHexString((int) (Math.random() * 256))
							+ ":"
							+ Integer.toHexString((int) (Math.random() * 256))
							+ ":"
							+ Integer.toHexString((int) (Math.random() * 256)));
			params.put(
					"determinNetwork",
					"<interface type='"
							+ params.get("interfaceType").toString() + "'>"
							+ "\n    <source bridge='"
							+ params.get("sourceBridge").toString() + "'/>"
							+ "\n    <mac address='"
							+ params.get("macAddress").toString() + "'/>"
							+ "\n  </interface>");
		} else {
			params.put("interfaceType", "network");
			params.put("sourceNetwork", "default");
			params.put(
					"determinNetwork",
					"<interface type='"
							+ params.get("interfaceType").toString()
							+ "'><source network='"
							+ params.get("sourceNetwork").toString()
							+ "'/></interface>");
		}

		if (fixVncMousePointer.equals("true")) {
			params.put("inputType", "tablet");
			params.put("bus", "usb");
			params.put("determinVnc", "<input type='"
					+ params.get("inputType").toString() + "' bus='"
					+ params.get("bus").toString() + "'/>");
		} else {

		}

		return Utils.expandTemplateFile(templateFpath, params);
	}
}
