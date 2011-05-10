package nova.master.models;

import nova.common.db.DbManager;
import nova.common.db.DbSpec;

/**
 * @hibernate.class table="vdisk"
 * 
 */
public class Vdisk {

	static DbManager manager = null;

	public static DbManager getManager() {
		if (manager == null) {
			DbSpec spec = new DbSpec();
			spec.addIndex("fileName");
			manager = DbManager.forClass(Vdisk.class, spec);
		}
		return manager;
	}

	public static Vdisk findById(long id) {
		return (Vdisk) getManager().findById(id);
	}

	/** The description for this disk image. */
	private String description;

	/** Type (format) of this image. */
	private String diskFormat;

	/** The name that will be seen by users. */
	private String displayName;

	/** The name on storage server. */
	private String fileName;

	/** for sqlite db */
	private long id = 1L;

	/**
	 * The kind of operation system of this image. Could be "windows", "linux".
	 */
	private String osFamily;

	/**
	 * The precise name of the operation system, with version included. Here is
	 * some suggested names: "Windows XP", "Windows XP (SP3)", "Windows Vista",
	 * "Windows 7"; "Ubuntu 8.04", "Ubuntu 10.04".
	 */
	private String osName;

	/** Ths list of softwares for this vdisk. Separated by comma. */
	private String softList;

	/** default constructor */
	public Vdisk() {
	}

	/** full constructor */
	public Vdisk(String fileName, String displayName, String description,
			String diskFormat, String osFamily, String osName, String softList) {
		this.fileName = fileName;
		this.displayName = displayName;
		this.description = description;
		this.diskFormat = diskFormat;
		this.osFamily = osFamily;
		this.osName = osName;
		this.softList = softList;
	}

	/**
	 * @hibernate.property column="description"
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @hibernate.property column="disk_format"
	 * 
	 */
	public String getDiskFormat() {
		return this.diskFormat;
	}

	/**
	 * @hibernate.property column="display_name"
	 * 
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * @hibernate.property column="file_name"
	 * 
	 */
	public String getFileName() {
		return this.fileName;
	}

	public long getId() {
		return id;
	}

	/**
	 * @hibernate.property column="os_family"
	 * 
	 */
	public String getOsFamily() {
		return this.osFamily;
	}

	/**
	 * @hibernate.property column="os_name"
	 * 
	 */
	public String getOsName() {
		return this.osName;
	}

	/**
	 * @hibernate.property column="soft_list"
	 * 
	 */
	public String getSoftList() {
		return this.softList;
	}

	public void save() {
		getManager().save(this);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDiskFormat(String diskFormat) {
		this.diskFormat = diskFormat;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setOsFamily(String osFamily) {
		this.osFamily = osFamily;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public void setSoftList(String softList) {
		this.softList = softList;
	}

}
