package nova.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import nova.Interface.IVCControl;
import nova.Interface.IVMMControl;
import nova.Interface.IVMMigrationCallback;
import nova.Interface.IVM_INFO;
import nova.Interface.Machine;
import nova.Interface.VM_HANDLE;
import nova.test.VMMigrationCallback;
import nova.test.VMPowerCallback;

public class Main extends Thread{
	
	/**
	 * Just for demo & test use.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello! This is Nova client library, Java version");
////		Client cli = new Client("10.0.1.201:3000", "root", "monkey");
////		cli.listPmachines();
////		List l = new ArrayList();
//		IVMMControl ivmmc = new VMMControl("10.0.1.202:3000", "root", "monkey");
//		VM_HANDLE vh = new VM_HANDLE();
//		vh.value = "8b99f1d0-ea33-4a99-9003-10300733ce12";
//		VMPowerCallback vmcallback = new VMPowerCallback();
//		IVMMigrationCallback mcallback = new VMMigrationCallback();
////		IVM_INFO vminfo =  ivmmc.getVirtualMachineInfo(vh);
////		System.out.println(vminfo.toString());
//
//		
//		ivmmc.startVirtualMachine(vh, vmcallback);
//		ivmmc.shutdownVirtualMachine(vh, vmcallback);
////		ivmmc.suspendVirtualMachine(vh, vmcallback);
//		while(true) {
//			System.out.println("Main");
//			try {
//				sleep(5000);
////				new VMSuspendThread(vh, vmcallback).start();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		
		//////////////////////////////////////////////////////////
		//test getVirtualMachineList
//		InetAddress addr;
//		try {
//			addr = InetAddress.getByName("hp03");
//			System.out.println("host_ip: " + addr.getHostAddress());
//			ivmmc.migrateVirtualMachine(vh, addr, mcallback);
////			VM_HANDLE[] avm = ivmmc.getVirtualMachineList(addr);
////			for(int i=0; avm[i]!=null; i++) {
////				System.out.println(avm[i].value);
////			}
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		while(true) {
//			System.out.println("Main");
//			try {
//				sleep(5000);
////				new VMSuspendThread(vh, vmcallback).start();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		IVCControl ivcc = new VCControl("10.0.1.202:3000", "root", "monkey");
		List<Machine> l = new ArrayList<Machine>();
		Machine m = new Machine("small.img", "node3", "2", "1024", "");
		Machine m1 = new Machine("small.img", "node4", "2", "1024", "");
		Machine m2 = new Machine("small.img", "node5", "2", "1024", "");
		l.add(m);
		l.add(m1);
		l.add(m2);
		if(ivcc.createVM("linux-test3", l)) {
			System.out.println("create new vcluster successfully !");
		} else {
			System.out.println("create new vcluster failed !");
		}
	}

}
