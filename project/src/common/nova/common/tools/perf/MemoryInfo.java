package nova.common.tools.perf;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

public class MemoryInfo {
	private Mem mem;
	private Swap swap;
	private Sigar sigar = new Sigar();

	private Long format(long value) // KB
	{
		return new Long(value / 1024);
	}

	public MemoryInfo() throws SigarException {
		this.mem = this.sigar.getMem();
		this.swap = this.sigar.getSwap();
	}

	// total memory
	public long getTotalMemory() {
		return this.format(mem.getTotal());
	}

	// used memory
	public long getUsedMemory() {
		return this.format(mem.getUsed());
	}

	// free memory
	public long getFreeMemory() {
		return this.format(mem.getFree());
	}

	// ram
	public long getTotalRam() // MB
	{
		return mem.getRam();
	}

	// total swap
	public long getTotalSwap() {
		return this.format(swap.getTotal());
	}

	// used swap
	public long getUsedSwap() {
		return this.format(swap.getUsed());
	}

	// free swap
	public long getFreeSwap() {
		return this.format(swap.getFree());
	}

	// page out
	public long getPageOut() {
		return this.format(swap.getPageOut());
	}

	// page in
	public long getPageIn() {
		return this.format(swap.getPageIn());
	}
}
