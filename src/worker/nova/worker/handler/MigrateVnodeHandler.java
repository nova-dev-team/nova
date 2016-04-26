package nova.worker.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Utils;
import nova.worker.NovaWorker;
import nova.worker.api.messages.MigrateVnodeMessage;

/**
 * worker side migration handler
 * 
 * @author Tianyu Chen
 *
 */
public class MigrateVnodeHandler implements SimpleHandler<MigrateVnodeMessage> {

    /**
     * band width during migration
     */
    private long bandWidth = 100;

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(MigrateVnodeHandler.class);

    private void checkpointProcessInContainer(String containerName)
            throws IOException, InterruptedException {
        // for debug
        log.info("container name: " + containerName);

        // check dependencies, refuse to checkpoint if requirements are not met
        Process checkDepd = Runtime.getRuntime().exec("which sshpass");
        if (checkDepd.waitFor() != 0) {
            log.error("checkpoint dependency check failed. ");
            return;
        }

        // get ip address of the container to migrate
        String uri = "lxc:///";
        String[] getIpAddrCmd = new String[3];
        getIpAddrCmd[0] = Utils.pathJoin(Utils.NOVA_HOME, "nova-vmaddrctl");
        getIpAddrCmd[1] = containerName;
        getIpAddrCmd[2] = uri;
        Process getIpAddr = Runtime.getRuntime().exec(getIpAddrCmd);
        if (getIpAddr.waitFor() != 0) {
            log.error("get guest ip addr failed. ");
            return;
        }
        BufferedReader stdOutReader = new BufferedReader(
                new InputStreamReader(getIpAddr.getInputStream()));
        String ipAddr = stdOutReader.readLine();
        if (ipAddr == null) {
            log.error("null ip addr. ");
            return;
        }
        InetAddressValidator validator = InetAddressValidator.getInstance();
        if (!validator.isValidInet4Address(ipAddr)) {
            log.error("invalid ip addr. ");
            return;
        }
        // for debug
        log.info("ip addr of container " + containerName + " is " + ipAddr);

        // login onto the container and replay the command inside the container
        String passwd = "940715";
        String user = "root";
        String replayCmd = "uname -a";
        String sshReplayCmd = "sshpass -p " + passwd + " ssh " + user + "@"
                + ipAddr + " " + replayCmd;
        // for debug
        log.info("cmd to replay: " + sshReplayCmd);
        Process replay = Runtime.getRuntime().exec(sshReplayCmd);
        if (replay.waitFor() != 0) {
            log.error("replay cmd failed! check ssh and sshpass. ");
            return;
        }
        stdOutReader = new BufferedReader(
                new InputStreamReader(replay.getInputStream()));
        String uname = stdOutReader.readLine();
        log.info("uname is " + uname);
    }

    @Override
    public void handleMessage(MigrateVnodeMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        try {
            NovaWorker.masteraddr = xreply;
            if (NovaWorker.getInstance().getMaster() == null || NovaWorker
                    .getInstance().getMaster().isConnected() == false) {
                NovaWorker.getInstance().registerMaster(xreply);
            }

            // get connections, these operations are hypervisor dependent
            String duri, suri;
            if (msg.hypervisor.equalsIgnoreCase("kvm")) {
                duri = "qemu+ssh://" + msg.migrateToAddr.getIp() + "/system";
                suri = "qemu:///system";
            } else if (msg.hypervisor.equalsIgnoreCase("lxc")) {
                duri = "lxc+ssh://" + msg.migrateToAddr.getIp();
                suri = "lxc:///";
            } else {
                // the hypervisor is not supported
                log.error("unsupported hypervisor migration! ");
                return;
            }

            // connect to destination (remote) machine
            Connect dconn = new Connect(duri);
            if (!dconn.isConnected()) {
                log.error("connect to destination failed! migration aborted");
                return;
            }
            // connect to source (local) machine
            Connect sconn = NovaWorker.getInstance().getConn(suri, false);
            if (!sconn.isConnected()) {
                log.error("connect to source failed! migration aborted");
                return;
            }

            // get source domain
            Domain srcDomain = sconn.domainLookupByUUIDString(msg.vnodeUuid);
            log.info("uuid of domain to migrate is " + msg.vnodeUuid);

            String strPort = null;
            if (msg.hypervisor.equalsIgnoreCase("kvm")) {
                long flag = 0;
                String uri = null;
                Domain dstDomain = srcDomain.migrate(dconn, flag,
                        srcDomain.getName(), uri, bandWidth);
                String strXML = dstDomain.getXMLDesc(0);
                int vncpos = strXML.indexOf("graphics type='vnc' port='");
                strPort = strXML.substring(vncpos + 26, vncpos + 30);
                log.info("port: " + strPort);
            } else if (msg.hypervisor.equalsIgnoreCase("lxc")) {
                // for debug
                // !!! TBD !!!
                log.info("entering debug code...");
                try {
                    this.checkpointProcessInContainer(msg.vnodeName);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                return;

                // // do migration here
                // // get the xml definition of the src domain
                // String xml = srcDomain.getXMLDesc(0);
                // // if domain is active, shut it down first
                // if (srcDomain.isActive() == 1) {
                // srcDomain.destroy();
                // }
                // // undefine the source domain
                // srcDomain.undefine();
                // Domain dstDomain = dconn.domainDefineXML(xml);
                // dstDomain.create();
            } else {
                log.error("unsupported hypervisor migration! ");
                return;
            }
            NovaWorker.getInstance().getMaster().sendMigrateComplete(
                    msg.vnodeUuid, msg.migrateToAddr.getIp(), strPort,
                    msg.hypervisor);
        } catch (LibvirtException e1) {
            log.error("migrate error, maybe caused by libvirt ", e1);
        }
    }
}
