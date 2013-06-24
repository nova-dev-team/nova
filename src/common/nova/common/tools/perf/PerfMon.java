package nova.common.tools.perf;

import org.apache.log4j.Logger;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;
import org.hyperic.sigar.ptql.ProcessFinder;

/**
 * Methods of get system information
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class PerfMon {

    static Logger logger = Logger.getLogger(PerfMon.class);

    private static Sigar sigar = new Sigar();
    private static SigarProxy proxy = SigarProxyCache.newInstance(sigar);

    /**
     * Utilization, frequency, quantity and model of CPU
     * 
     * @return {@link CpuInfo}
     */
    public static CpuInfo getCpuInfo() {
        CpuInfo cpu = new CpuInfo();

        try {
            /**
             * 100 as the standard
             */
            CpuPerc cpc = sigar.getCpuPerc();
            cpu.combinedTime = cpc.getCombined() * 100;
            org.hyperic.sigar.CpuInfo[] infoList = sigar.getCpuInfoList();
            cpu.mhz = infoList[0].getMhz();
            cpu.nCpu = infoList.length;
            cpu.model = infoList[0].getModel();
            cpu.dIdleTime = cpc.getIdle();
            cpu.dSysTime = cpc.getSys();
            cpu.dUserTime = cpc.getUser();
        } catch (SigarException e) {
            logger.error("Can't get cpu information!", e);
        }

        return cpu;
    }

    /**
     * Memory information. Memory size in B, RAM in MB
     * 
     * @return {@link MemoryInfo}
     */

    public static MemoryInfo getMemoryInfo() {
        MemoryInfo mem = new MemoryInfo();
        try {
            Mem smm = sigar.getMem();
            mem.freeMemorySize = smm.getActualFree();
            mem.usedMemorySize = smm.getActualUsed();
            mem.totalMemorySize = smm.getTotal();
            mem.ramSize = smm.getRam();
        } catch (SigarException e) {
            logger.error("Can't get memory information!", e);
        }

        return mem;
    }

    /**
     * Disk information. Disk in B
     * 
     * @return Return disk Info.
     */
    public static DiskInfo getDiskInfo() {
        DiskInfo disk = new DiskInfo();
        try {
            FileSystem[] fs = proxy.getFileSystemList();
            for (int i = 0; i < fs.length; i++) {
                disk.totalDiskSize += sigar.getFileSystemUsage(
                        fs[i].getDirName()).getTotal();
                disk.usedDiskSize += sigar.getFileSystemUsage(
                        fs[i].getDirName()).getUsed();
                disk.freeDiskSize += sigar.getFileSystemUsage(
                        fs[i].getDirName()).getFree();
            }
            /**
             * Disk size in b
             */
            disk.totalDiskSize *= 1000;
            disk.usedDiskSize *= 1000;
            disk.freeDiskSize *= 1000;

        } catch (SigarException e) {
            logger.error("Can't get disk information!", e);
        }
        return disk;
    }

    /**
     * Net information. BandWidth, down speed and up speed of net information;
     * Speed in B/s
     * 
     * @return {@link NetInfo}
     */
    public static NetInfo getNetInfo() {
        NetInfo net = new NetInfo();

        try {
            NetInterfaceConfig config = sigar.getNetInterfaceConfig(null);
            NetInterfaceStat netstat = sigar.getNetInterfaceStat(config
                    .getName());
            net.bandWidth = netstat.getSpeed();
            net.downSpeed = netstat.getRxBytes();
            net.upSpeed = netstat.getTxBytes();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            }

            netstat = sigar.getNetInterfaceStat(config.getName());
            net.downSpeed = netstat.getRxBytes() - net.downSpeed;
            net.upSpeed = netstat.getTxBytes() - net.upSpeed;
        } catch (SigarException e) {
            logger.error("Can't get net information!", e);
        }

        return net;
    }

    /**
     * Get process information through its name. CPU utilization in double.
     * virtual set size and resident set size in B. Last time in second.
     * 
     * @param processName
     * @return {@link ProcInfo}
     */
    public static ProcInfo getProcInfo(String processName) {
        ProcInfo process = new ProcInfo();

        try {
            processName = "State.Name.eq=" + processName;
            long[] pidList = ProcessFinder.find(proxy, processName);
            for (int i = 0; i < pidList.length; i++) {
                process.cpuUtilization += sigar.getProcCpu(pidList[i])
                        .getPercent();
                process.residentSetSize += sigar.getProcMem(pidList[i])
                        .getResident();
                process.virtualSetSize += sigar.getProcMem(pidList[i])
                        .getSize();
            }
            process.lastTimeOfProc = (System.currentTimeMillis() - sigar
                    .getProcTime(pidList[0]).getStartTime()) / 1000;
        } catch (SigarException e) {
            logger.error("Can't get process information!", e);
        }
        return process;
    }

    /**
     * Get common monitor information.
     * 
     * @return {@link GeneralMonitorInfo}
     */
    public static GeneralMonitorInfo getGeneralMonitorInfo() {
        final GeneralMonitorInfo cMonitor = new GeneralMonitorInfo();
        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                cMonitor.diskInfo = PerfMon.getDiskInfo();
            }
        });
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                cMonitor.netInfo = PerfMon.getNetInfo();
            }
        });
        t2.start();

        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                cMonitor.cpuInfo = PerfMon.getCpuInfo();
                cMonitor.memInfo = PerfMon.getMemoryInfo();
            }
        });
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            logger.error("Can't get general information!", e);
        }
        return cMonitor;
    }
}
