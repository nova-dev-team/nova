package nova.worker.daemons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import nova.common.util.Conf;
import nova.common.util.SimpleDaemon;
import nova.common.util.Utils;
import nova.master.api.MasterProxy;
import nova.master.models.Vnode;
import nova.worker.NovaWorker;

/**
 * Daemon thread that reports all current vnodes status to master node.
 * 
 * @author santa
 * 
 */
public class VnodeStatusDaemon extends SimpleDaemon {

    public static final long VNODE_STATUS_INTERVAL = 5000;

    /**
     * fetch virtual machine driver capabilities (in first round)
     */
    private String capabilities = null;

    public VnodeStatusDaemon() {
        super(VNODE_STATUS_INTERVAL);
    }

    /**
     * Log4j logger.
     */
    Logger logger = Logger.getLogger(VnodeStatusDaemon.class);

    public static Map<UUID, Vnode.Status> allStatus = new HashMap<UUID, Vnode.Status>();

    public static void putStatus(UUID uu, Vnode.Status vs) {
        allStatus.put(uu, vs);
    }

    public static void delStatus(UUID uu) {
        allStatus.remove(uu);
    }

    private void getStatus(String hypervisor) throws LibvirtException {
        // get libvirt connection URI
        String virtService = null;
        if (hypervisor.equals("lxc")) {
            virtService = "lxc:///";
        } else {
            virtService = hypervisor + ":///system";
        }

        String[] strs = NovaWorker.getInstance().getConn(virtService, false)
                .listDefinedDomains();
        for (int i = 0; i < strs.length; i++) {
            Domain dom = NovaWorker.getInstance().getConn(virtService, false)
                    .domainLookupByName(strs[i]);
            if (dom != null) {
                UUID uu = UUID.fromString(dom.getUUIDString());
                String info = dom.getInfo().state.toString();
                Vnode.Status vs = null;

                if (info.equalsIgnoreCase("VIR_DOMAIN_BLOCKED")) {
                    // the domain is blocked on resource
                    vs = Vnode.Status.PAUSED;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_CRASHED")) {
                    // the domain is crashed
                    vs = Vnode.Status.CONNECT_FAILURE;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_NOSTATE")) {
                    // the domain has no state
                    vs = Vnode.Status.UNKNOWN;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_PAUSED")) {
                    // the domain is paused by user
                    vs = Vnode.Status.PAUSED;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_RUNNING")) {
                    // the domain is running
                    vs = Vnode.Status.RUNNING;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_SHUTDOWN")) {
                    // the domain is being shut down
                    vs = Vnode.Status.SHUTTING_DOWN;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_SHUTOFF")) {
                    // the domain is shut off
                    vs = Vnode.Status.SHUT_OFF;
                }
                allStatus.put(uu, vs);
            }
        }

        int[] ids = NovaWorker.getInstance().getConn(virtService, false)
                .listDomains();
        for (int i = 0; i < ids.length; i++) {
            Domain dom = NovaWorker.getInstance().getConn(virtService, false)
                    .domainLookupByID(ids[i]);
            if (dom != null) {
                UUID uu = UUID.fromString(dom.getUUIDString());
                String info = dom.getInfo().state.toString();
                Vnode.Status vs = null;

                if (info.equalsIgnoreCase("VIR_DOMAIN_BLOCKED")) {
                    // the domain is blocked on resource
                    vs = Vnode.Status.PAUSED;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_CRASHED")) {
                    // the domain is crashed
                    vs = Vnode.Status.CONNECT_FAILURE;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_NOSTATE")) {
                    // the domain has no state
                    vs = Vnode.Status.UNKNOWN;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_PAUSED")) {
                    // the domain is paused by user
                    vs = Vnode.Status.PAUSED;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_RUNNING")) {
                    // the domain is running
                    vs = Vnode.Status.RUNNING;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_SHUTDOWN")) {
                    // the domain is being shut down
                    vs = Vnode.Status.SHUTTING_DOWN;
                } else if (info.equalsIgnoreCase("VIR_DOMAIN_SHUTOFF")) {
                    // the domain is shut off
                    vs = Vnode.Status.SHUT_OFF;
                }
                allStatus.put(uu, vs);
            }
        }
    }

