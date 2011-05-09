package nova.master.models;

import nova.common.db.DbManager;
import nova.common.db.DbSpec;

/**
 * table="softwarepackage"
 */
public class Appliance {

	static DbManager dbm = null;

	public long id = 1L;

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	static {
		DbSpec spec = new DbSpec();
		spec.addIndex("ip");
		spec.addIndex("fileName");
		dbm = DbManager.forClass(Appliance.class, spec);
	}

	public static Appliance findById(long id) {
		return (Appliance) dbm.findById(id);
	}

	public static Appliance findByFileName(String fileName) {
		return (Appliance) dbm.findBy("fileName", fileName);
	}

	/** description for the software. */
	private String description;;

	/** the name that will be seen by users */
	private String displayName;

	/** the real file name on storage server */
	private String fileName;

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

	/**
	 * @hibernate.property column="os_family"
	 * 
	 */
	public String getOsFamily() {
		return this.osFamily;
	}

	public void save() {
		dbm.saveEx(this);
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

	public void setOsFamily(String osFamily) {
		this.osFamily = osFamily;
	}

}
