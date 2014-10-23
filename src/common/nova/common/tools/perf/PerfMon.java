package nova.common.tools.perf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    public static String strBandwidth;
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
     * Memory information. Memory size in MB, RAM in MB
     * 
     * @return {@link MemoryInfo}
     */

    public static MemoryInfo getMemoryInfo() {
        MemoryInfo mem = new MemoryInfo();
        try {
            Mem smm = sigar.getMem();
            mem.freeMemorySize = smm.getFree() / 1024 / 1024;
            mem.usedMemorySize = smm.getUsed() / 1024 / 1024;
            mem.totalMemorySize = smm.getTotal() / 1024 / 1024;
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
                if (fs[i].getType() == FileSystem.TYPE_LOCAL_DISK) {
                    disk.totalDiskSize += sigar.getFileSystemUsage(
                            fs[i].getDirName()).getTotal();
                    disk.usedDiskSize += sigar.getFileSystemUsage(
                            fs[i].getDirName()).getUsed();
                    disk.freeDiskSize += sigar.getFileSystemUsage(
                            fs[i].getDirName()).getFree();
                }
            }
            /**
             * Disk size in GB
             */
            disk.totalDiskSize /= (1024 * 1024);
            disk.usedDiskSize /= (1024 * 1024);
            disk.freeDiskSize /= (1024 * 1024);

        } catch (SigarException e) {
            logger.error("Can't get disk information!", e);
        }
        return disk;
    }

    /**
     * Net information. BandWidth, down speed and up speed of net information;
     * Speed in b/s
     * 
     * @return {@link NetInfo}
     */
    public static NetInfo getNetInfo() {

        final NetInfo net = new NetInfo();

        try {
            NetInterfaceConfig config = sigar.getNetInterfaceConfig(null);
            NetInterfaceStat netstat = sigar.getNetInterfaceStat(config
                    .getName());
            net.bandWidth = netstat.getSpeed();
            net.downSpeed = netstat.getRxBytes();
            net.upSpeed = netstat.getTxBytes();
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            }
            long end = System.currentTimeMillis();
            netstat = sigar.getNetInterfaceStat(config.getName());
            net.downSpeed = (netstat.getRxBytes() - net.downSpeed) * 8
                    / (end - start) * 1000;
            net.upSpeed = (netstat.getTxBytes() - net.upSpeed) * 8
                    / (end - start) * 1000;

            String[] ifaces = sigar.getNetInterfaceList();
            String configname = config.getName();
            if (config.getName().indexOf("eth") < 0) {
                for (String iface : ifaces) {
                    netstat = sigar.getNetInterfaceStat(iface);
                    if (netstat.getRxBytes() > 100 && iface.indexOf("eth") >= 0) {
                        configname = iface;
                        break;
                    }
                }
            }
            // if get bandwidth failed, use "ethtool"
            if (net.bandWidth <= 0) {
                PerfMon.strBandwidth = "-1";
                String strcmd = "ethtool " + configname;
                try {

                    Process p = Runtime.getRuntime().exec(strcmd);
                    final InputStream is = p.getInputStream();
                    Thread getbw = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            String line, result = "";
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(is));
                            try {
                                while ((line = br.readLine()) != null) {

                                    if (line.indexOf("Speed:") >= 0) {
                                        result = line;
                                        break;
                                    }
                                }
                                if (result.equalsIgnoreCase("") == false) {
                                    PerfMon.strBandwidth = result
                                            .substring(result.indexOf("Speed:") + 6);
                                    if (strBandwidth.compareTo("-1") != 0) {
                                        if (strBandwidth.indexOf("Gb/s") >= 0) {
                                            net.bandWidth = Integer
                                                    .valueOf(strBandwidth
                                                            .substring(
                                                                    0,
                                                                    strBandwidth
                                                                            .indexOf("Gb/s"))
                                                            .trim()) * 1024 * 1024 * 1024;
                                        }
                                        if (strBandwidth.indexOf("Mb/s") >= 0) {
                                            net.bandWidth = Integer
                                                    .valueOf(strBandwidth
                                                            .substring(
                                                                    0,
                                                                    strBandwidth
                                                                            .indexOf("Mb/s"))
                                                            .trim()) * 1024 * 1024;
                                        }
                                        if (strBandwidth.indexOf("Kb/s") >= 0) {
                                            net.bandWidth = Integer
                                                    .valueOf(strBandwidth
                                                            .substring(
                                                                    0,
                                                                    strBandwidth
                                                                            .indexOf("Kb/s"))
                                                            .trim()) * 1024;
                                        }

                                    }
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                    getbw.start();

                    try {
                        if (p.waitFor() != 0) {
                            logger.error("use ethtool " + config.getName()
                                    + " get bandwidth return abnormal value!");
                        }
                        try {
                            getbw.join();
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        logger.error("use ethtool get bandwidth terminated!", e);
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    logger.error("use ethtool get bandwidth cmd error!", e);

                }

            }
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
