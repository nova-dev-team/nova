package nova.common.tools.perf;

import com.google.gson.Gson;

/**
 * Memory information. Contains total, used, free Size of memory and RAM size.
 * 
 * @author gaotao1987@gmail.com
 * 
 */

public class MemoryInfo {

    public long totalMemorySize = 0;
    public long usedMemorySize = 0;
    public long freeMemorySize = 0;
    public long ramSize = 0;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // private Mem mem;
    // private Swap swap;
    // private Sigar sigar = new Sigar();

    // public static void main(String args[]) {
    //
    // try {
    // MemoryInfo meminfo = new MemoryInfo();
    // System.out.println(meminfo.freeMemorySize);
    // } catch (SigarException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // }
    //
    // private Long format(long value) // KB
    // {
    // return new Long(value / 1024);
    // }
    //
    // public MemoryInfo() throws SigarException {
    // this.mem = this.sigar.getMem();
    // this.swap = this.sigar.getSwap();
    // }
    //
    // // total memory
    // public long getTotalMemory() {
    // return this.format(mem.getTotal());
    // }
    //
    // // used memory
    // public long getUsedMemory() {
    // return this.format(mem.getUsed());
    // }
    //
    // // free memory
    // public long getFreeMemory() {
    // return this.format(mem.getFree());
    // }
    //
    // // ram
    // public long getTotalRam() // MB
    // {
    // return mem.getRam();
    // }
    //
    // // total swap
    // public long getTotalSwap() {
    // return this.format(swap.getTotal());
    // }
    //
    // // used swap
    // public long getUsedSwap() {
    // return this.format(swap.getUsed());
    // }
    //
    // // free swap
    // public long getFreeSwap() {
    // return this.format(swap.getFree());
    // }
    //
    // // page out
    // public long getPageOut() {
    // return this.format(swap.getPageOut());
    // }
    //
    // // page in
    // public long getPageIn() {
    // return this.format(swap.getPageIn());
    // }
}
