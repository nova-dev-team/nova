package nova.test.functional.worker;

import nova.common.tools.perf.PerfMon;

import org.junit.Test;

public class TestDomain {
    @Test
    public static void main(String[] args) {
        // final String virtService = "qemu:///system";
        // try {
        // System.out.println(NovaWorker.getInstance()
        // .getConn(virtService, false).numOfDomains());
        // } catch (LibvirtException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        PerfMon.getGeneralMonitorInfo();

    }
}
