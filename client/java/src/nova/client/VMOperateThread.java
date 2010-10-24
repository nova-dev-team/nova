/**
 * 
 */
package nova.client;

import java.net.InetAddress;

import nova.Interface.IVMMigrationCallback;
import nova.Interface.IVMPowerCallback;
import nova.Interface.VM_HANDLE;
import nova.model.VmachineStatus;

/**
 * @author frankvictor mailto:frankvictor@qq.com
 * 
 */
public class VMOperateThread extends Thread {
	
	private VM_HANDLE vh = null;
	private IVMPowerCallback callback = null;
	private IVMMigrationCallback mcallback = null;
	private Client client = null;
	private String operation = null;
	private InetAddress addr = null;
	
	
	public VMOperateThread(VM_HANDLE vh, IVMPowerCallback callback, String operation) {
		super();
		this.vh = vh;
		this.callback = callback;
		this.operation = operation;
		this.client = new Client(MasterInfo.MASTER_IP, MasterInfo.MASTER_USER, MasterInfo.MASTER_PASSWD);
	}
	
	public VMOperateThread(VM_HANDLE vh, InetAddress addr, IVMMigrationCallback mcallback, String operation) {
		super();
		this.vh = vh;
		this.addr = addr;
		this.mcallback = mcallback;
		this.operation = operation;
		this.client = new Client(MasterInfo.MASTER_IP, MasterInfo.MASTER_USER, MasterInfo.MASTER_PASSWD);
	}


	public void run() {
		if(this.operation.equals(OperationOnVM.START)) {
			VmachineStatus vms = CallbackManageThread.checkStatus(vh.value, this.client);
			System.out.println(vms);
			if(vms.equals(VmachineStatus.SHUT_OFF)) {
				this.client.operateVmachine(vh.value, callback, OperationOnVM.START);
			} else if(vms.equals(VmachineStatus.SUSPENDED)) {
				this.client.operateVmachine(vh.value, callback, OperationOnVM.RESUME);
			}
			
			new CallbackManageThread(vh, callback, VmachineStatus.RUNNING).start();
		} else if(this.operation.equals(OperationOnVM.SUSPEND)) {
			VmachineStatus vms = CallbackManageThread.checkStatus(vh.value, this.client);
			System.out.println(vms);
			if(vms.equals(VmachineStatus.RUNNING)) {
				this.client.operateVmachine(vh.value, callback, OperationOnVM.SUSPEND);
			} else {
				System.out.println("suspend operation doesn't apply because vm isn't runnig");
			}
			new CallbackManageThread(vh, callback, VmachineStatus.SUSPENDED).start();
		} else if(this.operation.equals(OperationOnVM.SHUTOFF))  {
			VmachineStatus vms = CallbackManageThread.checkStatus(vh.value, this.client);
			System.out.println(vms);
			if(vms.equals(VmachineStatus.RUNNING) || vms.equals(VmachineStatus.SUSPENDED)) {
				this.client.operateVmachine(vh.value, callback, OperationOnVM.SHUTOFF);
			} else {
				System.out.println("suspend operation doesn't apply because vm isn't runnig or suspended");
			}
			new CallbackManageThread(vh, callback, VmachineStatus.SHUT_OFF).start();
		} else if(this.operation.equals(OperationOnVM.MIGRATE))  {
			VmachineStatus vms = CallbackManageThread.checkStatus(vh.value, this.client);
			System.out.println(vms);
			this.client.migrateVmachine(vh.value, addr, mcallback);
			new CallbackManageThread(vh, addr, mcallback, VmachineStatus.SHUT_OFF).start();
		}
		
	}

}
