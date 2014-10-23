package nova.master.handler;

import java.io.IOException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.master.api.messages.PnodeCreateVnodeMessage;
import nova.master.models.Vnode;
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

    @Override
    public void handleMessage(PnodeCreateVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        String masterIP = Conf.getString("master.bind_host");
        int masterPort = Utils.getFreePort();
        portMP(masterIP, masterPort, msg.PnodeIP, msg.VnodePort, msg.VnodeId);
        Utils.MASTER_VNC_MAP.put(String.valueOf(msg.VnodeId),
                String.valueOf(masterPort));
        Vnode vnode = Vnode.findById(msg.VnodeId);
        vnode.setUuid(msg.vnodeuuid);
        vnode.save();
    }

    private void portMP(String srcIP, int srcPort, String dstIP, int dstPort,
            long vnodeid) {
        String strcmd = "ssh -o StrictHostKeyChecking=no -CNfg -L " + srcPort
                + ":localhost:" + dstPort + " root@" + dstIP;
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

}
