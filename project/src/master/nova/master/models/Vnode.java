package nova.master.models;

import java.util.Date;

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
		 * The vnode status is not known.
		 */
		UNKNOWN,

		/**
		 * The vnode is shut off.
		 */
		SHUT_OFF,

		/**
		 * The virtual node is being scheduled.
		 */
		SCHEDULING,

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

	/** for sqlite db */
	private long id = 1L;

	/**
	 * Status of the vnode.
	 */
	Vnode.Status status;

	private String name;
	private String uuid;
	private Integer cpuCount;
	private String softList;

	/** Unit of memory size is MB */
	private Integer memorySize;

	/**
	 * Which device will the machine be booted. It could be "hd", or "cdrom"
	 */
	private String bootDevice;
	private String hda;
	private String cdrom;

	/**
	 * The architecture of the VM. Could be "i686" or "x86_64", etc.
	 */
	private String arch;

	/**
	 * The vnode's address.
	 */
	SimpleAddress addr;
	private String ip;
	private int port;

	private Integer vclusterId;
	private Integer pnodeId;

	/** The VNC port for the VM. */
	private Integer vncPort;

	/** The hypervisor to be used, could be "xen", "kvm", etc. */
	private String hypervisor;

	/** Migration info: the IP of physical machines. */
	private String migrateFrom;
	private String migrateTo;

	/** Schedule info: on which pmachine should the VM run. */
	private String schedTo;

	/**
	 * Time of last message from the vnode. Used to detect vnode failure.
	 */
	transient Date lastAckTime;

	/**
	 * If the vnode is in a pool, this is set to the pool.
	 */
	VnodePool pool = null;

	/** full constructor */
	public Vnode(String name, String uuid, Integer cpuCount, String softList,
			Integer memorySize, String hda, String cdrom, String arch,
			String ip, Integer vclusterId, Integer pmachineId,
			Vnode.Status status, Integer vncPort, String hypervisor,
			String migrateFrom, String migrateTo, String schedTo) {
		this.setName(name);
		this.setUuid(uuid);
		this.setCpuCount(cpuCount);
		this.setSoftList(softList);
		this.setMemorySize(memorySize);
		this.setHda(hda);
		this.setCdrom(cdrom);
		this.setArch(arch);
		this.ip = ip;
		this.setVclusterId(vclusterId);
		this.setPmachineId(pmachineId);
		this.setStatus(status);
		this.setVncPort(vncPort);
		this.setHypervisor(hypervisor);
		this.setMigrateFrom(migrateFrom);
		this.setMigrateTo(migrateTo);
		this.setSchedTo(schedTo);
	}

	/** default constructor */
	public Vnode() {
	}

	public void setAddr(SimpleAddress addr) {
		this.addr = addr;
		this.setIp(addr.ip);
		this.setPort(addr.port);
	}

	public SimpleAddress getAddr() {
		this.addr.ip = this.getIp();
		this.addr.port = this.getPort();
		return this.addr;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
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

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setCpuCount(Integer cpuCount) {
		this.cpuCount = cpuCount;
	}

	public Integer getCpuCount() {
		return cpuCount;
	}

	public void setSoftList(String softList) {
		this.softList = softList;
	}

	public String getSoftList() {
		return softList;
	}

	public void setMemorySize(Integer memorySize) {
		this.memorySize = memorySize;
	}

	public Integer getMemorySize() {
		return memorySize;
	}

	public void setHda(String hda) {
		this.hda = hda;
	}

	public String getHda() {
		return hda;
	}

	public void setCdrom(String cdrom) {
		this.cdrom = cdrom;
	}

	public String getCdrom() {
		return cdrom;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public String getArch() {
		return arch;
	}

	public void setVclusterId(Integer vclusterId) {
		this.vclusterId = vclusterId;
	}

	public Integer getVclusterId() {
		return vclusterId;
	}

	public void setPmachineId(Integer pmachineId) {
		this.pnodeId = pmachineId;
	}

	public Integer getPmachineId() {
		return pnodeId;
	}

	public void setVncPort(Integer vncPort) {
		this.vncPort = vncPort;
	}

	public Integer getVncPort() {
		return vncPort;
	}

	public void setHypervisor(String hypervisor) {
		this.hypervisor = hypervisor;
	}

	public String getHypervisor() {
		return hypervisor;
	}

	public void setMigrateFrom(String migrateFrom) {
		this.migrateFrom = migrateFrom;
	}

	public String getMigrateFrom() {
		return migrateFrom;
	}

	public void setMigrateTo(String migrateTo) {
		this.migrateTo = migrateTo;
	}

	public String getMigrateTo() {
		return migrateTo;
	}

	public void setSchedTo(String schedTo) {
		this.schedTo = schedTo;
	}

	public String getSchedTo() {
		return schedTo;
	}

	public void setBootDevice(String bootDevice) {
		this.bootDevice = bootDevice;
	}

	public String getBootDevice() {
		return bootDevice;
	}

	public void setStatus(Vnode.Status status) {
		this.status = status;
	}

	public Vnode.Status getStatus() {
		return status;
	}
}
