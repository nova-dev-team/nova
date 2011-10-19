package nova.agent.appliance;

import java.io.IOException;

import nova.agent.NovaAgent;
import nova.common.util.Cancellable;
import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;

import org.apache.log4j.Logger;

import sun.net.ftp.FtpClient;

/**
 * Fetch appliances through ftp
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class FtpApplianceFetcher extends ApplianceFetcher implements
        Cancellable {

    private String hostIp = Conf.getString("agent.ftp.host");
    private int ftpPort = Conf.getInteger("agent.ftp.port");
    private String userName = Conf.getString("agent.ftp.user_name");
    private String password = Conf.getString("agent.ftp.password");
    private String savePath = Utils.pathJoin(Utils.NOVA_HOME,
            Conf.getString("agent.software.save_path"));
    private String applianceName = null;

    /**
     * Log4j logger.
     */
    static Logger logger = Logger.getLogger(FtpApplianceFetcher.class);

    public FtpApplianceFetcher() {
    }

    @Override
    public synchronized void fetch(Appliance app) throws IOException {
        this.applianceName = app.getName();
        FtpClient fc = FtpUtils.connect(hostIp, ftpPort, userName, password);
        FtpUtils.downloadDir(fc, "/appliances/" + app.getName(),
                Utils.pathJoin(savePath, app.getName()), this);
        if (isCancelled()) {
            NovaAgent.getInstance().getAppliances().get(app.getName())
                    .setStatus(Appliance.Status.NOT_INSTALLED);
            Utils.rmdir(Utils.pathJoin(this.savePath, app.getName()));
        }
        try {
            fc.closeServer();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Failed to close connection to FTP server", e);
        }
    }

    @Override
    public void cancel() {
        NovaAgent.getInstance().getAppliances().get(this.applianceName)
                .setStatus(Appliance.Status.CANCELLED);
    }

    /**
     * Judge if the downloading appliance is cancelled
     * 
     * @return {@link Boolean}
     */
    public boolean isCancelled() {
        return NovaAgent.getInstance().getAppliances().get(this.applianceName)
                .getStatus().equals(Appliance.Status.CANCELLED);
    }
}
