package nova.shell;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import nova.Interface.IVCControl;
import nova.Interface.IVMMControl;
import nova.Interface.IVMPowerCallback;
import nova.Interface.IVM_INFO;
import nova.Interface.VM_HANDLE;
import nova.client.Client;
import nova.client.VCControl;
import nova.client.VMMControl;
import nova.client.VM_INFO;
import nova.conf.Configuration;
import nova.model.Vmachine;
import nova.test.VMPowerCallback;

public class ExeShellCMD {

	public static void main(String[] args) {
		//args[0] means confDir
		IVMPowerCallback callback;
		Configuration conf = new Configuration(args[0]);
		VMMControl vmmc = new VMMControl(conf.getMaster_ip(),conf.getUsername(),conf.getPasswd());
		
		if(args[1].equals("list-clusters")) {
			Client c = new Client(conf.getMaster_ip(),conf.getUsername(),conf.getPasswd());
			List<Vmachine> list = c.listVclusters();
			Iterator<Vmachine> it = list.iterator();
			while(it.hasNext()) {
				Vmachine vm = it.next();
				System.out.println(vm.getName());
			}
		} else if(args[1].equals("list-vmachines")) {
			List<IVM_INFO> list = vmmc.getVirtualClusterInfo(new VM_HANDLE(args[2]));
			Iterator<IVM_INFO> it = list.iterator();
			if( DebugEnv.print_debug_info ) System.out.println(list.size());
			while(it.hasNext()) {
				IVM_INFO vm_info = it.next();
				System.out.println(vm_info.getInfo(VM_INFO.VM_NAME));
			}
		} else if(args[1].equals("list-vmachineinfo")) {
			VM_INFO mf = (VM_INFO) vmmc.getVirtualMachineInfo(new VM_HANDLE(args[2]));
			System.out.println(VM_INFO.VM_CPU_COUNT + ": " + mf.getInfo(VM_INFO.VM_CPU_COUNT));
			System.out.println(VM_INFO.VM_DISK_IMAGE + ": " + mf.getInfo(VM_INFO.VM_DISK_IMAGE));
			System.out.println(VM_INFO.VM_MEM_SIZE + ": " + mf.getInfo(VM_INFO.VM_MEM_SIZE));
			System.out.println(VM_INFO.VM_NAME + ": " + mf.getInfo(VM_INFO.VM_NAME));
			System.out.println(VM_INFO.VM_NET_IP + ": " + mf.getInfo(VM_INFO.VM_NET_IP));
			System.out.println(VM_INFO.VM_SOFT_LIST + ": " + mf.getInfo(VM_INFO.VM_SOFT_LIST));
			System.out.println(VM_INFO.VM_STAUTS + ": " + mf.getInfo(VM_INFO.VM_STAUTS));
			System.out.println(VM_INFO.VM_UUID + ": " + mf.getInfo(VM_INFO.VM_UUID));
		} else if(args[1].equals("start-vmachines")) {
			callback = new VMPowerCallback();
			if(args.length < 4) {
				vmmc.startVirtualMachine(new VM_HANDLE(args[2]), callback);
			} else {
				vmmc.startVirtualMachine(new VM_HANDLE(args[2]), args[3], callback);
			}
		} else if(args[1].equals("destroy-vmachines")) {
			callback = new VMPowerCallback();
			vmmc.powerdownVirtualMachine(new VM_HANDLE(args[2]), callback);
		} else if(args[1].equals("suspend-vmachines")) {
			callback = new VMPowerCallback();
			vmmc.suspendVirtualMachine(new VM_HANDLE(args[2]), callback);
		} else if(args[1].equals("resume-vmachines")) {
			callback = new VMPowerCallback();
			vmmc.startVirtualMachine(new VM_HANDLE(args[2]), callback);
		} else if(args[1].equals("create-cluster")) {
			VMCreateFile vmcf = new VMCreateFile(args[2]);
			vmcf.creatCluster(conf.getMaster_ip(), conf.getUsername(), conf.getPasswd());
		}
		
	}
}
