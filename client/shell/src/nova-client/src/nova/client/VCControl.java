/**
 * 
 */
package nova.client;

import java.util.List;

import nova.Interface.IVCControl;
import nova.Interface.Machine;
import nova.shell.DebugEnv;

/**
 * @author frankvictor mailto:frankvictor@qq.com
 * 
 */
public class VCControl implements IVCControl {

	private String machinecreatepara = "";
	private Client client = null;
	
	public VCControl(String master_ip, String user, String passwd) {
		super();
		MasterInfo.MASTER_IP = master_ip;
		MasterInfo.MASTER_USER = user;
		MasterInfo.MASTER_PASSWD = passwd;
		this.client = new Client(MasterInfo.MASTER_IP, MasterInfo.MASTER_USER, MasterInfo.MASTER_PASSWD);
	}

	public VCControl(Client client) {
		super();
		this.client = client;
	}
	/* (non-Javadoc)
	 * @see nova.Interface.IVCControl#createVM(java.lang.String, int, nova.Interface.Machine[])
	 */
	@Override
	public boolean createVM(String name, List<Machine> machines) {
//		if(size != machines.size()) {
//			if( DebugEnv.print_debug_info ) System.out.println("vcluster size doesn't match vmachine number!");
//			return false;
//		}
		int size = machines.size();
		this.machinecreatepara = "";
//		this.machinecreatepara = "name:\n";
//		this.machinecreatepara += name + "\n\n";
//		this.machinecreatepara += "size:\n";
//		this.machinecreatepara += size + "\n\n";
		for(Machine m : machines) {
			this.machinecreatepara += m.getMachinepara() + "\n";
		}
		if( DebugEnv.print_debug_info ) System.out.println(this.machinecreatepara);
		return client.createVcluster(name, size, this.machinecreatepara);
	}

	@Override
	public List<String> listVclusters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> listVmachines(String clustername) {
		// TODO Auto-generated method stub
		return null;
	}

}
