package nova.common.service.message;

import nova.common.tools.perf.GeneralMonitorInfo;
import nova.common.tools.perf.PerfMon;
import nova.common.util.Conf;

/**
 * General monitor information message contains some. Use
 * getGeneralMonitorInfo() to get an object that contains monitor information
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class AgentPerfMessage {

    public String vnodeuuid;

    public AgentPerfMessage() {
        this.vnodeuuid = Conf.getString("vnode.uuid");
    }

    private GeneralMonitorInfo monitorInfo = PerfMon.getGeneralMonitorInfo();

    public GeneralMonitorInfo getGeneralMonitorInfo() {
        return this.monitorInfo;
    }

}
