package nova.common.tools.perf;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class CpuInfo {

	private CpuPerc cpu;
	private Sigar sigar = new Sigar();
	private org.hyperic.sigar.CpuInfo info;

	public CpuInfo() throws SigarException {
		org.hyperic.sigar.CpuInfo[] infos = this.sigar.getCpuInfoList();
		this.info = infos[0];
	}

	public CpuPerc[] getCpus() throws SigarException {
		return this.sigar.getCpuPercList();
	}

	// cpu numbers
	public int getCpuNum() throws SigarException {
		return this.sigar.getCpuPercList().length;
	}

	public void setCpu(CpuPerc cpuChosed) {
		cpu = cpuChosed;
	}

	// user time
	public Double getCpuUserTime() {
		return cpu.getUser();
	}

	// system time
	public Double getCpuSystemTime() {
		return cpu.getSys();
	}

	// total time
	public Double getCpuCombinedTime() {
		return cpu.getCombined();
	}

	// Cpu Mhz
	public int getCpuMhz() {
		return this.info.getMhz();
	}

	// Cpu Model
	public String getCpuModel() {
		return this.info.getModel();
	}
}
