package nova.worker.virt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	static Logger log = Logger.getLogger(Kvm.class);

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
		File foder = new File(Utils.pathJoin(Utils.NOVA_HOME, "run", params
				.get("name").toString()));
		File file = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
				params.get("name").toString(), "linux.img"));
		if (!foder.exists()) {
			foder.mkdirs();
		} else {
			// TODO @santa rename or stop or what?
			log.error("vm name " + params.get("name").toString()
					+ " has been used!");
		}
		if (file.exists() == false) {
			try {
				System.out.println("copying file");
				String sourceUrl = Utils.pathJoin(Utils.NOVA_HOME, "run",
						"linux.img");
				String destUrl = Utils.pathJoin(Utils.NOVA_HOME, "run", params
						.get("name").toString(), "linux.img");
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
				params.put("sourceFile", destUrl);
			} catch (IOException e) {
				log.error("copy image fail", e);
			}
		} else {
			params.put("sourceFile", Utils.pathJoin(Utils.NOVA_HOME, "run",
					params.get("name").toString(), "linux.img"));
		}

		if ((params.get("cdimg") != null)
				&& (!params.get("cdimg").toString().equals(""))) {
			params.put("bootDevice", "cdrom");
		} else {
			params.put("bootDevice", "hd");
		}

		if ("true".equals(params.get("runAgent"))) {
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
			params.put("determinVnc", "");
		}

		return Utils.expandTemplateFile(templateFpath, params);
	}
}
