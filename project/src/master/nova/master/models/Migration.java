package nova.master.models;

import java.util.ArrayList;
import java.util.List;

import nova.common.db.DbManager;
import nova.common.db.DbObject;
import nova.common.db.DbSpec;
import nova.common.util.Utils;

public class Migration extends DbObject {

	private static DbManager manager = null;

	public static List<Migration> all() {
		List<Migration> all = new ArrayList<Migration>();
		for (DbObject obj : getManager().all()) {
			all.add((Migration) obj);
		}
		return all;
	}

	public static void delete(Migration migration) {
		getManager().delete(migration);
	}

	public static Migration findById(long id) {
		return (Migration) getManager().findById(id);
	}

	public static Migration findVnodeId(long vnodeId) {
		return (Migration) getManager().findBy("vnodeId", vnodeId);
	}

	public static DbManager getManager() {
		if (manager == null) {
			DbSpec spec = new DbSpec();
			spec.addIndex("fileName");
			manager = DbManager.forClass(Migration.class, spec);
		}
		return manager;
	}

	/** Id of vnode to migrate */
	private long vnodeId;
	/** Id of pnode which the vm migrate from */
	private long migrateFrom;
	/** Id of pnode which the vm migrate to */
	private long migrateTo;

	public void setVnodeId(long vnodeId) {
		this.vnodeId = vnodeId;
	}

	public long getVnodeId() {
		return vnodeId;
	}

	public void setMigrateFrom(long migrateFrom) {
		this.migrateFrom = migrateFrom;
	}

	public long getMigrateFrom() {
		return migrateFrom;
	}

	public void setMigrateTo(long migrateTo) {
		this.migrateTo = migrateTo;
	}

	public long getMigrateTo() {
		return migrateTo;
	}

	/**
	 * Override string present.
	 */
	@Override
	public String toString() {
		return Utils
				.expandTemplate(
						"Migration @ vnode_id= '${vnodeId}', id = '${id}' , migrate_from = '${migrateFrom}', migrate_to = '${migrateTo}'",
						this);

	}
}
