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

			File[] fileList = file.listFiles();
			for (File f : fileList) {
				if (f.getName().equals("autorun.sh")) {
					Runtime.getRuntime().exec(
							Utils.pathJoin(path, "autorun.sh"));
				} else if (f.getName().equals("autorun.bash")) {
					Runtime.getRuntime().exec(
							Utils.pathJoin(path, "autorun.bash"));
				} else {
					logger.error("Can't install " + app.getName()
							+ "! Because there is no autorun file");
					// TODO by gaotao maybe delete this appliance
				}
			}
		} else {
			// TODO discuss with santa what to do here
		}
	}
}
