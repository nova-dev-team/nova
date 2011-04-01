package nova.common.tools.perf;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.google.gson.Gson;

public class CpuInfo {

	public final double combinedTime;
	public final int mhz;
	public final int nCpu;
	public final String model;

	private static Sigar sigar = new Sigar();

	public static CpuInfo get() {
		CpuInfo info = null;
		try {
			info = new CpuInfo();
		} catch (SigarException e) {
			e.printStackTrace();
		}
		return info;
	}

	private CpuInfo() throws SigarException {
		this.combinedTime = sigar.getCpuPerc().getCombined();
		org.hyperic.sigar.CpuInfo[] infoList = sigar.getCpuInfoList();
		this.mhz = infoList[0].getMhz();
		this.nCpu = infoList.length;
		this.model = infoList[0].getModel();
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
