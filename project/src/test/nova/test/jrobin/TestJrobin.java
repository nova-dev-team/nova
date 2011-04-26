package nova.test.jrobin;

import nova.common.tools.perf.GeneralMonitorInfo;
import nova.common.tools.perf.PerfMon;
import nova.common.tools.rrd.RRDTools;

import org.jrobin.core.RrdDb;
import org.jrobin.core.Util;
import org.junit.Test;

public class TestJrobin {
	@Test
	public void testRRDTools() {
		long startTime = System.currentTimeMillis();
		long endTime = startTime + 100000;

		String rootPath = "build/demo_flow.rrd";
		int timeInterval = 10;
		int rrdLength = 8640;

		String picPath = "build/demo_flow.png";

		/**
		 * 测试创建RrdDb
		 */
		RrdDb rrdDb = RRDTools.CreateMonitorInfoRRD(rootPath, timeInterval,
				rrdLength, startTime);
		/**
		 * 测试添加 初始数据
		 */
		GeneralMonitorInfo msg = PerfMon.getGeneralMonitorInfo();
		for (long t = startTime; t < endTime; t += 10)
			RRDTools.addMonitorInfoInRRD(rrdDb, msg, t);

		/**
		 * 测试绘图
		 */

		long startTm = endTime - 86400;
		RRDTools.plotCpuGraph(picPath, startTm, endTime, rootPath);
		/**
		 * RRDTools.plotMemoryGraph(picPath, startTm, endTime, rootPath);
		 * RRDTools.plotDiskGraph(picPath, startTm, endTime, rootPath);
		 * RRDTools.plotNetGraph(picPath, startTm, endTime, rootPath);
		 */

		/**
		 * 测试FetchData获取RRD
		 */
		double[][] values = RRDTools.fetchRRDData(rootPath, startTime, endTime);
		int rowCount = values[0].length;
		int columnCount = values.length;

		StringBuffer buffer = new StringBuffer("");
		long timeCur = startTime;
		for (int row = 0; row < rowCount; row++) {
			buffer.append(timeCur);
			timeCur += 10;
			buffer.append(":  ");
			for (int dsIndex = 0; dsIndex < columnCount; dsIndex++) {
				buffer.append(Util.formatDouble(values[dsIndex][row]));
				buffer.append("  ");
			}
			buffer.append("\n");
		}

		System.out.println(buffer);

	}
}
