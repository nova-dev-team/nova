package nova.common.tools.rrd;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import nova.common.tools.perf.GeneralMonitorInfo;

import org.apache.log4j.Logger;
import org.jrobin.core.DsTypes;
import org.jrobin.core.FetchData;
import org.jrobin.core.FetchRequest;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;

/**
 * Utilization of Jrobin
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class RRDTools {
	public static Logger logger = Logger.getLogger(RRDTools.class);

	/**
	 * Create one RRD to store monitor information
	 * 
	 * @param rootPath
	 *            Where to store RRD file
	 * @param timeInterval
	 *            The insert time interval of RRD
	 * @param rrdLength
	 *            The length of RRD
	 * @param startTime
	 *            Start time stamp
	 * @return {@link RrdDb}
	 */
	public static RrdDb CreateMonitorInfoRRD(String rootPath,
			long timeInterval, int rrdLength, long startTime) {
		try {

			/**
			 * Create one RRD definition
			 */
			RrdDef rrdDef = new RrdDef(rootPath, startTime - 1, timeInterval);

			/**
			 * Attributes of this RRD
			 */
			rrdDef.addDatasource("combinedTime", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);
			rrdDef.addDatasource("mhz", DsTypes.DT_GAUGE, timeInterval * 2, 0,
					Double.NaN);
			rrdDef.addDatasource("nCpu", DsTypes.DT_GAUGE, timeInterval * 2, 0,
					Double.NaN);

			rrdDef.addDatasource("freeMemorySize", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);
			rrdDef.addDatasource("usedMemorySize", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);
			rrdDef.addDatasource("totalMemorySize", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);
			rrdDef.addDatasource("ramSize", DsTypes.DT_GAUGE, timeInterval * 2,
					0, Double.NaN);

			rrdDef.addDatasource("freeDiskSize", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);
			rrdDef.addDatasource("usedDiskSize", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);
			rrdDef.addDatasource("totalDiskSize", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);

			rrdDef.addDatasource("bandWidth", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);
			rrdDef.addDatasource("downSpeed", DsTypes.DT_GAUGE,
					timeInterval * 2, 0, Double.NaN);
			rrdDef.addDatasource("upSpeed", DsTypes.DT_GAUGE, timeInterval * 2,
					0, Double.NaN);

			/**
			 * RRD parameters
			 */
			rrdDef.addArchive("AVERAGE", 0.5, 1, rrdLength);

			/**
			 * RRD file definition is completed
			 */
			logger.info("[One RRD is created! ]");

			/**
			 * RRD file is on your disk
			 */
			RrdDb rrdDb = new RrdDb(rrdDef);

			return rrdDb;

		} catch (Exception e) {
			e.printStackTrace();
			logger.equals("Create RRD failed!");
			return null;
		}
	}

	/**
	 * Default startTime of CreateMonitorInfoRRD
	 * 
	 * @param rootPath
	 *            Where to store RRD file
	 * @param timeInterval
	 *            The insert time interval of RRD
	 * @param rrdLength
	 *            The length of RRD
	 * @return rrdDb {@link RrdDb}
	 */
	public static RrdDb CreateMonitorInfoRRD(String rootPath,
			long timeInterval, int rrdLength) {
		return RRDTools.CreateMonitorInfoRRD(rootPath, timeInterval, rrdLength,
				System.currentTimeMillis());
	}

	/**
	 * Get datas in RRD between timeStart and timeEnd
	 * 
	 * @param rootPath
	 *            Where to store RRD file
	 * @param timeStart
	 *            From when
	 * @param timeEnd
	 *            To when
	 * @return double[][]
	 */
	public static double[][] fetchRRDData(String rootPath, long timeStart,
			long timeEnd) {
		try {
			/**
			 * open the file
			 */
			RrdDb rrd = new RrdDb(rootPath);

			/**
			 * create fetch request using the database reference
			 */
			FetchRequest request = rrd.createFetchRequest("AVERAGE", timeStart,
					timeEnd);
			FetchData fetchData = request.fetchData();

			return fetchData.getValues();
		} catch (Exception e) {
			e.printStackTrace();
			logger.equals("Fetch RRD datas failed!");
			return null;
		}

	}

	/**
	 * Insert one record in RRD
	 * 
	 * @param rrdDb
	 *            {@link RrdDb}
	 * @param msg
	 *            {@link GeneralMonitorInfo}
	 * @param timeStamp
	 */
	public static void addRRD(RrdDb rrdDb, GeneralMonitorInfo msg,
			long timeStamp) {
		Sample sample;
		try {
			sample = rrdDb.createSample(timeStamp);
			sample.setValue("combinedTime", msg.cpuInfo.combinedTime);
			sample.setValue("mhz", msg.cpuInfo.mhz);
			sample.setValue("nCpu", msg.cpuInfo.nCpu);

			sample.setValue("freeMemorySize", msg.memInfo.freeMemorySize);
			sample.setValue("usedMemorySize", msg.memInfo.usedMemorySize);
			sample.setValue("totalMemorySize", msg.memInfo.totalMemorySize);
			sample.setValue("ramSize", msg.memInfo.ramSize);

			sample.setValue("freeDiskSize", msg.diskInfo.freeDiskSize);
			sample.setValue("usedDiskSize", msg.diskInfo.usedDiskSize);
			sample.setValue("totalDiskSize", msg.diskInfo.totalDiskSize);

			sample.setValue("bandWidth", msg.netInfo.bandWidth);
			sample.setValue("downSpeed", msg.netInfo.downSpeed);
			sample.setValue("upSpeed", msg.netInfo.upSpeed);

			sample.update();

			logger.info("Insert one General monitor information");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RrdException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param picPath
	 *            Where to store this picture
	 * @param startTime
	 *            start time
	 * @param endTime
	 *            wh
	 * @param rrdFilePath
	 * @param valueType
	 */
	public static void plotPicture(String picPath, long startTime,
			long endTime, String rrdFilePath, String valueType) {
		RrdGraphDef gDef = null;

		// 生成最近一天的图形
		gDef = new RrdGraphDef();
		gDef.setFilename(picPath);
		gDef.setWidth(450);
		gDef.setHeight(250);
		gDef.setImageFormat("png");
		gDef.setTimeSpan(startTime, endTime);
		gDef.setTitle(valueType + " Demo");

		gDef.datasource("demo", rrdFilePath, valueType, "AVERAGE");

		gDef.line("demo", Color.GREEN, valueType);
		gDef.gprint("demo", "MIN", "%5.1lf Min");
		gDef.gprint("demo", "AVERAGE", "%5.1lf Avg");
		gDef.gprint("demo", "MAX", "%5.1lf Max");

		gDef.setSmallFont(new Font("Monospaced", Font.PLAIN, 11));
		gDef.setLargeFont(new Font("SansSerif", Font.BOLD, 14));
		try {
			new RrdGraph(gDef);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RrdException e) {
			e.printStackTrace();
		}
	}
}
