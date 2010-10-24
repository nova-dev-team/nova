package nova.Interface;

public class Machine {
	
	private static String VDISK_NAME = "vdisk_fname";
	private static String MACHINE_NAME = "machine_name";
	private static String CPU_COUNT = "cpu_count";
	private static String MEM_SIZE = "mem_size";
	private static String SOFT_LIST = "soft_list";
	
	private String machinepara;
//	private String vdisk_fname = null; 
//	private String machine_name = null; 
//	private String cpu_count = null; 
//	private String mem_size = null; 
//	private String soft_list = null; 
	
	/**
	 * 目前不支持soft_list,不论为何值均按空处理
	 * @param vdisk_fname
	 * @param machine_name
	 * @param cpu_count
	 * @param mem_size
	 * @param soft_list
	 * @return
	 */
	public Machine(String vdisk_fname, String machine_name, String cpu_count, String mem_size, String soft_list) {
		machinepara = "";
//		machinepara += "machines:\n";
		machinepara += Machine.VDISK_NAME + "=" + vdisk_fname + "\n";
		machinepara += Machine.MACHINE_NAME + "=" + machine_name + "\n";
		machinepara += Machine.CPU_COUNT + "=" + cpu_count + "\n";
		machinepara += Machine.MEM_SIZE + "=" + mem_size + "\n";
		machinepara += Machine.SOFT_LIST + "=" + soft_list + "\n";
	}

	public String getMachinepara() {
		return machinepara;
	}

	public void setMachinepara(String machinepara) {
		this.machinepara = machinepara;
	}
}
