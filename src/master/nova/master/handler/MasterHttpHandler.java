package nova.master.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHttpHandler;
import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.api.messages.AddUserMessage;
import nova.master.api.messages.CreateVclusterMessage;
import nova.master.api.messages.CreateVnodeMessage;
import nova.master.api.messages.DeletePnodeMessage;
import nova.master.api.messages.DeleteVclusterMessage;
import nova.master.api.messages.DeleteVnodeMessage;
import nova.master.api.messages.MasterInstallApplianceMessage;
import nova.master.api.messages.MasterMigrateVnodeMessage;
import nova.master.api.messages.RegisterApplianceMessage;
import nova.master.api.messages.RegisterVdiskMessage;
import nova.master.api.messages.UnregisterApplianceMessage;
import nova.master.api.messages.UnregisterVdiskMessage;
import nova.master.models.Appliance;
import nova.master.models.Pnode;
import nova.master.models.Users;
import nova.master.models.Vcluster;
import nova.master.models.Vdisk;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * Master's handler for http requests.
 * 
 * @author santa
 * 
 */
public class MasterHttpHandler extends SimpleHttpHandler {

    /**
     * Log4j logger.
     */
    Logger log = Logger.getLogger(MasterHttpHandler.class);

    public static boolean islogin = false;

    public static Users login_user = new Users();

    /**
     * Handle a request message, render result pages.
     */
    @Override
    public void handleMessage(DefaultHttpRequest req,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        log.info("New HTTP request from " + e.getChannel().getRemoteAddress());

        String reqUri = req.getUri();
        System.out.println("Request uri is: " + reqUri);

        // String CookieMeg = req.getHeader("Cookie");
        // Set<Cookie> cookies = new CookieDecoder().decode(CookieMeg);
        // for (Cookie cok : cookies) {
        // System.out.println("Cookie message is " + cok);
        // }

        if (reqUri.contains("css") || reqUri.contains("img")
                || reqUri.contains("js") || reqUri.contains("jar")) {

            String filepath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                    reqUri.replaceFirst("/", ""));

            try {
                BufferedInputStream bis = new BufferedInputStream(
                        new FileInputStream(new File(filepath)));

                ChannelBuffer buffer = HeapChannelBufferFactory.getInstance()
                        .getBuffer(10240000);

                int tempbyte;

                while ((tempbyte = bis.read()) != -1) {
                    buffer.writeByte(tempbyte);
                }

                bis.close();

                HttpResponse rep = new DefaultHttpResponse(
                        req.getProtocolVersion(), HttpResponseStatus.OK);
                HttpHeaders.setContentLength(rep, buffer.readableBytes());

                rep.setContent(buffer.readBytes(buffer.readableBytes()));

                ChannelFuture writeFuture = e.getChannel().write(rep);

                if (!HttpHeaders.isKeepAlive(req)) {
                    // Close the connection when the whole content is written
                    // out.
                    writeFuture.addListener(ChannelFutureListener.CLOSE);
                }
            } catch (IOException e1) {
                System.out.print("Can't find the file path!");
            }
        }

