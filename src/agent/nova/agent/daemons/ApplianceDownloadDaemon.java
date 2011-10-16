package nova.agent.daemons;

import java.io.IOException;

import nova.agent.NovaAgent;
import nova.agent.appliance.Appliance;
import nova.agent.ui.AgentFrame;
import nova.common.util.SimpleDaemon;

import org.apache.log4j.Logger;

/**
 * Daemon deal with downloading softwares
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ApplianceDownloadDaemon extends SimpleDaemon {

    Logger log = Logger.getLogger(ApplianceDownloadDaemon.class);

    public ApplianceDownloadDaemon() {
        super(100);
    }

    @Override
    protected void workOneRound() {
        for (Appliance app : NovaAgent.getInstance().getAppliances().values()) {
            if (app.getStatus().equals(Appliance.Status.DOWNLOAD_PENDING)) {
                log.info("Found DOWNLOAD_PENDING appliance: " + app.getName());
                try {
                    // AgentFrame downloading status
                    AgentFrame.setInfoDisplayWhenDown("Downloading "
                            + app.getName());

                    app.setStatus(Appliance.Status.DOWNLOADING);

                    NovaAgent.getInstance().getApplianceFetcher().fetch(app);

                    if (app.getStatus().equals(Appliance.Status.DOWNLOADING)) {
                        app.setStatus(Appliance.Status.INSTALL_PENDING);
                    }

                } catch (IOException e) {
                    log.error(
                            "Error downloading appliance, set to DOWNLOAD_FAILURE "
                                    + app.getName(), e);
                    app.setStatus(Appliance.Status.DOWNLOAD_FAILURE);
                }
            }
        }
    }
}
