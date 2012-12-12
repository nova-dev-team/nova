package nova.master.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

}
