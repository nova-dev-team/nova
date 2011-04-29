package nova.master.models;

/**
 * @hibernate.class table="vcluster"
 * 
 */
public class Vcluster {

	/** for sqlite db */
	private long id = 1L;

	/** cluster_name */
	private String clusterName;

	/**
	 * The first IP allocated to this cluster. # The VM's IP are determined
	 * according to this value.
	 */
	private String fristIp;

	/**
	 * Maximum size of this cluster. It is the limit of vmachines in this
	 * cluster.
	 */
	private Integer clusterSize;

	/** the owner's id */
	private Integer userId;

	/** the public key for ssh */
	private String sshPublicKey;

	/** the private key for ssh */
	private String sshPrivateKey;

	/** the username for the OS */
	private String osUsername;

	/** the password for the OS */
	private String osPassword;

	/** full constructor */
	public Vcluster(String clusterName, String fristIp, Integer clusterSize,
			Integer userId, String sshPublicKey, String sshPrivateKey,
			String osUsername, String osPassword) {
		this.clusterName = clusterName;
		this.fristIp = fristIp;
		this.clusterSize = clusterSize;
		this.userId = userId;
		this.sshPublicKey = sshPublicKey;
		this.sshPrivateKey = sshPrivateKey;
		this.osUsername = osUsername;
		this.osPassword = osPassword;
	}

	/** default constructor */
	public Vcluster() {
	}

	/**
	 * @hibernate.property column="cluster_name"
	 * 
	 */
	public String getClusterName() {
		return this.clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	/**
	 * @hibernate.property column="frist_ip"
	 * 
	 */
	public String getFristIp() {
		return this.fristIp;
	}

	public void setFristIp(String fristIp) {
		this.fristIp = fristIp;
	}

	/**
	 * @hibernate.property column="cluster_size"
	 * 
	 */
	public Integer getClusterSize() {
		return this.clusterSize;
	}

	public void setClusterSize(Integer clusterSize) {
		this.clusterSize = clusterSize;
	}

	/**
	 * @hibernate.property column="user_id"
	 * 
	 */
	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * @hibernate.property column="ssh_public_key"
	 * 
	 */
	public String getSshPublicKey() {
		return this.sshPublicKey;
	}

	public void setSshPublicKey(String sshPublicKey) {
		this.sshPublicKey = sshPublicKey;
	}

	/**
	 * @hibernate.property column="ssh_private_key"
	 * 
	 */
	public String getSshPrivateKey() {
		return this.sshPrivateKey;
	}

	public void setSshPrivateKey(String sshPrivateKey) {
		this.sshPrivateKey = sshPrivateKey;
	}

	/**
	 * @hibernate.property column="os_username"
	 * 
	 */
	public String getOsUsername() {
		return this.osUsername;
	}

	public void setOsUsername(String osUsername) {
		this.osUsername = osUsername;
	}

	/**
	 * @hibernate.property column="os_password"
	 * 
	 */
	public String getOsPassword() {
		return this.osPassword;
	}

	public void setOsPassword(String osPassword) {
		this.osPassword = osPassword;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

}
