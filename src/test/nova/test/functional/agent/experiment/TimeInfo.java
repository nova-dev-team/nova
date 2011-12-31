package nova.test.functional.agent.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nova.common.util.Pair;
import nova.common.util.Utils;

import org.apache.log4j.Logger;

public class TimeInfo {

    static Logger log = Logger.getLogger(TimeInfo.class);

    /*
     * key->value: vcluster name -> Pair<startIP, cluster size>
     */
    ConcurrentHashMap<String, Pair<String, Integer>> vClusterRange = new ConcurrentHashMap<String, Pair<String, Integer>>();
    /*
     * key->value: vnode IP -> CHMap<timeType, time>
     */
    ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> vNodesTime = new ConcurrentHashMap<String, ConcurrentHashMap<String, Long>>();
    private static TimeInfo instance = null;

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> getvNodesTime() {
        return this.vNodesTime;
    }

    public ConcurrentHashMap<String, Pair<String, Integer>> getvClusterRange() {
        return this.vClusterRange;
    }

    public static TimeInfo getInstance() {
        if (TimeInfo.instance == null)
            instance = new TimeInfo();

        return instance;
    }

    private TimeInfo() {
    }

    /**
     * Create a vnode time consumption hashMap with his IP as key
     * 
     * @param ip
     *            vNode ip
     */
    public static void setVnodeTimeCal(String ip) {
        ConcurrentHashMap<String, Long> vNodeTime = new ConcurrentHashMap<String, Long>();
        vNodeTime.put("startTime", (long) 0);
        vNodeTime.put("totalTime", (long) 0);
        vNodeTime.put("configTime", (long) 0);
        vNodeTime.put("downloadTime", (long) 0);
        vNodeTime.put("packIsoTime", (long) 0);
        vNodeTime.put("startVnodeTime", (long) 0);
        vNodeTime.put("deployTime", (long) 0);
        vNodeTime.put("lastStepTime", (long) 0);
        TimeInfo.getInstance().getvNodesTime().put(ip, vNodeTime);
    }

    /**
     * Set start time
     * 
     * @param ip
     *            vNode IP
     * @param curTime
     *            current time
     */
    public static void setStartTime(String ip, long curTime) {
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("startTime", curTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);

    }

