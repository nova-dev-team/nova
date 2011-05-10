package nova.agent.appliance;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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

	private static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("win") >= 0;
	}

	private static boolean isUnix() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0
				|| os.indexOf("nux") >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Install one appliance
	 * 
	 * @param app
	 *            {@link Appliance}
	 * @throws IOException
	 *             IOException of Runtime.getRuntime().exec()
	 */
	public static void install(Appliance app) throws IOException {
		String relativePath = Conf.getString("agent.software.save_path");
		String folderPath = Utils.pathJoin(Utils.NOVA_HOME, relativePath,
				app.getName());
		// Install statement used in windows
		if (isWindows()) {
			Process p = Runtime.getRuntime().exec("cmd /c autorun.bat",
					new String[0], new File(folderPath));

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				while ((br.readLine()) != null) {
				}
				br.close();
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
<<<<<<< HEAD
			// Install statement used in linux
		} else if (isUnix()) {
			Runtime.getRuntime().exec(Utils.pathJoin(folderPath, "autorun.sh"));
		} else {
			logger.error("Can't find the autorun file for this appliance: "
					+ folderPath);
=======
			if (!hasAutorun) {
				logger.info("Can't find the autorun file for this appliance: "
						+ path);
				// TODO @gaotao discuss with santa what to do here
			}
		} else {
			// TODO @gaotao discuss with santa what to do here
>>>>>>> 0e865314b6104994d2de47bdc74ac2a6905bc84f
		}

	}
}
