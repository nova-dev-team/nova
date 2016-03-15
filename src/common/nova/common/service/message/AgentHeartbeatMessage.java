package nova.common.service.message;

import nova.common.tools.perf.GeneralMonitorInfo;
import nova.common.tools.perf.PerfMon;
import nova.common.util.Conf;

/**
 * Heartbeat Message;
 */

public class AgentHeartbeatMessage {

    public String vnodeuuid;
    private GeneralMonitorInfo monitorInfo = PerfMon.getGeneralMonitorInfo();

    public AgentHeartbeatMessage() {
        System.out.println("GeneralMonitorInfo2");
        this.vnodeuuid = Conf.getString("vnode.uuid");

        System.out.println("Agent monitor2: "
                + this.monitorInfo.memInfo.totalMemorySize + "  " + vnodeuuid);
    }

    public GeneralMonitorInfo getGeneralMonitorInfo() {
        System.out.println("Agent monitor: "
                + this.monitorInfo.memInfo.totalMemorySize);
        return this.monitorInfo;
    }

}
