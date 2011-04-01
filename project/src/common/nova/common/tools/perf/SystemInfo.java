package nova.common.tools.perf;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class SystemInfo {
	private Sigar sigar = new Sigar();
	private CpuInfo cpu;
	private MemoryInfo mem;
	private NetInfo net;

	public SystemInfo() throws SigarException {
		this.cpu = new CpuInfo();
		this.mem = new MemoryInfo();
		this.net = new NetInfo();
	}

	/**
	 * 时间标准显示
	 */
	public static String formatUptime(double uptime) {
		String retval = "";

		int days = (int) uptime / (60 * 60 * 24);
		int minutes, hours;

		if (days != 0) {
			retval += days + " " + ((days > 1) ? "days" : "day") + ", ";
		}

		minutes = (int) uptime / 60;
		hours = minutes / 60;
		hours %= 24;
		minutes %= 60;

		if (hours != 0) {
			retval += hours + ":" + minutes;
		} else {
			retval += minutes + " min";
		}

		return retval;
	}

	/**
	 * Hostname
	 */
	public String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "unknown";
		}
	}

	// Cpu Model
	public String getCpuModel() {
		return this.cpu.getCpuModel();
	}

	// Cpu number
	public int getCpuNum() throws SigarException {
		return this.cpu.getCpuNum();
	}

	// Memory Size
	public long getMemeorySize() {
		return this.mem.getTotalMemory();
	}

	// Swap size
	public long getSwapSize() {
		return this.mem.getTotalSwap();
	}

	// IP address
	public String getIPAddress() {
		return this.net.getPrimaryIP();
	}

	// Up time
	public double getUpTime() throws SigarException {
		return this.sigar.getUptime().getUptime();
	}
}
