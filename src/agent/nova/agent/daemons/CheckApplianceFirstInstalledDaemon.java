package nova.agent.daemons;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import nova.agent.NovaAgent;
import nova.agent.appliance.Appliance;
import nova.common.service.SimpleAddress;
import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;
import nova.master.api.MasterProxy;

import org.apache.log4j.Logger;

/**
 * Check if all the apps is installed daemon. Start when vm start and it has
 * apps to install. Stop when user fisrt request apps has already installed
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class CheckApplianceFirstInstalledDaemon extends SimpleDaemon {
    Logger log = Logger.getLogger(CheckApplianceFirstInstalledDaemon.class);

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
     * Check time interval
     */
    public static final long CHECK_INTERVAL = 1000;

    private String[] appsInstalled;

    public CheckApplianceFirstInstalledDaemon(String[] args) {
        super(CHECK_INTERVAL);
        this.appsInstalled = args;
    }

    @Override
    protected void workOneRound() {
        ConcurrentHashMap<String, Appliance> appliancesMaster = NovaAgent
                .getInstance().getAppliances();
        boolean flag = true;
        for (String appName : appsInstalled) {
            if (!appliancesMaster.get(appName).getStatus()
                    .equals(Appliance.Status.INSTALLED)) {
                flag = false;
                break;
            }
        }

        // all apps is installed
        if (flag == true) {
            // eject the iso loaded in vm
            if (isWindows()) {
                // TODO windows eject
            } else if (isUnix()) {
                try {
                    // eject the iso in linux
                    String[] cmd = new String[] { "/bin/sh", "-c", "eject" };
                    Runtime.getRuntime().exec(cmd);
                    MasterProxy proxy = NovaAgent.getInstance().getMaster();
                    proxy.sendAppliancesFirstInstalledMessage(new SimpleAddress(
                            Conf.getString("agent.bind_host"), Conf
                                    .getInteger("agent.bind_port")));
                } catch (IOException e) {
                    log.error("Can't eject the cdrom!", e);
                }
            }
            // stop the check workRound
            this.stopWork();
            log.info("Check first installed finished!");
        }
    }
}
