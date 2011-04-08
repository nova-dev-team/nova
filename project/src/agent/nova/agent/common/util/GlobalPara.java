package nova.agent.common.util;

import java.util.HashMap;
import java.util.Map;

import nova.agent.core.service.GeneralMonitorProxy;
import nova.agent.core.service.HeartbeatProxy;
import nova.agent.core.service.IntimeProxy;

/**
 * Static variable used in agent
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class GlobalPara {
	public static int BIND_PORT = 9876;

	public static Map<String, HeartbeatProxy> heartbeatProxyMap = new HashMap<String, HeartbeatProxy>();
	public static Map<String, GeneralMonitorProxy> generalMonitorProxyMap = new HashMap<String, GeneralMonitorProxy>();
	public static Map<String, IntimeProxy> intimeProxyMap = new HashMap<String, IntimeProxy>();
}
