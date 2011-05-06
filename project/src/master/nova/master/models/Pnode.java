package nova.master.models;

import java.util.ArrayList;
import java.util.Date;

import nova.common.service.SimpleAddress;

/**
 * Model for a physical node.
 * 
 * @author santa
 * 
 */
public class Pnode {

	/**
	 * Status for the physical node.
	 * 
	 * @author santa
	 * 
	 */
	public enum Status {
		/**
		 * The pnode status is not known.
		 */
		UNKNOWN,

		/**
		 * The pnode is being added.
		 */
		ADD_PENDING,

		/**
		 * The pnode is running and healthy.
		 */
		RUNNING,

		/**
		 * The pnode is running and retired (not for use).
		 */
		RETIRED,

		/**
		 * Cannot connect the pnode.
		 */
		CONNECT_FAILURE,
	}

	/** for sqlite db */
	private long id = 1L;

	/**
	 * Status of the pnode.
	 */
	Pnode.Status status;

	/**
	 * The pnode's address.
	 */
	SimpleAddress addr;
	private String ip;
	private int port;

	/**
	 * Id of the pnode.
	 */
	private int pnodeId;

	/** The host name of physical machine. */
	private String hostname;

	/** The uuid of the worker machine. */
	private String uuid;

	/** The MAC address of the worker machine, used for remote booting */
	private String macAddress;

	/**
	 * The limit of running VMs on this machine. It is not a hard limit, but
	 * creating VMs more than this limit will result in low performance.
	 */
	private int vmCapacity;

	/**
	 * Time of last message from the pnode. Used to detect pnode failure. Marked
	 * "transient" because it does not need to be saved into database.
	 */
	transient Date lastAckTime = new Date();

	/**
	 * Last time a message was sent to this node.
	 */
	transient Date lastReqTime = new Date();

	/**
	 * If lastAckTime is not updated in this interval, the node will be
	 * considered as down.
	 */
	public static final long HEARTBEAT_TIMEOUT = 1000;

	/**
	 * Interval between each ping messages.
	 */
	public static final long PING_INTERVAL = 1000;

	public Pnode() {
		this.status = Pnode.Status.ADD_PENDING;
	}

	public Pnode(Pnode.Status status, SimpleAddress addr, int pnodeId,
			String hostname, String uuid, String macAddress, Integer vmCapacity) {
		this.status = Pnode.Status.ADD_PENDING;
		this.addr = addr;
		this.pnodeId = pnodeId;
		this.hostname = hostname;
		this.uuid = uuid;
		this.macAddress = macAddress;
		this.vmCapacity = vmCapacity;
	}

	/**
	 * Override string present.
	 */
	@Override
	public String toString() {
		return this.addr.toString();
	}

	public void setStatus(Pnode.Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return this.status;
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

	public void updateLastReqTime() {
		this.lastReqTime = new Date();
	}

	public void setAddr(SimpleAddress addr) {
		this.addr = addr;
		this.ip = addr.ip;
		this.port = addr.port;
	}

	public SimpleAddress getAddr() {
		this.addr.ip = this.getIp();
		this.addr.port = this.getPort();
		return this.addr;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setPnodeId(int pnodeId) {
		this.pnodeId = pnodeId;
	}

	public int getPnodeId() {
		return pnodeId;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setVmCapacity(Integer vmCapacity) {
		this.vmCapacity = vmCapacity;
	}

	public Integer getVmCapacity() {
		return vmCapacity;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	// TODO @zhaoxun
	public void save() {

	}

	// TODO @zhaoxun
	private static ArrayList<Pnode> allPnodes = new ArrayList<Pnode>();

	public static ArrayList<Pnode> all() {
		// TODO @zhaoxun
		return allPnodes;
	}

	public static Pnode findByHost(String ip) {
		// TODO @zhaoxun
		return null;
	}
}
