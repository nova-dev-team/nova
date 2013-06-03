package nova.master.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.master.api.messages.PnodeCreateVnodeMessage;
import nova.worker.models.StreamGobbler;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class PnodeCreateVnodeHandler implements
        SimpleHandler<PnodeCreateVnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger logger = Logger.getLogger(PnodeCreateVnodeMessage.class);

    public static int getFreePort() {
        ServerSocket s = null;
        int MINPORT = 5901;
        int MAXPORT = 6900;
        for (; MINPORT < MAXPORT; MINPORT++) {
            try {
                s = new ServerSocket(MINPORT);
                s.close();
                return MINPORT;
            } catch (IOException e) {
                continue;
            }
        }
        return -1;

    }

    @Override
    public void handleMessage(PnodeCreateVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        String masterIP = Conf.getString("master.bind_host");
        int masterPort = getFreePort();
        // MasterVNCPM.addService(masterIP, masterPort, msg.PnodeIP,
        // msg.VnodePort);
        portMP(masterIP, masterPort, msg.PnodeIP, msg.VnodePort, msg.VnodeId);
        Utils.MASTER_VNC_MAP.put(String.valueOf(msg.VnodeId),
                String.valueOf(masterPort));
    }

    private void portMP(String srcIP, int srcPort, String dstIP, int dstPort,
            long vnodeid) {
        String strcmd = "ssh -CNfg -L " + srcPort + ":" + srcIP + ":" + dstPort
                + " root@" + dstIP;
        try {

            Process p = Runtime.getRuntime().exec(strcmd);
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
                    "ERROR");
            errorGobbler.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
                    "STDOUT");
            outGobbler.start();
            try {
                if (p.waitFor() != 0) {
                    logger.error("add port map for " + vnodeid
                            + " returned abnormal value!");
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("add port map for " + vnodeid
                        + " process terminated!", e);

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("add port map for " + vnodeid + "  cmd error!", e);
        }
    }

    private void delMP(int port) {
        String strcmd = "lsof -i:" + port;
        try {

            Process p = Runtime.getRuntime().exec(strcmd);
            final InputStream is = p.getInputStream();

            new Thread() {
                public void run() {
                    String line, result = "";
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    try {
                        while ((line = br.readLine()) != null) {

                            result = line;
                        }
                        String pid = result.split("[\\t \\n]+")[1];
                        String killcmd = "kill -9 " + pid;
                        Runtime.getRuntime().exec(killcmd);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }.start();

            try {
                if (p.waitFor() != 0) {
                    logger.error("del port map:" + port
                            + " return abnormal value!");
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("del port map:" + port + " terminated!", e);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("del port map:" + port + " cmd error!", e);

        }

    }
}
