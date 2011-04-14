package nova.master.models;

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
		 * The pnode is being added.
		 */
		PENDING,

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

	/**
	 * Status of the pnode.
	 */
	Pnode.Status status;

	/**
	 * The pnode's address.
	 */
	SimpleAddress addr;

	/**
	 * Id of the pnode.
	 */
	int id;

	/**
	 * Time of last message from the pnode. Used to detect pnode failure. Marked
	 * "transient" because it does not need to be saved into database.
	 */
	transient Date lastAckTime = new Date();

	/**
	 * Last time a ping message was sent to this node.
	 */
	transient Date lastPingTime = new Date();

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
		this.status = Pnode.Status.PENDING;
	}

	/**
	 * Override string present.
	 */
	@Override
	public String toString() {
		return this.addr.toString();
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
		long timespan = now.getTime() - this.lastPingTime.getTime();
		return timespan > Pnode.PING_INTERVAL;
	}

	public void updateLastPingTime() {
		this.lastPingTime = new Date();
	}

	public SimpleAddress getAddress() {
		return this.addr;
	}
}
