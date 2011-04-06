package nova.common.tools.perf;

import com.google.gson.Gson;

/**
 * General monitor information. Contains cpuInfo, memInfo, diskInfo, and netInfo
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class CommonMonitorInfo {
	public CpuInfo cpuInfo = new CpuInfo();
	public MemoryInfo memInfo = new MemoryInfo();
	public DiskInfo diskInfo = new DiskInfo();
	public NetInfo netInfo = new NetInfo();

	public CommonMonitorInfo() {

	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
