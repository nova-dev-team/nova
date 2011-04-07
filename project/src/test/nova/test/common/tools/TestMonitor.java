package nova.test.common.tools;

import nova.common.tools.perf.CpuInfo;
import nova.common.tools.perf.DiskInfo;
import nova.common.tools.perf.GeneralMonitorInfo;
import nova.common.tools.perf.MemoryInfo;
import nova.common.tools.perf.NetInfo;
import nova.common.tools.perf.PerfMon;
import nova.common.tools.perf.ProcInfo;

import org.hyperic.sigar.SigarException;
import org.junit.Test;

public class TestMonitor {
	@Test
	public void TestCpuInfo() throws SigarException {
		CpuInfo ci = PerfMon.getCpuInfo();
		System.out.println(ci);
	}

	@Test
	public void TestMemoryInfo() {
		MemoryInfo mi = PerfMon.getMemoryInfo();
		System.out.println(mi);
	}

	@Test
	public void TestDiskInfo() {
		DiskInfo di = PerfMon.getDiskInfo();
		System.out.println(di);
	}

	@Test
	public void TestNetInfo() {
		NetInfo ni = PerfMon.getNetInfo();
		System.out.println(ni);
	}

	@Test
	public void TestProcInfo() {
		ProcInfo pi = PerfMon.getProcInfo("java");
		System.out.println(pi);
	}

	@Test
	public void TestGeneralMonitorInfo() {
		GeneralMonitorInfo cmi = PerfMon.getGeneralMonitorInfo();
		System.out.println(cmi);
	}

	// @Test
	// public void TestMemoryInfo() throws SigarException {
	// MemoryInfo mi = new MemoryInfo();
	// System.out.println("Memory:");
	// System.out.println("Total: " + mi.getTotalMemory());
	// System.out.println("Used: " + mi.getUsedMemory());
	// System.out.println("Free: " + mi.getFreeMemory());
	// System.out.println("RAM:" + mi.getTotalRam() + "MB");
	//
	// System.out.println("Swap:");
	// System.out.println("Total: " + mi.getTotalSwap());
	// System.out.println("Used: " + mi.getUsedSwap());
	// System.out.println("Free: " + mi.getFreeSwap());
	// System.out.println("Page Out:" + mi.getPageOut());
	// System.out.println("Page In:" + mi.getPageIn());
	// }
	//
	// @Test
	// public void TestDiskInfo() throws SigarException {
	// DiskInfo di = new DiskInfo();
	// FileSystem[] fss = di.getFileSystems();
	// for (int i = 0; i < fss.length; i++) {
	// di.setFileSystem(fss[i]);
	// System.out.println(di.getFileSystemName());
	// System.out.println("Size: " + di.getTotalDisk());
	// System.out.println("Used: " + di.getUsedDisk());
	// System.out.println("Avail: " + di.getFreeDisk());
	// }
	// }
	//
	// @Test
	// public void TestNetInfo() throws SigarException, InterruptedException {
	// NetInfo ni = new NetInfo();
	// System.out.println("Name: " + ni.getPrimaryInterface());
	// System.out.println("IP: " + ni.getPrimaryIP());
	// System.out.println("Netmask: " + ni.getNetMask());
	// System.out.println("Primary dns: " + ni.getPrimaryDns());
	// System.out.println("Second dns: " + ni.getSecondDns());
	// System.out.println("Default gateway: " + ni.getDefaultGateway());
	// System.out.println("BandWidth: " + ni.getBandWidth());
	// System.out.println("UpSpeed: " + ni.getUpSpeed());
	// System.out.println("DownSpeed: " + ni.getDownSpeed());
	// }
	//
	// @Test
	// public void TestSystemInfo() throws SigarException {
	// SystemInfo si = new SystemInfo();
	// System.out.println("Hostname = " + si.getHostname());
	// System.out.println("IPAddress = " + si.getIPAddress());
	// System.out.println("CpuInfo = " + si.getCpuModel());
	// System.out.println("CpuNum = " + si.getCpuNum());
	// System.out.println("Memory Size = " + si.getMemeorySize());
	// System.out.println("Swap Size = " + si.getSwapSize());
	// System.out.println("Uptime = " + si.getUpTime());
	//
	// }
	//
	// @Test
	// public void TestProcInfo() throws SigarException {
	// ProcInfo pi = new ProcInfo();
	// long[] pids = pi.getPidList();
	// System.out.println("Pids:");
	// for (int i = 0; i < pids.length; i++)
	// System.out.println(pids[i]);
	//
	// pids = pi.getPidsThroughProcessName("QQ");
	// System.out.println("svchost pids:");
	// for (int i = 0; i < pids.length; i++)
	// System.out.println(pids[i]);
	// pi.setPid(pids[0]);
	// System.out.println("QQ start time: "
	// + pi.changeTimeFormat(pi.getStartTime()));
	// System.out.println("Current Time: "
	// + pi.changeTimeFormat(pi.getCurrenTime()));
	// System.out.println("QQ time last: " + pi.getLastTime());
	// System.out.println("QQ Rss: " + pi.getProcMemmoryRss());
	// System.out.println("QQ Vss: " + pi.getProcMemmoryVss());
	// System.out.println("Cpu Util: " + pi.getProcCpuPercent());
	// }
}
