package nova.agent.appliance;

import java.io.IOException;

import sun.net.ftp.FtpProtocolException;

public abstract class ApplianceFetcher {

    public abstract void fetch(Appliance app) throws IOException,
            FtpProtocolException;

}
