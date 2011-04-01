package nova.common.tools.perf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;
import org.hyperic.sigar.ptql.ProcessFinder;

public class ProcInfo {
	private Sigar sigar = new Sigar();
	private SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
	private long pid;
	private ProcMem mem;
	private ProcCpu cpu;

	// private ProcNet net;

	public void setPid(long procid) throws SigarException {
		this.pid = procid;
		this.mem = sigar.getProcMem(procid);
		this.cpu = sigar.getProcCpu(procid);
	}

	public long getCurrentPid() {
		return this.pid;
	}

	public String changeTimeFormat(long currentTime) {
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return fmt.format(currentTime);
	}

	// total pid
	public long[] getPidList() throws SigarException {
		return this.proxy.getProcList();
	}

	// Find pid through process name
	public long[] getPidsThroughProcessName(String processName)
			throws SigarException {
		processName = "State.Name.eq=" + processName;
		return ProcessFinder.find(proxy, processName);
	}

	// Can be changable
	public long getStartTime() throws SigarException {
		return this.sigar.getProcTime(pid).getStartTime();
	}

	// Can be changable
	public long getCurrenTime() {
		return System.currentTimeMillis();
	}

	// Last time in second
	public long getLastTime() throws SigarException {
		return (getCurrenTime() - getStartTime()) / 1000;
	}

	// Resident set size of process in B
	public long getProcMemmoryRss() {
		return mem.getResident();
	}

	// virtual set size of process in B
	public long getProcMemmoryVss() {
		return mem.getSize();
	}

	// Cpu Utilization
	public double getProcCpuPercent() {
		return cpu.getPercent();
	}
}
