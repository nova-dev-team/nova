/**
 *
 */
package nova.client;

import java.net.InetAddress;
import java.util.List;

import nova.Interface.IVMMigrationCallback;
import nova.Interface.IVMPowerCallback;
import nova.Interface.IVM_INFO;
import nova.Interface.VM_HANDLE;
import nova.model.Vmachine;
import nova.model.VmachineStatus;

/**
 * @author frankvictor mailto:frankvictor@qq.com
 *
 */
public class CallbackManageThread extends Thread {

	private VM_HANDLE vh = null;
	private IVMPowerCallback callback = null;
	private IVMMigrationCallback mcallback = null;
	private VmachineStatus stopStatus = null;
	private InetAddress desaddr = null;
	private static Client cl = null;
	/**
	 *
	 */
	public CallbackManageThread(VM_HANDLE vh, IVMPowerCallback callback, VmachineStatus stopStatus) {
//		this.cl = client;
		cl = new Client(MasterInfo.MASTER_IP, MasterInfo.MASTER_USER, MasterInfo.MASTER_PASSWD);
		this.vh = vh;
		this.callback = callback;
		this.stopStatus =stopStatus;
	}
	
	public CallbackManageThread(VM_HANDLE vh, InetAddress desaddr, IVMMigrationCallback mcallback, VmachineStatus stopStatus) {
//		this.cl = client;
		cl = new Client(MasterInfo.MASTER_IP, MasterInfo.MASTER_USER, MasterInfo.MASTER_PASSWD);
		this.vh = vh;
		this.desaddr = desaddr;
		this.mcallback = mcallback;
		this.stopStatus =stopStatus;
	}
	
	public void run() {
//		WebSession.CLIENT.startVmachine(vh.value, callback);
		if(mcallback == null) {
			for(int i = 0; i<100; i++)
			{
//				System.out.println(i + tem);
				try {
					VmachineStatus vs = null;
					vs = checkStatus(this.vh.value, cl);
					if(vs.equals(stopStatus)) {
						System.out.println(vs);
						callback.onOperationDone(vh);
						return ;
					} else {
						System.out.println(vs);
					}
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			callback.onError(vh, 1);//1 maybe replaced by some macro
			
			return;
		} else {
			InetAddress srcaddr = null;
			for(int i = 0; i<100; i++)
			{
				
//				System.out.println(i + tem);
				try {
					srcaddr = cl.getHostIp(this.vh.value);
					if(srcaddr == null) {
						mcallback.OnError(vh, srcaddr, desaddr);
						return ;
					} else {
						if(srcaddr.equals(desaddr)){
							mcallback.onMigrationDone(vh, srcaddr, desaddr);
							return ;
						}
					}
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mcallback.OnError(vh, srcaddr, desaddr);//1 maybe replaced by some macro
			return;
		}
		
	}
	
	public static VmachineStatus checkStatus(String uuid, Client client) {
		VM_HANDLE vh = new VM_HANDLE();
		vh.value = uuid;
		IVM_INFO vminfo = new VMMControl(client).getVirtualMachineInfo(vh);
		if(vminfo == null) {
			System.out.println("check failed");
			return VmachineStatus.CONNECT_FAILURE; // maybe some other new status
		} else {
			String statusText = (String) vminfo.getInfo(VM_INFO.VM_STAUTS);
			System.out.println("statusText: " + statusText);
			if(statusText.equals("not-running")) {
				return VmachineStatus.NOT_RUNNING;
			} else if(statusText.equals("start-pending")) {
				return VmachineStatus.START_PENDING;
			} else if(statusText.equals("start-preparing")) {
				return VmachineStatus.START_PREPARING;
			} else if(statusText.equals("suspended")) {
				return VmachineStatus.SUSPENDED;
			} else if(statusText.equals("running")) {
				return VmachineStatus.RUNNING;
			} else if(statusText.equals("shutdown-pending")) {
				return VmachineStatus.SHUTDOWN_PENDING;
			} else if(statusText.equals("boot-failure")) {
				return VmachineStatus.BOOT_FAILURE;
			} else if(statusText.equals("connect-failure")) {
				return VmachineStatus.CONNECT_FAILURE;
			} else if(statusText.equals("shut-off")) {
				return VmachineStatus.SHUT_OFF;
			}
		}
		
		return VmachineStatus.CONNECT_FAILURE; // maybe some other new status
	}
	
	public static void main(String[] args) {
//		new CheckStateThread(3).start();
//		while(true){
//			System.out.println("Main Thread");
//			try {
//				sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

}
