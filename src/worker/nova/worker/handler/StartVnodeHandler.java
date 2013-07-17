package nova.worker.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.UUID;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.common.util.Conf;
import nova.common.util.FtpUtils;
import nova.common.util.Utils;
import nova.master.api.MasterProxy;
import nova.master.models.Vnode;
import nova.storage.NovaStorage;
import nova.worker.NovaWorker;
import nova.worker.api.messages.StartVnodeMessage;
import nova.worker.daemons.VnodeStatusDaemon;
import nova.worker.models.StreamGobbler;
import nova.worker.virt.Kvm;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import sun.net.ftp.FtpClient;

/**
 * Handler for "start new vnode" request.
 * 
 * @author santa
 * 
 */
// TODO delete experiment codes between //////////////////// and //////////////
public class StartVnodeHandler implements SimpleHandler<StartVnodeMessage> {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(StartVnodeHandler.class);

    /**
     * Handle "start new vnode" request.
     */
    @Override
    public void handleMessage(StartVnodeMessage msg, ChannelHandlerContext ctx,
            MessageEvent e, SimpleAddress xreply) {
        long retVnodeID = Long.parseLong(msg.getUuid());
        // ////////////////////////////////////////////////////////////////////
        String ip = new SimpleAddress(msg.getIpAddr(), 4100).toString();

        // Start vnode begin
        MasterProxy mp = new MasterProxy(new SimpleAddress(msg.getIpAddr(),
                4100));
        mp.connect(new InetSocketAddress(Conf.getString("master.bind_host"),
                Conf.getInteger("master.bind_port")));
        mp.sendExpTimeMessage(ip, "startCal", System.currentTimeMillis());
        mp.sendExpTimeMessage(ip, "startTime", System.currentTimeMillis());
        // ////////////////////////////////////////////////////////////////////

        final String virtService;
        if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
            virtService = "qemu:///system";
        } else if (msg.getHyperVisor().equalsIgnoreCase("vstaros")) {
            virtService = "vstaros:///system";
        } else {
            virtService = "some xen";
        }

