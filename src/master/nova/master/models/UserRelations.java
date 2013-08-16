package nova.master.models;

import java.util.ArrayList;
import java.util.List;

import nova.common.db.DbManager;
import nova.common.db.DbObject;
import nova.common.db.DbSpec;
import nova.common.util.Utils;

/**
 * @hibernate.class table="UserRelations"
 * 
 */
public class UserRelations extends DbObject {

    /**
     * @anthor: hestream
     */
    private static DbManager manager = null;

    public static List<UserRelations> all() {
        List<UserRelations> all = new ArrayList<UserRelations>();
        for (DbObject obj : getManager().all()) {
            all.add((UserRelations) obj);
        }

        return all;
    }

    public static List<UserRelations> getByAdminUserId(long admin_id) {
        List<UserRelations> ur_admin = new ArrayList<UserRelations>();
        for (UserRelations obj : UserRelations.all()) {
            if (obj.getAdminUserId().equals(admin_id))
                ur_admin.add(obj);
        }

        return ur_admin;
    }

    public static void delete(UserRelations userrelation) {
        getManager().delete(userrelation);
    }

    public static UserRelations findById(long id) {
        return (UserRelations) getManager().findById(id);
    }

    public static UserRelations findByNormalId(long normalId) {
        return (UserRelations) getManager().findBy("NormalUserId", normalId);
    }

    public static DbManager getManager() {
        if (manager == null) {
            DbSpec spec = new DbSpec();
            spec.addIndex("NormalUserId");
            manager = DbManager.forClass(UserRelations.class, spec);
        }

        return manager;
    }

    public void save() {
        getManager().save(this);
    }

    /** Userrelations NormalUserId */
    private long NormalUserId;

    /** Userrelations AdminUserId */
    private long AdminUserId;

    /** default constructor */
    public UserRelations() {
        this.NormalUserId = 0;
        this.AdminUserId = 0;
    }

    /** full constructor */
    public UserRelations(long NormalUserId, long AdminUserId) {
        this.NormalUserId = NormalUserId;
        this.AdminUserId = AdminUserId;
    }

    public Long getNormalUserId() {
        return this.NormalUserId;
    }

    public Long getAdminUserId() {
        return this.AdminUserId;
    }

    public void setNormalUserId(long NormalUserId) {
        this.NormalUserId = NormalUserId;
    }

    public void setAdminUserId(long AdminUserId) {
        this.AdminUserId = AdminUserId;
    }

    /**
     * Override string present.
     */
    @Override
    public String toString() {
        return Utils
                .expandTemplate(
                        "{UserRelation @ NormalUserId='${NormalUserId}', AdminUserId='${AdminUserId}'}",
                        this);
    }

}
