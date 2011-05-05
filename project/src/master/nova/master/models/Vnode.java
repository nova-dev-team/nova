package nova.master.models;

import java.util.Date;
import java.util.UUID;

import nova.common.service.SimpleAddress;

/**
 * Model for a virtual node.
 * 
 * @author santa
 * 
 */
public class Vnode {

	/**
	 * Status for the virtual node.
	 * 
	 * @author santa
	 * 
	 */
	public static enum Status {
		/**
		 * The vnode is shut off.
		 */
		SHUT_OFF,

		/**
		 * The virtual node is being scheduled.
		 */
		PENDING,

		/**
		 * The vnode is scheduled and it is being prepared on a pnode.
		 */
		PREPARING,

		/**
		 * The vnode is running.
		 */
		RUNNING,

		/**
		 * The vnode is shutting down.
		 */
		SHUTTING_DOWN,

		/**
		 * The vnode failed to boot.
		 */
		BOOT_FAILURE,

		/**
		 * Failed to connect to the vnode. Could be caused by pnode downtime.
		 */
		CONNECT_FAILURE,
	}

	/**
	 * Status of the vnode.
	 */
	transient Vnode.Status status;

	/**
	 * The vnode's address.
	 */
	SimpleAddress addr;

	/** for sqlite db */
	private long id = 1L;

	UUID uuid;

	/**
	 * Time of last message from the vnode. Used to detect vnode failure.
	 */
	transient Date lastAckTime;

	/**
	 * If the vnode is in a pool, this is set to the pool.
	 */
	VnodePool pool = null;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
}
