package nova.master.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * VNC Port Map for Master<------>Worker
 * 
 * @author eaglewatcher
 */
public class MasterVNCPM {

    private static List<Server> serverList = new ArrayList<Server>();

    private static List<Route> routeList = new ArrayList<Route>();

    public static void main(String args[]) {
        startService();
    }

    // start
    public static void startService() {
        if (!loadCfgFile()) {
            System.exit(1);
        }
        while (serverList.size() > 0) {
            Server ts = serverList.remove(0);
            ts.closeServer();
        }
        for (int i = 0; i < routeList.size(); i++) {
            Route r = routeList.get(i);
            Server server = new Server(r);
            serverList.add(server);
        }
    }

    public static void stop() {
        while (serverList.size() > 0) {
            Server ts = serverList.remove(0);
            ts.closeServer();
        }

    }

    public static void addService(String srcIP, int srcPort, String dstIP,
            int dstPort) {
        Route r = new Route();
        r.LocalIP = srcIP;
        r.LocalPort = srcPort;
        r.DestHost = dstIP;
        r.DestPort = dstPort;
        r.AllowClient = "*.*.*.*";
        Server server = new Server(r);
        serverList.add(server);
    }

    /**
     * read portmap info
     * 
     * @return boolean
     */
    private static boolean loadCfgFile() {
        try {
            String userHome = System.getProperties().getProperty("user.dir");
            if (userHome == null) {
                userHome = "";
            } else {
                userHome = userHome + File.separator;
            }
            userHome += "conf" + File.separator + "VNCPM.cfg";
            InputStream is = new FileInputStream(userHome);
            Properties pt = new Properties();
            pt.load(is);
            int ServiceCount = Integer
                    .parseInt(pt.getProperty("TransferCount"));
            for (; ServiceCount > 0; ServiceCount--) {
                Route r = new Route();
                r.LocalIP = pt.getProperty("LocalIP." + ServiceCount).trim();
                r.LocalPort = Integer.parseInt(pt.getProperty(
                        "LocalPort." + ServiceCount).trim());
                r.DestHost = pt.getProperty("DestHost." + ServiceCount).trim();
                r.DestPort = Integer.parseInt(pt.getProperty(
                        "DestPort." + ServiceCount).trim());
                r.AllowClient = pt.getProperty("AllowClient." + ServiceCount)
                        .trim();
                routeList.add(r);
            }
            is.close();
            SysLog.info("system Read cfg file OK");
        } catch (Exception e) {
            System.out.println("loadCfgFile false:" + e);
            SysLog.severe("loadCfgFile false :" + e);
            return false;
        }
        return true;
    }

    public static boolean saveCfgFile() {
        try {
            String userHome = System.getProperties().getProperty("user.dir");
            if (userHome == null) {
                userHome = "";
            } else {
                userHome = userHome + File.separator;
            }
            userHome += "conf" + File.separator + "VNCPM.cfg";
            OutputStream os = new FileOutputStream(userHome);
            Properties pt = new Properties();
            pt.store(os, "VNC Port Map Configure File");
            int ServiceCount = routeList.size();
            pt.setProperty("TransferCount", String.valueOf(ServiceCount));
            for (; ServiceCount > 0; ServiceCount--) {
                Route r = routeList.get(ServiceCount - 1);
                pt.setProperty("LocalIP." + ServiceCount, r.LocalIP);
                pt.setProperty("LocalPort." + ServiceCount,
                        String.valueOf(r.LocalPort));
                pt.setProperty("DestHost." + ServiceCount, r.DestHost);
                pt.setProperty("DestPort." + ServiceCount,
                        String.valueOf(r.DestPort));
                pt.setProperty("AllowClient." + ServiceCount,
                        String.valueOf(r.AllowClient));
            }
            os.flush();
            os.close();
            SysLog.info("system Save cfg file OK");
        } catch (Exception e) {
            System.out.println("SaveCfgFile false:" + e);
            SysLog.severe("SaveCfgFile false :" + e);
            return false;
        }
        return true;
    }
}
