package nova.master.handler;

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

    @Override
    public void handleMessage(CreateVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

        if (msg.is_one) {
            new CreateVclusterHandler().handleMessage(
                    new CreateVclusterMessage(msg.vmName, 1), null, null, null);
        }

        // TODO Auto-generated method stub
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
        System.out
                .println("======================================================================================================");
        vnode.save();
        log.info("Created new vnode: " + vnode.getIp());

        Pnode pnode = Pnode.findById(pid);
        System.out.println(pnode.getIp());
        WorkerProxy wp = new WorkerProxy(new SimpleAddress(
                Conf.getString("master.bind_host"),
                Conf.getInteger("master.bind_port")));

        // @ zhaoxun to do...
        wp.connect(new InetSocketAddress(pnode.getIp(), Conf
                .getInteger("worker.bind_port")));
        /*
         * System.out.println("kvm" + vAddr + "true" +
         * String.valueOf(msg.memorySize) + String.valueOf(msg.cpuCount) +
         * msg.vmImage + "false");
         */
        // TODO @zhaoxun pass the correct params
        String[] apps;
        if (msg.applianceList != null && !msg.applianceList.equals("")) {
            apps = msg.applianceList.split("%2C");
        } else {
            apps = null;
        }

        String ipAddr = vAddr.getIp();
        String subnetMask = Conf.getString("vnode.subnet_mask");
        String gateWay = Conf.getString("vnode.gateway_ip");

        System.out.println(msg.hypervisor + " " + msg.vmName + " " + vAddr
                + " " + String.valueOf(msg.memorySize) + " "
                + String.valueOf(msg.cpuCount) + " " + msg.vmImage + " " + true
                + " " + apps + " " + ipAddr + " " + subnetMask + " " + gateWay);
        // copy a ssh pair
        if (Utils.isUnix()) {
            wp.sendObtainSshKeys(msg.vClusterName, msg.vmName);
        }

        wp.sendStartVnode(msg.hypervisor, msg.vmName, vAddr,
                String.valueOf(msg.memorySize), String.valueOf(msg.cpuCount),
                msg.vmImage, true, apps, ipAddr, subnetMask, gateWay,
                String.valueOf(vnode.getId()));

    }
}
