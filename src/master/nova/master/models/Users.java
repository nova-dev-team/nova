package nova.master.models;

import java.util.ArrayList;
import java.util.List;

import nova.common.db.DbManager;
import nova.common.db.DbObject;
import nova.common.db.DbSpec;
import nova.common.util.Utils;

/**
 * @hibernate.class table="users"
 * 
 */
public class Users extends DbObject {

    /**
     * @anthor: hestream
     */
    private static DbManager manager = null;

    public static List<Users> all() {
        List<Users> all = new ArrayList<Users>();
        for (DbObject obj : getManager().all()) {
            all.add((Users) obj);
        }

        return all;
    }

    public static Users last() {
        Users last = new Users();
        for (DbObject obj : getManager().all()) {
            last = (Users) obj;
        }
        return last;
    }

    public static void delete(Users user) {
        getManager().delete(user);
    }

    public static Users findById(long id) {
        return (Users) getManager().findById(id);
    }

    public static Users findByName(String name) {
        return (Users) getManager().findBy("name", name);
    }

    public static DbManager getManager() {
        if (manager == null) {
            DbSpec spec = new DbSpec();
            spec.addIndex("name");
            manager = DbManager.forClass(Users.class, spec);
        }

        return manager;
    }

    public void save() {
        getManager().save(this);
    }

    /** user name */
    private String name;

    /** user email */
    private String email;

    /** user password */
    private String password;

    /** user privilege */
    private String privilege;

    /** user activated */
    private String activated;

    /** default constructor */
    public Users() {

        this.name = "";
        this.email = "";
        this.password = "";
        this.privilege = "";
        this.activated = "";

    }

    /** full constructor */
    public Users(String name, String email, String password, String privilege,
            String activated) {

        this.name = name;
        this.email = email;
        this.password = password;
        this.privilege = privilege;
        this.activated = activated;

    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getPrivilege() {
        return this.privilege;
    }

    public String getActivated() {
        return this.activated;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public void setActivated(String activated) {
        this.activated = activated;
    }

    /**
     * Override string present.
     */
    @Override
    public String toString() {
        return Utils
                .expandTemplate(
                        "{Users @ user_name='${name}', id='${id}',user_email='${email}', user_privilege='${privilege}'}",
                        this);
    }
}
