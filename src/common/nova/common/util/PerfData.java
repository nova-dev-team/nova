/**
 * 
 */
package nova.common.util;

/**
 * convert the performance data to percentile
 * 
 * 0.cpu 1.mhz 2.ncpu 3.free mem 4.used mem 5.total mem 6.ram size 7.free disk
 * 8.used disk 9.total disk 10.bandwidth 11.down 12.up
 * 
 * @author Tianyu Chen
 */
public class PerfData {
    private double cpuLoad;
    private double memLoad;
    private double netInLoad;
    private double netOutLoad;
    private double memSize;
    private double freeMem;
    private double usedMem;
    private double bandWidth;
    private double netIn;
    private double netOut;

    public PerfData(double perf[]) {
        this.cpuLoad = perf[0] / 100.0;
        this.memLoad = perf[4] / perf[5];
        this.netInLoad = perf[11] / perf[10];
        this.netOutLoad = perf[12] / perf[10];
        this.memSize = perf[5];
        this.freeMem = perf[3];
        this.usedMem = perf[4];
        this.bandWidth = perf[10];
        this.netIn = perf[11];
        this.netOut = perf[12];
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public double getMemLoad() {
        return memLoad;
    }

    public double getNetInLoad() {
        return netInLoad;
    }

    public double getNetOutLoad() {
        return netOutLoad;
    }

    public double getMemSize() {
        return memSize;
    }

    public double getFreeMemSize() {
        return freeMem;
    }

    public double getUsedMemSize() {
        return usedMem;
    }

    public double getBandWidth() {
        return bandWidth;
    }

    public double getNetIn() {
        return netIn;
    }

    public double getNetOut() {
        return netOut;
    }
}
