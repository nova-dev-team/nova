package nova.master.models;

/**
 * @hibernate.class table="vdisk"
 * 
 */
public class Vdisk {

	/** for sqlite db */
	private long id = 1L;

	/** The name on storage server. */
	private String fileName;

	/** The name that will be seen by users. */
	private String displayName;

	/** The description for this disk image. */
	private String description;

	/** Type (format) of this image. */
	private String diskFormat;

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

	/** default constructor */
	public Vdisk() {
	}

	/**
	 * @hibernate.property column="file_name"
	 * 
	 */
	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @hibernate.property column="display_name"
	 * 
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @hibernate.property column="description"
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @hibernate.property column="disk_format"
	 * 
	 */
	public String getDiskFormat() {
		return this.diskFormat;
	}

	public void setDiskFormat(String diskFormat) {
		this.diskFormat = diskFormat;
	}

	/**
	 * @hibernate.property column="os_family"
	 * 
	 */
	public String getOsFamily() {
		return this.osFamily;
	}

	public void setOsFamily(String osFamily) {
		this.osFamily = osFamily;
	}

	/**
	 * @hibernate.property column="os_name"
	 * 
	 */
	public String getOsName() {
		return this.osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	/**
	 * @hibernate.property column="soft_list"
	 * 
	 */
	public String getSoftList() {
		return this.softList;
	}

	public void setSoftList(String softList) {
		this.softList = softList;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

}
