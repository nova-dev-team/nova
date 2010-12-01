package nova.model;

/**
 * @author frankvictor
 * Actions: edit, host_ip, migrate_to, migration_status, reset_error, resume, shut_off, start, and suspend
 */
public class Vmachine {
	
	private String name;
	private int size;
	private String first_ip;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getFirst_ip() {
		return first_ip;
	}
	public void setFirst_ip(String first_ip) {
		this.first_ip = first_ip;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\nname=" + name + ",");
		sb.append("\nsize=" + size + ",");
		sb.append("\nfirst_ip=" + first_ip);
		return sb.toString();
	}
}
