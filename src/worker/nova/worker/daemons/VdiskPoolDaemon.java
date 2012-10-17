package nova.worker.daemons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import nova.common.util.SimpleDaemon;
import nova.common.util.Utils;
import nova.worker.NovaWorker;

import org.apache.log4j.Logger;
import org.libvirt.LibvirtException;

/**
 * Daemon thread that maintains vdisk pool
 * 
 * @author shayf
 * 
 */
public class VdiskPoolDaemon extends SimpleDaemon {

    // TODO @shayf need to update fileStatus when add or del imgs
    private HashMap<String, VdiskFile> fileStatus;

    private void updateFileStatus() {
        File pathFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run"));
        if (!pathFile.exists()) {
            Utils.mkdirs(pathFile.getAbsolutePath());
        }

        for (String stdImgFile : pathFile.list()) {
            File tmp = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    stdImgFile));
            if (!tmp.isDirectory()) {
                for (int i = 1; i <= POOL_SIZE; i++) {
                    VdiskFile tmpVdiskFile = new VdiskFile();
                    tmpVdiskFile.setStatValue(i, stdImgFile);
                    tmpVdiskFile.setLastVisitTime(System.currentTimeMillis());
                    fileStatus.put(stdImgFile + ".pool." + Integer.toString(i),
                            tmpVdiskFile);
                }

            }
        }
    }

    public VdiskPoolDaemon() {
        super(2000);
        fileStatus = new HashMap<String, VdiskFile>();
        updateFileStatus();
        File imgfile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                "small.img"));
        if (!imgfile.exists()) {
            Process p;
            String cmd = "qemu-img create -f qcow2 "
                    + Utils.pathJoin(Utils.NOVA_HOME, "run", "small.img")
                    + " 4G";
            System.out.println(cmd);
            try {
                p = Runtime.getRuntime().exec(cmd);
                InputStream fis = p.getInputStream();
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.setLastCheckIsVmRunningTime(System.currentTimeMillis());
        this.setMbps(1);
        this.setVmRunning(false);
    }

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(VdiskPoolDaemon.class);

    static int POOL_SIZE = 2;

    private long lastCheckIsVmRunningTime;
    private int mbps;
    private boolean vmRunning;

    public boolean isVmRunning() throws LibvirtException {
        if (System.currentTimeMillis() - this.getLastCheckIsVmRunningTime() < 5000) {
            return this.vmRunning;
        } else {
            this.setLastCheckIsVmRunningTime(System.currentTimeMillis());

            synchronized (NovaWorker.getInstance().getConnLock()) {
                if (NovaWorker.getInstance().getConn("qemu:///system", false)
                        .numOfDomains() > 0) {
                    // NovaWorker.getInstance().closeConnectToKvm();
                    this.setVmRunning(true);
                } else {
                    // NovaWorker.getInstance().closeConnectToKvm();
                    this.setVmRunning(false);
                }
            }
            return this.vmRunning;
        }
    }

    public void setVmRunning(boolean vmRunning) {
        this.vmRunning = vmRunning;
    }

    public int getMbps() {
        return mbps;
    }

    public void setMbps(int mbps) {
        this.mbps = mbps;
    }

    public long getLastCheckIsVmRunningTime() {
        return lastCheckIsVmRunningTime;
    }

    public void setLastCheckIsVmRunningTime(long lastCheckIsVmRunningTime) {
        this.lastCheckIsVmRunningTime = lastCheckIsVmRunningTime;
    }

    private static class VdiskFile {
        Status stat;
        long len;
        long lastVisitTime;

        public long getLen() {
            return len;
        }

        public void setLen(long len) {
            this.len = len;
        }

        public long getLastVisitTime() {
            return lastVisitTime;
        }

        public void setLastVisitTime(long lastVisitTime) {
            this.lastVisitTime = lastVisitTime;
        }

        public Status getStat() {
            return stat;
        }

        public void setStat(Status stat) {
            this.stat = stat;
        }

        public void setStatValue(int i, String stdImgFile) {
            File img = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "vdiskpool", stdImgFile + ".pool." + Integer.toString(i)));
            File lockedImg = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "vdiskpool", stdImgFile + ".pool." + Integer.toString(i)
                            + ".lock"));
            if (!img.exists() && !lockedImg.exists()) {
                this.stat = Status.NOT_EXIST;
                len = -1;
            } else if (img.exists() && !lockedImg.exists()) {
                this.stat = Status.AVAILABLE;
                len = img.length();
            } else if (!img.exists() && lockedImg.exists()) {
                this.stat = Status.LOCKED;
                len = lockedImg.length();
            } else { // both exist
                lockedImg.delete();
                this.stat = Status.AVAILABLE;
                len = img.length();
            }
        }

        enum Status {
            /**
             * The vdiskfile is not existed.
             */
            NOT_EXIST,

            /**
             * The vdiskfile is locked for writing
             */
            LOCKED,

            /**
             * The vdiskfile is available
             */
            AVAILABLE,
        }
    }

    public static int getPOOL_SIZE() {
        return POOL_SIZE;
    }

    public static void setPOOL_SIZE(int poolSize) {
        POOL_SIZE = poolSize;
    }

    private void checkRevoke(String parent) {
        File parentPath = new File(parent);
        boolean found = false;
        while (true) {
            String prefix = null;
            for (String fn : parentPath.list()) {
                if (fn.endsWith(".revoke")) {
                    prefix = fn.substring(0, fn.length() - 7);
                    found = true;
                    break;
                }
            }
            if (found) {
                File[] delList = parentPath.listFiles();
                for (File delfn : delList) {
                    if (delfn.getName().startsWith(prefix)) {
                        delfn.delete();
                    }
                }
                File[] delList2 = parentPath.getParentFile().listFiles();
                for (File delfn : delList2) {
                    if (delfn.getName().startsWith(prefix)) {
                        delfn.delete();
                    }
                }
                found = false;
            } else {
                break;
            }
        }
    }

    private void autoSpeedCopy(File sourceFile, File tmpFile)
            throws IOException, LibvirtException {
        FileInputStream input = new FileInputStream(sourceFile);
        FileOutputStream output = new FileOutputStream(tmpFile);
        long bytesCopied = 0;
        byte[] b = new byte[1024 * 5];
        int cnt;
        int loop = 0;
        long timeStamp = System.currentTimeMillis();
        double sleepUsec = 1.0;
        while (!this.isStopping()) {
            loop++;
            if (loop % 1024 == 0) {
                checkRevoke(tmpFile.getParent());
            }

            cnt = input.read(b);
            if (cnt <= 0)
                break;
            output.write(b, 0, cnt);
            if (this.getMbps() > 0) {
                bytesCopied += cnt;
                if (bytesCopied > this.getMbps() * 1024 * 1024) {
                    timeStamp -= System.currentTimeMillis();
                    if (bytesCopied < 1024 * 1024 * this.getMbps() * timeStamp
                            / 1000) {
                        sleepUsec /= 1.1;
                    } else {
                        sleepUsec *= 1.1;
                    }
                    if (sleepUsec > 1000 * 20) {
                        sleepUsec = 1000 * 20;
                    }
                    try {
                        Thread.sleep(Math.round(sleepUsec));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (!this.isVmRunning()) {
                    this.setMbps(0);
                }
            } else {
                if (this.isVmRunning()) {
                    timeStamp = System.currentTimeMillis();
                    sleepUsec = 1.0;
                    this.setMbps(1);
                    bytesCopied = 0;
                }
            }
        }
        output.flush();
        output.close();
        input.close();
    }

    @Override
    protected void workOneRound() {
        if (this.isStopping() == true) {
            return;
        }
        final String path = Utils.pathJoin(Utils.NOVA_HOME, "run", "vdiskpool");
        File vdiskpoolFoder = new File(path);
        if (!vdiskpoolFoder.exists()) {
            vdiskpoolFoder.mkdirs();
        }
        checkRevoke(path);
        updateFileStatus();
        File pathFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run"));
        if (pathFile.list().length > 0) {
            for (String stdImgFile : pathFile.list()) {
                File tmp = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                        stdImgFile));
                if (!tmp.isDirectory()) {
                    for (int i = POOL_SIZE; i > 0; i--) {
                        if (fileStatus
                                .get(stdImgFile + ".pool."
                                        + Integer.toString(i)).getStat()
                                .equals(VdiskFile.Status.NOT_EXIST)) {
                            fileStatus
                                    .get(stdImgFile + ".pool."
                                            + Integer.toString(i)).setStat(
                                            VdiskFile.Status.LOCKED);
                            try {
                                String sourceUrl = Utils.pathJoin(
                                        Utils.NOVA_HOME, "run", stdImgFile);
                                String destUrl = Utils
                                        .pathJoin(path, stdImgFile + ".pool."
                                                + Integer.toString(i) + ".lock");
                                File sourceFile = new File(sourceUrl);
                                File tmpFile = new File(destUrl);
                                File destFile = new File(Utils.pathJoin(
                                        path,
                                        stdImgFile + ".pool."
                                                + Integer.toString(i)));
                                if (sourceFile.isFile()) {
                                    this.autoSpeedCopy(sourceFile, tmpFile);
                                }

                                if (tmpFile.length() == sourceFile.length()) {
                                    tmpFile.renameTo(destFile);
                                    fileStatus
                                            .get(stdImgFile + ".pool."
                                                    + Integer.toString(i))
                                            .setStat(VdiskFile.Status.AVAILABLE);
                                    fileStatus.get(
                                            stdImgFile + ".pool."
                                                    + Integer.toString(i))
                                            .setLen(destFile.length());
                                    fileStatus.get(
                                            stdImgFile + ".pool."
                                                    + Integer.toString(i))
                                            .setLastVisitTime(
                                                    System.currentTimeMillis());
                                } else {
                                    fileStatus.get(
                                            stdImgFile + ".pool."
                                                    + Integer.toString(i))
                                            .setLen(tmpFile.length());
                                    fileStatus.get(
                                            stdImgFile + ".pool."
                                                    + Integer.toString(i))
                                            .setLastVisitTime(
                                                    System.currentTimeMillis());
                                }
                                if (this.isStopping()) {
                                    break;
                                }
                            } catch (IOException e) {
                                log.error("copy image fail", e);
                            } catch (LibvirtException e) {
                                log.error("libvirt connection fail", e);
                            }

                        } else if (fileStatus
                                .get(stdImgFile + ".pool."
                                        + Integer.toString(i)).getStat()
                                .equals(VdiskFile.Status.LOCKED)) {
                            if (System.currentTimeMillis()
                                    - fileStatus.get(
                                            stdImgFile + ".pool."
                                                    + Integer.toString(i))
                                            .getLastVisitTime() > 1000) {
                                File lockedImg = new File(
                                        Utils.pathJoin(
                                                Utils.NOVA_HOME,
                                                "run",
                                                "vdiskpool",
                                                stdImgFile + ".pool."
                                                        + Integer.toString(i)
                                                        + ".lock"));
                                if (fileStatus.get(
                                        stdImgFile + ".pool."
                                                + Integer.toString(i)).getLen() == lockedImg
                                        .length()) {
                                    // copy failed
                                    lockedImg.delete();
                                }
                                fileStatus.get(
                                        stdImgFile + ".pool."
                                                + Integer.toString(i)).setStat(
                                        VdiskFile.Status.NOT_EXIST);
                                fileStatus.get(
                                        stdImgFile + ".pool."
                                                + Integer.toString(i)).setLen(
                                        -1);
                                fileStatus.get(
                                        stdImgFile + ".pool."
                                                + Integer.toString(i))
                                        .setLastVisitTime(
                                                System.currentTimeMillis());
                            }
                        }
                    }
                }
            }
        }

    }
}
