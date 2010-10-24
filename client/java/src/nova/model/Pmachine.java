package nova.model;


public class Pmachine {
	public int vmRunning;
	public PmachineStatus status;
	public int vmPreparing;
	public int vmCapacity;
	public int vmFailure;
	public String hostname;
	public int id;
	public String ip;
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nstatus=" + status + ",");
		sb.append("\nvmCapacity=" + vmCapacity + ",");
		sb.append("\nvmPreparing=" + vmPreparing + ",");
		sb.append("\nvmFailure=" + vmFailure + ",");
		sb.append("\nhostname=" + hostname + ",");
		sb.append("\nid=" + id + ",");
		sb.append("\nvmRunning=" + vmRunning + ",");
		sb.append("\nip=" + ip);
		return sb.toString();
	}
}
