package nova.master.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import nova.common.service.SimpleAddress;

import org.apache.log4j.Logger;

/**
 * Abstract of master node's database.
 * 
 * @author santa
 * 
 */
public class MasterDB {

	Logger log = Logger.getLogger(MasterDB.class);

	ArrayList<Pnode> allPnodes = new ArrayList<Pnode>();
	// HashMap<Integer, Pnode> allPnodesById = new HashMap<Integer, Pnode>();
	HashMap<SimpleAddress, Pnode> allPnodesByAddress = new HashMap<SimpleAddress, Pnode>();

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

	public synchronized void updatePnodeStatus(SimpleAddress pAddr,
			Pnode.Status status) {
		// TODO @zhaoxun update status in db

		log.debug("update " + pAddr + ", set its status to " + status);

		if (allPnodesByAddress.get(pAddr) == null) {
			// new node!
			this.addPnodeLocked(pAddr, status);
		}
		Pnode pnode = allPnodesByAddress.get(pAddr);

		if (pnode != null) {
			pnode.status = status;
		}

	}

	private void addPnodeLocked(SimpleAddress pAddr, Pnode.Status status) {

		Pnode newPnode = new Pnode();
		newPnode.addr = pAddr;

		// have to update all storage together
		this.allPnodesByAddress.put(pAddr, newPnode);
		this.allPnodes.add(newPnode);
		// TODO @zhaoxun update status in db
	}

	public void updatePnodeAliveTime(SimpleAddress pAddr) {
		Pnode pnode = allPnodesByAddress.get(pAddr);
		if (pnode != null) {
			if (pnode.status == Pnode.Status.CONNECT_FAILURE
					|| pnode.status == Pnode.Status.PENDING) {
				pnode.status = Pnode.Status.RUNNING;
			}
			pnode.lastAliveTime = new Date();
		}
	}

	public Pnode getPnodeByAddress(SimpleAddress pAddr) {
		return this.allPnodesByAddress.get(pAddr);
	}
}
