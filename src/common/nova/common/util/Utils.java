package nova.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.stringtree.Fetcher;
import org.stringtree.finder.StringFinder;
import org.stringtree.template.InlineTemplater;

/**
 * Provides handy utility functions.
 * 
 * @author santa
 * 
 */
public class Utils {

    /**
     * Log4j logger.
     */
    static Logger logger = Logger.getLogger(Utils.class);

    /**
     * The home for nova.
     */
    public static final String NOVA_HOME;

    public static HashMap<String, String> WORKER_VNC_MAP = new HashMap<String, String>();
    public static HashMap<String, String> MASTER_VNC_MAP = new HashMap<String, String>();

    public static int delMPEmptycount;
    /*
     * Static constructor to determine NOVA_HOME.
     */
    static {
        final String confName = Utils.pathJoin("conf", "nova.properties");
        File cwd = new File(".");
        String folderPath = null;
        try {
            folderPath = cwd.getCanonicalPath();
        } catch (IOException e) {
            logger.error("Failed to determine current working directory", e);
        }

        // find conf file in folders
        while (true) {
            File folder = new File(folderPath);

            String confPath = Utils.pathJoin(folderPath, confName);
            File confFile = new File(confPath);
            if (confFile.exists()) {
                break;
            }

            String parentPath = folder.getParent();
            if (parentPath == null || parentPath.equals(folderPath)) {
                folderPath = null;
                break;
            } else {
                folderPath = parentPath;
            }
        }

        NOVA_HOME = folderPath;
        if (NOVA_HOME == null) {
            System.err.println("Failed to locate conf/nova.properties!");
            System.err
                    .println("Create the config files according to examples!");
            System.exit(1);
        }
    }

    /**
     * Recursively create dirs.
     * 
     * @param paths
     *            The whole path to be created.
     */
    public static void mkdirs(String... paths) {
        String path = Utils.pathJoin(paths);
        File pathFile = new File(path);
        pathFile.mkdirs();
    }

