package nova.common.tools.perf;

import com.google.gson.Gson;

/**
 * Process information. Contains CPU utilizaiton, resident and virtual set size,
 * and last time.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ProcInfo {
	public double cpuUtilization = 0;
	public long residentSetSize = 0;
	public long virtualSetSize = 0;
	public long lastTimeOfProc = 0;

	public ProcInfo() {

	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	// private Sigar sigar = new Sigar();
	// private SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
	// private long pid;
	// private ProcMem mem;
	// private ProcCpu cpu;
	//
	// // private ProcNet net;
	//
	// public void setPid(long procid) throws SigarException {
	// this.pid = procid;
	// this.mem = sigar.getProcMem(procid);
	// this.cpu = sigar.getProcCpu(procid);
	// }
	//
	// public long getCurrentPid() {
	// return this.pid;
	// }
	//
	// public String changeTimeFormat(long currentTime) {
	// DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// return fmt.format(currentTime);
	// }
	//
	// // total pid
	// public long[] getPidList() throws SigarException {
	// return this.proxy.getProcList();
	// }
	//
	// // Find pid through process name
	// public long[] getPidsThroughProcessName(String processName)
	// throws SigarException {
	// processName = "State.Name.eq=" + processName;
	// return ProcessFinder.find(proxy, processName);
	// }
	//
	// // Can be changable
	// public long getStartTime() throws SigarException {
	// return this.sigar.getProcTime(pid).getStartTime();
	// }
	//
	// // Can be changable
	// public long getCurrenTime() {
	// return System.currentTimeMillis();
	// }
	//
	// // Last time in second
	// public long getLastTime() throws SigarException {
	// return (getCurrenTime() - getStartTime()) / 1000;
	// }
	//
	// // Resident set size of process in B
	// public long getProcMemmoryRss() {
	// return mem.getResident();
	// }
	//
	// // virtual set size of process in B
	// public long getProcMemmoryVss() {
	// return mem.getSize();
	// }
	//
	// // Cpu Utilization
	// public double getProcCpuPercent() {
	// return cpu.getPercent();
	// }
}
