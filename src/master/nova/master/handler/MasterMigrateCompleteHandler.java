package nova.master.handler;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.master.api.messages.MasterMigrateCompleteMessage;
import nova.master.models.Pnode;
import nova.master.models.Vnode;
import nova.worker.models.StreamGobbler;

public class MasterMigrateCompleteHandler
        implements SimpleHandler<MasterMigrateCompleteMessage> {

    /**
     * Log4j logger.
     */
    Logger logger = Logger.getLogger(MasterMigrateCompleteMessage.class);

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
                logger.error(
                        "add port map for " + vnodeid + " process terminated!",
                        e);

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("add port map for " + vnodeid + "  cmd error!", e);
        }
    }

    @Override
    public void handleMessage(MasterMigrateCompleteMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        // TODO Auto-generated method stub

        Vnode vnode = Vnode.findByUuid(msg.migrateUuid);
        if (!msg.hypervisor.equalsIgnoreCase("lxc")) {
            String masterIP = Conf.getString("master.bind_host");
            int masterPort = Utils.getFreePort();
            portMP(masterIP, masterPort, msg.dstPnodeIP,
                    Integer.valueOf(msg.dstVNCPort), vnode.getId());
            Utils.MASTER_VNC_MAP.put(String.valueOf(vnode.getId()),
                    String.valueOf(masterPort));
        }
        logger.info("set vnode status to running! ");
        vnode.setStatus(Vnode.Status.RUNNING);
        Pnode dstpnode = Pnode.findByIp(msg.dstPnodeIP);
        vnode.setPmachineId((int) dstpnode.getId());
        vnode.save();
    }

}
