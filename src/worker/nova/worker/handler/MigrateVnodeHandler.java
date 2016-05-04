package nova.worker.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    /**
     * checkpoint the target process inside the container on the source machine
     * 
     * @param containerName
     *            the source container
     * @param processName
     *            the target process
     * @throws IOException
     * @throws InterruptedException
     */
    private void checkpointProcessInContainer(String containerName,
            String processName) throws IOException, InterruptedException {
        // for debug
        log.info("checkpoint! container name: " + containerName
                + "; process name: " + processName);

        String checkpointStr = Utils.pathJoin(Utils.NOVA_HOME, "lxc-cr-wrapper")
                + " checkpoint -c " + containerName + " -p " + processName;
        Process checkpointProcess = Runtime.getRuntime().exec(checkpointStr);
        if (checkpointProcess.waitFor() != 0) {
            log.error("checkpoint failed! ");
            BufferedReader stdOutReader = new BufferedReader(
                    new InputStreamReader(checkpointProcess.getInputStream()));
            BufferedReader stdErrReader = new BufferedReader(
                    new InputStreamReader(checkpointProcess.getErrorStream()));
            String line;
            while ((line = stdOutReader.readLine()) != null) {
                log.info(line);
            }
            while ((line = stdErrReader.readLine()) != null) {
                log.info(line);
            }
            return;
        }
    }

    /**
     * restore the target process from the dump files inside container on the
     * destination machine
     * 
     * @param dstIpAddr
     *            the ip address of the destination machine
     * @param containerName
     *            the name of the container that contains the dumped files
     * @param processName
     *            the name of the target process, which is considered
     *            mission-critical
     * @throws IOException
     * @throws InterruptedException
     */
    private void restoreProcessInContainer(String dstIpAddr,
            String containerName, String processName)
            throws IOException, InterruptedException {
        // for debug
        log.info("restore! dst ip: " + dstIpAddr + "; container name: "
                + containerName + "; process name: " + processName);

        String restoreStr = "ssh " + dstIpAddr + " "
                + Utils.pathJoin(Utils.NOVA_HOME, "lxc-cr-wrapper")
                + " restore -c " + containerName + " -p " + processName;
        log.info("cmd: " + restoreStr);
        Process restoreProcess = Runtime.getRuntime().exec(restoreStr);
        if (restoreProcess.waitFor() != 0) {
            log.error("restore failed! ");
            BufferedReader stdOutReader = new BufferedReader(
                    new InputStreamReader(restoreProcess.getInputStream()));
            BufferedReader stdErrReader = new BufferedReader(
                    new InputStreamReader(restoreProcess.getErrorStream()));
            String line;
            while ((line = stdOutReader.readLine()) != null) {
                log.info(line);
            }
            while ((line = stdErrReader.readLine()) != null) {
                log.info(line);
            }
            return;
        }
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
                // do snapshot
                try {
                    this.checkpointProcessInContainer(msg.vnodeName, "toy.py");
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                // do migration here
                // get the xml definition of the src domain
                String xml = srcDomain.getXMLDesc(0);
                // if domain is active, shut it down first
                if (srcDomain.isActive() == 1) {
                    srcDomain.destroy();
                }
                // undefine the source domain
                srcDomain.undefine();
                Domain dstDomain = dconn.domainDefineXML(xml);
                // create destination domain
                dstDomain.create();
                log.info("dst domain created. ");
                try {
                    // sleep for a while...
                    Thread.sleep(1000);
                    log.info("restoring process in destination domain... ");
                    this.restoreProcessInContainer(msg.migrateToAddr.getIp(),
                            msg.vnodeName, "toy.py");
                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else {
                log.error("unsupported hypervisor migration! ");
                return;
            }
            log.info("send back migration complete message to master! ");
            NovaWorker.getInstance().getMaster().sendMigrateComplete(
                    msg.vnodeUuid, msg.migrateToAddr.getIp(), strPort,
                    msg.hypervisor);
            log.info("migration completed. check process on destination. ");
        } catch (LibvirtException e1) {
            log.error("migrate error, maybe caused by libvirt ", e1);
        }
    }
}
