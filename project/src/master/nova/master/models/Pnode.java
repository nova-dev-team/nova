package nova.master.models;

import java.util.Date;

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
	 * Id of the pnode.
	 */
	int id;

	/**
	 * Ip address of the pnode.
	 */
	String ip;

	/**
	 * Time of last message from the pnode. Used to detect pnode failure.
	 */
	Date lastAliveTime;

}
