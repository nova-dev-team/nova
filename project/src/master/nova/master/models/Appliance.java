package nova.master.models;

/**
 * table="softwarepackage"
 */
public class Appliance {

	/** for sqlite3 db */
	private long id = 1L;;

	/** the real file name on storage server */
	private String fileName;

	/** the name that will be seen by users */
	private String displayName;

	/** description for the software. */
	private String description;

	/** the os family that best matchs the software */
	private String osFamily;

	/** full constructor */
	public Appliance(String fileName, String display, String description,
			String osFamily) {
		this.fileName = fileName;
		this.displayName = display;
		this.description = description;
		this.osFamily = osFamily;
	}

	/** default constructor */
	public Appliance() {
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
	 * @hibernate.property column="display"
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
	 * @hibernate.property column="os_family"
	 * 
	 */
	public String getOsFamily() {
		return this.osFamily;
	}

	public void setOsFamily(String osFamily) {
		this.osFamily = osFamily;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

}