    /**
     * the method probes the current ip addresses of all defined domains (of a
     * certain kind of hypervisor) and updates the corresponding hash map.
     * 
     * @param hypervisor
     *            the type of hypervisor used
     * @throws LibvirtException
     * @throws IOException
     * @throws InterruptedException
     */
    private void getIpAddr(String hypervisor)
            throws LibvirtException, IOException, InterruptedException {
        // get libvirt connection URI
        String virtService = null;
        if (hypervisor.equals("lxc")) {
            virtService = "lxc:///";
        } else {
            virtService = hypervisor + ":///system";
        }

        // get libvirt driver connection
        NovaWorker worker = NovaWorker.getInstance();
        Connect connection = worker.getConn(virtService, false);
        // defined but inactive domains
        String[] definedDomains = connection.listDefinedDomains();
        // active domains
        int[] activeDomains = connection.listDomains();
        // iterate through all domains
        for (String domainName : definedDomains) {
            // defined but inactive domains
            Domain dom = connection.domainLookupByName(domainName);
            // get the uuid of the domain
            UUID uuid = null;
            if (dom != null) {
                uuid = UUID.fromString(dom.getUUIDString());
            }
            // get the ip address of the domain
            String ipAddr = null;
            String fetchIpAddrCmd = Utils.pathJoin(Utils.NOVA_HOME,
                    "nova-vmaddrctl") + " " + domainName + " " + virtService;
            Process fetchIpAddr = Runtime.getRuntime().exec(fetchIpAddrCmd);
            // if the process called returns successfully
            if (fetchIpAddr.waitFor() == 0) {
                BufferedReader stdOutReader = new BufferedReader(
                        new InputStreamReader(fetchIpAddr.getInputStream()));
                ipAddr = stdOutReader.readLine();
            }
            if (ipAddr != null && uuid != null) {
                // store the ip address of the doamin into the worker hash map
                worker.getVnodeIP().put(uuid, ipAddr);
            }
        }
        for (int id : activeDomains) {
            // active (running) domains
            Domain dom = connection.domainLookupByID(id);
            // get the uuid of the domain
            UUID uuid = null;
            if (dom != null) {
                uuid = UUID.fromString(dom.getUUIDString());
            }
            // get the ip address of the domain
            String ipAddr = null;
            String fetchIpAddrCmd = Utils.pathJoin(Utils.NOVA_HOME,
                    "nova-vmaddrctl") + " " + dom.getName() + " " + virtService;
            Process fetchIpAddr = Runtime.getRuntime().exec(fetchIpAddrCmd);
            // if the process called returns successfully
            if (fetchIpAddr.waitFor() == 0) {
                BufferedReader stdOutReader = new BufferedReader(
                        new InputStreamReader(fetchIpAddr.getInputStream()));
                ipAddr = stdOutReader.readLine();
            }
            if (ipAddr != null && uuid != null) {
                worker.getVnodeIP().put(uuid, ipAddr);
            }
        }
    }

    @Override
    protected void workOneRound() {
        synchronized (NovaWorker.getInstance().getConnLock()) {
            if (this.capabilities == null) {
                this.capabilities = Conf.getString("hypervisor.engine").trim();
                logger.info(
                        "update hypervisor capabilities: " + this.capabilities);
            }
            try {
                // if worker machine is kvm capable
                if (this.capabilities.indexOf("kvm") >= 0) {
                    getStatus("qemu");
                }
                // if worker machine is vs... capable
                // i don't know what vs... is, never mind
                if (this.capabilities.indexOf("vstaros") >= 0) {
                    getStatus("vstaros");
                }
                // if worker machine is lxc capable
                if (this.capabilities.indexOf("lxc") >= 0) {
                    getStatus("lxc");
                    getIpAddr("lxc");
                }
            } catch (LibvirtException e) {
                logger.error("libvirt driver connection fail", e);
            } catch (IOException e) {
                logger.error("unexpected io exception", e);
            } catch (InterruptedException e) {
                logger.error("unexpected interrupt", e);
            }
        }

        MasterProxy master = NovaWorker.getInstance().getMaster();

        if (this.isStopping() == false && master != null) {
            for (UUID uuid : allStatus.keySet()) {
                Vnode.Status status = allStatus.get(uuid);
                master.sendVnodeStatus(
                        NovaWorker.getInstance().getVnodeIP().get(uuid),
                        uuid.toString(), status);
            }
        }
    }
}
