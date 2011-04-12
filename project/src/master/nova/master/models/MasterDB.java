package nova.master.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import nova.master.models.Pnode.Identity;

/**
 * Abstract of master node's database.
 * 
 * @author santa
 * 
 */
public class MasterDB {

	ArrayList<Pnode> allPnodes = new ArrayList<Pnode>();
	// HashMap<Integer, Pnode> allPnodesById = new HashMap<Integer, Pnode>();
	HashMap<Pnode.Identity, Pnode> allPnodesByIdentity = new HashMap<Pnode.Identity, Pnode>();

	/**
	 * Get list of all pnodes.
	 * 
	 * @return List of all pnodes
	 */
	public synchronized ArrayList<Pnode> getAllPnodes() {
		ArrayList<Pnode> pnodes = new ArrayList<Pnode>();
		// TODO @zhaoxun get all pnodes in database
		pnodes.addAll(this.allPnodes);
		return pnodes;
	}

	public synchronized void updatePnodeStatus(Pnode.Identity pIdent,
			Pnode.Status status) {
		// TODO @zhaoxun update status in db

		if (allPnodesByIdentity.get(pIdent) == null) {
			// new node!
			this.addPnodeLocked(pIdent, status);
		}

	}

	private void addPnodeLocked(Pnode.Identity pIdent, Pnode.Status status) {

		Pnode newPnode = new Pnode();
		newPnode.ident = pIdent;

		// have to update all storage together
		this.allPnodesByIdentity.put(pIdent, newPnode);
		this.allPnodes.add(newPnode);
		// TODO @zhaoxun update status in db
	}

	public void updatePnodeAliveTime(Identity pIdent) {
		Pnode pnode = allPnodesByIdentity.get(pIdent);

		if (pnode != null) {
			pnode.lastAliveTime = new Date();
		}
	}

}