    /**
     * Set config Time
     * 
     * @param ip
     *            vNode IP
     * @param curTime
     *            current time
     */
    public static void setConfigTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("configTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    /**
     * Set download apps Time
     * 
     * @param ip
     *            vNode IP
     * @param curTime
     *            current time
     */
    public static void setDownloadTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("downloadTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    /**
     * Set pack ISO Time
     * 
     * @param ip
     *            vNode IP
     * @param curTime
     *            current time
     */
    public static void setPackIsoTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("packIsoTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    /**
     * Set start Vnode Time
     * 
     * @param ip
     *            vNode IP
     * @param curTime
     *            current time
     */
    public static void setStartVnodeTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("startVnodeTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    /**
     * Set deploy apps Time
     * 
     * @param ip
     *            vNode IP
     * @param curTime
     *            current time
     */
    public static void setDeployTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("deployTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    /**
     * Set total Time
     * 
     * @param ip
     *            vNode IP
     * @param curTime
     *            current time
     */
    public static void setTotalTime(String ip, long curTime) {
        long startTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("startTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("totalTime", curTime - startTime);
    }

    /**
     * Set different time value for different type
     * 
     * @param ip
     *            vNode time
     * @param timeType
     *            which type of this operation
     * @param curTime
     *            current time
     */
    public static void setTime(String ip, String timeType, long curTime) {
        if (timeType.equals("startTime"))
            setStartTime(ip, curTime);
        else if (timeType.equals("configTime"))
            setConfigTime(ip, curTime);
        else if (timeType.equals("downloadTime"))
            setDownloadTime(ip, curTime);
        else if (timeType.equals("packIsoTime"))
            setPackIsoTime(ip, curTime);
        else if (timeType.equals("startVnodeTime"))
            setStartVnodeTime(ip, curTime);
        else if (timeType.equals("deployTime"))
            setDeployTime(ip, curTime);
        else if (timeType.equals("totalTime"))
            setTotalTime(ip, curTime);
        else if (timeType.equals("startCal"))
            setVnodeTimeCal(ip);
    }

    /**
     * 
     * @param vClusterName
     *            vcluster name
     * @param firstIP
     *            vcluster capable first IP
     * @param size
     *            vcluster size
     */
    public static void setVClusterInfo(String vClusterName, String firstIP,
            int size) {
        Pair<String, Integer> range = new Pair<String, Integer>();
        range.setFirst(firstIP);
        range.setSecond(size);
        TimeInfo.getInstance().getvClusterRange().put(vClusterName, range);
    }

    /**
     * Get vcluster size by its name
     * 
     * @param vClusterName
     * @return vcluster size
     */
    public static int getVClusterSizeByName(String vClusterName) {
        return TimeInfo.getInstance().getvClusterRange().get(vClusterName)
                .getSecond();
    }

    /**
     * Loop vNodesTime to find the how many nodes are in vluster with name as
     * vClusterName
     * 
     * @param vClusterName
     * @return How many vnodes are ready in this vcluster with name as
     *         vClusterName
     */
    public static int getCurrentVClusterSizeByName(String vClusterName) {
        int size = 0;
        ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> vNodes = TimeInfo
                .getInstance().getvNodesTime();
        Iterator<?> iter = vNodes.entrySet().iterator();
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            String ipAddr = ((String) entry.getKey()).split(":")[0];
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<String, Long> vNode = (ConcurrentHashMap<String, Long>) entry
                    .getValue();
            if (TimeInfo.getVClusterNameByIP(ipAddr).equals(vClusterName)
                    && vNode.get("totalTime") != 0)
                size += 1;

        }
        return size;
    }

    /**
     * Get vcluster name by vnode ip
     * 
     * @param ip
     * @return vcluster name
     */
    public static String getVClusterNameByIP(String ip) {
        String vName = null;
        ConcurrentHashMap<String, Pair<String, Integer>> vClusterRange = TimeInfo
                .getInstance().getvClusterRange();
        Iterator<?> iter = vClusterRange.entrySet().iterator();
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            @SuppressWarnings("unchecked")
            Pair<String, Integer> pair = (Pair<String, Integer>) entry
                    .getValue();
            if (Utils.ipv4ToInteger(pair.getFirst()) <= Utils.ipv4ToInteger(ip)
                    && Utils.ipv4ToInteger(ip) < Utils.ipv4ToInteger(pair
                            .getFirst()) + pair.getSecond()) {
                System.out.println(entry.getKey());
                vName = (String) entry.getKey();
                break;
            }

        }
        return vName;
    }

    /**
     * for test
     */
    public static void print() {
        ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> vNodes = TimeInfo
                .getInstance().getvNodesTime();
        Iterator<?> iter = vNodes.entrySet().iterator();
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            System.out.println(entry.getKey() + " : ");
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> vNode = (ConcurrentHashMap<String, ConcurrentHashMap<String, Long>>) entry
                    .getValue();
            Iterator<?> iter2 = vNode.entrySet().iterator();
            while (iter2.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry entry2 = (Map.Entry) iter2.next();
                System.out.println(entry2.getKey().toString() + " : "
                        + entry2.getValue().toString());
            }
            System.out.println("*****************");
            System.out.println("*****************");
        }
    }

    /**
     * Write experiments info of this vcluster in a file
     * 
     * @param vClusterName
     */
    public static void writeToFile(String vClusterName) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> vNodes = TimeInfo
                .getInstance().getvNodesTime();
        // max total time among all vnodes
        long maxTotalTime = 0;

        // save in a file like xxxx_2011-10-12_23:14:23.txt
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd_HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String formatDate = dateFormat.format(date);
        File testFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                "softwares", "testResult", vClusterName + "_" + formatDate
                        + ".txt"));

        try {
            if (!testFile.exists()) {
                testFile.createNewFile();
            }
            OutputStream os = new FileOutputStream(testFile);
            os.write("++++++++++++++++++++++++++++++++++++++++++++++++"
                    .getBytes());
            os.write("\n".getBytes());
            os.write((vClusterName + ": ").getBytes());
            os.write("\n".getBytes());
            Iterator<?> iter = vNodes.entrySet().iterator();
            while (iter.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry entry = (Map.Entry) iter.next();
                String ipAddr = ((String) entry.getKey()).split(":")[0];
                if (TimeInfo.getVClusterNameByIP(ipAddr).equals(vClusterName)) {
                    os.write((entry.getKey() + " : ").getBytes());
                    os.write("\n".getBytes());
                    @SuppressWarnings("unchecked")
                    ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> vNode = (ConcurrentHashMap<String, ConcurrentHashMap<String, Long>>) entry
                            .getValue();
                    Iterator<?> iter2 = vNode.entrySet().iterator();
                    while (iter2.hasNext()) {
                        @SuppressWarnings("rawtypes")
                        Map.Entry entry2 = (Map.Entry) iter2.next();
                        if (entry2.getKey().toString().equals("totalTime")) {
                            if ((Long) entry2.getValue() > maxTotalTime) {
                                maxTotalTime = (Long) entry2.getValue();
                            }
                        }
                        os.write((entry2.getKey().toString() + " : " + entry2
                                .getValue().toString()).getBytes());
                        os.write("\n".getBytes());
                    }
                    os.write("**********************************".getBytes());
                    os.write("\n".getBytes());
                }
            }
            os.write(("MaxTotalTime: " + maxTotalTime).getBytes());
            os.write("\n".getBytes());
            os.write("++++++++++++++++++++++++++++++++++++++++++++++++"
                    .getBytes());
            os.write("\n".getBytes());
            os.close();
        } catch (FileNotFoundException e1) {
            log.error("file not found!", e1);
        } catch (IOException e1) {
            log.error("file write fail!", e1);
        }
    }
}
