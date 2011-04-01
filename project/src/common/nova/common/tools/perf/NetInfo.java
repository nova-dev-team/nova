package nova.common.tools.perf;

import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class NetInfo {
	private Sigar sigar = new Sigar();
	private NetInterfaceConfig config;
	private org.hyperic.sigar.NetInfo info;
	private NetInterfaceStat netstat;

	public NetInfo() throws SigarException {
		config = this.sigar.getNetInterfaceConfig(null);
		info = this.sigar.getNetInfo();
		netstat = this.sigar.getNetInterfaceStat(config.getName());
	}

	// Primary Interface name
	public String getPrimaryInterface() {
		return this.config.getName();
	}

	// Primary IP
	public String getPrimaryIP() {
		return this.config.getAddress();
	}

	// NetMask
	public String getNetMask() {
		return this.config.getNetmask();
	}

	// Gateway
	public String getDefaultGateway() {
		return this.info.getDefaultGateway();
	}

	// Primary dns
	public String getPrimaryDns() {
		return this.info.getPrimaryDns();
	}

	// Second dns
	public String getSecondDns() {
		return this.info.getSecondaryDns();
	}

	// Net bandwidth
	public long getBandWidth() {
		return this.netstat.getSpeed();
	}

	// Total upBytes
	public long getTranferBytes() {
		return this.netstat.getTxBytes();
	}

	// Total downBytes
	public long getReceiveBytes() {
		return this.netstat.getRxBytes();
	}

	// UpSpeed in B/s
	public long getUpSpeed() throws InterruptedException, SigarException {
		netstat = this.sigar.getNetInterfaceStat(config.getName());
		long upSpeed = this.getTranferBytes();
		Thread.sleep(1000);
		netstat = this.sigar.getNetInterfaceStat(config.getName());
		long newUpSpeed = this.getTranferBytes();
		return (newUpSpeed - upSpeed);
	}

	// DownSpeed in B/s
	public long getDownSpeed() throws InterruptedException, SigarException {
		netstat = this.sigar.getNetInterfaceStat(config.getName());
		long downSpeed = this.getReceiveBytes();
		Thread.sleep(1000);
		netstat = this.sigar.getNetInterfaceStat(config.getName());
		long newDownSpeed = this.getReceiveBytes();
		return (newDownSpeed - downSpeed);
	}
}
