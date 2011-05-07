package nova.master.models;

/**
 * table="softwarepackage"
 */
public class Appliance {

	/** description for the software. */
	private String description;;

	/** the name that will be seen by users */
	private String displayName;

	/** the real file name on storage server */
	private String fileName;

	/** for sqlite3 db */
	private long id = 1L;

	/** the os family that best matchs the software */
	private String osFamily;

	/** default constructor */
	public Appliance() {
	}

	/** full constructor */
	public Appliance(String fileName, String display, String description,
			String osFamily) {
		this.fileName = fileName;
		this.displayName = display;
		this.description = description;
		this.osFamily = osFamily;
	}

	/**
	 * @hibernate.property column="description"
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @hibernate.property column="display"
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

	public void save() {
		MasterDb.save(this);
	}

	public void setDescription(String description) {
		this.description = description;
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

}
