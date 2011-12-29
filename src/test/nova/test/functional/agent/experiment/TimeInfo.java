package nova.test.functional.agent.experiment;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeInfo {

    ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> vNodesTime = new ConcurrentHashMap<String, ConcurrentHashMap<String, Long>>();
    private static TimeInfo instance = null;

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> getvNodesTime() {
        return this.vNodesTime;
    }

    public static TimeInfo getInstance() {
        if (TimeInfo.instance == null)
            instance = new TimeInfo();

        return instance;
    }

    private TimeInfo() {
    }

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

    public static void setStartTime(String ip, long curTime) {
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("startTime", curTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);

    }

    public static void setConfigTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("configTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    public static void setDownloadTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("downloadTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    public static void setPackIsoTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("packIsoTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    public static void setStartVnodeTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("startVnodeTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    public static void setDeployTime(String ip, long curTime) {
        long lastStepTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("lastStepTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("deployTime", curTime - lastStepTime);
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("lastStepTime", curTime);
    }

    public static void setTotalTime(String ip, long curTime) {
        long startTime = TimeInfo.getInstance().getvNodesTime().get(ip)
                .get("startTime");
        TimeInfo.getInstance().getvNodesTime().get(ip)
                .replace("totalTime", curTime - startTime);
    }

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
}
