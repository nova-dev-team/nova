package nova.common.tools.perf;

import com.google.gson.Gson;

public class CpuInfo {

	public double combinedTime = 0;
	public int mhz = 0;
	public int nCpu = 0;
	public String model = "";

	public CpuInfo() {

	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
