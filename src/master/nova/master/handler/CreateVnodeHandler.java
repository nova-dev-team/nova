package nova.master.handler;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

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

public class CreateVnodeHandler implements SimpleHandler<CreateVnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(CreateVnodeMessage.class);

    @Override
    public void handleMessage(CreateVnodeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {

        /**
         * for debug
         */
        log.info("entering create vnode handler");
        log.info("hypervisor to be created: " + msg.hypervisor);

        // send create vcluster message if only one vnode is to be created
        if (msg.is_one) {
            new CreateVclusterHandler().handleMessage(
                    new CreateVclusterMessage(msg.vmName, 1, msg.user_id), null,
                    null, null);
        }

        // select the cluster of myself
        Vcluster vcluster = new Vcluster();
        for (Vcluster vc : Vcluster.all()) {
            vcluster = vc;
        }

        SimpleAddress vAddr = new SimpleAddress(Utils.integerToIpv4(
                (Utils.ipv4ToInteger(vcluster.getFristIp()) + msg.ipOffset)),
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
        WorkerProxy wp = new WorkerProxy(
                new SimpleAddress(Conf.getString("master.bind_host"),
                        Conf.getInteger("master.bind_port")));

        wp.connect(new InetSocketAddress(pnode.getIp(),
                Conf.getInteger("worker.bind_port")));
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

        // send start vnode request (to client)
        wp.sendStartVnode(msg.hypervisor, msg.vmName, vAddr,
                String.valueOf(msg.memorySize), String.valueOf(msg.cpuCount),
                msg.vmImage, true, apps, ipAddr, subnetMask, gateWay,
                String.valueOf(vnode.getId()), msg.isvim);

    }
}
