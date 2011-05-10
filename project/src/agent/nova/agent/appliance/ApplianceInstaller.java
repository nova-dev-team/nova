package nova.agent.appliance;

import java.io.File;
import java.io.IOException;

import nova.agent.NovaAgent;
import nova.common.util.Conf;
import nova.common.util.Utils;

import org.apache.log4j.Logger;

/**
 * Install one appliance
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ApplianceInstaller {
	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(ApplianceInstaller.class);

	public static void install(Appliance app) throws IOException {
		if (NovaAgent.getInstance().getAppliances().get(app.getName())
				.getStatus().equals(Appliance.Status.INSTALL_PENDING)) {
			// Wait for install
			String localPath = Conf.getString("agent.software.save_path");
			String path = Utils.pathJoin(Utils.NOVA_HOME, localPath,
					app.getName());
			File file = new File(path);
			boolean hasAutorun = false; // Whether or not this appliance has a
										// autorun file

			File[] fileList = file.listFiles();
			for (File f : fileList) {
				if (f.getName().equals("autorun.sh")) {
					Runtime.getRuntime().exec(
							Utils.pathJoin(path, "autorun.sh"));
					hasAutorun = true;
					break;
				} else if (f.getName().equals("autorun.bash")) {
					Runtime.getRuntime().exec(
							Utils.pathJoin(path, "autorun.bash"));
					hasAutorun = true;
					break;
				}
			}
			if (!hasAutorun) {
				logger.info("Can't find the autorun file for this appliance: "
						+ path);
				// TODO @gaotao discuss with santa what to do here
			}
		} else {
			// TODO @gaotao discuss with santa what to do here
		}
	}
}
