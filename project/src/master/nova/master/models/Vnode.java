package nova.master.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nova.common.db.DbManager;
import nova.common.db.DbObject;
import nova.common.db.DbSpec;
import nova.common.service.SimpleAddress;

/**
 * Model for a virtual node.
 * 
 * @author santa
 * 
 */
public class Vnode extends DbObject {

	/**
	 * Status for the virtual node.
	 * 
	 * @author santa
	 * 
	 */
	public static enum Status {
		/**
		 * The vnode failed to boot.
		 */
		BOOT_FAILURE,

		/**
		 * Failed to connect to the vnode. Could be caused by pnode downtime.
		 */
		CONNECT_FAILURE,

		/**
		 * The vnode is scheduled and it is being prepared on a pnode.
		 */
		PREPARING,

		/**
		 * The vnode is running.
		 */
		RUNNING,

		/**
		 * The virtual node is being scheduled.
		 */
		SCHEDULING,

		/**
		 * The vnode is shut off.
		 */
		SHUT_OFF,

		/**
		 * The vnode is shutting down.
		 */
		SHUTTING_DOWN,

		/**
		 * The vnode status is not known.
		 */
		UNKNOWN,
	}

	private static DbManager manager = null;

	public static List<Vnode> all() {
		List<Vnode> all = new ArrayList<Vnode>();
		for (DbObject obj : getManager().all()) {
			all.add((Vnode) obj);
		}
		return all;
	}

	public static Vnode findById(long id) {
		return (Vnode) getManager().findById(id);
	}

	public static Vnode findByIP(String ip) {
		return (Vnode) getManager().findBy("ip", ip);
	}

	public static DbManager getManager() {
		if (manager == null) {
			DbSpec spec = new DbSpec();
			spec.addIndex("uuid");
			manager = DbManager.forClass(Vnode.class, spec);
		}
		return manager;
	}

	/**
	 * The vnode's address.
	 */
	SimpleAddress addr;

	/**
	 * The architecture of the VM. Could be "i686" or "x86_64", etc.
	 */
	private String arch;

	/**
	 * Which device will the machine be booted. It could be "hd", or "cdrom"
	 */
	private String bootDevice;

	private String cdrom;

	private Integer cpuCount;
	private String hda;
	/** The hypervisor to be used, could be "xen", "kvm", etc. */
	private String hypervisor;

	private String ip;

	/**
	 * Time of last message from the vnode. Used to detect vnode failure.
	 */
	transient Date lastAckTime;
	/** Unit of memory size is MB */
	private Integer memorySize;
	/** Migration info: the IP of physical machines. */
	private String migrateFrom;

	private String migrateTo;

	private String name;
	private Integer pnodeId;
	/**
	 * If the vnode is in a pool, this is set to the pool.
	 */
	VnodePool pool = null;

	private int port;
	/** Schedule info: on which pmachine should the VM run. */
	private String schedTo;

	private String softList;

	/**
	 * Status of the vnode.
	 */
	String statusCode;

	private String uuid;
	private Integer vclusterId;

	/** The VNC port for the VM. */
	private Integer vncPort;

	/** default constructor */
	public Vnode() {
	}

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

	public SimpleAddress getAddr() {
		this.addr.ip = this.getIp();
		this.addr.port = this.getPort();
		return this.addr;
	}

	public String getArch() {
		return arch;
	}

	public String getBootDevice() {
		return bootDevice;
	}

	public String getCdrom() {
		return cdrom;
	}

	public Integer getCpuCount() {
		return cpuCount;
	}

	public String getHda() {
		return hda;
	}

	public String getHypervisor() {
		return hypervisor;
	}

	public String getIp() {
		return ip;
	}

	public Integer getMemorySize() {
		return memorySize;
	}

	public String getMigrateFrom() {
		return migrateFrom;
	}

	public String getMigrateTo() {
		return migrateTo;
	}

	public String getName() {
		return name;
	}

	public Integer getPmachineId() {
		return pnodeId;
	}

	public int getPort() {
		return port;
	}

	public String getSchedTo() {
		return schedTo;
	}

	public String getSoftList() {
		return softList;
	}

	public Vnode.Status getStatus() {
		return Status.valueOf(this.statusCode);
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getUuid() {
		return uuid;
	}

	public Integer getVclusterId() {
		return vclusterId;
	}

	public Integer getVncPort() {
		return vncPort;
	}

	public void save() {
		getManager().save(this);
	}

	public void setAddr(SimpleAddress addr) {
		this.addr = addr;
		this.setIp(addr.ip);
		this.setPort(addr.port);
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public void setBootDevice(String bootDevice) {
		this.bootDevice = bootDevice;
	}

	public void setCdrom(String cdrom) {
		this.cdrom = cdrom;
	}

	public void setCpuCount(Integer cpuCount) {
		this.cpuCount = cpuCount;
	}

	public void setHda(String hda) {
		this.hda = hda;
	}

	public void setHypervisor(String hypervisor) {
		this.hypervisor = hypervisor;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setMemorySize(Integer memorySize) {
		this.memorySize = memorySize;
	}

	public void setMigrateFrom(String migrateFrom) {
		this.migrateFrom = migrateFrom;
	}

	public void setMigrateTo(String migrateTo) {
		this.migrateTo = migrateTo;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPmachineId(Integer pmachineId) {
		this.pnodeId = pmachineId;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setSchedTo(String schedTo) {
		this.schedTo = schedTo;
	}

	public void setSoftList(String softList) {
		this.softList = softList;
	}

	public void setStatus(Vnode.Status status) {
		this.statusCode = status.toString();
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public void setUuid(String uuid) {
		getManager().getIndex("uuid").remove(this.uuid);
		this.uuid = uuid;
		getManager().getIndex("uuid").put(this.uuid, this);
	}

	public void setVclusterId(Integer vclusterId) {
		this.vclusterId = vclusterId;
	}

	public void setVncPort(Integer vncPort) {
		this.vncPort = vncPort;
	}

}
