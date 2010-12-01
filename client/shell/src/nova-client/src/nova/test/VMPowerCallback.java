/**
 * 
 */
package nova.test;

import nova.Interface.IVMPowerCallback;
import nova.Interface.VM_HANDLE;

/**
 * @author frankvictor mailto:frankvictor@qq.com
 * 
 */
public class VMPowerCallback implements IVMPowerCallback {

	/* (non-Javadoc)
	 * @see nova.Interface.IVMPowerCallback#onOperationDone(nova.Interface.VM_HANDLE)
	 */
	@Override
	public void onOperationDone(VM_HANDLE machine) {
		// TODO Auto-generated method stub
		System.out.println("vmachine start sucessfully");
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVMPowerCallback#onError(nova.Interface.VM_HANDLE, int)
	 */
	@Override
	public void onError(VM_HANDLE machine, int errno) {
		// TODO Auto-generated method stub
		switch(errno) {
			case 0:
				System.out.println("Cannot find VM with the UUID = " + machine.value);
				break;
			case 1:
				System.out.println("vmachine start failed, cannot connect");
		}
		
	}

}
