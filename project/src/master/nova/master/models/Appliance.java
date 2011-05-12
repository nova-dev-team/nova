package nova.master.models;

import java.util.ArrayList;
import java.util.List;

import nova.common.db.DbManager;
import nova.common.db.DbObject;
import nova.common.db.DbSpec;

/**
 * table="softwarepackage"
 */
public class Appliance extends DbObject {

	private static DbManager manager = null;

	public static List<Appliance> all() {
		List<Appliance> all = new ArrayList<Appliance>();
		for (DbObject obj : getManager().all()) {
			all.add((Appliance) obj);
		}
		return null;
	}

	public static void delete(Appliance appliance) {
		getManager().delete(appliance);
	}

	public static Appliance findById(long id) {
		return (Appliance) getManager().findById(id);
	}

	public static Appliance findByFileName(String fileName) {
		return (Appliance) getManager().findBy("fileName", fileName);
	}

	public static DbManager getManager() {
		if (manager == null) {
			DbSpec spec = new DbSpec();
			spec.addIndex("fileName");
			manager = DbManager.forClass(Appliance.class, spec);
		}
		return manager;
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
		getManager().save(this);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setFileName(String fileName) {
		getManager().updateField(this, "fileName", fileName);
	}

	public void setOsFamily(String osFamily) {
		this.osFamily = osFamily;
	}

}
