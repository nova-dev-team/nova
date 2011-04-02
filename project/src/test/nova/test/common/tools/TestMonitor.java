package nova.test.common.tools;

import nova.common.tools.perf.CpuInfo;
import nova.common.tools.perf.DiskInfo;
import nova.common.tools.perf.MemoryInfo;
import nova.common.tools.perf.NetInfo;
import nova.common.tools.perf.PerfMon;
import nova.common.tools.perf.ProcInfo;
import nova.common.tools.perf.SystemInfo;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.SigarException;
import org.junit.Test;

public class TestMonitor {
	@Test
	public void TestCpuInfo() throws SigarException {
		CpuInfo ci = PerfMon.getCpuInfo();
		System.out.println(ci);
	}

	@Test
	public void TestMemoryInfo() throws SigarException {
		MemoryInfo mi = new MemoryInfo();
		System.out.println("Memory:");
		System.out.println("Total: " + mi.getTotalMemory());
		System.out.println("Used: " + mi.getUsedMemory());
		System.out.println("Free: " + mi.getFreeMemory());
		System.out.println("RAM:" + mi.getTotalRam() + "MB");

		System.out.println("Swap:");
		System.out.println("Total: " + mi.getTotalSwap());
		System.out.println("Used: " + mi.getUsedSwap());
		System.out.println("Free: " + mi.getFreeSwap());
		System.out.println("Page Out:" + mi.getPageOut());
		System.out.println("Page In:" + mi.getPageIn());
	}

	@Test
	public void TestDiskInfo() throws SigarException {
		DiskInfo di = new DiskInfo();
		FileSystem[] fss = di.getFileSystems();
		for (int i = 0; i < fss.length; i++) {
			di.setFileSystem(fss[i]);
			System.out.println(di.getFileSystemName());
			System.out.println("Size: " + di.getTotalDisk());
			System.out.println("Used: " + di.getUsedDisk());
			System.out.println("Avail: " + di.getFreeDisk());
		}
	}

	@Test
	public void TestNetInfo() throws SigarException, InterruptedException {
		NetInfo ni = new NetInfo();
		System.out.println("Name: " + ni.getPrimaryInterface());
		System.out.println("IP: " + ni.getPrimaryIP());
		System.out.println("Netmask: " + ni.getNetMask());
		System.out.println("Primary dns: " + ni.getPrimaryDns());
		System.out.println("Second dns: " + ni.getSecondDns());
		System.out.println("Default gateway: " + ni.getDefaultGateway());
		System.out.println("BandWidth: " + ni.getBandWidth());
		System.out.println("UpSpeed: " + ni.getUpSpeed());
		System.out.println("DownSpeed: " + ni.getDownSpeed());
	}

	@Test
	public void TestSystemInfo() throws SigarException {
		SystemInfo si = new SystemInfo();
		System.out.println("Hostname = " + si.getHostname());
		System.out.println("IPAddress = " + si.getIPAddress());
		System.out.println("CpuInfo = " + si.getCpuModel());
		System.out.println("CpuNum = " + si.getCpuNum());
		System.out.println("Memory Size = " + si.getMemeorySize());
		System.out.println("Swap Size = " + si.getSwapSize());
		System.out.println("Uptime = " + si.getUpTime());

	}

	@Test
	public void TestProcInfo() throws SigarException {
		ProcInfo pi = new ProcInfo();
		long[] pids = pi.getPidList();
		System.out.println("Pids:");
		for (int i = 0; i < pids.length; i++)
			System.out.println(pids[i]);

		String interestedProcessName = "java";
		pids = pi.getPidsThroughProcessName(interestedProcessName);
		System.out.println("svchost pids:");
		for (int i = 0; i < pids.length; i++)
			System.out.println(pids[i]);
		if (pids.length > 0) {
			pi.setPid(pids[0]);
			System.out.println(interestedProcessName + " start time: "
					+ pi.changeTimeFormat(pi.getStartTime()));
			System.out.println("Current Time: "
					+ pi.changeTimeFormat(pi.getCurrenTime()));
			System.out.println(interestedProcessName + " time last: "
					+ pi.getLastTime());
			System.out.println(interestedProcessName + " Rss: "
					+ pi.getProcMemmoryRss());
			System.out.println(interestedProcessName + " Vss: "
					+ pi.getProcMemmoryVss());
			System.out.println("Cpu Util: " + pi.getProcCpuPercent());
		}
	}
}
