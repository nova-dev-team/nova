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
	 * Id of the vnode pool.
	 */
	int id;

	/**
	 * the commonly used disk.
	 */
	Vdisk disk;

	/**
	 * Size of the pool.
	 */
	int poolSize;

	/**
	 * List of vnodes in the pool.
	 */
	ArrayList<Vnode> vnodes;

}
