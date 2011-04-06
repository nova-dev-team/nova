/**
 *
 */
package nova.client;

import java.util.HashMap;

import nova.Interface.IVM_INFO;

/**
 * @author frankvictor mailto:frankvictor@qq.com
 *
 */
public class VM_INFO implements IVM_INFO {

	//info in vclusters
	public static final String VM_STAUTS = "vm_status";
	public static final String VM_MEM_SIZE = "vm_mem_size";
	public static final String VM_DISK_IMAGE = "vm_disk_image";
	public static final String VM_SOFT_LIST = "vm_soft_list";
	public static final String VM_UUID = "vm_uuid";
	public static final String VM_NAME = "vm_name";
	public static final String VM_CPU_COUNT = "vm_cpu_count";
	public static final String VM_NET_IP = "vm_net_ip";
	
	//info not in vclusters
	public static final String MIGRATON_STATE = "migration_state";
	public static final String MIGRATION_PROCESS = "migration_process";
	public static final String MIGRATION_TARGET = "migration_target";
	
	//hashmap used to instore the info
	HashMap<String,String> hm = null;

	public VM_INFO() {
		super();
		hm = new HashMap<String, String>();
//		hm.put("vm_status", "");
//		hm.put("vm_mem_size", "");
//		hm.put("vm_disk_image", "");
//		hm.put("vm_soft_list", "");
//		hm.put("vm_uuid", "");
//		hm.put("vm_name", "");
//		hm.put("vm_cpu_count", "");
//		hm.put("vm_type", "");
//		hm.put("vm_net_ip", "");
//		hm.put("migration_state", "");
//		hm.put("migration_process", "");
//		hm.put("migration_target", "");
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVM_INFO#getInfo(java.lang.String)
	 */
	@Override
	public String getInfo(String key) {
		return hm.get(key);
	}

	/* (non-Javadoc)
	 * @see nova.Interface.IVM_INFO#hasInfo(java.lang.String)
	 */
	@Override
	public boolean hasInfo(String key) {
		return hm.containsKey(key);
	}
	
	public void put(String key, String value) {
		hm.put(key, value);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n" + MIGRATION_PROCESS + ": " + hm.get(MIGRATION_PROCESS) +  ",");
		sb.append("\n" + MIGRATION_TARGET + ": " + hm.get(MIGRATION_TARGET) +  ",");
		sb.append("\n" + MIGRATON_STATE + ": " + hm.get(MIGRATON_STATE) +  ",");
		sb.append("\n" + VM_CPU_COUNT + ": " + hm.get(VM_CPU_COUNT) +  ",");
		sb.append("\n" + VM_DISK_IMAGE + ": " + hm.get(VM_DISK_IMAGE) +  ",");
		sb.append("\n" + VM_MEM_SIZE + ": " + hm.get(VM_MEM_SIZE) +  ",");
		sb.append("\n" + VM_NAME + ": " + hm.get(VM_NAME) +  ",");
		sb.append("\n" + VM_NET_IP + ": " + hm.get(VM_NET_IP) +  ",");
		sb.append("\n" + VM_SOFT_LIST + ": " + hm.get(VM_SOFT_LIST) +  ",");
		sb.append("\n" + VM_STAUTS + ": " + hm.get(VM_STAUTS) +  ",");
		sb.append("\n" + VM_UUID + ": " + hm.get(VM_UUID));
		return sb.toString();
	}

}
