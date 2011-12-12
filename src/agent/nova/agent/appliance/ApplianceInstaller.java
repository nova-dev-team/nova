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
     */
    public static void install(Appliance app) {
        String relativePath = Conf.getString("agent.software.save_path");
        String folderPath = Utils.pathJoin(Utils.NOVA_HOME, relativePath,
                app.getName());
        try {
            executeInstall(folderPath);
        } catch (IOException e) {
            app.setStatus(Appliance.Status.INSTALL_FAILURE);
            logger.error("Can't install " + app.getName(), e);
        }

    }

    /**
     * Install one appliance when first start up this vm
     * 
     * @param app
     *            {@link Appliance}
     * 
     */
    public static void firstInstall(Appliance app) {
        String relativePath = "";
        if (isWindows()) {
            relativePath = Conf.getString("agent.iso.windows.save_path");
        } else {
            relativePath = Conf.getString("agent.iso.linux.save_path");
        }
        String folderPath = Utils.pathJoin(relativePath, "appliances",
                app.getName());
        try {
            logger.info("Installing " + folderPath);
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
            Runtime.getRuntime().exec(
                    "sh " + Utils.pathJoin(folderPath, "autorun.sh"),
                    new String[0], new File(folderPath));
        } else {
            logger.error("Can't find the autorun file for this appliance: "
                    + folderPath);
        }
    }
}