        else {

            super.handleMessage(req, ctx, e, xreply);
        }
    }

    /**
     * Render the result pages.
     */
    @Override
    public String renderResult(DefaultHttpRequest req) {

        String fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                "index.html");

        // html变量map
        HashMap<String, Object> values = new HashMap<String, Object>(); // HTML页面变量字典
        values.put("content", "NOVA MASTER");

        // get action info
        URL url = null;
        try {
            url = new URL("http://127.0.0.1:3000" + req.getUri());
            System.out.println("url is: " + url);
        } catch (MalformedURLException e) {
            log.error(e);
        }

        String act = getAction(url.getFile());
        values.put("act", "act:" + act);

        // get query info
        String query = "";
        if (req.getMethod().equals(HttpMethod.valueOf("POST"))) {
            ChannelBuffer content = req.getContent();
            for (int i = 0; i != content.capacity(); i++) {
                byte b = content.getByte(i);
                query = query + (char) b;
            }
            System.out.println("Post query is: " + query);
        } else if (req.getMethod().equals(HttpMethod.valueOf("GET"))) {
            query = url.getQuery();
            System.out.println("Get query is: " + query);
        }

        Map<String, String> queryMap = null;
        queryMap = getQueryMap(query);

        // 初始化系统html显示变量
        values.put("user_total", 4);
        values.put("root_user_total", 1);
        values.put("admin_user_total", 1);
        values.put("normal_user_total", 1);
        values.put("not_activated_user_total", 0);
        values.put("pmachine_total", 0);
        values.put("working_pmachine_toal", 0);
        values.put("failed_pmachine_total", 0);
        values.put("retired_pmachine_total", 0);
        values.put("vcluster_num", 0);
        values.put("vmachine_num", 0);

        // 如果没有登录
        if (!islogin) {
            if (act != null) {
                if (act.equals("login")) {
                    if (queryMap != null) {
                        String user_name = queryMap.get("username");
                        String user_passwd = queryMap.get("password");
                        Users ur = Users.findByName(user_name);
                        if (ur != null && user_passwd.equals(ur.getPassword())) {
                            fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                    "master", "overview.html");
                            islogin = true;
                            login_user = ur;
                            values.put("username", login_user.getName());
                            values.put("userprivilege",
                                    login_user.getPrivilege());
                        } else {
                            String ret = "<p>The username or the password is error</p>"
                                    + "<p>please return to input again!</p>";

                            return ret;
                        }
                    }
                }

                else if (act.equals("register")) {
                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "register.html");
                }

                else if (act.equals("user_register")) {
                    new AddUserHandler().handleMessage(new AddUserMessage(
                            queryMap.get("username"), queryMap.get("email"),
                            queryMap.get("password"), "normal", "true"), null,
                            null, null);

                    String ret = "<html><head><meta http-equiv='refresh' content='3,;url=login'></head>"
                            + "<body><p>Register success! Waiting to jump to main login page ...</p></body></html>";

                    return ret;

                }
            }
        }

        // 如果已经登录
        else {
            fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                    "overview.html");
            values.put("username", login_user.getName());
            values.put("userprivilege", login_user.getPrivilege());

            if (act != null) {

                // ----------------------------- http request of logout
                // ------------------------------

                if (act.equals("logout")) {
                    islogin = false;
                    login_user = null;
                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "index.html");
                }

                // --------------------------- http request from Intance page
                // --------------------------

                else if (act.equals("instance") || act.equals("add_vnode")
                        || act.equals("delete_vnode")
                        || act.equals("add_cluster")
                        || act.equals("delete_cluster")
                        || act.equals("migration")) {

                    if (act.equals("add_vnode")) {
                        new CreateVnodeHandler()
                                .handleMessage(
                                        new CreateVnodeMessage(
                                                queryMap.get("vnode_disk"),
                                                queryMap.get("vnode_name"),
                                                Integer.parseInt(queryMap
                                                        .get("vnode_cpucount")),
                                                Integer.parseInt(queryMap
                                                        .get("vnode_memsize")),
                                                null, Integer.parseInt(queryMap
                                                        .get("vnode_pnodeId")
                                                        .split("-")[0]), 0,
                                                null), null, null, null);
                    }

                    else if (act.equals("delete_vnode")) {
                        new DeleteVnodeHandler().handleMessage(
                                new DeleteVnodeMessage(Integer
                                        .parseInt(queryMap.get("vnode_id"))),
                                null, null, null);
                    }

                    else if (act.equals("migration")) {
                        Vnode migrate_vnode = Vnode.findById(Long
                                .parseLong(queryMap.get("mig_vnid")));

                        new MasterMigrateVnodeHandler().handleMessage(
                                new MasterMigrateVnodeMessage(migrate_vnode
                                        .getId(),
                                        migrate_vnode.getPmachineId(), Long
                                                .parseLong(queryMap.get(
                                                        "vnode_migrateto")
                                                        .split("-")[0])), null,
                                null, null);
                    }

                    else if (act.equals("add_cluster")) {
                        new CreateVclusterHandler().handleMessage(
                                new CreateVclusterMessage(queryMap
                                        .get("vcluster_name"),
                                        Integer.parseInt(queryMap
                                                .get("vcluster_size"))), null,
                                null, null);
                    }

                    else if (act.equals("delete_cluster")) {
                        new DeleteVclusterHandler()
                                .handleMessage(
                                        new DeleteVclusterMessage(Integer
                                                .parseInt(queryMap
                                                        .get("vcluster_id"))),
                                        null, null, null);
                    }

                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "instance.html");

                    // show all vnode
                    String vnode_show = "";
                    for (Vnode vnode : Vnode.all()) {
                        vnode_show = vnode_show
                                + "<tr><td>"
                                + vnode.getId()
                                + "</td><td>"
                                + vnode.getName()
                                + "</td><td>"
                                + vnode.getIp()
                                + "</td><td>"
                                + vnode.getBootDevice()
                                + "</td><td>"
                                + vnode.getMemorySize()
                                + "</td><td>"
                                + vnode.getVclusterId()
                                + "</td><td>"
                                + vnode.getPmachineId()
                                + "</td><td>"
                                + vnode.getStatus()
                                + "</td><td><div class='btn-group'><button class='btn btn-danger dropdown-toggle' "
                                + "data-toggle='dropdown'> Action <span class='caret'></span></button>"
                                + "<ul class='dropdown-menu'> "
                                + "<li><a href='vncview?vnode_id="
                                + vnode.getId()
                                + "'>View</a></li>"
                                + "<li><a href='#Migration_Modal' onclick='migration_process("
                                + vnode.getId() + ")" + "'>Migration</a></li>"
                                + "<li><a href='wakeup_vnode?vnode_id="
                                + vnode.getId() + "'>Wakeup</a></li>"
                                + "<li><a href='pause_vnode?vnode_id="
                                + vnode.getId() + "'>Pause</a></li>"
                                + "<li><a href='shutdown_vnode?vnode_id="
                                + vnode.getId() + "'> Shutdown </a></li>"
                                + "<li class='divider'>"
                                + "<li><a href='delete_vnode?vnode_id="
                                + vnode.getId() + "'>Delete</a></li>"
                                + "</ul></div></td></tr>";
                    }

                    values.put("vnode_show", vnode_show);
                    if (vnode_show == "") {
                        values.put("vnode_show", "None Instance!");
                    }

                    // show all vcluster
                    String vcluster_show = "";
                    for (Vcluster vcluster : Vcluster.all()) {
                        vcluster_show = vcluster_show
                                + "<tr><td>"
                                + vcluster.getId()
                                + "</td><td>"
                                + vcluster.getClusterName()
                                + "</td><td>"
                                + vcluster.getFristIp()
                                + "</td><td>"
                                + vcluster.getClusterSize()
                                + "</td><td>"
                                + vcluster.getSshPublicKey()
                                + "</td><td>"
                                + vcluster.getSshPrivateKey()
                                + "</td><td>"
                                + vcluster.getOsUsername()
                                + "</td><td><div class='btn-group'><button class='btn btn-danger dropdown-toggle' "
                                + "data-toggle='dropdown'> Action <span class='caret'></span></button>"
                                + "<ul class='dropdown-menu'> "
                                + "<li><a href='view_cluster?vcluster_id="
                                + vcluster.getId()
                                + "'>View Instances</a></li>"
                                + "<li class='divider'>"
                                + "<li><a href='delete_cluster?vcluster_id="
                                + vcluster.getId()
                                + "'> Delete Cluster</a></li>"
                                + "</ul></div></td></tr>";
                    }

                    values.put("vcluster_show", vcluster_show);
                    if (vcluster_show == "") {
                        values.put("vcluster_show", "None Cluster!");
                    }

                    // list all vdisk
                    String vdisk_list = "";
                    for (Vdisk vdisk : Vdisk.all()) {
                        vdisk_list = vdisk_list + "<option>"
                                + vdisk.getFileName() + "."
                                + vdisk.getDiskFormat() + "</option>";
                    }
                    values.put("vdisk_list", vdisk_list);

                    // list all pnode
                    String pnode_list = "";
                    for (Pnode pnode : Pnode.all()) {
                        pnode_list = pnode_list + "<option>" + pnode.getId()
                                + "-" + pnode.getIp() + "</option>";
                    }
                    values.put("pnode_list", pnode_list);

                    // list all appliance
                    String appliance_list = "";
                    for (Appliance app : Appliance.all()) {

                    }
                    values.put("appliance_list", appliance_list);
                }

                else if (act.equals("vncview")) {

                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "vncview.html");

                    values.put("vnc_port",
                            Utils.MASTER_VNC_MAP.get(queryMap.get("vnode_id")));

                }

                // --------------------------- http request from machine page
                // --------------------------
                else if (act.equals("machine") || act.equals("add_pnode")
                        || act.equals("delete_pnode")) {

                    if (act.equals("add_pnode")) {
                        new AddPnodeHandler().handleMessage(
                                new AddPnodeMessage(new SimpleAddress(queryMap
                                        .get("pnode_ip"), 4000), queryMap
                                        .get("pnode_name")), null, null, null);
                    }

                    else if (act.equals("delete_pnode")) {
                        new DeletePnodeHandler().handleMessage(
                                new DeletePnodeMessage(Integer
                                        .parseInt(queryMap.get("pnode_id"))),
                                null, null, null);
                    }

                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "machine.html");

                    // show all pnode
                    String pnode_show = "";
                    for (Pnode pnode : Pnode.all()) {
                        pnode_show = pnode_show
                                + "<tr><td>"
                                + pnode.getId()
                                + "</td><td>"
                                + pnode.getIp()
                                + "</td><td>"
                                + pnode.getPort()
                                + "</td><td>"
                                + pnode.getHostname()
                                + "</td><td>"
                                + pnode.getMacAddress()
                                + "</td><td>"
                                + pnode.getVmCapacity()
                                + "</td><td>"
                                + pnode.getStatus()
                                + "</td><td><div class='btn-group'><button class='btn btn-danger dropdown-toggle' "
                                + "data-toggle='dropdown'> Action <span class='caret'></span></button>"
                                + "<ul class='dropdown-menu'> "
                                + "<li><a href='view_pnode?pnode_id="
                                + pnode.getId()
                                + "'>View Machine Status</a></li>"
                                + "<li class='divider'>"
                                + "<li><a href='delete_pnode?pnode_id="
                                + pnode.getId() + "'> Delete Machine</a></li>"
                                + "</ul></div></td></tr>";
                    }
                    values.put("pnode_show", pnode_show);
                    if (pnode_show == "") {
                        values.put("pnode_show", "None worker!");
                    }
                }

                // --------------------------- http request from volume page
                // --------------------------
                else if (act.equals("volume") || act.equals("add_vdisk")
                        || act.equals("delete_vdisk")) {

                    if (act.equals("add_vdisk")) {
                        new RegisterVdiskHandler().handleMessage(
                                new RegisterVdiskMessage(queryMap
                                        .get("vdisk_displayname"), queryMap
                                        .get("vdisk_filename"), queryMap
                                        .get("vdisk_disktype"), queryMap
                                        .get("vdisk_osfamily"), queryMap
                                        .get("vdisk_osname"), queryMap
                                        .get("vdisk_descrption")), null, null,
                                null);
                    }

                    else if (act.equals("delete_vdisk")) {
                        new UnregisterVdiskHandler().handleMessage(
                                new UnregisterVdiskMessage(Integer
                                        .parseInt(queryMap.get("vdisk_id"))),
                                null, null, null);
                    }

                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "volume.html");

                    // list all vdisk
                    String vdisk_show = "";
                    for (Vdisk vdisk : Vdisk.all()) {
                        vdisk_show = vdisk_show
                                + "<tr><td>"
                                + vdisk.getId()
                                + "</td><td>"
                                + vdisk.getDisplayName()
                                + "</td><td>"
                                + vdisk.getFileName()
                                + "."
                                + vdisk.getDiskFormat()
                                + "</td><td>"
                                + vdisk.getOsFamily()
                                + "</td><td>"
                                + vdisk.getOsName()
                                + "</td><td>"
                                + vdisk.getDescription()
                                + "</td><td><div class='btn-group'><button class='btn btn-danger dropdown-toggle' "
                                + "data-toggle='dropdown'> Action <span class='caret'></span></button>"
                                + "<ul class='dropdown-menu'> "
                                + "<li><a href='delete_vdisk?vdisk_id="
                                + vdisk.getId() + "'> Delete Volume</a></li>"
                                + "</ul></div></td></tr>";
                    }
                    values.put("vdisk_show", vdisk_show);
                }

                else if (act.equals("image")) {
                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "image.html");
                }

                else if (act.equals("monitor")) {
                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "monitor.html");
                }

                else if (act.equals("account")) {
                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "account.html");
                }

                else if (act.equals("add_pnode")) {
                    new AddPnodeHandler().handleMessage(
                            new AddPnodeMessage(new SimpleAddress(queryMap
                                    .get("pnode_ip"), Conf
                                    .getInteger("worker.bind_port"))), null,
                            null, null);
                }

                else if (act.equals("register_vdisk")) {
                    new RegisterVdiskHandler().handleMessage(
                            new RegisterVdiskMessage(queryMap
                                    .get("display_name"), queryMap
                                    .get("file_name"), queryMap
                                    .get("image_type"), queryMap
                                    .get("os_family"), queryMap.get("os_name"),
                                    queryMap.get("description")), null, null,
                            null);
                } else if (act.equals("register_appliance")) {
                    new RegisterApplianceHandler().handleMessage(
                            new RegisterApplianceMessage(queryMap
                                    .get("display_name"), queryMap
                                    .get("file_name"), queryMap
                                    .get("os_family"), queryMap
                                    .get("description")), null, null, null);
                    // } else if (act.equals("create_vnode")) {
                    // int j = 0;
                    // new CreateVnodeHandler().handleMessage(
                    // new CreateVnodeMessage(queryMap.get("vnode_image"),
                    // queryMap.get("vnode_name"), Integer
                    // .parseInt(queryMap.get("cpu_count")),
                    // Integer.parseInt(queryMap.get("memory_size")),
                    // queryMap.get("appliance_list"), Integer
                    // .parseInt(queryMap.get("pnode_ip")), j,
                    // null), null, null, null);
                } else if (act.equals("create_vcluster")) {

                    String vclusterFrame = "<form action=\"create_vcluster_node\" method=\"get\">";
                    for (int i = 0; i < Integer.parseInt(queryMap
                            .get("vcluster_size")); i++) {
                        vclusterFrame += "<h3><font color=\"#FF0000\">"
                                + "Create vnode"
                                + (i + 1)
                                + "<br></font></h3>"
                                + "VM Image<input type=\"text\" name=\"vnode_image"
                                + (i + 1)
                                + "\"><br>"
                                + "VM Name<input type=\"text\" name=\"vnode_name"
                                + (i + 1)
                                + "\"><br>"
                                + "CPU Count	<select name=\"cpu_count"
                                + (i + 1)
                                + "\" >"
                                + "<option value=\"1\">1"
                                + "<option value=\"2\">2"
                                + "<option value=\"4\">4"
                                + "</select>"
                                + "Memory Size<input type=\"text\" name=\"memory_size"
                                + (i + 1)
                                + "\">KB<br>"
                                + "Appliance<input type=\"text\" name=\"appliance_list"
                                + (i + 1)
                                + "\"><br>"
                                + "Pnode Id<input type=\"text\" name=\"pnode_id"
                                + (i + 1) + "\">";
                    }
                    vclusterFrame += "<input type=\"submit\" value=\"create vnode\">"
                            + "</form>";
                    values.put("vcluster_frame", vclusterFrame);

                    System.out.println(queryMap.get("vcluster_name")
                            + "       "
                            + Integer.parseInt(queryMap.get("vcluster_size")));

                    new CreateVclusterHandler().handleMessage(
                            new CreateVclusterMessage(queryMap
                                    .get("vcluster_name"), Integer
                                    .parseInt(queryMap.get("vcluster_size"))),
                            null, null, null);

                    String appsList = "hadoop";
                    int clusterSize = Integer.parseInt(queryMap
                            .get("vcluster_size"));
                    String clusterName = queryMap.get("vcluster_name");

                    if (Utils.isUnix()) {
                        // we don't have to create new ssh key pair
                        if (clusterSize <= 10) {
                            String ftpDir = Utils.pathJoin(Utils.NOVA_HOME,
                                    "data", "ftp_home", "ssh_keys");
                            Utils.mkdirs(Utils.pathJoin(ftpDir, Vcluster.last()
                                    .getClusterName()));
                            for (int i = 0; i < clusterSize; i++) {
                                String vmName = "ubuntu" + (i + 1);
                                Utils.mkdirs(Utils.pathJoin(ftpDir,
                                        clusterName, vmName));
                                String tmpFile = "id_rsa";
                                Utils.copyOneFile(Utils.pathJoin(ftpDir,
                                        tmpFile), Utils.pathJoin(ftpDir,
                                        clusterName, vmName, tmpFile));
                                String tmpPubFile = "id_rsa.pub";
                                Utils.copyOneFile(Utils.pathJoin(ftpDir,
                                        tmpPubFile), Utils.pathJoin(ftpDir,
                                        clusterName, vmName, tmpPubFile));
                            }
                            Utils.copyOneFile(Utils.pathJoin(ftpDir,
                                    "authorized_keys"), Utils.pathJoin(ftpDir,
                                    clusterName, "authorized_keys"));
                        }
                        System.err.println("OK3");
                    } else {
                        // TODO
                    }
                    // Hadoop config
                    for (int i = 0; i < clusterSize; i++) {
                        if (appsList.contains("hadoop")) {

                            File ipFile = new File(Utils.pathJoin(
                                    Utils.NOVA_HOME, "run", "params",
                                    "clusterInfo.txt"));

                            try {
                                if (!ipFile.exists()) {
                                    ipFile.createNewFile();
                                }
                                OutputStream os = new FileOutputStream(ipFile);
                                os.write("10.0.1.90".getBytes());
                                os.write("\n".getBytes());
                                os.write(String.valueOf(clusterSize).getBytes());
                                os.write("\n".getBytes());
                                os.close();
                            } catch (FileNotFoundException e1) {
                                log.error("file not found!", e1);
                            } catch (IOException e1) {
                                log.error("file write fail!", e1);
                            }
                            String[] cmd = new String[] {
                                    "/bin/sh",
                                    "-c",
                                    "sh "
                                            + Utils.pathJoin(Utils.NOVA_HOME,
                                                    "data", "ftp_home",
                                                    "appliances", "hadoop",
                                                    "createConfigFile.sh") };
                            try {
                                Process proc = Runtime.getRuntime().exec(cmd);

                                try {
                                    if (proc.waitFor() == 0) {
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            break;
                        }
                    }
                    for (int i = 0; i < clusterSize; i++) {

                        // new CreateVnodeHandler().handleMessage(
                        // new CreateVnodeMessage("ubuntu.img", "ubuntu"
                        // + (i * clusterSize + 1), 1, 512000,
                        // appsList, (i / 1 + 1), i, clusterName),
                        // null, null, null);

                        new CreateVnodeHandler().handleMessage(
                                new CreateVnodeMessage("ubuntu.img", "ubuntu"
                                        + ((i % 3) * 3 + i / 3 + 1), 1, 512000,
                                        appsList, (i % 3 + 1), i, clusterName),
                                null, null, null);

                        // new CreateVnodeHandler().handleMessage(
                        // new CreateVnodeMessage("ubuntu.img", "ubuntu"
                        // + (i + 1), 1, 512000, appsList,
                        // (i / 3 + 1), i, clusterName), null, null,
                        // null);
                    }

                } else if (act.equals("create_vcluster_node")) {
                    // create ssh key pairs for linux
                    // if (Utils.isUnix()) {
                    // try {
                    // for (int i = 0; i < Vcluster.last().getClusterSize();
                    // i++) {
                    // File folder = new File(Utils.pathJoin(
                    // Utils.NOVA_HOME, "data", "ftp_home",
                    // "ssh_keys", Vcluster.last()
                    // .getClusterName(), queryMap
                    // .get("vnode_name"
                    // + String.valueOf(i + 1))));
                    // if (!folder.exists())
                    // folder.mkdirs();
                    // String[] cmd = new String[] {
                    // "/bin/sh",
                    // "-c",
                    // "ssh-keygen -t rsa -P  -f "
                    // + Utils.pathJoin(
                    // Utils.NOVA_HOME,
                    // "data",
                    // "ftp_home",
                    // "ssh_keys",
                    // Vcluster.last()
                    // .getClusterName(),
                    // queryMap.get("vnode_name"
                    // + String.valueOf(i + 1)),
                    // "id_rsa") };
                    //
                    // Process proc = Runtime.getRuntime().exec(cmd);
                    // try {
                    // if (proc.waitFor() == 0) {
                    // }
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    // }
                    // String[] cmd = new String[] {
                    // "/bin/sh",
                    // "-c",
                    // "touch "
                    // + Utils.pathJoin(Utils.NOVA_HOME,
                    // "data", "ftp_home", "ssh_keys",
                    // Vcluster.last()
                    // .getClusterName(),
                    // "authorized_keys") };
                    // Process proc = Runtime.getRuntime().exec(cmd);
                    // try {
                    // if (proc.waitFor() == 0) {
                    // }
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    //
                    // for (int i = 0; i < Vcluster.last().getClusterSize();
                    // i++) {
                    // String[] catCmd = new String[] {
                    // "/bin/sh",
                    // "-c",
                    // "cat "
                    // + Utils.pathJoin(
                    // Utils.NOVA_HOME,
                    // "data",
                    // "ftp_home",
                    // "ssh_keys",
                    // Vcluster.last()
                    // .getClusterName(),
                    // queryMap.get("vnode_name"
                    // + String.valueOf(i + 1)),
                    // "id_rsa.pub")
                    // + " >> "
                    // + Utils.pathJoin(Utils.NOVA_HOME,
                    // "data", "ftp_home",
                    // "ssh_keys", Vcluster.last()
                    // .getClusterName(),
                    // "authorized_keys") };
                    // Process proc1 = Runtime.getRuntime().exec(catCmd);
                    // try {
                    // if (proc1.waitFor() == 0) {
                    // }
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    // }
                    // } catch (IOException e1) {
                    // log.error("Can't create ssh pair! ", e1);
                    // }
                    // }
                    System.err.println("ok!");
                    if (Utils.isUnix()) {
                        // we don't have to create new ssh key pair
                        if (Vcluster.last().getClusterSize() <= 10) {
                            String ftpDir = Utils.pathJoin(Utils.NOVA_HOME,
                                    "data", "ftp_home", "ssh_keys");
                            Utils.mkdirs(Utils.pathJoin(ftpDir, Vcluster.last()
                                    .getClusterName()));
                            for (int i = 0; i < Vcluster.last()
                                    .getClusterSize(); i++) {
                                String vmName = queryMap.get("vnode_name"
                                        + String.valueOf(i + 1));
                                Utils.mkdirs(Utils.pathJoin(ftpDir, Vcluster
                                        .last().getClusterName(), vmName));
                                String tmpFile = "id_rsa";
                                Utils.copyOneFile(Utils.pathJoin(ftpDir,
                                        tmpFile), Utils.pathJoin(ftpDir,
                                        Vcluster.last().getClusterName(),
                                        vmName, tmpFile));
                                String tmpPubFile = "id_rsa.pub";
                                Utils.copyOneFile(Utils.pathJoin(ftpDir,
                                        tmpPubFile), Utils.pathJoin(ftpDir,
                                        Vcluster.last().getClusterName(),
                                        vmName, tmpPubFile));
                            }
                            Utils.copyOneFile(Utils.pathJoin(ftpDir,
                                    "authorized_keys"), Utils.pathJoin(ftpDir,
                                    Vcluster.last().getClusterName(),
                                    "authorized_keys"));
                        }
                        System.err.println("OK3");
                    } else {
                        // TODO
                    }
                    // Hadoop config
                    for (int i = 0; i < Vcluster.last().getClusterSize(); i++) {
                        if (queryMap.get(
                                "appliance_list" + String.valueOf(i + 1))
                                .contains("hadoop")) {

                            File ipFile = new File(Utils.pathJoin(
                                    Utils.NOVA_HOME, "run", "params",
                                    "clusterInfo.txt"));

                            try {
                                if (!ipFile.exists()) {
                                    ipFile.createNewFile();
                                }
                                OutputStream os = new FileOutputStream(ipFile);
                                os.write(Vcluster.last().getFristIp()
                                        .getBytes());
                                os.write("\n".getBytes());
                                os.write(Vcluster.last().getClusterSize()
                                        .toString().getBytes());
                                os.write("\n".getBytes());
                                os.close();
                            } catch (FileNotFoundException e1) {
                                log.error("file not found!", e1);
                            } catch (IOException e1) {
                                log.error("file write fail!", e1);
                            }
                            String[] cmd = new String[] {
                                    "/bin/sh",
                                    "-c",
                                    "sh "
                                            + Utils.pathJoin(Utils.NOVA_HOME,
                                                    "data", "ftp_home",
                                                    "appliances", "hadoop",
                                                    "createConfigFile.sh") };
                            try {
                                Process proc = Runtime.getRuntime().exec(cmd);

                                try {
                                    if (proc.waitFor() == 0) {
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            break;
                        }
                    }
                    for (int i = 0; i < Vcluster.last().getClusterSize(); i++) {
                        System.err.println("OK4");

                        new CreateVnodeHandler()
                                .handleMessage(
                                        new CreateVnodeMessage(
                                                queryMap.get("vnode_image"
                                                        + String.valueOf(i + 1)),
                                                queryMap.get("vnode_name"
                                                        + String.valueOf(i + 1)),
                                                Integer.parseInt(queryMap.get("cpu_count"
                                                        + String.valueOf(i + 1))),
                                                Integer.parseInt(queryMap.get("memory_size"
                                                        + String.valueOf(i + 1))),
                                                queryMap.get("appliance_list"
                                                        + String.valueOf(i + 1)),
                                                Integer.parseInt(queryMap.get("pnode_id"
                                                        + String.valueOf(i + 1))),
                                                i, Vcluster.last()
                                                        .getClusterName()),
                                        null, null, null);

                        System.out.println(queryMap.get("vnode_image"
                                + String.valueOf(i + 1))
                                + "~~~~~~"
                                + queryMap.get("vnode_name"
                                        + String.valueOf(i + 1))
                                + "~~~~~~"
                                + Integer.parseInt(queryMap.get("cpu_count"
                                        + String.valueOf(i + 1)))
                                + "~~~~~~"
                                + Integer.parseInt(queryMap.get("memory_size"
                                        + String.valueOf(i + 1)))
                                + "~~~~~~"
                                + queryMap.get("appliance_list"
                                        + String.valueOf(i + 1)));

                    }
                } else if (act.equals("delete_pnode")) {
                    new DeletePnodeHandler().handleMessage(
                            new DeletePnodeMessage(Long.parseLong(queryMap
                                    .get("pnode_id"))), null, null, null);

                } else if (act.equals("delete_vnode")) {
                    new DeleteVnodeHandler().handleMessage(
                            new DeleteVnodeMessage(Long.parseLong(queryMap
                                    .get("vnode_id"))), null, null, null);

                } else if (act.equals("delete_vcluster")) {
                    new DeleteVclusterHandler().handleMessage(
                            new DeleteVclusterMessage(Long.parseLong(queryMap
                                    .get("vcluster_id"))), null, null, null);

                } else if (act.equals("unregister_vdisk")) {
                    new UnregisterVdiskHandler().handleMessage(
                            new UnregisterVdiskMessage(Long.parseLong(queryMap
                                    .get("vdisk_id"))), null, null, null);

                } else if (act.equals("unregister_appliance")) {
                    new UnregisterApplianceHandler().handleMessage(
                            new UnregisterApplianceMessage(Long
                                    .parseLong(queryMap.get("appliance_id"))),
                            null, null, null);
                } else if (act.equals("install_app")) {
                    String[] appNames = queryMap.get("app_name").split(",");
                    new MasterInstallApplianceHandler().handleMessage(
                            new MasterInstallApplianceMessage(Long
                                    .parseLong(queryMap.get("vnode_id")),
                                    appNames), null, null, null);
                } else if (act.equals("migrate")) {
                    new MasterMigrateVnodeHandler()
                            .handleMessage(
                                    new MasterMigrateVnodeMessage(
                                            Long.parseLong(queryMap
                                                    .get("vnode_id")),
                                            Long.parseLong(queryMap
                                                    .get("migration_from")),
                                            Long.parseLong(queryMap
                                                    .get("migrate_to"))), null,
                                    null, null);
                }
            }
        }

        return Utils.expandTemplateFile(fpath, values);
    }

    public static Map<String, String> getQueryMap(String query) {
        if (query != null && !query.equals("")) {
            String[] params = query.split("&");
            Map<String, String> map = new HashMap<String, String>();
            for (String param : params) {
                String name = param.split("=")[0];
                String value = null;
                if (param.split("=").length > 1) {
                    value = param.split("=")[1];
                }
                map.put(name, value);
            }
            return map;
        } else {
            return null;
        }
    }

    public static String getAction(String actFile) {
        if (actFile.length() > 1) {
            String[] act = actFile.split("\\?");
            return act[0].split("/")[1];
        } else
            return null;
    }
}
