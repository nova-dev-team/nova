package nova.test.functional.worker;

import java.io.IOException;

import nova.agent.NovaAgent;
import nova.agent.appliance.Appliance;
import nova.agent.appliance.FtpApplianceFetcher;
import nova.storage.NovaStorage;

import org.junit.Test;

import sun.net.ftp.FtpProtocolException;

public class TestFetchFtp {
    @Test
    public void testFetchFtp() {

        NovaStorage.getInstance().startFtpServer();

        FtpApplianceFetcher fp = new FtpApplianceFetcher();
        try {

            Appliance app = new Appliance("picture");
            NovaAgent.getInstance().getAppliances().put("picture", app);
            // app.setStatus(Status.CANCELLED);
            try {
                fp.fetch(app);
            } catch (FtpProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        NovaStorage.getInstance().shutdown();
        // fp.deleteDir("", "picture");
    }
}
