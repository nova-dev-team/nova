package nova.test.unit.common;

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

}
