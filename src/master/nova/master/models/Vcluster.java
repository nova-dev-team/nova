package nova.master.models;

import java.util.ArrayList;
import java.util.List;

import nova.common.db.DbManager;
import nova.common.db.DbObject;
import nova.common.db.DbSpec;
import nova.common.util.Utils;

/**
 * @hibernate.class table="vcluster"
 * 
 */
public class Vcluster extends DbObject {

    private static DbManager manager = null;

    public static List<Vcluster> all() {
        List<Vcluster> all = new ArrayList<Vcluster>();
        for (DbObject obj : getManager().all()) {
            all.add((Vcluster) obj);
        }
        return all;
    }

    public static List<Vcluster> getVclusterByUserId(long user_id) {
        List<Vcluster> searchvcluster = new ArrayList<Vcluster>();
        for (DbObject obj : getManager().all()) {
            if (((Vcluster) obj).getUserId() == user_id) {
                searchvcluster.add((Vcluster) obj);
            }
        }
        return searchvcluster;
    }

    public static Vcluster last() {
        Vcluster last = new Vcluster();
        for (DbObject obj : getManager().all()) {
            last = (Vcluster) obj;
        }
        return last;
    }

    public static void delete(Vcluster vcluster) {
        getManager().delete(vcluster);
    }

    public static Vcluster findById(long id) {
        return (Vcluster) getManager().findById(id);
    }

    public static DbManager getManager() {
        if (manager == null) {
            DbSpec spec = new DbSpec();
            manager = DbManager.forClass(Vcluster.class, spec);
        }
        return manager;
    }

    /** cluster_name */
    private String clusterName;

    /**
     * Maximum size of this cluster. It is the limit of vmachines in this
     * cluster.
     */
    private Integer clusterSize;

    /**
     * The first IP allocated to this cluster. # The VM's IP are determined
     * according to this value.
     */
    private String fristIp;

    /** the password for the OS */
    private String osPassword;

    /** the username for the OS */
    private String osUsername;

    /** the private key for ssh */
    private String sshPrivateKey;

    /** the public key for ssh */
    private String sshPublicKey;

    /** the owner's id */
    private long userId;

    /** vnode first name */
    private String firstname;

    /** default constructor */
    public Vcluster() {
    }

    /** full constructor */
    public Vcluster(String clusterName, String fristIp, Integer clusterSize,
            long userId, String sshPublicKey, String sshPrivateKey,
            String osUsername, String osPassword, String firstname) {
        this.clusterName = clusterName;
        this.fristIp = fristIp;
        this.clusterSize = clusterSize;
        this.userId = userId;
        this.sshPublicKey = sshPublicKey;
        this.sshPrivateKey = sshPrivateKey;
        this.osUsername = osUsername;
        this.osPassword = osPassword;
        this.firstname = firstname;
    }

    /**
     * @hibernate.property column="cluster_name"
     * 
     */
    public String getClusterName() {
        return this.clusterName;
    }

    /**
     * @hibernate.property column="cluster_size"
     * 
     */
    public Integer getClusterSize() {
        return this.clusterSize;
    }

    /**
     * @hibernate.property column="frist_ip"
     * 
     */
    public String getFristIp() {
        return this.fristIp;
    }

    public String getFristName() {
        return this.firstname;
    }

    /**
     * @hibernate.property column="os_password"
     * 
     */
    public String getOsPassword() {
        return this.osPassword;
    }

    /**
     * @hibernate.property column="os_username"
     * 
     */
    // modified by herb
    public String getOsUsername() {
        return this.firstname;
    }

    /**
     * @hibernate.property column="ssh_private_key"
     * 
     */
    public String getSshPrivateKey() {
        return this.sshPrivateKey;
    }

    /**
     * @hibernate.property column="ssh_public_key"
     * 
     */
    public String getSshPublicKey() {
        return this.sshPublicKey;
    }

    /**
     * @hibernate.property column="user_id"
     * 
     */
    public long getUserId() {
        return this.userId;
    }

    public void save() {
        getManager().save(this);
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setClusterSize(Integer clusterSize) {
        this.clusterSize = clusterSize;
    }

    public void setFristIp(String fristIp) {
        this.fristIp = fristIp;
    }

    public void setFristName(String fristname) {
        this.firstname = fristname;
    }

    public void setOsPassword(String osPassword) {
        this.osPassword = osPassword;
    }

    // modified by herb
    public void setOsUsername(String firstname) {
        this.osUsername = firstname;
    }

    public void setSshPrivateKey(String sshPrivateKey) {
        this.sshPrivateKey = sshPrivateKey;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * Override string present.
     */
    @Override
    public String toString() {
        return Utils
                .expandTemplate(
                        "{Vcluster @ cluster_name='${clusterName}', id='${id}',cluster_size='${clusterSize}', first_ip='${fristIp}'}",
                        this);
    }

}
