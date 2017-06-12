package nova.worker.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;
import nova.master.models.Vnode;
import nova.storage.NovaStorage;
import nova.worker.NovaWorker;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.daemons.VnodeStatusDaemon;
import nova.worker.models.StreamGobbler;
import nova.worker.virt.Kvm;
import nova.worker.virt.Lxc;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpProtocolException;

/**
 * Handler for "start new vnode" request.
 * 
 * @author santa, Tianyu Chen, Herb, eagle
 * 
 */
// TODO delete experiment codes between //////////////////// and //////////////
public class StartVnodeHandler implements SimpleHandler<StartVnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(StartVnodeHandler.class);

    /**
     * transfer os and software images
     * 
     * @param msg
     * @param stdImgFile
     *            - the file name of the os image, with extension
     */
    private void ftpFileTransfer(StartVnodeMessage msg, String stdImgFile) {
        File stdFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                stdImgFile));
        if (!stdFile.exists()) {
            if (NovaStorage.getInstance().getFtpServer() == null) {
                // NovaStorage.getInstance().startFtpServer();
            }
            try {
                FtpClient fc = FtpUtils.connect(
                        Conf.getString("storage.ftp.bind_host"),
                        Conf.getInteger("storage.ftp.bind_port"),
                        Conf.getString("storage.ftp.admin.username"),
                        Conf.getString("storage.ftp.admin.password"));
                FtpUtils.downloadFile(fc, Utils.pathJoin("img", stdImgFile),
                        Utils.pathJoin(Utils.NOVA_HOME, "run", stdImgFile));
            } catch (NumberFormatException e1) {
                log.error("port format error!", e1);
            } catch (IOException e1) {
                log.error("ftp connection fail!", e1);
            } catch (FtpProtocolException e) {
                log.error("ftp protocol exception! ", e);
            }
        }
        File foder = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                msg.getName()));
        if (!foder.exists()) {
            foder.mkdirs();
        }
        File dstFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                msg.getName(), stdImgFile));

        if (dstFile.exists() == false) {
            // create incremental images of source image
            System.out.println("Source image is " + msg.getHdaImage());
            Process createIncrmtlImgs;
            String cmd = "qemu-img create -b "
                    + Utils.pathJoin(Utils.NOVA_HOME, "run", stdImgFile)
                    + " -f qcow2 "
                    + Utils.pathJoin(Utils.NOVA_HOME, "run", msg.getName(),
                            stdImgFile);
            System.out.println("Ftp: " + cmd);

            try {
                createIncrmtlImgs = Runtime.getRuntime().exec(cmd);
                StreamGobbler errorGobbler = new StreamGobbler(
                        createIncrmtlImgs.getErrorStream(), "ERROR");
                errorGobbler.start();
                StreamGobbler outGobbler = new StreamGobbler(
                        createIncrmtlImgs.getInputStream(), "STDOUT");
                outGobbler.start();
                try {
                    if (createIncrmtlImgs.waitFor() != 0) {
                        log.error("create incremental image returned abnormal value!");
                    }
                } catch (InterruptedException e1) {
                    log.error("create incremental image process terminated", e1);
                }
            } catch (IOException e1) {
                log.error("exec create incremental image cmd error!", e1);
                return;
            }
        }

        if (msg.getRunAgent()) {
            File pathFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "softwares"));
            if (!pathFile.exists()) {
                Utils.mkdirs(pathFile.getAbsolutePath());
            }
            File paramsFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "softwares", "params"));
            if (!paramsFile.exists()) {
                Utils.mkdirs(paramsFile.getAbsolutePath());
            }
            File ipFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "softwares", "params", "ipconfig.txt"));

            try {
                if (!ipFile.exists()) {
                    ipFile.createNewFile();
                }
                OutputStream os = new FileOutputStream(ipFile);
                os.write(msg.getIpAddr().getBytes());
                os.write("\n".getBytes());
                os.write(msg.getSubnetMask().getBytes());
                os.write("\n".getBytes());
                os.write(msg.getGateWay().getBytes());
                os.write("\n".getBytes());
                os.write(msg.getName().getBytes());
                os.write("\n".getBytes());
                os.close();
            } catch (FileNotFoundException e1) {
                log.error("file not found!", e1);
            } catch (IOException e1) {
                log.error("file write fail!", e1);
            }

            // write nova.agent.ipaddress.properties file
            File ipAddrFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "conf",
                    "nova.agent.extrainfo.properties"));
            if (!ipAddrFile.exists()) {
                try {
                    ipAddrFile.createNewFile();
                } catch (IOException e1) {
                    log.error(
                            "create nova.agent.extrainfo.properties file fail!",
                            e1);
                }
            }

            try {
                PrintWriter outpw = new PrintWriter(new FileWriter(ipAddrFile));
                outpw.println("agent.bind_host="
                        + Conf.getString("worker.bind_host"));
                outpw.println("master.bind_host=" + msg.getIpAddr());
                outpw.println("master.bind_port=3000");
                outpw.close();
            } catch (IOException e1) {
                log.error("write nova.agent.extrainfo.properties file fail!",
                        e1);
            }

            // start download apps
            if (msg.getApps() != null) {
                for (String appName : msg.getApps()) {
                    if (NovaWorker.getInstance().getAppStatus()
                            .containsKey(appName) == false) {
                        try {
                            FtpClient fc = FtpUtils
                                    .connect(
                                            Conf.getString("storage.ftp.bind_host"),
                                            Conf.getInteger("storage.ftp.bind_port"),
                                            Conf.getString("storage.ftp.admin.username"),
                                            Conf.getString("storage.ftp.admin.password"));
                            FtpUtils.downloadDir(fc, Utils.pathJoin(
                                    "appliances", appName), Utils.pathJoin(
                                    Utils.NOVA_HOME, "run", "softwares",
                                    "appliances", appName)); // first
                                                             // install
                                                             // appliances
                            log.info("Download " + appName + " complete.");
                        } catch (NumberFormatException e1) {
                            log.error("port format error!", e1);
                            return;
                        } catch (IOException e1) {
                            log.error("ftp connection fail!", e1);
                            return;
                        } catch (FtpProtocolException e) {
                            log.error("ftp protocol exception! ", e);
                            return;
                        }
                        NovaWorker.getInstance().getAppStatus()
                                .put(appName, appName);
                    }
                }
            }

            // packiso process start
            // copy files to Novahome/run/softwares
            File agentProgramFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                    "run", "softwares", "run"));
            if (!agentProgramFile.exists()) {
                Utils.mkdirs(agentProgramFile.getAbsolutePath());
            }
            Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "conf"),
                    Utils.pathJoin(agentProgramFile.getAbsolutePath(), "conf"));
            Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "bin"),
                    Utils.pathJoin(agentProgramFile.getAbsolutePath(), "bin"));
            Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "lib"),
                    Utils.pathJoin(agentProgramFile.getAbsolutePath(), "lib"));
            // @Herbert
            System.out.println("HHHIIIA" + msg.getIsvim());
            if (msg.getIsvim() == 0) {
                // copy without ftp_home folder install vim
                String[] ingoreList = { "ftp_home", "hadoop-0.20.2", "WPS" };
                Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                        "data"), Utils.pathJoin(
                        agentProgramFile.getAbsolutePath(), "data"), ingoreList);
            }
            if (msg.getIsvim() == 1) {
                // install hadoop
                String[] ingoreList = { "ftp_home", "vim73", "WPS" };
                Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                        "data"), Utils.pathJoin(
                        agentProgramFile.getAbsolutePath(), "data"), ingoreList);
            }
            if (msg.getIsvim() == 2) {
                // install WPS
                String[] ingoreList = { "ftp_home", "vim73", "hadoop-0.20.2" };
                Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                        "data"), Utils.pathJoin(
                        agentProgramFile.getAbsolutePath(), "data"), ingoreList);
            }

            else {
                String[] ingoreList = { "ftp_home", "vim73", "hadoop-0.20.2",
                        "WPS" };
                Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                        "data"), Utils.pathJoin(
                        agentProgramFile.getAbsolutePath(), "data"), ingoreList);
            }
            Utils.copyOneFile(Utils.pathJoin(Utils.NOVA_HOME, "VERSION"), Utils
                    .pathJoin(agentProgramFile.getAbsolutePath(), "VERSION"));

            // pack iso files
            File agentCdFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    msg.getName(), "agentcd"));
            if (!agentCdFile.exists()) {
                Utils.mkdirs(agentCdFile.getAbsolutePath());
            }
            System.out.println("packing iso");
            Process p;
            String cmd = "mkisofs -J -T -R -V cdrom -o "
                    + Utils.pathJoin(Utils.NOVA_HOME, "run", msg.getName(),
                            "agentcd", "agent-cd.iso") + " "
                    + Utils.pathJoin(Utils.NOVA_HOME, "run", "softwares");

            try {
                p = Runtime.getRuntime().exec(cmd);
                StreamGobbler errorGobbler = new StreamGobbler(
                        p.getErrorStream(), "ERROR");
                errorGobbler.start();
                StreamGobbler outGobbler = new StreamGobbler(
                        p.getInputStream(), "STDOUT");
                outGobbler.start();
                try {
                    if (p.waitFor() != 0) {
                        log.error("pack iso returned abnormal value!");
                    }
                } catch (InterruptedException e1) {
                    log.error("pack iso process terminated", e1);
                }
            } catch (IOException e1) {
                log.error("exec mkisofs cmd error!", e1);
                return;
            }
        }
    }

    /**
     * transfer os and software images using pnfs protocol
     * 
     * @param msg
     * @param stdImgFile
     *            - the file name of the os image, with extension
     */
    private void pnfsFileTransfer(StartVnodeMessage msg, String stdImgFile) {
        // for debug
        log.info("image file: " + stdImgFile);
        String pnfsBaseDir = Utils.pathJoin(Utils.NOVA_HOME, "run", "run");
        File stdFile = new File(Utils.pathJoin(pnfsBaseDir, stdImgFile));
        if (stdFile.exists() == false) {
            log.error("disk image " + stdFile.getAbsolutePath() + " not found!");
            return;
        }
        String strWorkerIP = NovaWorker.getInstance().getAddr().getIp();

        // make the root directory of the guest domain
        File guestDir = null;

        // copy image file to the guest's directory
        if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
            guestDir = new File(Utils.pathJoin(pnfsBaseDir, strWorkerIP + "_"
                    + msg.getName()));
            if (!guestDir.exists()) {
                guestDir.mkdirs();
            }
            File file = new File(Utils.pathJoin(pnfsBaseDir, strWorkerIP + "_"
                    + msg.getName(), stdImgFile));
            if (file.exists() == false) {
                // create incremental images of source image
                Process createNFSIncrmtlImgs;
                String cmdofincrtlnfs = "qemu-img create -b "
                        + Utils.pathJoin(pnfsBaseDir, stdImgFile)
                        + " -f qcow2 "
                        + Utils.pathJoin(pnfsBaseDir,
                                strWorkerIP + "_" + msg.getName(), stdImgFile
                                        + " 100G");

                try {
                    createNFSIncrmtlImgs = Runtime.getRuntime().exec(
                            cmdofincrtlnfs);
                    StreamGobbler errorGobbler = new StreamGobbler(
                            createNFSIncrmtlImgs.getErrorStream(), "ERROR");
                    errorGobbler.start();
                    StreamGobbler outGobbler = new StreamGobbler(
                            createNFSIncrmtlImgs.getInputStream(), "STDOUT");
                    outGobbler.start();
                    try {
                        if (createNFSIncrmtlImgs.waitFor() != 0) {
                            log.error("create NFS incremental image returned abnormal value!");
                        }
                    } catch (InterruptedException e1) {
                        log.error(
                                "create NFS incremental image process terminated",
                                e1);
                    }
                } catch (IOException e1) {
                    log.error("exec create NFS incremental image cmd error!",
                            e1);
                    return;
                }
            }
        } else if (msg.getHyperVisor().equalsIgnoreCase("lxc")) {
            // do nothing
            log.info("we won't deploy file system here. ");
        } else {
            log.info("unsupported vm");
            return;
        }

        // deploy agent
        // TBD disable agent for lxc for now!!!
        if (msg.getRunAgent()) {
            File pathFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "run", strWorkerIP + "_" + msg.getName(), "softwares"));
            if (!pathFile.exists()) {
                Utils.mkdirs(pathFile.getAbsolutePath());
            }
            File paramsFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "run", strWorkerIP + "_" + msg.getName(), "softwares",
                    "params"));
            if (!paramsFile.exists()) {
                Utils.mkdirs(paramsFile.getAbsolutePath());
            }
            File ipFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "run", strWorkerIP + "_" + msg.getName(), "softwares",
                    "params", "ipconfig.txt"));

            try {
                if (!ipFile.exists()) {
                    ipFile.createNewFile();
                }
                OutputStream os = new FileOutputStream(ipFile);
                os.write(msg.getIpAddr().getBytes());
                os.write("\n".getBytes());
                os.write(msg.getSubnetMask().getBytes());
                os.write("\n".getBytes());
                os.write(msg.getGateWay().getBytes());
                os.write("\n".getBytes());
                os.write(msg.getName().getBytes());
                os.write("\n".getBytes());
                os.close();
            } catch (FileNotFoundException e1) {
                log.error("file not found!", e1);
            } catch (IOException e1) {
                log.error("file write fail!", e1);
            }
            // Herbert
            // For generating the network interfaces configuration file.
            File inFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "run", strWorkerIP + "_" + msg.getName(), "softwares",
                    "params", "interfaces"));

            try {
                if (!inFile.exists()) {
                    inFile.createNewFile();
                }
                OutputStream os = new FileOutputStream(inFile);
                os.write("auto eth0".getBytes());
                os.write("\n".getBytes());
                os.write("iface eth0 inet static".getBytes());
                os.write("\n".getBytes());
                os.write("address ".getBytes());
                os.write(msg.getIpAddr().getBytes());
                os.write("\n".getBytes());
                os.write("netmask ".getBytes());
                os.write("255.255.255.0".getBytes());
                os.write("\n".getBytes());
                os.write("gateway ".getBytes());
                os.write("192.168.122.1".getBytes());
                os.write("\n".getBytes());
                os.close();
            } catch (FileNotFoundException e1) {
                log.error("file not found!", e1);
            } catch (IOException e1) {
                log.error("file write fail!", e1);
            }

            // write nova.agent.ipaddress.properties file
            File ipAddrFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "conf",
                    "nova.agent.extrainfo.properties"));
            if (!ipAddrFile.exists()) {
                try {
                    ipAddrFile.createNewFile();
                } catch (IOException e1) {
                    log.error(
                            "create nova.agent.extrainfo.properties file fail!",
                            e1);
                }
            }

            try {
                PrintWriter outpw = new PrintWriter(new FileWriter(ipAddrFile));
                outpw.println("agent.bind_host=" + msg.getIpAddr());
                outpw.println("agent.bind_port=4100");
                outpw.println("master.bind_host=" + NovaWorker.masteraddr.ip);
                outpw.println("master.bind_port=" + NovaWorker.masteraddr.port);
                outpw.println("vnode.uuid=" + msg.getUuid());
                outpw.close();
            } catch (IOException e1) {
                log.error("write nova.agent.extrainfo.properties file fail!",
                        e1);
            }
            // prepare apps
            if (msg.getApps() != null) {
                File folderApp = new File(Utils.pathJoin(Utils.NOVA_HOME,
                        "run", "run", strWorkerIP + "_" + msg.getName(),
                        "softwares", "appliances"));
                if (folderApp.exists() == false)
                    folderApp.mkdirs();
                for (String appName : msg.getApps()) {
                    File appFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "run", strWorkerIP + "_" + msg.getName(),
                            "softwares", "appliances", appName));

                    if (appFile.exists() == false) {
                        File sourceFile = new File(Utils.pathJoin(
                                Utils.NOVA_HOME, "run", "appliances", appName));
                        if (sourceFile.isDirectory())
                            Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "run",
                                    "appliances", appName), Utils.pathJoin(
                                    Utils.NOVA_HOME, "run", "run", strWorkerIP
                                            + "_" + msg.getName(), "softwares",
                                    "appliances", appName));
                        else
                            Utils.copyOneFile(Utils.pathJoin(Utils.NOVA_HOME,
                                    "run", "appliances", appName), Utils
                                    .pathJoin(Utils.NOVA_HOME, "run", "run",
                                            strWorkerIP + "_" + msg.getName(),
                                            "softwares", "appliances", appName));
                    }
                }
            }

            // packiso process start
            // copy files to Novahome/run/$WORKERIP_NAME/softwares
            File agentProgramFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                    "run", "run", strWorkerIP + "_" + msg.getName(),
                    "softwares", "run"));
            if (!agentProgramFile.exists()) {
                Utils.mkdirs(agentProgramFile.getAbsolutePath());
            }
            Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "conf"),
                    Utils.pathJoin(agentProgramFile.getAbsolutePath(), "conf"));
            Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "run", "bin"),
                    Utils.pathJoin(agentProgramFile.getAbsolutePath(), "bin"));
            Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "run", "lib"),
                    Utils.pathJoin(agentProgramFile.getAbsolutePath(), "lib"));
            // @Herbert
            System.out.println("HHHIIIB" + msg.getIsvim());
            if (msg.getIsvim() == 0) {
                // copy without ftp_home folder install vim
                System.out.println("install vim");
                String[] ingoreList = { "ftp_home", "hadoop-0.20.2", "WPS" };
                Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                        "run", "data"), Utils.pathJoin(
                        agentProgramFile.getAbsolutePath(), "data"), ingoreList);
            } else if (msg.getIsvim() == 1) {
                // install hadoop
                System.out.println("install hadoop");
                String[] ingoreList = { "ftp_home", "vim73", "WPS" };
                Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                        "run", "data"), Utils.pathJoin(
                        agentProgramFile.getAbsolutePath(), "data"), ingoreList);
            } else if (msg.getIsvim() == 2) {
                // install WPS
                System.out.println("install WPS");
                String[] ingoreList = { "ftp_home", "vim73", "hadoop-0.20.2" };
                Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                        "run", "data"), Utils.pathJoin(
                        agentProgramFile.getAbsolutePath(), "data"), ingoreList);
            } else {
                System.out.println("install nothing");
                String[] ingoreList = { "ftp_home", "vim73", "hadoop-0.20.2",
                        "WPS" };
                Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                        "run", "data"), Utils.pathJoin(
                        agentProgramFile.getAbsolutePath(), "data"), ingoreList);
            }
            Utils.copyOneFile(
                    Utils.pathJoin(Utils.NOVA_HOME, "run", "VERSION"), Utils
                            .pathJoin(agentProgramFile.getAbsolutePath(),
                                    "VERSION"));

            // pack iso files
            File agentCdFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "run", strWorkerIP + "_" + msg.getName(), "agentcd"));
            if (!agentCdFile.exists()) {
                Utils.mkdirs(agentCdFile.getAbsolutePath());
            }
            System.out.println("packing iso");
            Process p;
            String cmd = "mkisofs -J -T -R -V cdrom -o "
                    + Utils.pathJoin(Utils.NOVA_HOME, "run", "run", strWorkerIP
                            + "_" + msg.getName(), "agentcd", "agent-cd.iso")
                    + " "
                    + Utils.pathJoin(Utils.NOVA_HOME, "run", "run", strWorkerIP
                            + "_" + msg.getName(), "softwares");

            try {
                p = Runtime.getRuntime().exec(cmd);
                StreamGobbler errorGobbler = new StreamGobbler(
                        p.getErrorStream(), "ERROR");
                errorGobbler.start();
                StreamGobbler outGobbler = new StreamGobbler(
                        p.getInputStream(), "STDOUT");
                outGobbler.start();
                try {
                    if (p.waitFor() != 0) {
                        log.error("pack iso returned abnormal value!");
                    }
                } catch (InterruptedException e1) {
                    log.error("pack iso process terminated", e1);
                }
            } catch (IOException e1) {
                log.error("exec mkisofs cmd error!", e1);
                return;
            }
        }

        // modify permissions if hypervisor is kvm
        if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
            String cmd = Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                    strWorkerIP + "_" + msg.getName());
            cmd = "chmod -R 777 " + cmd;
            Process p;
            try {
                p = Runtime.getRuntime().exec(cmd);
                StreamGobbler errorGobbler = new StreamGobbler(
                        p.getErrorStream(), "ERROR");
                errorGobbler.start();
                StreamGobbler outGobbler = new StreamGobbler(
                        p.getInputStream(), "STDOUT");
                outGobbler.start();
                try {
                    if (p.waitFor() != 0) {
                        log.error("chmod returned abnormal value!");
                    }
                } catch (InterruptedException e1) {
                    log.error("chmod process terminated", e1);
                }
            } catch (IOException e1) {
                log.error("chmod cmd error!", e1);
                return;
            }
        }
    }

    /**
     * Handle "start new vnode" request.
     * 
     * @throws FtpProtocolException
     */
    @Override
    public void handleMessage(StartVnodeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) throws FtpProtocolException {
        // debug; enter handle message
        log.info("entering start vnode handler: " + xreply);

        long retVnodeID = 0;
        // register master
        NovaWorker.masteraddr = xreply;
        if (NovaWorker.getInstance().getMaster() == null
                || NovaWorker.getInstance().getMaster().isConnected() == false) {
            NovaWorker.getInstance().registerMaster(xreply);
        }

        // determine virtualization driver
        final String virtService;
        if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
            // if qemu-kvm
            virtService = "qemu:///system";
        } else if (msg.getHyperVisor().equalsIgnoreCase("vstaros")) {
            // i don't know
            virtService = "vstaros:///system";
        } else if (msg.getHyperVisor().equalsIgnoreCase("lxc")) {
            // if lxc
            virtService = "lxc:///";
        } else {
            // others
            virtService = "some xen";
        }

        if (msg.getWakeupOnly()) {
            // domain exists; wake up only
            // threading lock
            synchronized (NovaWorker.getInstance().getConnLock()) {
                try {
                    Domain testDomain = null;
                    if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
                        testDomain = NovaWorker.getInstance()
                                .getConn("qemu:///system", false)
                                .domainLookupByUUIDString(msg.getUuid());
                    } else if (msg.getHyperVisor().equalsIgnoreCase("vstaros")) {
                        testDomain = NovaWorker.getInstance()
                                .getConn("vstaros:///system", false)
                                .domainLookupByUUIDString(msg.getUuid());
                    } else if (msg.getHyperVisor().equalsIgnoreCase("lxc")) {
                        testDomain = NovaWorker.getInstance()
                                .getConn("lxc:///", false)
                                .domainLookupByUUIDString(msg.getUuid());
                    }
                    testDomain.resume();
                } catch (LibvirtException ex) {
                    log.error("Domain with UUID='" + msg.getUuid()
                            + "' can't be found!", ex);
                }
            }
        } else {
            // domain doesn't exist; create before run
            retVnodeID = Long.parseLong(msg.getUuid());

            // set message default values
            msg.setUuid(UUID.randomUUID().toString());
            msg.setRunAgent(true);
            if ((msg.getMemSize() == null)
                    || (msg.getMemSize().trim().equals(""))) {
                msg.setMemSize("512000");
            }

            if ((msg.getCpuCount() != null) && (!msg.getCpuCount().equals(""))) {
                if ((Integer.parseInt(msg.getCpuCount()) <= 0)
                        || (Integer.parseInt(msg.getCpuCount()) >= 10))
                    msg.setCpuCount("1");
            }

            if ((msg.getArch() == null)
                    || msg.getArch().trim().equalsIgnoreCase("")) {
                msg.setArch("x86_64");
            }
            String stdImgFile = "small.img";
            if ((msg.getHdaImage() != null) && (!msg.getHdaImage().equals(""))) {
                stdImgFile = msg.getHdaImage();
            }

            // transfer file
            if (Conf.getString("storage.engine").equalsIgnoreCase("ftp")) {
                // debug info
                log.info("using ftp as storage protocol");
                // transfer file here
                this.ftpFileTransfer(msg, stdImgFile);
            } else if (Conf.getString("storage.engine")
                    .equalsIgnoreCase("pnfs")) {
                // debug info
                log.info("using nfs as storage protocol");
                // transfer file here
                // do nothing if lxc
                if (!msg.getHyperVisor().equalsIgnoreCase("lxc")) {
                    this.pnfsFileTransfer(msg, stdImgFile);
                }
            }

            // create domain and show some info
            synchronized (NovaWorker.getInstance().getConnLock()) {
                // debug info
                log.info("hypervisor to start: " + virtService);

                // generate the domain definition xml
                String xmlDes = null;
                if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
                    xmlDes = Kvm.emitDomain(msg.getHashMap());
                } else if (msg.getHyperVisor().equalsIgnoreCase("lxc")) {
                    xmlDes = Lxc.emitDomain(msg.getHashMap());
                } else {
                    log.info("unsupported vm! ");
                    return;
                }

                try {
                    // create libvirt domain
                    Domain newDomain = NovaWorker.getInstance()
                            .getConn(virtService, false)
                            .domainDefineXML(xmlDes);
                    if (newDomain != null) {
                        // launch new domain
                        newDomain.create();
                        // update domain status
                        VnodeStatusDaemon.putStatus(
                                UUID.fromString(newDomain.getUUIDString()),
                                Vnode.Status.PREPARING);
                        NovaWorker
                                .getInstance()
                                .getVnodeIP()
                                .put(UUID.fromString(newDomain.getUUIDString()),
                                        msg.getIpAddr());
                    }
                    // debug info
                    log.info("domain name: " + newDomain.getName() + "; id: "
                            + newDomain.getID());
                    log.info("uuid: "
                            + newDomain.getUUIDString()
                            + "; vncport: "
                            + Utils.WORKER_VNC_MAP.get(newDomain
                                    .getUUIDString()));
                    // send message to master to finish domain creation
                    // update uuid string in master database
                    // TBD disable vnc for now (lxc)
                    String port = Utils.WORKER_VNC_MAP.get(newDomain
                            .getUUIDString());
                    if (port == null) {
                        port = "0";
                    }
                    NovaWorker
                            .getInstance()
                            .getMaster()
                            .sendPnodeCreateVnodeMessage(
                                    NovaWorker.getInstance().getAddr().getIp(),
                                    retVnodeID, Integer.parseInt(port),
                                    newDomain.getUUIDString(),
                                    msg.getHyperVisor());
                } catch (LibvirtException ex) {
                    log.error("create domain failed! ", ex);
                }
            }
        }

    }
}
