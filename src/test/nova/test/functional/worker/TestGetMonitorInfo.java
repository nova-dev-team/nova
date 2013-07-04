package nova.test.functional.worker;

import nova.common.tools.perf.GeneralMonitorInfo;
import nova.common.tools.perf.PerfMon;

public class TestGetMonitorInfo {
    public static void main(String[] args) {
        // final String virtService = "qemu:///system";
        // try {
        // System.out.println(NovaWorker.getInstance()
        // .getConn(virtService, false).numOfDomains());
        // } catch (LibvirtException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        GeneralMonitorInfo info = PerfMon.getGeneralMonitorInfo();
        System.out.println("......" + info.diskInfo.totalDiskSize);

    }
}
