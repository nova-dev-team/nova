/**
 *
 */
package nova.client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import nova.Interface.ISTATE_INFO;
import nova.Interface.IVMMControl;
import nova.Interface.IVMMigrationCallback;
import nova.Interface.IVMPowerCallback;
import nova.Interface.IVMStateCallback;
import nova.Interface.IVM_INFO;
import nova.Interface.STATE_HANDLE;
import nova.Interface.VM_HANDLE;

/**
 * @author frankvictor
 *
 */
public class VMMControl implements IVMMControl {

	
	private Client client = null;
	
	public VMMControl(String master_ip, String user, String passwd) {
		super();
		MasterInfo.MASTER_IP = master_ip;
		MasterInfo.MASTER_USER = user;
		MasterInfo.MASTER_PASSWD = passwd;
		this.client = new Client(MasterInfo.MASTER_IP, MasterInfo.MASTER_USER, MasterInfo.MASTER_PASSWD);
	}

	public VMMControl(Client client) {
		super();
		this.client = client;
	}



	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#getVirtualMachineList(java.net.InetAddress)
	 */
	@Override
	public VM_HANDLE[] getVirtualMachineList(InetAddress addr) {
		VM_HANDLE[] avm = new VM_HANDLE[50];
		JsonObject obj = client.getRunningVmachines(addr);
		if (obj.get("success").getAsBoolean() == true) {
			JsonObject vmachine = obj.get("data").getAsJsonObject();
			JsonArray vmachineinfo = vmachine.get("vm_list").getAsJsonArray();
			int i = 0;
			for(JsonElement eobj : vmachineinfo) {
				JsonObject vmObj = (eobj.getAsJsonObject()).get("vmachine").getAsJsonObject();
				avm[i] = new VM_HANDLE();
				avm[i++].value = vmObj.get("uuid").getAsString();
			}
		}
		return avm;
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#getVirtualMachineInfo(nova.Interface.VM_HANDLE)
	 */
	public List<IVM_INFO> getVirtualClusterInfo(VM_HANDLE machine) {
		List<IVM_INFO> list = null;
		
		JsonObject obj = client.getVclustersInfo(machine.value);
		if (obj.get("success").getAsBoolean() == true) {
			String first_ip = obj.get("first_ip").getAsString();
			int p_dot = first_ip.lastIndexOf('.');
			String prefix_ip = first_ip.substring(0, p_dot);
			int suffix_ip = Integer.parseInt(first_ip.substring(p_dot + 1));
			JsonArray vmachineArray = obj.get("machines").getAsJsonArray();
			for (JsonElement vmElem : vmachineArray ) {
				list = new ArrayList<IVM_INFO>();
				JsonObject vmObj = vmElem.getAsJsonObject();
				VM_INFO ivi = new VM_INFO();
				ivi.put(VM_INFO.VM_CPU_COUNT, vmObj.get("cpu_count").getAsString());
				ivi.put(VM_INFO.VM_DISK_IMAGE, vmObj.get("disk_image").getAsString());
				ivi.put(VM_INFO.VM_MEM_SIZE, vmObj.get("mem_size").getAsString());
				ivi.put(VM_INFO.VM_NAME, vmObj.get("name").getAsString());	
				ivi.put(VM_INFO.VM_SOFT_LIST, vmObj.get("soft_list").getAsString());
				ivi.put(VM_INFO.VM_STAUTS, vmObj.get("status").getAsString());
				ivi.put(VM_INFO.VM_UUID, vmObj.get("uuid").getAsString());
				ivi.put(VM_INFO.VM_NET_IP, prefix_ip + suffix_ip);
				suffix_ip++;
				list.add(ivi);
			}
		}
		return list;
	}
	
	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#getVirtualMachineInfo(nova.Interface.VM_HANDLE)
	 */
	@Override
	public IVM_INFO getVirtualMachineInfo(VM_HANDLE machine) {
		VM_INFO ivi = null;
		JsonObject obj = client.getVmachineInfo(machine.value);
		if (obj.get("success").getAsBoolean() == true) {
			JsonObject vmachine = obj.get("data").getAsJsonObject();
			JsonObject vmachineinfo = vmachine.get("vmachine").getAsJsonObject();
			ivi = new VM_INFO();
			ivi.put(VM_INFO.VM_CPU_COUNT, vmachineinfo.get("cpu_count").getAsString());
			ivi.put(VM_INFO.VM_DISK_IMAGE, vmachineinfo.get("hda").getAsString());
			ivi.put(VM_INFO.VM_MEM_SIZE, vmachineinfo.get("memory_size").getAsString());
			ivi.put(VM_INFO.VM_NAME, vmachineinfo.get("name").getAsString());	
			ivi.put(VM_INFO.VM_SOFT_LIST, vmachineinfo.get("soft_list").getAsString());
			ivi.put(VM_INFO.VM_STAUTS, vmachineinfo.get("status").getAsString());
			ivi.put(VM_INFO.VM_UUID, vmachineinfo.get("uuid").getAsString());
			ivi.put(VM_INFO.VM_NET_IP, vmachineinfo.get("ip").getAsString());
//			if(vmachineinfo.get("migrate_to").getAsString() != "")
//				ivi.put(VM_INFO.MIGRATION_TARGET, vmachineinfo.get("migrate_to").getAsString());
			}
		return ivi;
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#startVirtualMachine(nova.Interface.VM_HANDLE, nova.Interface.IVMPowerCallback)
	 */
	@Override
	public void startVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback) {
			new VMOperateThread(machine, callback, OperationOnVM.START).start();
			System.out.println("start Virtualmachine finished successfully(VMMControl)");
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#powerdownVirtualMachine(nova.Interface.VM_HANDLE, nova.Interface.IVMPowerCallback)
	 */
	@Override
	public void powerdownVirtualMachine(VM_HANDLE machine,
			IVMPowerCallback callback) {
		new VMOperateThread(machine, callback, OperationOnVM.SHUTOFF).start();
		System.out.println("shutoff Virtualmachine finished successfully(VMMControl)");
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#shutdownVirtualMachine(nova.Interface.VM_HANDLE, nova.Interface.IVMPowerCallback)
	 */
	@Override
	public void shutdownVirtualMachine(VM_HANDLE machine,
			IVMPowerCallback callback) {
		new VMOperateThread(machine, callback, OperationOnVM.SHUTOFF).start();
		System.out.println("shutoff Virtualmachine finished successfully(VMMControl)");

	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#resetVirtualMachine(nova.Interface.VM_HANDLE, nova.Interface.IVMPowerCallback)
	 */
	@Override
	public void resetVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#pauseVirtualMachine(nova.Interface.VM_HANDLE, nova.Interface.IVMPowerCallback)
	 */
	@Override
	public void pauseVirtualMachine(VM_HANDLE machine, IVMPowerCallback callback) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#suspendVirtualMachine(nova.Interface.VM_HANDLE, nova.Interface.IVMPowerCallback)
	 */
	@Override
	public void suspendVirtualMachine(VM_HANDLE machine,
			IVMPowerCallback callback) {
		new VMOperateThread(machine, callback, OperationOnVM.SUSPEND).start();
		System.out.println("suspend Virtualmachine finished successfully(VMMControl)");
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#migrateVirtualMachine(nova.Interface.VM_HANDLE, java.net.InetAddress, nova.Interface.IVMMigrationCallback)
	 */
	@Override
	public void migrateVirtualMachine(VM_HANDLE machine, InetAddress addr,
			IVMMigrationCallback callback) {
		new VMOperateThread(machine, addr, callback, OperationOnVM.MIGRATE).start();
		System.out.println("migrate Virtualmachine finished successfully(VMMControl)");
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#getVirtualMachineStateList(nova.Interface.VM_HANDLE)
	 */
	@Override
	public STATE_HANDLE[] getVirtualMachineStateList(VM_HANDLE machine) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#getVirtualMachineStateInfo(nova.Interface.VM_HANDLE, nova.Interface.STATE_HANDLE)
	 */
	@Override
	public ISTATE_INFO getVirtualMachineStateInfo(VM_HANDLE machine,
			STATE_HANDLE state) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#saveVirtualMachineState(nova.Interface.VM_HANDLE, nova.Interface.IVMStateCallback)
	 */
	@Override
	public void saveVirtualMachineState(VM_HANDLE machine,
			IVMStateCallback callback) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMMControl#loadVirtualMachineState(nova.Interface.STATE_HANDLE, nova.Interface.IVMStateCallback)
	 */
	@Override
	public void loadVirtualMachineState(STATE_HANDLE state,
			IVMStateCallback callback) {
		// TODO Auto-generated method stub

	}

}
