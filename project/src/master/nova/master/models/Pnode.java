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
	transient Date lastAliveTime = new Date();

	public static final long HEARTBEAT_TIMEOUT = 1000;

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

	public boolean isHeartbeatTimeout() {
		Date now = new Date();
		long timespan = now.getTime() - lastAliveTime.getTime();
		return timespan > Pnode.HEARTBEAT_TIMEOUT;
	}

	public SimpleAddress getAddress() {
		return this.addr;
	}
}