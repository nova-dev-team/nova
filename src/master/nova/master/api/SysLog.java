package nova.master.api;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Calendar;

/**
 * syslog to file
 * 
 * @author eaglewatcher
 */
public class SysLog {

    public static void info(String s) {
        writeToTodayLog("INFO  :", s);
    }

    public static void warning(String s) {
        writeToTodayLog("WARN:", s);
    }

    public static void severe(String s) {
        writeToTodayLog("ERROR:", s);
    }

    private static void writeToTodayLog(String flag, String msg) {
        RandomAccessFile raf = null;
        try {
            Calendar now = Calendar.getInstance();
            String yyyy = String.valueOf(now.get(java.util.Calendar.YEAR));
            String mm = String.valueOf(now.get(Calendar.MONTH) + 1);
            String dd = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
            String hh = String.valueOf(now.get(Calendar.HOUR_OF_DAY));
            String ff = String.valueOf(now.get(Calendar.MINUTE));
            String ss = String.valueOf(now.get(Calendar.SECOND));
            mm = (1 == mm.length()) ? ("0" + mm) : mm;
            dd = (1 == dd.length()) ? ("0" + dd) : dd;
            hh = (1 == hh.length()) ? ("0" + hh) : hh;
            ff = (1 == ff.length()) ? ("0" + ff) : ff;
            ss = (1 == ss.length()) ? ("0" + ss) : ss;
            String yyyymmdd = yyyy + mm + dd;
            String hhffss = hh + ff + ss;
            String path = System.getProperties().getProperty("user.dir")
                    + File.separator + "log";
            File p = new File(path);
            if (!p.exists()) {
                p.mkdirs();
            }
            path += File.separator + "VNCPM_" + yyyymmdd + ".log";
            File f = new File(path);
            if (f.isDirectory()) {
                f.delete();
            }
            raf = new RandomAccessFile(f, "rw");
            raf.seek(raf.length());
            raf.writeBytes(hhffss + "  " + flag + " : " + msg + "\r\n");
            raf.close();
        } catch (Exception ex) {
            System.out.println("write file has error=" + ex);
        }
    }

    private SysLog() {
    }
}