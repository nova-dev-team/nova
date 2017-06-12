package nova.master.handler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.master.api.messages.CreateVclusterMessage;
import nova.master.api.messages.CreateVnodeMessage;
import nova.master.models.Pnode;
import nova.master.models.Vcluster;
import nova.master.models.Vnode;
import nova.worker.api.WorkerProxy;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class CreateVnodeHandler implements SimpleHandler<CreateVnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(CreateVnodeMessage.class);

    /**
     * create nfs directory for lxc guest
     * 
     * @author Tianyu Chen
     * @param name
     *            name of guest container
     * @param ip
     *            ip addr of worker node
     * @param imageFileName
     *            file name of rootfs tarball
     * @return the full path of the guest directory
     */
    private String lxcCreateGuestDir(String name, String imageFileName) {
        String nfsBaseDir = Utils.pathJoin(Conf.getString("storage.pnfs.root"),
                "run");
        // for debug
        log.info("nfs base dir: " + nfsBaseDir);

        File guestDir = new File(Utils.pathJoin(nfsBaseDir, name));
        if (!guestDir.exists()) {
            guestDir.mkdirs();
        }

        File rootFs = new File(Utils.pathJoin(guestDir.getAbsolutePath(),
                "rootfs"));
        if (!rootFs.exists()) {
            String extractCmd = "tar -C " + guestDir.getAbsolutePath()
                    + " -xf " + Utils.pathJoin(nfsBaseDir, imageFileName);
            log.info("extract cmd: " + extractCmd);

            // extract the root fs tarball into the target directory
            try {
                Process extract = Runtime.getRuntime().exec(extractCmd);
                if (extract.waitFor() != 0) {
                    log.error("extract cmd returns non-zero! ");
                }
            } catch (IOException ioe) {
                log.error("io exception when trying to extract tarball! ");
                ioe.printStackTrace();
            } catch (InterruptedException ie) {
                log.error("extract cmd terminated unexpectedly! ");
                ie.printStackTrace();
            }
        }

        return guestDir.getAbsolutePath();
    }

    @Override
    public void handleMessage(CreateVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

        /**
         * for debug
         */
        log.info("entering create vnode handler");
        log.info("hypervisor to be created: " + msg.hypervisor);

        // send create vcluster message if only one vnode is to be created
        if (msg.is_one) {
            new CreateVclusterHandler().handleMessage(
                    new CreateVclusterMessage(msg.vmName, 1, msg.user_id,
                            msg.vmName), null, null, null);
        }

        // select the cluster of myself
        Vcluster vcluster = new Vcluster();
        for (Vcluster vc : Vcluster.all()) {
            vcluster = vc;
        }

        SimpleAddress vAddr = new SimpleAddress(Utils.integerToIpv4((Utils
                .ipv4ToInteger(vcluster.getFristIp()) + msg.ipOffset)),
                Conf.getInteger("worker.bind_port"));

        int pid = msg.pnodeId;

        Vnode vnode = new Vnode();
        vnode.setAddr(vAddr);
        vnode.setPmachineId(pid);
        vnode.setVclusterId((int) vcluster.getId());
        vnode.setName(msg.vmName);
        vnode.setCpuCount(msg.cpuCount);
        vnode.setMemorySize(msg.memorySize);
        vnode.setSoftList(msg.applianceList);
        vnode.setCdrom(msg.vmImage);
        vnode.setStatus(Vnode.Status.PREPARING);
        vnode.setHypervisor(msg.hypervisor);
        vnode.setUserId(msg.user_id);
        vnode.save();
        log.info("Created new vnode: " + vnode.getIp());

        Pnode pnode = Pnode.findById(pid);
        log.info("Pnode ip addr: " + pnode.getIp());
        WorkerProxy wp = new WorkerProxy(new SimpleAddress(
                Conf.getString("master.bind_host"),
                Conf.getInteger("master.bind_port")));

        wp.connect(new InetSocketAddress(pnode.getIp(), Conf
                .getInteger("worker.bind_port")));
        String[] apps;
        if (msg.applianceList != null && !msg.applianceList.equals("")) {
            apps = msg.applianceList.split("%2C");
        } else {
            apps = null;
        }

        String ipAddr = vAddr.getIp();
        String subnetMask = Conf.getString("vnode.subnet_mask");
        String gateWay = Conf.getString("vnode.gateway_ip");

        /**
         * for debug
         */
        log.info("message handled by master: v=" + msg.isvim + " h="
                + msg.hypervisor + " n=" + msg.vmName + " a=" + vAddr + " m="
                + String.valueOf(msg.memorySize) + " c="
                + String.valueOf(msg.cpuCount) + " i=" + msg.vmImage + " app="
                + apps + " ip=" + ipAddr + " sm=" + subnetMask + " g="
                + gateWay);
        // copy a ssh pair
        if (Utils.isUnix()) {
            wp.sendObtainSshKeys(msg.vClusterName, msg.vmName);
        }

        // do extraction of root file system
        if (msg.hypervisor.equalsIgnoreCase("lxc")) {
            this.lxcCreateGuestDir(msg.vmName, msg.vmImage);
        }

        // send start vnode request (to client)
        wp.sendStartVnode(msg.hypervisor, msg.vmName, vAddr,
                String.valueOf(msg.memorySize), String.valueOf(msg.cpuCount),
                msg.vmImage, true, apps, ipAddr, subnetMask, gateWay,
                String.valueOf(vnode.getId()), msg.isvim, msg.network);

    }
}
