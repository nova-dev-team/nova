package nova.master.models;

import java.util.ArrayList;

/**
 * Represents a pool for vnodes.
 * 
 * @author santa
 * 
 */
public class VnodePool {

    /**
     * the commonly used disk.
     */
    Vdisk disk;

    /**
     * Id of the vnode pool.
     */
    int id;

    /**
     * Size of the pool.
     */
    int poolSize;

    /**
     * List of vnodes in the pool.
     */
    ArrayList<Vnode> vnodes;

}
