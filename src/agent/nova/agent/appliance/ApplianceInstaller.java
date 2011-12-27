package nova.agent.appliance;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.worker.models.StreamGobbler;

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
     */
    public static void install(Appliance app) {
        String relativePath = Conf.getString("agent.software.save_path");
        String folderPath = Utils.pathJoin(Utils.NOVA_HOME, relativePath,
                app.getName());
        // if first install we have different folderPath
        if (app.getStatus().equals(Appliance.Status.FIRST_INSTALLING)) {
            if (isWindows()) {
                relativePath = Conf.getString("agent.iso.windows.save_path");
            } else {
                relativePath = Conf.getString("agent.iso.linux.save_path");
            }
            folderPath = Utils.pathJoin(relativePath, "appliances",
                    app.getName());
        }

        try {
            executeInstall(folderPath);
        } catch (IOException e) {
            app.setStatus(Appliance.Status.INSTALL_FAILURE);
            logger.error("Can't install " + app.getName(), e);
        }

    }

    /**
     * Install one appliance
     * 
     * @param folderPath
     *            where find the appliance
     * @throws IOException
     *             IOException of Runtime.getRuntime().exec()
     */
    private static void executeInstall(String folderPath) throws IOException {
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

            // Install statement used in linux
        } else if (isUnix()) {
            String cmd = "sh " + Utils.pathJoin(folderPath, "autorun.sh");
            Process p;
            try {
                p = Runtime.getRuntime().exec(cmd);
                StreamGobbler errorGobbler = new StreamGobbler(
                        p.getErrorStream(), "ERROR");
                errorGobbler.start();
                StreamGobbler outGobbler = new StreamGobbler(
                        p.getInputStream(), "STDOUT");
                outGobbler.start();
                try {
                    if (p.waitFor() != 0) {
                        logger.info(folderPath + " Install failure!");
                    }
                } catch (InterruptedException e1) {
                    logger.info(folderPath + " Install failure!", e1);
                }
            } catch (IOException e1) {
                logger.info(folderPath + " Install failure!", e1);
            }
        } else {
            logger.error("Can't find the autorun file for this appliance: "
                    + folderPath);
        }
    }
}
