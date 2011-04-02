package nova.common.tools.perf;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class PerfMon {

	private static Sigar sigar = new Sigar();

	public static CpuInfo getCpuInfo() {
		CpuInfo info = new CpuInfo();

		try {
			info.combinedTime = sigar.getCpuPerc().getCombined();
			org.hyperic.sigar.CpuInfo[] infoList = sigar.getCpuInfoList();
			info.mhz = infoList[0].getMhz();
			info.nCpu = infoList.length;
			info.model = infoList[0].getModel();
		} catch (SigarException e) {
			e.printStackTrace();
		}

		return info;
	}

}
