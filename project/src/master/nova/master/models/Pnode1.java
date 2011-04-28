package nova.master.models;

import java.io.Serializable;

public class Pnode1 implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id = 1L;
	/** identifier field */
	private String ip;

	/** identifier field */
	private String status;

	/** identifier field */
	private String hostname;

	/** identifier field */
	private String uuid;

	/** identifier field */
	private String macAddress;

	/** identifier field */
	private Integer vmCapacity;

	/** full constructor */
	public Pnode1(String ip, String status, String hostname, String uuid,
			String macAddress, Integer vmCapacity) {
		this.ip = ip;
		this.status = status;
		this.hostname = hostname;
		this.uuid = uuid;
		this.macAddress = macAddress;
		this.vmCapacity = vmCapacity;
	}

	/** default constructor */
	public Pnode1() {
	}

	/**
	 * @hibernate.property column="ip"
	 * 
	 */
	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @hibernate.property column="status"
	 * 
	 */
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @hibernate.property column="hostname"
	 * 
	 */
	public String getHostname() {
		return this.hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @hibernate.property column="uuid"
	 * 
	 */
	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @hibernate.property column="mac_address"
	 * 
	 */
	public String getMacAddress() {
		return this.macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @hibernate.property column="vm_capacity"
	 * 
	 */
	public Integer getVmCapacity() {
		return this.vmCapacity;
	}

	public void setVmCapacity(Integer vmCapacity) {
		this.vmCapacity = vmCapacity;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

}
