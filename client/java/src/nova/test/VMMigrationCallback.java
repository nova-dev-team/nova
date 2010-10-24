package nova.test;

import java.net.InetAddress;

import nova.Interface.IVMMigrationCallback;
import nova.Interface.VM_HANDLE;

public class VMMigrationCallback implements IVMMigrationCallback {

	@Override
	public void onMigrationBegin(VM_HANDLE machine, InetAddress srcAddr,
			InetAddress desAddr) {
		System.out.println(machine.value + " begin to migrate from: " + srcAddr + "to " + desAddr);

	}

	@Override
	public void onMigrationDone(VM_HANDLE machine, InetAddress srcAddr,
			InetAddress desAddr) {
		System.out.println(machine.value + "sucessfully  migrated from: " + srcAddr + "to " + desAddr);
	}

	@Override
	public void OnError(VM_HANDLE machine, InetAddress srcAddr,
			InetAddress desAddr) {
		System.out.println("error occurs when " + machine.value + " migrate from: " + srcAddr + "to " + desAddr);
	}

}
