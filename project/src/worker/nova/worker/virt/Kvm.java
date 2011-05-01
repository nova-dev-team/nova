package nova.worker.virt;

import java.util.Map;

import nova.common.util.Utils;

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
	public static String emitDomain(Map<String, Object> params) {
		String templateFpath = Utils.pathJoin(Utils.NOVA_HOME, "conf", "virt",
				"kvm-domain-template.xml");
		return Utils.expandTemplateFile(templateFpath, params);
	}

}
