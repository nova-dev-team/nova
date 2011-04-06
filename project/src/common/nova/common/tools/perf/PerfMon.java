package nova.common.tools.perf;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;
import org.hyperic.sigar.ptql.ProcessFinder;

/**
 * Methods of get system information
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class PerfMon {

	private static Sigar sigar = new Sigar();
	private static SigarProxy proxy = SigarProxyCache.newInstance(sigar);

	/**
	 * Utilization, frequency, quantity and model of cpu
	 * 
	 * @return {@link CpuInfo}
	 */
	public static CpuInfo getCpuInfo() {
		CpuInfo cpu = new CpuInfo();

		try {
			cpu.combinedTime = sigar.getCpuPerc().getCombined();
			org.hyperic.sigar.CpuInfo[] infoList = sigar.getCpuInfoList();
			cpu.mhz = infoList[0].getMhz();
			cpu.nCpu = infoList.length;
			cpu.model = infoList[0].getModel();
		} catch (SigarException e) {
			e.printStackTrace();
		}

		return cpu;
	}

	/**
	 * Memory information. Memory size in B, RAM in MB
	 * 
	 * @return {@link MemoryInfo}
	 */

	public static MemoryInfo getMemoryInfo() {
		MemoryInfo mem = new MemoryInfo();
		try {
			mem.freeMemorySize = sigar.getMem().getActualFree();
			mem.usedMemorySize = sigar.getMem().getActualUsed();
			mem.totalMemorySize = sigar.getMem().getTotal();
			mem.ramSize = sigar.getMem().getRam();
		} catch (SigarException e) {
			e.printStackTrace();
		}

		return mem;
	}

	/**
	 * Disk information. Disk in KB
	 * 
	 * @return
	 */
	public static DiskInfo getDiskInfo() {
		DiskInfo disk = new DiskInfo();
		try {
			FileSystem[] fs = proxy.getFileSystemList();
			for (int i = 0; i < fs.length; i++) {
				disk.totalDiskSize += sigar.getFileSystemUsage(
						fs[i].getDirName()).getTotal();
				disk.usedDiskSize += sigar.getFileSystemUsage(
						fs[i].getDirName()).getUsed();
				disk.freeDiskSize += sigar.getFileSystemUsage(
						fs[i].getDirName()).getFree();
			}
		} catch (SigarException e) {
			e.printStackTrace();
		}
		return disk;
	}

	/**
	 * Net information. BandWidth, down speed and up speed of net information;
	 * Speed in B/s
	 * 
	 * @return {@link NetInfo}
	 */
	public static NetInfo getNetInfo() {
		NetInfo net = new NetInfo();

		try {
			NetInterfaceConfig config = sigar.getNetInterfaceConfig(null);
			NetInterfaceStat netstat = sigar.getNetInterfaceStat(config
					.getName());
			net.bandWidth = netstat.getSpeed();
			net.downSpeed = netstat.getRxBytes();
			net.upSpeed = netstat.getTxBytes();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			netstat = sigar.getNetInterfaceStat(config.getName());
			net.downSpeed = netstat.getRxBytes() - net.downSpeed;
			net.upSpeed = netstat.getTxBytes() - net.upSpeed;
		} catch (SigarException e) {
			e.printStackTrace();
		}

		return net;
	}

	/**
	 * Get process information through its name. CPU utilization in double.
	 * virtual set size and resident set size in B. Last time in second.
	 * 
	 * @param processName
	 * @return {@link ProcInfo}
	 */
	public static ProcInfo getProcInfo(String processName) {
		ProcInfo process = new ProcInfo();

		try {
			processName = "State.Name.eq=" + processName;
			long[] pidList = ProcessFinder.find(proxy, processName);
			for (int i = 0; i < pidList.length; i++) {
				process.cpuUtilization += sigar.getProcCpu(pidList[i])
						.getPercent();
				process.residentSetSize += sigar.getProcMem(pidList[i])
						.getResident();
				process.virtualSetSize += sigar.getProcMem(pidList[i])
						.getSize();
			}
			process.lastTimeOfProc = (System.currentTimeMillis() - sigar
					.getProcTime(pidList[0]).getStartTime()) / 1000;
		} catch (SigarException e) {
			e.printStackTrace();
		}
		return process;
	}

	/**
	 * Get common monitor information.
	 * 
	 * @return {@link CommonMonitorInfo}
	 */
	public static CommonMonitorInfo getCommonMonitorInfo() {
		CommonMonitorInfo cMonitor = new CommonMonitorInfo();
		cMonitor.cpuInfo = PerfMon.getCpuInfo();
		cMonitor.memInfo = PerfMon.getMemoryInfo();
		cMonitor.diskInfo = PerfMon.getDiskInfo();
		cMonitor.netInfo = PerfMon.getNetInfo();
		return cMonitor;
	}
}
