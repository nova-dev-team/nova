package nova.master.models;

import java.util.ArrayList;

/**
 * Abstract of master node's database.
 * 
 * @author santa
 * 
 */
public class MasterDB {

	/**
	 * Get list of all pnodes.
	 * 
	 * @return List of all pnodes
	 */
	public ArrayList<Pnode> getAllPnodes() {
		ArrayList<Pnode> pnodes = new ArrayList<Pnode>();
		// TODO @santa get all pnodes in database
		return pnodes;
	}

}
