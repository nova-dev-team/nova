package nova.test.unit;

import java.io.File;
import java.io.IOException;

import nova.common.tools.perf.GeneralMonitorInfo;
import nova.common.tools.perf.PerfMon;
import nova.common.util.RRDTools;
import nova.common.util.Utils;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;
import org.junit.Test;

public class TestJrobin {
    @Test
    public void testRRDTools() {
        long startTime = Util.getTime();
        long endTime = startTime + 86400;

        File tmpDir = new File("tmp");
        tmpDir.mkdirs();

        String rootPath = Utils.pathJoin(tmpDir.getPath(), "demo_flow.rrd");
        int timeInterval = 600;
        int rrdLength = 8640;

        String picPath = Utils.pathJoin(tmpDir.getPath(), "demo_flow.png");

        /*
         * 测试创建RrdDb
         */
        RRDTools.CreateMonitorInfoRRD(rootPath, timeInterval, rrdLength,
                startTime);
        /*
         * 测试添加 初始数据
         */
        GeneralMonitorInfo msg = PerfMon.getGeneralMonitorInfo();
        for (long t = startTime; t < endTime; t += 10) {
            RrdDb rrd;
            try {
                rrd = new RrdDb(rootPath);
                RRDTools.addMonitorInfoInRRD(rrd, msg, t);
                rrd.close();
            } catch (IOException e) {

                e.printStackTrace();
            } catch (RrdException e) {
                e.printStackTrace();
            }

        }

        /*
         * 测试绘图
         */

        long startTm = endTime - 86400;
        RRDTools.plotCpuGraph(picPath, startTm, endTime, rootPath);
        /*
         * RRDTools.plotMemoryGraph(picPath, startTm, endTime, rootPath);
         * RRDTools.plotDiskGraph(picPath, startTm, endTime, rootPath);
         * RRDTools.plotNetGraph(picPath, startTm, endTime, rootPath);
         */

        /*
         * 测试FetchData获取RRD
         */
        double[][] values = RRDTools.fetchRRDData(rootPath, startTime, endTime);
        int rowCount = values[0].length;
        int columnCount = values.length;
        System.out.println(rowCount + " " + columnCount);
        StringBuffer buffer = new StringBuffer("");
        long timeCur = startTime;
        for (int row = 0; row < rowCount; row++) {
            for (int dsIndex = 0; dsIndex < columnCount; dsIndex++) {
                buffer.append(Util.formatDouble(values[dsIndex][row]));
                buffer.append("  ");
            }
            buffer.append(timeCur);
            timeCur += 5;
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
