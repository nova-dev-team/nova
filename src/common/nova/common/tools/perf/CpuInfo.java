package nova.common.tools.perf;

import com.google.gson.Gson;

/**
 * CPU information. Contains quantity, frequency, model and utilization.
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class CpuInfo {

    public double combinedTime = 0.0;
    public int mhz = 0;
    public int nCpu = 0;
    public double dUserTime = 0.0;
    public double dSysTime = 0.0;
    public double dIdleTime = 0.0;
    public String model = "";

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
