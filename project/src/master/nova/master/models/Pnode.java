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
	 * Basic information needed to connect to a pnode.
	 * 
	 * @author santa
	 * 
	 */
	public static class Identity {

		/**
		 * Ip address of the vnode.
		 */
		String ip = null;

		/**
		 * Port of the vnode.
		 */
		int port;

		/**
		 * No-arg constructore for gson.
		 */
		public Identity() {

		}

		public Identity(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		/**
		 * Override string present.
		 */
		@Override
		public String toString() {
			return this.ip + ":" + this.port;
		}

		/**
		 * Override equal checker.
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof Pnode.Identity) {
				Pnode.Identity other = (Pnode.Identity) o;
				return this.ip.equals(other.ip) && this.port == other.port;
			}
			return false;
		}

		/**
		 * Override hash function.
		 */
		@Override
		public int hashCode() {
			return ip.hashCode() ^ port;
		}

		public String getIp() {
			return ip;
		}

		public int getPort() {
			return port;
		}

	}

	/**
	 * Status of the pnode.
	 */
	Pnode.Status status;

	/**
	 * The pnode's identity
	 */
	Pnode.Identity ident;

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

	/**
	 * Override string present.
	 */
	@Override
	public String toString() {
		return this.ident.toString();
	}

	public Status getStatus() {
		return this.status;
	}

	public boolean isHeartbeatTimeout() {
		Date now = new Date();
		return now.getTime() - lastAliveTime.getTime() > Pnode.HEARTBEAT_TIMEOUT;
	}

	public Identity getIdentity() {
		return this.ident;
	}
}
