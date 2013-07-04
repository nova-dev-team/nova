package nova.common.util;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
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
import org.jrobin.core.Util;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;

/**
 * Utilization of jrobin
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
     */
    public static void CreateMonitorInfoRRD(String rootPath, long timeInterval,
            int rrdLength, long startTime) {
        try {

            /**
             * Create one RRD definition
             */
            RrdDef rrdDef = new RrdDef(rootPath, startTime - 1, timeInterval);

            /**
             * Attributes of this RRD
             */
            rrdDef.addDatasource("combinedTime", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);
            rrdDef.addDatasource("mhz", DsTypes.DT_GAUGE, timeInterval * 2,
                    Double.NaN, Double.NaN);
            rrdDef.addDatasource("nCpu", DsTypes.DT_GAUGE, timeInterval * 2,
                    Double.NaN, Double.NaN);

            rrdDef.addDatasource("freeMemorySize", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);
            rrdDef.addDatasource("usedMemorySize", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);
            rrdDef.addDatasource("totalMemorySize", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);
            rrdDef.addDatasource("ramSize", DsTypes.DT_GAUGE, timeInterval * 2,
                    Double.NaN, Double.NaN);

            rrdDef.addDatasource("freeDiskSize", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);
            rrdDef.addDatasource("usedDiskSize", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);
            rrdDef.addDatasource("totalDiskSize", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);

            rrdDef.addDatasource("bandWidth", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);
            rrdDef.addDatasource("downSpeed", DsTypes.DT_GAUGE,
                    timeInterval * 2, Double.NaN, Double.NaN);
            rrdDef.addDatasource("upSpeed", DsTypes.DT_GAUGE, timeInterval * 2,
                    Double.NaN, Double.NaN);

            /**
             * RRD parameters
             */
            rrdDef.addArchive("AVERAGE", 0.5, 1, rrdLength);

            /**
             * RRD file definition is completed
             */
            logger.info("One RRD is created!");

            /**
             * RRD file is on your disk
             */
            new RrdDb(rrdDef);

        } catch (Exception e) {
            logger.error("Error creating RRD file", e);
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
     */
    public static void CreateMonitorInfoRRD(String rootPath, long timeInterval,
            int rrdLength) {
        RRDTools.CreateMonitorInfoRRD(rootPath, timeInterval, rrdLength,
                Util.getTime());
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
            logger.error("Failed to fetch data from RRD file", e);
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
    public static void addMonitorInfoInRRD(RrdDb rrdDb, GeneralMonitorInfo msg,
            long timeStamp) {
        Sample sample;

        try {
            sample = rrdDb.createSample(timeStamp);
            sample.setValue("combinedTime", msg.cpuInfo.combinedTime);
            sample.setValue("mhz", msg.cpuInfo.mhz);
            sample.setValue("nCpu", msg.cpuInfo.nCpu);

            sample.setValue("freeMemorySize", 100 * msg.memInfo.freeMemorySize
                    / msg.memInfo.totalMemorySize);
            sample.setValue("usedMemorySize", 100 * msg.memInfo.usedMemorySize
                    / msg.memInfo.totalMemorySize);
            sample.setValue("totalMemorySize", msg.memInfo.totalMemorySize);// MB
            sample.setValue("ramSize", msg.memInfo.ramSize);// MB

            sample.setValue("freeDiskSize", 100 * msg.diskInfo.freeDiskSize
                    / msg.diskInfo.totalDiskSize);
            sample.setValue("usedDiskSize", 100 * msg.diskInfo.usedDiskSize
                    / msg.diskInfo.totalDiskSize);
            sample.setValue("totalDiskSize", msg.diskInfo.totalDiskSize);// GB

            sample.setValue("bandWidth", msg.netInfo.bandWidth / 1024 / 1024);// mbps
            sample.setValue("downSpeed", 100 * msg.netInfo.downSpeed
                    / msg.netInfo.bandWidth);
            sample.setValue("upSpeed", 100 * msg.netInfo.upSpeed
                    / msg.netInfo.bandWidth);

            sample.update();

            rrdDb.close();
        } catch (IOException e) {
            logger.error("Error inserting new value", e);
        } catch (RrdException e) {
            logger.error("Error inserting new value", e);
        }
    }

    /**
     * Common definition of plot graph
     * 
     * @param picPath
     *            Where to store this picture
     * @param startTime
     *            Start time
     * @param endTime
     *            End time
     * @param rrdFilePath
     *            where to find the RRD file
     * @return gDef {@link RrdGraphDef}
     */
    private static RrdGraphDef plotGraph(String picPath, long startTime,
            long endTime, String rrdFilePath) {
        RrdGraphDef gDef = null;
        gDef = new RrdGraphDef();

        gDef.setFilename(picPath);

        gDef.setWidth(450);
        gDef.setHeight(250);

        gDef.setImageFormat("png");
        gDef.setTimeSpan(startTime, endTime);
        gDef.setMinValue(0);

        gDef.setSmallFont(new Font("Monospaced", Font.PLAIN, 11));
        gDef.setLargeFont(new Font("SansSerif", Font.BOLD, 14));
        return gDef;
    }

    /**
     * Plot CPU utilization graph
     * 
     * @param picPath
     *            where to store this picture
     * @param startTime
     *            start time
     * @param endTime
     *            end time
     * @param rrdFilePath
     *            where to find the RRD file
     */
    public static void plotCpuGraph(String picPath, long startTime,
            long endTime, String rrdFilePath) {
        RrdGraphDef gDef = null;
        String valueType = "combinedTime";

        gDef = plotGraph(picPath, startTime, endTime, rrdFilePath);
        gDef.setTitle("CPU Utilization");
        gDef.setVerticalLabel("Utilization [%]");

        gDef.datasource("CPU", rrdFilePath, valueType, "AVERAGE");

        gDef.area("CPU", Color.GREEN, "CPU Utilization");
        gDef.setMaxValue(100);

        gDef.gprint("CPU", "MIN", "Min %5.1lf ");
        gDef.gprint("CPU", "AVERAGE", " Avg %5.1lf ");
        gDef.gprint("CPU", "MAX", "Max %5.1lf ");

        try {
            new RrdGraph(gDef);
        } catch (IOException e) {
            logger.error("Error plotting RRD graph", e);
        } catch (RrdException e) {
            logger.error("Error plotting RRD graph", e);
        }
    }

    /**
     * Plot memory utilization graph
     * 
     * @param picPath
     *            where to store this picture
     * @param startTime
     *            start time
     * @param endTime
     *            end time
     * @param rrdFilePath
     *            where to find the RRD file
     */
    public static void plotMemoryGraph(String picPath, long startTime,
            long endTime, String rrdFilePath) {
        RrdGraphDef gDef = null;
        String valueType = "usedMemorySize";
        String totalSize = "totalMemorySize";

        gDef = plotGraph(picPath, startTime, endTime, rrdFilePath);
        gDef.setTitle("Memory Utilization");
        gDef.setVerticalLabel("Utilization [GB]");
        gDef.setBase(1024);

        gDef.datasource("Memory", rrdFilePath, valueType, "AVERAGE");
        gDef.datasource("totalMemory", rrdFilePath, totalSize, "AVERAGE");

        gDef.line("totalMemory", Color.BLACK, "Total Memory");
        gDef.area("Memory", Color.GREEN, "Memory Utilization");

        gDef.gprint("Memory", "MIN", "Min %5.1lf B ");
        gDef.gprint("Memory", "AVERAGE", " Avg %5.1lf B ");
        gDef.gprint("Memory", "MAX", "Max %5.1lf B ");

        try {
            new RrdGraph(gDef);
        } catch (IOException e) {
            logger.error("Error plotting RRD graph", e);
        } catch (RrdException e) {
            logger.error("Error plotting RRD graph", e);
        }
    }

    /**
     * Plot disk utilization graph
     * 
     * @param picPath
     *            where to store this picture
     * @param startTime
     *            start time
     * @param endTime
     *            end time
     * @param rrdFilePath
     *            where to find the RRD file
     */
    public static void plotDiskGraph(String picPath, long startTime,
            long endTime, String rrdFilePath) {
        RrdGraphDef gDef = null;
        String valueType = "usedDiskSize";
        String totalSize = "totalDiskSize";

        gDef = plotGraph(picPath, startTime, endTime, rrdFilePath);
        gDef.setTitle("Disk Utilization");
        gDef.setVerticalLabel("Utilization [GB]");
        gDef.setBase(1024);

        gDef.datasource("Disk", rrdFilePath, valueType, "AVERAGE");
        gDef.datasource("totalDisk", rrdFilePath, totalSize, "AVERAGE");

        gDef.area("Disk", Color.GREEN, "Memory Utilization");
        gDef.line("totalDisk", Color.BLACK, "Total Memory");

        gDef.gprint("Disk", "MIN", "Min %5.1lf B ");
        gDef.gprint("Disk", "AVERAGE", " Avg %5.1lf B ");
        gDef.gprint("Disk", "MAX", "Max %5.1lf B ");

        try {
            new RrdGraph(gDef);
        } catch (IOException e) {
            logger.error("Error plotting RRD graph", e);
        } catch (RrdException e) {
            logger.error("Error plotting RRD graph", e);
        }
    }

    /**
     * Plot net utilization graph
     * 
     * @param picPath
     *            where to store this picture
     * @param startTime
     *            start time
     * @param endTime
     *            end time
     * @param rrdFilePath
     *            where to find the RRD file
     */
    public static void plotNetGraph(String picPath, long startTime,
            long endTime, String rrdFilePath) {
        RrdGraphDef gDef = null;
        String valueType1 = "upSpeed";
        String valueType2 = "downSpeed";

        gDef = plotGraph(picPath, startTime, endTime, rrdFilePath);
        gDef.setTitle("Net Utilization");
        gDef.setVerticalLabel("Utilization [B/s]");

        gDef.datasource("upSpeed", rrdFilePath, valueType1, "AVERAGE");
        gDef.datasource("downSpeed", rrdFilePath, valueType2, "AVERAGE");

        gDef.area("downSpeed", new Color(0, 206, 0), "Down speed");
        gDef.line("upSpeed", Color.BLUE, "Up speed");

        gDef.gprint("upSpeed", "MIN", "Min up speed %5.1lf B/s ");
        gDef.gprint("upSpeed", "AVERAGE", " Avg up speed %5.1lf B/s ");
        gDef.gprint("upSpeed", "MAX", "Max up speed %5.1lf B/s ");
        gDef.gprint("downSpeed", "MIN", "Min down speed %5.1lf B/s ");
        gDef.gprint("downSpeed", "AVERAGE", " Avg down speed %5.1lf B/s ");
        gDef.gprint("downSpeed", "MAX", "Max down speed %5.1lf B/s ");

        try {
            new RrdGraph(gDef);
        } catch (IOException e) {
            logger.error("Error plotting RRD graph", e);
        } catch (RrdException e) {
            logger.error("Error plotting RRD graph", e);
        }
    }

    public static double[][] getMonitorInfo(int pnodeid) {
        double[][] info = null;
        String rddfile = "build/" + pnodeid + ".rrd";
        File file = new File(rddfile);
        if (file.exists() == false)
            return info;
        FetchData fetchData = null;
        try {
            /**
             * open the file
             */
            RrdDb rrd = new RrdDb(rddfile);

            /**
             * create fetch request using the database reference
             */
            FetchRequest request = rrd.createFetchRequest("AVERAGE",
                    Util.getTime() - 5 * 110, Util.getTime() + 10);
            fetchData = request.fetchData();
            info = new double[100][fetchData.getColumnCount()];
        } catch (Exception e) {
            logger.error("Failed to fetch data from RRD file", e);
            return info;
        }

        double[][] data = fetchData.getValues();
        int index = 100 - 1;
        for (int i = data[0].length - 1; i >= 0; i--) {
            if (Double.isNaN(data[0][i]))
                continue;
            else {
                for (int j = 0; j < info[0].length; j++)
                    info[index][j] = data[j][i];
                index--;
                if (index < 0)
                    break;
            }
        }
        return info;
    }
}
