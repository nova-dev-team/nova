package nova.master.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nova.common.db.DbManager;
import nova.common.db.DbObject;
import nova.common.db.DbSpec;
import nova.common.service.SimpleAddress;
import nova.common.util.Utils;

/**
 * Model for a physical node.
 * 
 * @author santa
 * 
 */
public class Pnode extends DbObject {

    /**
     * Status for the physical node.
     * 
     * @author santa
     * 
     */
    public enum Status {
        /**
         * The pnode is being added.
         */
        ADD_PENDING,

        /**
         * Cannot connect the pnode.
         */
        CONNECT_FAILURE,

        /**
         * The pnode is running and retired (not for use).
         */
        RETIRED,

        /**
         * The pnode is running and healthy.
         */
        RUNNING,

        /**
         * The pnode status is not known.
         */
        UNKNOWN
    }

    /**
     * If lastAckTime is not updated in this interval, the node will be
     * considered as down.
     */
    public static final long HEARTBEAT_TIMEOUT = 1000;

    private static DbManager manager = null;

    /**
     * Interval between each ping messages.
     */
    public static final long PING_INTERVAL = 1000;

    public static List<Pnode> all() {
        List<Pnode> all = new ArrayList<Pnode>();
        for (DbObject obj : getManager().all()) {
            all.add((Pnode) obj);
        }
        return all;
    }

    public static void delete(Pnode pnode) {
        getManager().delete(pnode);
    }

    public static Pnode findById(long id) {
        return (Pnode) getManager().findById(id);
    }

    public static Pnode findByIp(String ip) {
        return (Pnode) getManager().findBy("ip", ip);
    }

    public static DbManager getManager() {
        if (manager == null) {
            DbSpec spec = new DbSpec();
            spec.addIndex("ip");
            manager = DbManager.forClass(Pnode.class, spec);
        }
        return manager;
    }

    /** The host name of physical machine. */
    private String hostname;

    private String ip;

    /**
     * Time of last message from the pnode. Used to detect pnode failure. Marked
     * "transient" because it does not need to be saved into database.
     */
    transient Date lastAckTime = new Date();

    /**
     * Last time a message was sent to this node.
     */
    transient Date lastReqTime = new Date();

    /** The MAC address of the worker machine, used for remote booting */
    private String macAddress;

    /**
     * Id of the pnode.
     */
    private int pnodeId;

    private int port;

    /**
     * Status of the pnode. Only used by Hibernate.
     */
    private String statusCode;

    /** The uuid of the worker machine. */
    private String uuid;

    /**
     * The limit of running VMs on this machine. It is not a hard limit, but
     * creating VMs more than this limit will result in low performance.
     */
    private int vmCapacity;

    public Pnode() {
        this.setStatus(Pnode.Status.ADD_PENDING);
    }

    public Pnode(Pnode.Status status, String ip, int port, int pnodeId,
            String hostname, String uuid, String macAddress, Integer vmCapacity) {
        this.setStatus(Pnode.Status.ADD_PENDING);
        this.ip = ip;
        this.port = port;
        this.pnodeId = pnodeId;
        this.hostname = hostname;
        this.uuid = uuid;
        this.macAddress = macAddress;
        this.vmCapacity = vmCapacity;
    }

    public SimpleAddress getAddr() {
        return new SimpleAddress(this.ip, this.port);
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getPnodeId() {
        return pnodeId;
    }

    public int getPort() {
        return port;
    }

    public Status getStatus() {
        return Status.valueOf(this.statusCode);
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getUuid() {
        return uuid;
    }

    public Integer getVmCapacity() {
        return vmCapacity;
    }

    public void gotAck() {
        this.lastAckTime = new Date();
    }

    public boolean isHeartbeatTimeout() {
        Date now = new Date();
        long timespan = now.getTime() - lastAckTime.getTime();
        return timespan > Pnode.HEARTBEAT_TIMEOUT;
    }

    public boolean needNewPingMessage() {
        Date now = new Date();
        long timespan = now.getTime() - this.lastReqTime.getTime();
        return timespan > Pnode.PING_INTERVAL;
    }

    public void save() {
        getManager().save(this);
    }

    public void setAddr(SimpleAddress addr) {
        this.setIp(addr.ip);
        this.port = addr.port;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setIp(String ip) {
        getManager().updateField(this, "ip", ip);
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setPnodeId(int pnodeId) {
        this.pnodeId = pnodeId;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setStatus(Pnode.Status status) {
        this.statusCode = status.toString();
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setVmCapacity(Integer vmCapacity) {
        this.vmCapacity = vmCapacity;
    }

    /**
     * Override string present.
     */
    @Override
    public String toString() {
        return Utils
                .expandTemplate(
                        "{Pnode @ ${ip}:${port}, pid='${id}',hostname='${hostname}', Status='${statusCode}', uuid='${uuid}'}",
                        this);
    }

    public void updateLastReqTime() {
        this.lastReqTime = new Date();
    }
}
