package nova.client.java.model;


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
		sb.append("status=" + status + ",");
		sb.append("vmCapacity=" + vmCapacity + ",");
		sb.append("vmPreparing=" + vmPreparing + ",");
		sb.append("vmFailure=" + vmFailure + ",");
		sb.append("hostname=" + hostname + ",");
		sb.append("id=" + id + ",");
		sb.append("vmRunning=" + vmRunning + ",");
		sb.append("ip=" + ip);
		return sb.toString();
	}
}