    /**
     * Recursively remove dirs and files.
     */
    public static void rmdir(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                } else if (f.isDirectory()) {
                    rmdir(f.getAbsolutePath());
                }
            }
            dir.delete();
        }
    }

    /**
     * Join several paths.
     * 
     * @param paths
     *            Arbitrary length arguments, each is a path segment.
     * @return The joined full path.
     * @throws IllegalArgumentException
     *             If the path segments is badly formed.
     */
    public static String pathJoin(String... paths) {
        StringBuffer sb = new StringBuffer();

        // normalized path segments
        ArrayList<String> normPaths = new ArrayList<String>();

        for (String path : paths) {
            if (path.equals("")) {
                continue;
            } else {
                normPaths.add(path);
            }
        }

        if (normPaths.size() > 0) {
            sb.append(normPaths.get(0));
            for (int i = 1; i < normPaths.size(); i++) {
                String pathSegment = normPaths.get(i);
                if (pathSegment.startsWith(File.separator)) {
                    sb = new StringBuffer();
                } else {
                    sb.append(File.separator);
                }
                sb.append(pathSegment);
            }
        }

        LinkedList<String> normSegs = new LinkedList<String>();

        // split string should be *regular expression*!!
        String splitToken = null;
        if (File.separator.equals("\\")) {
            splitToken = "\\\\";
        } else {
            splitToken = "/";
        }
        String[] splt = sb.toString().split(splitToken);

        for (String seg : splt) {
            if (seg.equals(".")) {
                continue;
            } else if (seg.equals("..")) {
                if (normSegs.size() > 0) {
                    normSegs.removeLast();
                } else {
                    logger.error("Bad path segments in: " + sb.toString());
                    throw new IllegalArgumentException("Bad path segments");
                }
            } else {
                normSegs.addLast(seg);
            }
        }

        StringBuffer normSb = new StringBuffer();
        for (String seg : normSegs) {
            if (normSb.length() > 0) {
                normSb.append(File.separator);
            }
            normSb.append(seg);
        }
        if (sb.toString().startsWith(File.separator)) {
            normSb.insert(0, File.separator);
        }

        if (normSb.length() == 0) {
            normSb.append(".");
        }

        return normSb.toString();
    }

    public static String[] pathSplit(String path) {
        File f = new File(path);
        return new String[] { f.getParent(), f.getName() };
    }

    @SuppressWarnings({ "rawtypes" })
    public static Object getField(Object obj, String fieldName) {
        Class objClass = obj.getClass();
        for (;;) {
            try {
                Field field = objClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (SecurityException e) {
                logger.error("Error fetching field " + fieldName
                        + " from class " + objClass.getName(), e);
            } catch (NoSuchFieldException e) {
                Class superClass = objClass.getSuperclass();
                if (superClass == null) {
                    logger.error("Error fetching field " + fieldName
                            + " from class " + objClass.getName(), e);
                } else {
                    objClass = superClass;
                    continue;
                }
            } catch (IllegalArgumentException e) {
                logger.error("Error fetching field " + fieldName
                        + " from class " + objClass.getName(), e);
            } catch (IllegalAccessException e) {
                logger.error("Error fetching field " + fieldName
                        + " from class " + objClass.getName(), e);
            }
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public static void setField(Object obj, String fieldName, Object newValue) {
        Class objClass = obj.getClass();
        try {
            Field field = objClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, newValue);
        } catch (SecurityException e) {
            logger.error("Error fetching field " + fieldName + " from class "
                    + objClass.getName(), e);
        } catch (NoSuchFieldException e) {
            logger.error("Error fetching field " + fieldName + " from class "
                    + objClass.getName(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching field " + fieldName + " from class "
                    + objClass.getName(), e);
        } catch (IllegalAccessException e) {
            logger.error("Error fetching field " + fieldName + " from class "
                    + objClass.getName(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static boolean hasField(Object obj, String fieldName) {
        Class objClass = obj.getClass();
        try {
            objClass.getDeclaredField(fieldName);
            return true;
        } catch (SecurityException e) {
            logger.error("Error fetching field " + fieldName + " from class "
                    + objClass.getName(), e);
        } catch (NoSuchFieldException e) {
            return false;
        }
        return false;
    }

    /**
     * Expand from a template. The values are extracted from an object.
     * 
     * @param template
     *            The template string.
     * @param obj
     *            The object from which the values should be retrieved.
     * @return The expanded template.
     */
    public static String expandTemplate(String template, final Object obj) {
        StringFinder finder = new StringFinder() {

            @Override
            public Object getObject(String key) {
                return getField(obj, key);
            }

            @Override
            public Fetcher getUnderlyingFetcher() {
                return null;
            }

            @Override
            public boolean contains(String key) {
                return hasField(obj, key);
            }

            @Override
            public String get(String key) {
                return getObject(key).toString();
            }
        };

        InlineTemplater templater = new InlineTemplater(finder);
        return templater.expand(template);
    }

    /**
     * Generate string from a template.
     * 
     * @param template
     *            The template content, place holders are like "${key}".
     * @param values
     *            The template values in a map.
     * @return Generated content.
     */
    public static String expandTemplate(String template,
            final Map<String, Object> values) {

        StringFinder finder = new StringFinder() {

            @Override
            public Object getObject(String key) {
                return values.get(key);
            }

            @Override
            public Fetcher getUnderlyingFetcher() {
                return null;
            }

            @Override
            public boolean contains(String key) {
                return values.containsKey(key);
            }

            @Override
            public String get(String key) {
                return values.get(key).toString();
            }
        };

        InlineTemplater templater = new InlineTemplater(finder);
        return templater.expand(template);
    }

    /**
     * Expand strings from a template file.
     * 
     * @param fpath
     *            Path to the template file.
     * @param values
     *            The template values in a map.
     * @return Generated content.
     */
    public static String expandTemplateFile(String fpath,
            Map<String, Object> values) {
        String template = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(
                    fpath)));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            template = sb.toString();
            br.close();
        } catch (IOException e) {
            logger.error("Failed to read '" + fpath + "'", e);
        }
        return Utils.expandTemplate(template, values);
    }

    /**
     * Change ipv4 to integer.
     * 
     * @param ipv4
     * @return intIp
     */
    public static long ipv4ToInteger(String ipv4) {
        String[] params = ipv4.split("\\.");
        return ((Long.parseLong(params[0]) * 256 + Long.parseLong(params[1])) * 256 + Long
                .parseLong(params[2])) * 256 + Long.parseLong(params[3]);
    }

    /**
     * Change intIp to ipv4.
     * 
     * @param intIp
     * @return ipv4
     */
    public static String integerToIpv4(long intIp) {
        long[] params = new long[4];
        for (int i = 3; i > -1; i--) {
            params[i] = intIp % 256;
            intIp = intIp / 256;
            ;
        }
        return String.valueOf(params[0]) + "." + String.valueOf(params[1])
                + "." + String.valueOf(params[2]) + "."
                + String.valueOf(params[3]);
    }

    /**
     * copy file
     */
    public static void copyOneFile(String srcFile, String dstFile) {

        FileInputStream fin = null;
        FileOutputStream fout = null;
        try {
            fin = new FileInputStream(srcFile);
        } catch (FileNotFoundException e) {
            logger.error("file " + srcFile + " not found ", e);
            return;
        }

        try {
            fout = new FileOutputStream(new File(dstFile));
        } catch (FileNotFoundException e) {
            logger.error("file " + srcFile + " not found ", e);
        }
        int c;
        byte[] b = new byte[1024 * 5];
        try {
            while ((c = fin.read(b)) != -1) {
                fout.write(b, 0, c);
            }
            fin.close();
            fout.flush();
            fout.close();
        } catch (IOException e) {
            logger.error("copy file error", e);
        }

    }

    /**
     * copy folder
     */
    public static void copy(String srcFile, String dstFile) {
        File in = new File(srcFile);
        File out = new File(dstFile);
        if (!in.exists()) {
            System.out.println(in.getAbsolutePath() + "源文件路径错误！！！");
            return;
        }

        if (!out.exists())
            out.mkdirs();
        File[] file = in.listFiles();
        FileInputStream fin = null;
        FileOutputStream fout = null;
        if (file != null) {
            for (int i = 0; i < file.length; i++) {
                if (file[i].isFile()) {
                    try {
                        fin = new FileInputStream(file[i]);
                    } catch (FileNotFoundException e) {
                        logger.error("file " + file[i].getName()
                                + " not found ", e);
                    }

                    try {
                        fout = new FileOutputStream(new File(dstFile + "/"
                                + file[i].getName()));
                    } catch (FileNotFoundException e) {
                        logger.error("file " + file[i].getName()
                                + " not found ", e);
                    }
                    int c;
                    byte[] b = new byte[1024 * 5];
                    try {
                        while ((c = fin.read(b)) != -1) {
                            fout.write(b, 0, c);
                        }
                        fin.close();
                        fout.flush();
                        fout.close();
                    } catch (IOException e) {
                        logger.error("copy file error", e);
                    }
                } else {
                    copy(Utils.pathJoin(srcFile, file[i].getName()),
                            Utils.pathJoin(dstFile, file[i].getName()));
                }
            }
        }
    }

    /**
     * copy folder with ignore list
     */
    public static void copyWithIgnore(String srcFile, String dstFile,
            String[] ignoreList) {
        File in = new File(srcFile);
        File out = new File(dstFile);
        if (!in.exists()) {
            System.out.println(in.getAbsolutePath() + "源文件路径错误！！！");
            return;
        }

        if (!out.exists())
            out.mkdirs();
        File[] file = in.listFiles();
        FileInputStream fin = null;
        FileOutputStream fout = null;
        for (int i = 0; i < file.length; i++) {
            if (!isInclude(ignoreList, file[i].getName())) {
                if (file[i].isFile()) {
                    try {
                        fin = new FileInputStream(file[i]);
                    } catch (FileNotFoundException e) {
                        logger.error("file " + file[i].getName()
                                + " not found ", e);
                    }

                    try {
                        fout = new FileOutputStream(new File(dstFile + "/"
                                + file[i].getName()));
                    } catch (FileNotFoundException e) {
                        logger.error("file " + file[i].getName()
                                + " not found ", e);
                    }
                    int c;
                    byte[] b = new byte[1024 * 5];
                    try {
                        while ((c = fin.read(b)) != -1) {
                            fout.write(b, 0, c);
                        }
                        fin.close();
                        fout.flush();
                        fout.close();
                    } catch (IOException e) {
                        logger.error("copy file error", e);
                    }
                } else {
                    copyWithIgnore(Utils.pathJoin(srcFile, file[i].getName()),
                            Utils.pathJoin(dstFile, file[i].getName()),
                            ignoreList);
                }
            }
        }
    }

    /**
     * copy folder with ignore sub-folders
     */
    public static void copyWithIgnoreFolder(String srcFile, String dstFile,
            String[] ignoreList) {
        File in = new File(srcFile);
        File out = new File(dstFile);
        if (!in.exists()) {
            System.out.println(in.getAbsolutePath() + "源文件路径错误！！！");
            return;
        }

        if (!out.exists())
            out.mkdirs();
        File[] file = in.listFiles();
        FileInputStream fin = null;
        FileOutputStream fout = null;
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                try {
                    fin = new FileInputStream(file[i]);
                } catch (FileNotFoundException e) {
                    logger.error("file " + file[i].getName() + " not found ", e);
                }

                try {
                    fout = new FileOutputStream(new File(dstFile + "/"
                            + file[i].getName()));
                } catch (FileNotFoundException e) {
                    logger.error("file " + file[i].getName() + " not found ", e);
                }
                int c;
                byte[] b = new byte[1024 * 5];
                try {
                    while ((c = fin.read(b)) != -1) {
                        fout.write(b, 0, c);
                    }
                    fin.close();
                    fout.flush();
                    fout.close();
                } catch (IOException e) {
                    logger.error("copy file error", e);
                }
            } else if (!isInclude(ignoreList, file[i].getName())) {
                copyWithIgnore(Utils.pathJoin(srcFile, file[i].getName()),
                        Utils.pathJoin(dstFile, file[i].getName()), ignoreList);
            }
        }
    }

    private static boolean isInclude(String[] names, String name) {
        for (String s : names) {
            if (name.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            temp = new File(Utils.pathJoin(path, tempList[i]));
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(Utils.pathJoin(path, tempList[i]));
                delFolder(Utils.pathJoin(path, tempList[i]));
            }
        }
        delFolder(path);
        return;
    }

    public static void delFolder(String folderPath) {
        try {
            java.io.File myFilePath = new java.io.File(folderPath);
            myFilePath.delete();
        } catch (Exception e) {
            logger.error("del folder " + folderPath + " error", e);
        }

    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.indexOf("win") >= 0;
    }

    public static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0
                || os.indexOf("nux") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static int getFreePort() {
        ServerSocket s = null;
        int MINPORT = 5960;
        int MAXPORT = 6990;
        for (; MINPORT < MAXPORT; MINPORT++) {
            try {
                s = new ServerSocket(MINPORT);
                s.close();
                return MINPORT;
            } catch (IOException e) {
                continue;
            }
        }
        return -1;

    }

    public static void delMP(int port) {
        String strcmd = "lsof -i:" + port;
        try {

            Process p = Runtime.getRuntime().exec(strcmd);
            final InputStream is = p.getInputStream();

            new Thread() {
                public void run() {
                    String line, result = "";
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    try {
                        while ((line = br.readLine()) != null) {

                            result = line;
                        }
                        String[] strs = result.split("[\\t \\n]+");
                        if (strs.length >= 2 && strs[0].equalsIgnoreCase("ssh")) {
                            String pid = strs[1];
                            String killcmd = "kill -9 " + pid;
                            Runtime.getRuntime().exec(killcmd);
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }.start();

            try {
                if (p.waitFor() != 0)
                    delMPEmptycount++;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error("del port map:" + port + " terminated!", e);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("del port map:" + port + " cmd error!", e);

        }

    }

}
