package nova.agent.common.util;

import java.net.InetSocketAddress;

import nova.agent.core.models.GeneralMonitorProxy;
import nova.agent.core.models.HeartbeatProxy;
import nova.agent.core.models.IntimeProxy;

public class GlobalPara {
	public static int BIND_PORT = 9876;
	public static HeartbeatProxy HEARTBEAT_PROXY = new HeartbeatProxy(
			new InetSocketAddress("localhost", 9876));
	public static GeneralMonitorProxy GENERAL_MONITOR_PROXY = new GeneralMonitorProxy(
			new InetSocketAddress("localhost", 9876));
	public static IntimeProxy INTIME_PROXY = new IntimeProxy(
			new InetSocketAddress("localhost", 9876));
}