        if (msg.getWakeupOnly()) {
            synchronized (NovaWorker.getInstance().getConnLock()) {
                try {
                    if (msg.getHyperVisor().equalsIgnoreCase("kvm")) {
                        Domain testDomain = NovaWorker.getInstance()
                                .getConn("qemu:///system", false)
                                .domainLookupByUUIDString(msg.getUuid());
                        testDomain.resume();
                    } else if (msg.getHyperVisor().equalsIgnoreCase("vstaros")) {
                        Domain testDomain = NovaWorker.getInstance()
                                .getConn("vstaros:///system", false)
                                .domainLookupByUUIDString(msg.getUuid());
                        testDomain.resume();
                    }

                } catch (LibvirtException ex) {
                    log.error("Domain with UUID='" + msg.getUuid()
                            + "' can't be found!", ex);
                }
            }
        } else {
            msg.setUuid(UUID.randomUUID().toString());
            msg.setRunAgent(false);
            if ((msg.getMemSize() != null) && (!msg.getMemSize().equals(""))) {
                msg.setMemSize("512000");
            }

            if ((msg.getCpuCount() != null) && (!msg.getCpuCount().equals(""))) {
                if ((Integer.parseInt(msg.getCpuCount()) <= 0)
                        || (Integer.parseInt(msg.getCpuCount()) >= 10))
                    msg.setCpuCount("1");
            }

            if ((msg.getArch() == null)
                    || msg.getArch().trim().equalsIgnoreCase("")) {
                msg.setArch("i686");
            }

            String stdImgFile = "small.img";
            if ((msg.getHdaImage() != null) && (!msg.getHdaImage().equals(""))) {
                stdImgFile = msg.getHdaImage();
            }
            File stdFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    stdImgFile));
            // @eagle
            if (Conf.getString("storage.engine").equalsIgnoreCase("ftp")) {
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
                        FtpUtils.downloadFile(fc, Utils.pathJoin("img",
                                stdImgFile), Utils.pathJoin(Utils.NOVA_HOME,
                                "run", stdImgFile));
                    } catch (NumberFormatException e1) {
                        log.error("port format error!", e1);
                    } catch (IOException e1) {
                        log.error("ftp connection fail!", e1);
                    }
                    // NovaStorage.getInstance().shutdown();
                }
                File foder = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                        msg.getName()));
                if (!foder.exists()) {
                    foder.mkdirs();
                }
                File dstFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                        msg.getName(), stdImgFile));
                // if (dstFile.exists() == false || dstFile.length() != stdLen)
                // {
                // for (int i = VdiskPoolDaemon.getPOOL_SIZE(); i >= 1; i--) {
                // File srcFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                // "run", "vdiskpool", stdImgFile + ".pool."
                // + Integer.toString(i)));
                // if (srcFile.exists() && (srcFile.length() == stdLen)) {
                // System.out.println("file " + stdImgFile + ".pool."
                // + Integer.toString(i) + "exists!");
                //
                // srcFile.renameTo(dstFile);
                // found = true;
                // break;
                // } else {
                // System.out.println("file " + stdImgFile + ".pool."
                // + Integer.toString(i) + "not exist!");
                // }
                // }
                // if (!found) {
                // // copy img files
                // foder = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                // msg.getName()));
                // if (!foder.exists()) {
                // foder.mkdirs();
                // } else {
                // // TODO @whoever rename or stop or what?
                // log.error("vm name " + msg.getName()
                // + " has been used!");
                // }
                // File file = new File(Utils.pathJoin(Utils.NOVA_HOME,
                // "run", msg.getName(), stdImgFile));
                // // if (file.exists() == false) {
                // try {
                // System.out.println("copying file");
                // String sourceUrl = Utils.pathJoin(Utils.NOVA_HOME,
                // "run", stdImgFile);
                // String destUrl = Utils.pathJoin(Utils.NOVA_HOME,
                // "run", msg.getName(), stdImgFile);
                // File sourceFile = new File(sourceUrl);
                // if (sourceFile.isFile()) {
                // FileInputStream input = new FileInputStream(
                // sourceFile);
                // FileOutputStream output = new FileOutputStream(
                // destUrl);
                // byte[] b = new byte[1024 * 5];
                // int len;
                // while ((len = input.read(b)) != -1) {
                // output.write(b, 0, len);
                // }
                // output.flush();
                // output.close();
                // input.close();
                // }
                // } catch (IOException ex) {
                // log.error("copy image fail", ex);
                // }
                // // }
                // }
                // }
                if (dstFile.exists() == false) {
                    // create incremental images of source image
                    System.out.println("Source image is " + msg.getHdaImage());
                    Process createIncrmtlImgs;
                    String cmd = "qemu-img create -b "
                            + Utils.pathJoin(Utils.NOVA_HOME, "run", stdImgFile)
                            + " -f qcow2 "
                            + Utils.pathJoin(Utils.NOVA_HOME, "run",
                                    msg.getName(), stdImgFile);
                    System.out
                            .println("Ftp______________________________________________________________: "
                                    + cmd);

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
                            log.error(
                                    "create incremental image process terminated",
                                    e1);
                        }
                    } catch (IOException e1) {
                        log.error("exec create incremental image cmd error!",
                                e1);
                        return;
                    }
                }
                if (msg.getRunAgent()) {
                    File pathFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "softwares"));
                    if (!pathFile.exists()) {
                        Utils.mkdirs(pathFile.getAbsolutePath());
                    }
                    File paramsFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "softwares", "params"));
                    if (!paramsFile.exists()) {
                        Utils.mkdirs(paramsFile.getAbsolutePath());
                    }
                    File ipFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "softwares", "params", "ipconfig.txt"));

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
                    File ipAddrFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "conf", "nova.agent.extrainfo.properties"));
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
                        PrintWriter outpw = new PrintWriter(new FileWriter(
                                ipAddrFile));
                        outpw.println("agent.bind_host=" + msg.getIpAddr());
                        outpw.println("master.bind_host="
                                + Conf.getString("master.bind_host"));
                        outpw.println("master.bind_port="
                                + Conf.getString("master.bind_port"));
                        outpw.close();
                    } catch (IOException e1) {
                        log.error(
                                "write nova.agent.extrainfo.properties file fail!",
                                e1);
                    }
                    // ////////////////////////////////////////////////////////////////////
                    mp.sendExpTimeMessage(ip, "configTime",
                            System.currentTimeMillis());
                    // ////////////////////////////////////////////////////////////////////
                    // start download apps
                    if (msg.getApps() != null) {
                        for (String appName : msg.getApps()) {
                            if (NovaWorker.getInstance().getAppStatus()
                                    .containsKey(appName) == false) {
                                try {
                                    // if
                                    // (NovaStorage.getInstance().getFtpServer()
                                    // == null) {
                                    // //
                                    // NovaStorage.getInstance().startFtpServer();
                                    // }
                                    FtpClient fc = FtpUtils
                                            .connect(
                                                    Conf.getString("storage.ftp.bind_host"),
                                                    Conf.getInteger("storage.ftp.bind_port"),
                                                    Conf.getString("storage.ftp.admin.username"),
                                                    Conf.getString("storage.ftp.admin.password"));
                                    FtpUtils.downloadDir(fc, Utils.pathJoin(
                                            "appliances", appName), Utils
                                            .pathJoin(Utils.NOVA_HOME, "run",
                                                    "softwares", "appliances",
                                                    appName)); // first
                                                               // install
                                                               // appliances
                                    log.info("Download " + appName
                                            + " complete.");
                                } catch (NumberFormatException e1) {
                                    log.error("port format error!", e1);
                                    return;
                                } catch (IOException e1) {
                                    log.error("ftp connection fail!", e1);
                                    return;
                                }
                                NovaWorker.getInstance().getAppStatus()
                                        .put(appName, appName);
                            }
                        }
                    }
                    // ////////////////////////////////////////////////////////////////////
                    // finish download apps
                    mp.sendExpTimeMessage(ip, "downloadTime",
                            System.currentTimeMillis());
                    // ////////////////////////////////////////////////////////////////////

                    // packiso process start
                    // copy files to Novahome/run/softwares
                    File agentProgramFile = new File(Utils.pathJoin(
                            Utils.NOVA_HOME, "run", "softwares", "run"));
                    if (!agentProgramFile.exists()) {
                        Utils.mkdirs(agentProgramFile.getAbsolutePath());
                    }
                    // String[] ignoreList = { "nova.properties" };
                    // Utils.copyWithIgnore(Utils.pathJoin(Utils.NOVA_HOME,
                    // "conf"),
                    // Utils.pathJoin(agentProgramFile.getAbsolutePath(),
                    // "conf"), ignoreList);
                    Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "conf"), Utils
                            .pathJoin(agentProgramFile.getAbsolutePath(),
                                    "conf"));
                    Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "bin"),
                            Utils.pathJoin(agentProgramFile.getAbsolutePath(),
                                    "bin"));
                    Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "lib"),
                            Utils.pathJoin(agentProgramFile.getAbsolutePath(),
                                    "lib"));

                    // copy without ftp_home folder
                    String[] ingoreList = { "ftp_home" };
                    Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                            "data"), Utils.pathJoin(
                            agentProgramFile.getAbsolutePath(), "data"),
                            ingoreList);

                    Utils.copyOneFile(Utils
                            .pathJoin(Utils.NOVA_HOME, "VERSION"), Utils
                            .pathJoin(agentProgramFile.getAbsolutePath(),
                                    "VERSION"));

                    // pack iso files
                    File agentCdFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", msg.getName(), "agentcd"));
                    if (!agentCdFile.exists()) {
                        Utils.mkdirs(agentCdFile.getAbsolutePath());
                    }
                    System.out.println("packing iso");
                    Process p;
                    String cmd = "mkisofs -J -T -R -V cdrom -o "
                            + Utils.pathJoin(Utils.NOVA_HOME, "run",
                                    msg.getName(), "agentcd", "agent-cd.iso")
                            + " "
                            + Utils.pathJoin(Utils.NOVA_HOME, "run",
                                    "softwares");

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

            } else if (Conf.getString("storage.engine")
                    .equalsIgnoreCase("pnfs")) {
                stdFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                        "run", stdImgFile));
                if (stdFile.exists() == false) {
                    log.error("std Img not found in pnfs server!");
                    return;
                }
                String strWorkerIP = NovaWorker.getInstance().getAddr().getIp();
                // long stdLen = stdFile.length();
                // boolean found = false;
                // for (int i = VdiskPoolDaemon.getPOOL_SIZE(); i >= 1; i--) {
                // File srcFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                // "run", "run", "vdiskpool", stdImgFile + ".pool."
                // + Integer.toString(i)));
                // if (srcFile.exists() && (srcFile.length() == stdLen)) {
                // System.out.println("file " + stdImgFile + ".pool."
                // + Integer.toString(i) + "exists!");
                // File foder = new File(
                // Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                // strWorkerIP + "_" + msg.getName()));
                // if (!foder.exists()) {
                // foder.mkdirs();
                // }
                // File dstFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                // "run", "run",
                // strWorkerIP + "_" + msg.getName(), stdImgFile));
                // srcFile.renameTo(dstFile);
                // found = true;
                // break;
                // } else {
                // System.out.println("file " + stdImgFile + ".pool."
                // + Integer.toString(i) + "not exist!");
                // }
                // }

                // copy img files
                File foder = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                        "run", strWorkerIP + "_" + msg.getName()));
                if (!foder.exists()) {
                    foder.mkdirs();
                } else {
                    // TODO @whoever rename or stop or what?
                    log.error("vm name " + strWorkerIP + "_" + msg.getName()
                            + " has been used!");
                }
                File file = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                        "run", strWorkerIP + "_" + msg.getName(), stdImgFile));
                if (file.exists() == false) {
                    // create incremental images of source image
                    Process createNFSIncrmtlImgs;
                    String cmdofincrtlnfs = "qemu-img create -b "
                            + Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                                    stdImgFile)
                            + " -f qcow2 "
                            + Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                                    strWorkerIP + "_" + msg.getName(),
                                    stdImgFile + " 20G");
                    System.out
                            .println("pNFS___________________________________________________________________-: "
                                    + cmdofincrtlnfs);

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
                        log.error(
                                "exec create NFS incremental image cmd error!",
                                e1);
                        return;
                    }
                }
                if (msg.getRunAgent()) {
                    File pathFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "run", strWorkerIP + "_" + msg.getName(),
                            "softwares"));
                    if (!pathFile.exists()) {
                        Utils.mkdirs(pathFile.getAbsolutePath());
                    }
                    File paramsFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "run", strWorkerIP + "_" + msg.getName(),
                            "softwares", "params"));
                    if (!paramsFile.exists()) {
                        Utils.mkdirs(paramsFile.getAbsolutePath());
                    }
                    File ipFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "run", strWorkerIP + "_" + msg.getName(),
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
                    File ipAddrFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "conf", "nova.agent.extrainfo.properties"));
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
                        PrintWriter outpw = new PrintWriter(new FileWriter(
                                ipAddrFile));
                        outpw.println("agent.bind_host=" + msg.getIpAddr());
                        outpw.println("master.bind_host="
                                + Conf.getString("master.bind_host"));
                        outpw.println("master.bind_port="
                                + Conf.getString("master.bind_port"));
                        outpw.close();
                    } catch (IOException e1) {
                        log.error(
                                "write nova.agent.extrainfo.properties file fail!",
                                e1);
                    }
                    // ////////////////////////////////////////////////////////////////////
                    mp.sendExpTimeMessage(ip, "configTime",
                            System.currentTimeMillis());
                    // ////////////////////////////////////////////////////////////////////
                    // prepare apps
                    if (msg.getApps() != null) {
                        File folderApp = new File(Utils.pathJoin(
                                Utils.NOVA_HOME, "run", "run", strWorkerIP
                                        + "_" + msg.getName(), "softwares",
                                "appliances"));
                        if (folderApp.exists() == false)
                            folderApp.mkdirs();
                        for (String appName : msg.getApps()) {
                            File appFile = new File(Utils.pathJoin(
                                    Utils.NOVA_HOME, "run", "run", strWorkerIP
                                            + "_" + msg.getName(), "softwares",
                                    "appliances", appName));

                            if (appFile.exists() == false) {
                                File sourceFile = new File(Utils.pathJoin(
                                        Utils.NOVA_HOME, "run", "appliances",
                                        appName));
                                if (sourceFile.isDirectory())
                                    Utils.copy(Utils.pathJoin(Utils.NOVA_HOME,
                                            "run", "appliances", appName),
                                            Utils.pathJoin(
                                                    Utils.NOVA_HOME,
                                                    "run",
                                                    "run",
                                                    strWorkerIP + "_"
                                                            + msg.getName(),
                                                    "softwares", "appliances",
                                                    appName));
                                else
                                    Utils.copyOneFile(Utils.pathJoin(
                                            Utils.NOVA_HOME, "run",
                                            "appliances", appName), Utils
                                            .pathJoin(Utils.NOVA_HOME, "run",
                                                    "run", strWorkerIP + "_"
                                                            + msg.getName(),
                                                    "softwares", "appliances",
                                                    appName));
                            }
                        }
                    }

                    // packiso process start
                    // copy files to Novahome/run/$WORKERIP_NAME/softwares
                    File agentProgramFile = new File(Utils.pathJoin(
                            Utils.NOVA_HOME, "run", "run", strWorkerIP + "_"
                                    + msg.getName(), "softwares", "run"));
                    if (!agentProgramFile.exists()) {
                        Utils.mkdirs(agentProgramFile.getAbsolutePath());
                    }
                    // String[] ignoreList = { "nova.properties" };
                    // Utils.copyWithIgnore(Utils.pathJoin(Utils.NOVA_HOME,
                    // "conf"),
                    // Utils.pathJoin(agentProgramFile.getAbsolutePath(),
                    // "conf"), ignoreList);
                    Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "run", "conf"),
                            Utils.pathJoin(agentProgramFile.getAbsolutePath(),
                                    "conf"));
                    Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "run", "bin"),
                            Utils.pathJoin(agentProgramFile.getAbsolutePath(),
                                    "bin"));
                    Utils.copy(Utils.pathJoin(Utils.NOVA_HOME, "run", "lib"),
                            Utils.pathJoin(agentProgramFile.getAbsolutePath(),
                                    "lib"));

                    // copy without ftp_home folder
                    String[] ingoreList = { "ftp_home" };
                    Utils.copyWithIgnoreFolder(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "data"), Utils.pathJoin(
                            agentProgramFile.getAbsolutePath(), "data"),
                            ingoreList);

                    Utils.copyOneFile(Utils.pathJoin(Utils.NOVA_HOME, "run",
                            "VERSION"), Utils.pathJoin(
                            agentProgramFile.getAbsolutePath(), "VERSION"));

                    // pack iso files
                    File agentCdFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                            "run", "run", strWorkerIP + "_" + msg.getName(),
                            "agentcd"));
                    if (!agentCdFile.exists()) {
                        Utils.mkdirs(agentCdFile.getAbsolutePath());
                    }
                    System.out.println("packing iso");
                    Process p;
                    String cmd = "mkisofs -J -T -R -V cdrom -o "
                            + Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                                    strWorkerIP + "_" + msg.getName(),
                                    "agentcd", "agent-cd.iso")
                            + " "
                            + Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                                    strWorkerIP + "_" + msg.getName(),
                                    "softwares");

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

            // ////////////////////////////////////////////////////////////////////
            // packiso finished
            mp.sendExpTimeMessage(ip, "packIsoTime", System.currentTimeMillis());
            // ////////////////////////////////////////////////////////////////////

            // create domain and show some info

            synchronized (NovaWorker.getInstance().getConnLock()) {
                try {

                    // start a vnode
                    String tt = Kvm.emitDomain(msg.getHashMap());
                    System.out.println(tt);
                    System.out.println(virtService);

                    Domain testDomain = NovaWorker.getInstance()
                            .getConn(virtService, false).domainDefineXML(tt);

                    if (testDomain != null) {
                        testDomain.create();
                        VnodeStatusDaemon.putStatus(
                                UUID.fromString(testDomain.getUUIDString()),
                                Vnode.Status.PREPARING);
                        NovaWorker
                                .getInstance()
                                .getVnodeIP()
                                .put(UUID
                                        .fromString(testDomain.getUUIDString()),
                                        msg.getIpAddr());
                    }
                    System.out.println("Domain:" + testDomain.getName()
                            + " id " + testDomain.getID() + " running "
                            + testDomain.getOSType());
                    System.out.println("Domian UUID:"
                            + testDomain.getUUIDString()
                            + " "
                            + "vncport:"
                            + Utils.WORKER_VNC_MAP.get(testDomain
                                    .getUUIDString()));
                    mp.sendPnodeCreateVnodeMessage(NovaWorker.getInstance()
                            .getAddr().getIp(), retVnodeID, Integer
                            .parseInt(Utils.WORKER_VNC_MAP.get(testDomain
                                    .getUUIDString())), testDomain
                            .getUUIDString());
                    // Domain testDomain = conn.domainLookupByName("test");
                    // System.out.println("xml desc\n" +
                    // testDomain.getXMLDesc(0));
                    // NovaWorker.getInstance().closeConnectToKvm();
                } catch (LibvirtException ex) {
                    log.error("Create domain failed", ex);
                }
            }

        }
    }
}
