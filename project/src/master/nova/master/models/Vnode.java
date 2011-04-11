package nova.master.models;

import java.util.Date;

/**
 * Model for a virtual node.
 * 
 * @author santa
 * 
 */
public class Vnode extends VnodeBasic {

	/**
	 * Status for the virtual node.
	 * 
	 * @author santa
	 * 
	 */
	public enum Status {
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
	 * Id of the vnode.
	 */
	int id;

	/**
	 * Time of last message from the vnode. Used to detect vnode failure.
	 */
	Date lastAliveTime;

	/**
	 * If the vnode is in a pool, this is set to the pool.
	 */
	VnodePool pool = null;

}
