package nova.master.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHttpHandler;
import nova.common.util.RRDTools;
import nova.common.util.Utils;
import nova.master.api.messages.ActiveUserMessage;
import nova.master.api.messages.AddPnodeMessage;
import nova.master.api.messages.AddUserMessage;
import nova.master.api.messages.CreateVclusterMessage;
import nova.master.api.messages.CreateVnodeMessage;
import nova.master.api.messages.DeletePnodeMessage;
import nova.master.api.messages.DeleteUserMessage;
import nova.master.api.messages.DeleteVclusterMessage;
import nova.master.api.messages.DeleteVnodeMessage;
import nova.master.api.messages.MasterMigrateVnodeMessage;
import nova.master.api.messages.ModifyUserPassMessage;
import nova.master.api.messages.RegisterVdiskMessage;
import nova.master.api.messages.ResumeVnodeMessage;
import nova.master.api.messages.StartVnodeMessage;
import nova.master.api.messages.StopUserMessage;
import nova.master.api.messages.StopVnodeMessage;
import nova.master.api.messages.UnregisterVdiskMessage;
import nova.master.models.Appliance;
import nova.master.models.Pnode;
import nova.master.models.UserRelations;
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
import org.jrobin.core.Util;

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

    public static String remote_ipaddr = null;

    public static HashMap<String, Boolean> session_ip_islogin = new HashMap<String, Boolean>();

    public static HashMap<String, Users> session_ip_loginuser = new HashMap<String, Users>();

    /**
     * Handle a request message, render result pages.
     */
    @Override
    public void handleMessage(DefaultHttpRequest req,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        log.info("New HTTP request from " + e.getChannel().getRemoteAddress());

        remote_ipaddr = e.getChannel().getRemoteAddress().toString().split(":")[0];
        if (session_ip_islogin == null
                || !session_ip_islogin.containsKey(remote_ipaddr)) {
            session_ip_islogin.put(remote_ipaddr, false);
            session_ip_loginuser.put(remote_ipaddr, null);
        }

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

        // 如果没有登录
        if (!session_ip_islogin.get(remote_ipaddr)) {
            if (act != null) {
                if (act.equals("login")) {
                    if (queryMap != null) {
                        String user_name = queryMap.get("username");
                        String user_passwd = queryMap.get("password");
                        Users ur = Users.findByName(user_name);
                        if (ur != null && user_passwd.equals(ur.getPassword())
                                && ur.getActivated().equals("true")) {
                            fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                    "master", "overview.html");
                            if (ur.getPrivilege().equals("Normal")) {
                                fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                        "master", "overview1.html");
                            }
                            session_ip_islogin.put(remote_ipaddr, true);
                            session_ip_loginuser.put(remote_ipaddr, ur);
                            values.put("username",
                                    session_ip_loginuser.get(remote_ipaddr)
                                            .getName());
                            values.put("userprivilege", session_ip_loginuser
                                    .get(remote_ipaddr).getPrivilege());
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
                    if (Users.findByName(queryMap.get("username")) != null) {
                        String adduser_error = "The User Name has exist! Please change another username.";
                        return adduser_error;
                    } else if (Users.findByEmail(queryMap.get("email")) != null) {
                        String adduser_error = "The Email Address has exist! please change another email address";
                        return adduser_error;
                    } else {
                        String register_privilege = queryMap.get("privilege");
                        Users.user_type usertype = null;

                        if (register_privilege.equals("Root")
                                || register_privilege.equals("Individual")
                                || register_privilege.equals("Enterprise")) {
                            if (register_privilege.equals("Root")) {
                                usertype = Users.user_type.Root;
                            } else if (register_privilege.equals("Enterprise")) {
                                usertype = Users.user_type.Enterprise;
                            } else if (register_privilege.equals("Individual")) {
                                usertype = Users.user_type.Individual;
                            }

                            new AddUserHandler().handleMessage(
                                    new AddUserMessage(
                                            queryMap.get("username"), queryMap
                                                    .get("email"), queryMap
                                                    .get("password"), usertype,
                                            "true", 0), null, null, null);

                            String ret = "<html><head><meta http-equiv='refresh' content='3;url=login'></head>"
                                    + "<body><p>Register success! Waiting to jump to main login page ...</p></body></html>";

                            return ret;
                        } else {
                            return "<html>Wrong User Type!</html>";
                        }
                    }

                }
            }
        }

        // 如果已经登录
        else {
            fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                    "overview.html");

            String userprivilege = session_ip_loginuser.get(remote_ipaddr)
                    .getPrivilege();
            values.put("username", session_ip_loginuser.get(remote_ipaddr)
                    .getName());
            values.put("userprivilege", userprivilege);

            if (userprivilege.equals("Normal")) {
                fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                        "overview1.html");
            }

            // ------------------------------------------------------------
            // ------------------------ act begin -------------------------
            // ------------------------------------------------------------
            if (act != null) {

                // ----------------------------- http request of logout
                // ------------------------------

                if (act.equals("logout")) {
                    session_ip_islogin.put(remote_ipaddr, false);
                    session_ip_loginuser.put(remote_ipaddr, null);

                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "index.html");
                }

                // --------------------------- http request from Intance
                // page
                // --------------------------
                else if (act.equals("faq")) {
                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "faq.html");
                } else if (act.equals("instance") || act.equals("add_vnode")
                        || act.equals("delete_vnode")
                        || act.equals("add_cluster")
                        || act.equals("delete_cluster")
                        || act.equals("migration") || act.equals("pause_vnode")
                        || act.equals("shutdown_vnode")
                        || act.equals("wakeup_vnode")
                        || act.equals("start_vnode")) {

                    if (act.equals("add_vnode")
                            && !userprivilege.equals("Normal")) {

                        String pnode_id = queryMap.get("vnode_pnodeId");

                        // pnode choose a detail pnode or auto
                        if (pnode_id.equals("auto")) {
                            ChooseBestPnodeHandler handler = new ChooseBestPnodeHandler();
                            handler.handleMessage(null, null, null, null);
                            if (handler.pnodeid != -1) {
                                pnode_id = String.valueOf(handler.pnodeid);
                                new CreateVnodeHandler()
                                        .handleMessage(
                                                new CreateVnodeMessage(
                                                        queryMap.get("vnode_disk"),
                                                        queryMap.get("vnode_name"),
                                                        Integer.parseInt(queryMap
                                                                .get("vnode_cpucount")),
                                                        Integer.parseInt(queryMap
                                                                .get("vnode_memsize")),
                                                        null,
                                                        Integer.parseInt(pnode_id),
                                                        0,
                                                        null,
                                                        true,
                                                        queryMap.get("vnode_hypervisor"),
                                                        session_ip_loginuser
                                                                .get(remote_ipaddr)
                                                                .getId()),
                                                null, null, null);
                            }

                            else {
                                String create_instance_error = "alert('No available Physical Machine Found!')";
                                values.put("create_instance_error",
                                        create_instance_error);
                            }
                        } else {
                            pnode_id = queryMap.get("vnode_pnodeId").split("-")[0];
                            Pnode pnode = Pnode.findById(Integer
                                    .parseInt(pnode_id));
                            if (pnode.getStatus() != Pnode.Status.RUNNING) {
                                String create_instance_error = "alert('Cannot connect to the selected Physical Machine!')";
                                values.put("create_instance_error",
                                        create_instance_error);
                            } else if (pnode.getVmCapacity() <= pnode
                                    .getCurrentVMNum()) {
                                String create_instance_error = "alert('The selected Physical Machine cannot hosted any more VMs!')";
                                values.put("create_instance_error",
                                        create_instance_error);
                            } else {

                                new CreateVnodeHandler()
                                        .handleMessage(
                                                new CreateVnodeMessage(
                                                        queryMap.get("vnode_disk"),
                                                        queryMap.get("vnode_name"),
                                                        Integer.parseInt(queryMap
                                                                .get("vnode_cpucount")),
                                                        Integer.parseInt(queryMap
                                                                .get("vnode_memsize")),
                                                        null,
                                                        Integer.parseInt(pnode_id),
                                                        0,
                                                        null,
                                                        true,
                                                        queryMap.get("vnode_hypervisor"),
                                                        session_ip_loginuser
                                                                .get(remote_ipaddr)
                                                                .getId()),
                                                null, null, null);
                            }
                        }

                    }

                    else if (act.equals("start_vnode")) {

                        Vnode start_vnode = Vnode.findById(Long
                                .parseLong(queryMap.get("vnode_id")));

                        new StartVnodeHandler().handleMessage(
                                new StartVnodeMessage(start_vnode.getUuid()),
                                null, null, null);

                    }

                    else if (act.equals("wakeup_vnode")) {

                        Vnode wakeup_vnode = Vnode.findById(Long
                                .parseLong(queryMap.get("vnode_id")));

                        new ResumeVnodeHandler().handleMessage(
                                new ResumeVnodeMessage(wakeup_vnode
                                        .getPmachineId(), wakeup_vnode.getId(),
                                        wakeup_vnode.getHypervisor()), null,
                                null, null);

                    }

                    else if (act.equals("pause_vnode")) {
                        Vnode shutdown_vnode = Vnode.findById(Long
                                .parseLong(queryMap.get("vnode_id")));

                        new StopVnodeHandler().handleMessage(
                                new StopVnodeMessage(shutdown_vnode
                                        .getHypervisor(), shutdown_vnode
                                        .getId(), true, shutdown_vnode
                                        .getPmachineId().toString()), null,
                                null, null);
                    }

                    else if (act.equals("shutdown_vnode")) {
                        Vnode shutdown_vnode = Vnode.findById(Long
                                .parseLong(queryMap.get("vnode_id")));

                        new StopVnodeHandler().handleMessage(
                                new StopVnodeMessage(shutdown_vnode
                                        .getHypervisor(), shutdown_vnode
                                        .getId(), false, shutdown_vnode
                                        .getPmachineId().toString()), null,
                                null, null);
                    }

                    else if (act.equals("delete_vnode")
                            && !userprivilege.equals("Normal")) {
                        new DeleteVnodeHandler().handleMessage(
                                new DeleteVnodeMessage(Integer
                                        .parseInt(queryMap.get("vnode_id"))),
                                null, null, null);
                    }

                    else if (act.equals("migration")) {
                        Vnode migrate_vnode = Vnode.findById(Long
                                .parseLong(queryMap.get("mig_vnid")));
                        long migration_to = Long.parseLong(queryMap.get(
                                "vnode_migrateto").split("-")[0]);
                        if (migration_to == migrate_vnode.getPmachineId()) {
                            String migration_error = "alert('The Destination and Source Physical Machines are the same!')";
                            values.put("migration_error", migration_error);
                        } else {
                            Pnode to_pnode = Pnode.findById(migration_to);
                            if (to_pnode == null) {
                                String migration_error = "alert('Cannot find the Destination Physical Machine!')";
                                values.put("migration_error", migration_error);
                            } else if (to_pnode.getStatus() != Pnode.Status.RUNNING) {
                                String migration_error = "alert('Cannot connect to the Destination Physical Machine!')";
                                values.put("migration_error", migration_error);
                            } else if (to_pnode.getCurrentVMNum() >= to_pnode
                                    .getVmCapacity()) {
                                String migration_error = "alert('The Destination Physical Machine cannot hosted any more VM!')";
                                values.put("migration_error", migration_error);
                            } else {
                                new MasterMigrateVnodeHandler()
                                        .handleMessage(
                                                new MasterMigrateVnodeMessage(
                                                        migrate_vnode.getId(),
                                                        migrate_vnode
                                                                .getPmachineId(),
                                                        migration_to), null,
                                                null, null);
                            }
                        }
                    }

                    else if (act.equals("add_cluster")
                            && !userprivilege.equals("Normal")) {

                        int v_size = Integer.parseInt(queryMap
                                .get("vcluster_size"));

                        HashMap<Long, Integer> hm = new HashMap<Long, Integer>();
                        for (Pnode pn : Pnode.all()) {
                            hm.put(pn.getId(), 0);
                        }
                        for (int i = 0; i < v_size; i++) {
                            if (queryMap.get("vinstance_pnodeId" + i)
                                    .equalsIgnoreCase("auto") == false) {
                                long pid = Integer.parseInt(queryMap.get(
                                        "vinstance_pnodeId" + i).split("-")[0]);
                                hm.put(pid, hm.get(pid) + 1);
                            }
                        }
                        boolean enoughcapacity = true;
                        for (int i = 0; i < v_size; i++) {
                            if (queryMap.get("vinstance_pnodeId" + i)
                                    .equalsIgnoreCase("auto") == false) {
                                long pid = Integer.parseInt(queryMap.get(
                                        "vinstance_pnodeId" + i).split("-")[0]);
                                Pnode pnode = Pnode.findById(pid);
                                if (pnode.getCurrentVMNum() + hm.get(pid) > pnode
                                        .getVmCapacity()) {
                                    String createvcluster_error = "alert('The Destination Physical Machine cannot hosted any more VM!')";
                                    values.put("createvcluster_error",
                                            createvcluster_error);
                                    enoughcapacity = false;
                                    break;
                                }
                            }
                        }
                        if (enoughcapacity) {
                            new CreateVclusterHandler().handleMessage(
                                    new CreateVclusterMessage(queryMap
                                            .get("vcluster_name"), v_size,
                                            session_ip_loginuser.get(
                                                    remote_ipaddr).getId()),
                                    null, null, null);
                            for (int i = 0; i != v_size; i++) {
                                String pnodeid_str = queryMap
                                        .get("vinstance_pnodeId" + i);
                                if (pnodeid_str.equalsIgnoreCase("auto")) {
                                    ChooseBestPnodeHandler handler = new ChooseBestPnodeHandler();
                                    handler.handleMessage(null, null, null,
                                            null);
                                    if (handler.pnodeid != -1) {
                                        pnodeid_str = String
                                                .valueOf(handler.pnodeid);
                                        new CreateVnodeHandler()
                                                .handleMessage(
                                                        new CreateVnodeMessage(
                                                                queryMap.get("vinstance_disk"
                                                                        + i),
                                                                queryMap.get("vinstance_name"
                                                                        + i),
                                                                Integer.parseInt(queryMap
                                                                        .get("vinstance_cpucount"
                                                                                + i)),
                                                                Integer.parseInt(queryMap
                                                                        .get("vinstance_memsize"
                                                                                + i)),
                                                                null,
                                                                Integer.parseInt(pnodeid_str),
                                                                i,
                                                                queryMap.get("vcluster_name"
                                                                        + i),
                                                                false,
                                                                queryMap.get("vinstance_hypervisor"
                                                                        + i),
                                                                session_ip_loginuser
                                                                        .get(remote_ipaddr)
                                                                        .getId()),
                                                        null, null, null);

                                    } else {
                                        String create_instance_error = "alert('No available Physical Machine Found For "
                                                + queryMap.get("vinstance_name"
                                                        + i) + "!')";
                                        values.put("create_instance_error",
                                                create_instance_error);
                                        v_size = i;
                                        break;
                                    }
                                } else {
                                    new CreateVnodeHandler()
                                            .handleMessage(
                                                    new CreateVnodeMessage(
                                                            queryMap.get("vinstance_disk"
                                                                    + i),
                                                            queryMap.get("vinstance_name"
                                                                    + i),
                                                            Integer.parseInt(queryMap
                                                                    .get("vinstance_cpucount"
                                                                            + i)),
                                                            Integer.parseInt(queryMap
                                                                    .get("vinstance_memsize"
                                                                            + i)),
                                                            null,
                                                            Integer.parseInt(pnodeid_str
                                                                    .split("-")[0]),
                                                            i,
                                                            queryMap.get("vcluster_name"
                                                                    + i),
                                                            false,
                                                            queryMap.get("vinstance_hypervisor"
                                                                    + i),
                                                            session_ip_loginuser
                                                                    .get(remote_ipaddr)
                                                                    .getId()),
                                                    null, null, null);
                                }
                            }

                        }
                    }

                    else if (act.equals("delete_cluster")
                            && !userprivilege.equals("Normal")) {
                        new DeleteVclusterHandler()
                                .handleMessage(
                                        new DeleteVclusterMessage(Integer
                                                .parseInt(queryMap
                                                        .get("vcluster_id"))),
                                        null, null, null);
                    }

                    // show all vnode
                    String vnode_show = "";

                    // show all vcluster
                    String vcluster_show = "";
                    String vcluster_list = "";
                    String vcluster_indiv = "";
                    String vcluster_script_in_instance = "";
                    String vcluster_script = "";

                    // ------------- root and admin user's vnode
                    // show-------------
                    if (!userprivilege.equals("Normal")) {
                        fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                "master", "instance.html");

                        // list vnodes
                        List<Vnode> searchvnode = new ArrayList<Vnode>();
                        if (userprivilege.equals("Root")) {
                            searchvnode = Vnode.all();
                        } else {
                            searchvnode = Vnode
                                    .getVnodeByUserId(session_ip_loginuser.get(
                                            remote_ipaddr).getId());
                        }
                        for (Vnode vnode : searchvnode) {
                            vnode_show = vnode_show
                                    + "<tr><td>"
                                    + vnode.getId()
                                    + "</td><td>"
                                    + vnode.getName()
                                    + "</td><td>"
                                    + vnode.getIp()
                                    + "</td><td>"
                                    + vnode.getMemorySize()
                                    + "</td><td>"
                                    + vnode.getHypervisor()
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
                                    + vnode.getId() + ")"
                                    + "'>Migrate</a></li>"
                                    + "<li><a href='start_vnode?vnode_id="
                                    + vnode.getId() + "'>Start</a></li>"
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

                        // list vclusters
                        List<Vcluster> searchvcluster = new ArrayList<Vcluster>();
                        if (userprivilege.equals("Root")) {
                            searchvcluster = Vcluster.all();
                        } else {
                            searchvcluster = Vcluster
                                    .getVclusterByUserId(session_ip_loginuser
                                            .get(remote_ipaddr).getId());
                        }
                        for (Vcluster vcluster : searchvcluster) {
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
                                    + "<li><a href='#' onclick='cluster_"
                                    + vcluster.getId()
                                    + "()'>View Instances</a></li>"
                                    + "<li class='divider'>"
                                    + "<li><a href='delete_cluster?vcluster_id="
                                    + vcluster.getId()
                                    + "'> Delete Cluster</a></li>"
                                    + "</ul></div></td></tr>";
                            vcluster_list = vcluster_list
                                    + "<li class='' id='lid_"
                                    + vcluster.getClusterName()
                                    + "'><a data-toggle='tab' onclick='cluster_"
                                    + vcluster.getId() + "()' href='#'>"
                                    + "Cluster: " + vcluster.getClusterName()
                                    + "</a></li>";

                            vcluster_script_in_instance = vcluster_script_in_instance
                                    + "document.getElementById('lid_"
                                    + vcluster.getClusterName()
                                    + "').className=';'; "
                                    + "document.getElementById('col_"
                                    + vcluster.getClusterName()
                                    + "').style.display='none';";

                            vcluster_script = vcluster_script
                                    + "function cluster_" + vcluster.getId()
                                    + "(){";
                            // disable all the 'active' and 'block' cluster
                            for (Vcluster vcluster0 : searchvcluster) {
                                if (vcluster0 != vcluster)
                                    vcluster_script = vcluster_script
                                            + "document.getElementById('lid_"
                                            + vcluster0.getClusterName()
                                            + "').className = ';';"
                                            + "document.getElementById('col_"
                                            + vcluster0.getClusterName()
                                            + "').style.display='none';";
                            }
                            vcluster_script = vcluster_script
                                    + "document.getElementById('all_instance').className='';"
                                    + "document.getElementById('view_instance').style.display='none';"
                                    + "document.getElementById('lid_"
                                    + vcluster.getClusterName()
                                    + "').className='active';"
                                    + "document.getElementById('col_"
                                    + vcluster.getClusterName()
                                    + "').style.display='block';}\n";

                            vcluster_indiv = vcluster_indiv
                                    + "<div id='col_"
                                    + vcluster.getClusterName()
                                    + "' class='tab-content' style='display:none'>"
                                    + "<legend>"
                                    + vcluster.getClusterName()
                                    + "\'s Instances"
                                    + "</legend>"
                                    + "<table class='table table-striped table-hover'><thead><tr><th>Id</th><th>Instance Name</th>"
                                    + "<th>Ip Address</th><th>Memery Size</th>"
                                    + "<th>Hypervisor</th><th>Virtual Cluster Id</th>"
                                    + "<th>Physical Machine Id</th><th>Status</th>"
                                    + "<th>Action</th></tr></thead><tbody>";
                            for (Vnode vnode : searchvnode) {
                                if (vnode.getVclusterId() == vcluster.getId()) {
                                    vcluster_indiv = vcluster_indiv
                                            + "<tr><td>"
                                            + vnode.getId()
                                            + "</td><td>"
                                            + vnode.getName()
                                            + "</td><td>"
                                            + vnode.getIp()
                                            + "</td><td>"
                                            + vnode.getMemorySize()
                                            + "</td><td>"
                                            + vnode.getHypervisor()
                                            + "</td><td>"
                                            + vnode.getVclusterId()
                                            + "</td><td>"
                                            + vnode.getPmachineId()
                                            + "</td><td>"
                                            + vnode.getStatus()
                                            + "</td><td>\n<div class='btn-group'><button class='btn btn-danger dropdown-toggle' "
                                            + "data-toggle='dropdown'> Action <span class='caret'></span></button>"
                                            + "<ul class='dropdown-menu'> "
                                            + "<li><a href='vncview?vnode_id="
                                            + vnode.getId()
                                            + "'>View</a></li>"
                                            + "<li><a href='#Migration_Modal' onclick='migration_process("
                                            + vnode.getId()
                                            + ")"
                                            + "'>Migrate</a></li>"
                                            + "<li><a href='start_vnode?vnode_id="
                                            + vnode.getId()
                                            + "'>Start</a></li>"
                                            + "<li><a href='wakeup_vnode?vnode_id="
                                            + vnode.getId()
                                            + "'>Wakeup</a></li>"
                                            + "<li><a href='pause_vnode?vnode_id="
                                            + vnode.getId()
                                            + "'>Pause</a></li>"
                                            + "<li><a href='shutdown_vnode?vnode_id="
                                            + vnode.getId()
                                            + "'> Shutdown </a></li>"
                                            + "<li class='divider'>"
                                            + "<li><a href='delete_vnode?vnode_id="
                                            + vnode.getId()
                                            + "'>Delete</a></li>"
                                            + "</ul></div>\n</td></tr>";
                                }
                            }
                            vcluster_indiv = vcluster_indiv
                                    + "</tbody></table></div>\n";

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
                            pnode_list = pnode_list + "<option>"
                                    + pnode.getId() + "-" + pnode.getIp()
                                    + "</option>";
                        }
                        values.put("pnode_list", pnode_list);

                        // list all appliance
                        String appliance_list = "";
                        for (Appliance app : Appliance.all()) {
                            app.toString();
                        }
                        values.put("appliance_list", appliance_list);

                    }

                    else { // ------------ Normal user's vnode show -----------

                        fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                "master", "instance1.html");

                        UserRelations ur = UserRelations
                                .findByNormalId(session_ip_loginuser.get(
                                        remote_ipaddr).getId());

                        for (Vnode vnode : Vnode.getVnodeByUserId(ur
                                .getAdminUserId())) {
                            vnode_show = vnode_show
                                    + "<tr><td>"
                                    + vnode.getId()
                                    + "</td><td>"
                                    + vnode.getName()
                                    + "</td><td>"
                                    + vnode.getIp()
                                    + "</td><td>"
                                    + vnode.getMemorySize()
                                    + "</td><td>"
                                    + vnode.getHypervisor()
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
                                    + vnode.getId() + "'>View</a></li>"
                                    + "<li><a href='start_vnode?vnode_id="
                                    + vnode.getId() + "'>Start</a></li>"
                                    + "<li><a href='wakeup_vnode?vnode_id="
                                    + vnode.getId() + "'>Wakeup</a></li>"
                                    + "<li><a href='pause_vnode?vnode_id="
                                    + vnode.getId() + "'>Pause</a></li>"
                                    + "<li><a href='shutdown_vnode?vnode_id="
                                    + vnode.getId() + "'> Shutdown </a></li>"
                                    + "<li class='divider'>"
                                    + "</ul></div></td></tr>";
                        }

                        for (Vcluster vcluster : Vcluster
                                .getVclusterByUserId(ur.getAdminUserId())) {
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
                                    + "<li><a href='#' onClick='cluster_"
                                    + vcluster.getId()
                                    + "()'>View Instances</a></li>"
                                    + "<li class='divider'>"
                                    + "<li><a href='delete_cluster?vcluster_id="
                                    + vcluster.getId()
                                    + "'> Delete Cluster</a></li>"
                                    + "</ul></div></td></tr>";
                        }

                    }

                    values.put("vnode_show", vnode_show);
                    values.put("vcluster_list", vcluster_list);
                    values.put("vcluster_script_in_instance",
                            vcluster_script_in_instance);
                    values.put("vcluster_script", vcluster_script);
                    values.put("vcluster_indiv", vcluster_indiv);

                    if (vnode_show == "") {
                        values.put("vnode_show", "None Instance!");
                    }

                    values.put("vcluster_show", vcluster_show);
                    if (vcluster_show == "") {
                        values.put("vcluster_show", "None Cluster!");
                    }

                }

                else if (act.equals("vncview")) {

                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "vncview.html");

                    values.put("vnc_port",
                            Utils.MASTER_VNC_MAP.get(queryMap.get("vnode_id")));

                    values.put("vnc_userid", queryMap.get("vnode_id"));

                    values.put(
                            "vnc_username",
                            Vnode.findById(
                                    Integer.parseInt(queryMap.get("vnode_id")))
                                    .getName());

                }

                // --------------------------- http request from machine
                // page
                // --------------------------
                else if (!userprivilege.equals("Normal")
                        && act.equals("machine") || act.equals("add_pnode")
                        || act.equals("delete_pnode")) {

                    if (userprivilege.equals("Root") && act.equals("add_pnode")) {
                        new AddPnodeHandler().handleMessage(
                                new AddPnodeMessage(new SimpleAddress(queryMap
                                        .get("pnode_ip"), 4000), Integer
                                        .parseInt(queryMap
                                                .get("pnode_vmCapacity"))),
                                null, null, null);
                    }

                    else if (userprivilege.equals("Root")
                            && act.equals("delete_pnode")) {
                        new DeletePnodeHandler().handleMessage(
                                new DeletePnodeMessage(Integer
                                        .parseInt(queryMap.get("pnode_id"))),
                                null, null, null);
                    }

                    // show all pnode
                    String pnode_show = "";
                    if (userprivilege.equals("Root")) {
                        fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                "master", "machine.html");
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
                                    + "<li><a href='monitor?pnode_id="
                                    + pnode.getId()
                                    + "'>View Machine Status</a></li>"
                                    + "<li class='divider'>"
                                    + "<li><a href='delete_pnode?pnode_id="
                                    + pnode.getId()
                                    + "'> Delete Machine</a></li>"
                                    + "</ul></div></td></tr>";
                        }
                    } else {
                        fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                "master", "machine1.html");
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
                                    + "<li><a href='monitor?pnode_id="
                                    + pnode.getId()
                                    + "'>View Machine Status</a></li>"
                                    + "</ul></div></td></tr>";
                        }
                    }

                    values.put("pnode_show", pnode_show);
                    if (pnode_show == "") {
                        values.put("pnode_show", "None worker!");
                    }
                }

                // --------------------------- http request from image page
                // --------------------------
                else if (!userprivilege.equals("Normal") && act.equals("image")
                        || act.equals("add_vdisk")
                        || act.equals("delete_vdisk")
                        || act.equals("showlaunch")) {

                    if (userprivilege.equals("Root") && act.equals("add_vdisk")) {
                        new RegisterVdiskHandler().handleMessage(
                                new RegisterVdiskMessage(queryMap
                                        .get("vdisk_displayname"), queryMap
                                        .get("vdisk_filename"), queryMap
                                        .get("vdisk_disktype"), queryMap
                                        .get("vdisk_osfamily"), queryMap
                                        .get("vdisk_osname"), queryMap
                                        .get("vdisk_imgPath"), queryMap
                                        .get("vdisk_descrption")), null, null,
                                null);
                    }

                    else if (userprivilege.equals("Root")
                            && act.equals("delete_vdisk")) {
                        new UnregisterVdiskHandler().handleMessage(
                                new UnregisterVdiskMessage(Integer
                                        .parseInt(queryMap.get("vdisk_id"))),
                                null, null, null);
                    }

                    else if (userprivilege.equals("Root")
                            && act.equals("showlaunch")) {
                        String vdisk_launch = Vdisk.findById(
                                Integer.parseInt(queryMap.get("vdisk_id")))
                                .getFileName()
                                + "."
                                + Vdisk.findById(
                                        Integer.parseInt(queryMap
                                                .get("vdisk_id")))
                                        .getDiskFormat();

                        String launch_module_show = "$('#LaunchInstance_Modal').modal('show');";

                        values.put("vdisk_launch", vdisk_launch);
                        values.put("launch_module_show", launch_module_show);
                    }

                    // list all vdisk
                    String vdisk_show = "";
                    if (userprivilege.equals("Root")) {
                        fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                "master", "image.html");
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
                                    + "<li><a data-toggle='modal' href='showlaunch?vdisk_id="
                                    + vdisk.getId()
                                    + "'> Launch Instance </a></li>"
                                    + "<li class='divider'>"
                                    + "<li><a href='delete_vdisk?vdisk_id="
                                    + vdisk.getId()
                                    + "'> Delete Image</a></li>"
                                    + "</ul></div></td></tr>";
                        }
                    } else {
                        fpath = Utils.pathJoin(Utils.NOVA_HOME, "www",
                                "master", "image1.html");
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
                                    + "</ul></div></td></tr>";
                        }
                    }
                    values.put("vdisk_show", vdisk_show);

                    // list all pnode
                    String pnode_list = "";
                    for (Pnode pnode : Pnode.all()) {
                        pnode_list = pnode_list + "<option>" + pnode.getId()
                                + "-" + pnode.getIp() + "</option>";
                    }
                    values.put("pnode_list", pnode_list);
                }

                // --------------------------- http request to show monitor info
                // --------------------------
                else if (!userprivilege.equals("Normal")
                        && act.equals("monitor")) {

                    String pnode_monitor_show = "";
                    String pnode_id_list = "";

                    double[][] monitor_data;

                    if (queryMap == null) {
                        // show all the pnode monitor info
                        for (Pnode pnd : Pnode.all()) {

                            monitor_data = RRDTools.getMonitorInfo((int) pnd
                                    .getId());

                            pnode_monitor_show += "<div class='row'><div class='span12'><h3>Worker "
                                    + pnd.getId()
                                    + "<small>"
                                    + "  - Ip Address: "
                                    + pnd.getIp()
                                    + ";</small>"
                                    + "<small>"
                                    + "&nbsp;&nbsp;&nbsp;&nbsp;Status: "
                                    + pnd.getStatus()
                                    + ";</small></h3> <ul class='thumbnails'><li class='span3'>"
                                    + "<a class='thumbnail'><p>CPU Info: "
                                    + (int) monitor_data[monitor_data.length - 1][2]
                                    + " Cores, "
                                    + monitor_data[monitor_data.length - 1][1]
                                    + " Mhz</p><div id='Monitor"
                                    + pnd.getId()
                                    + "_1" // id_1 presents cpu info
                                    + "' class='plotpic'> </div></a></li>"
                                    + "<li class='span3'>"
                                    + "<a class='thumbnail'><p>Memory Info: Total Memory "
                                    + monitor_data[monitor_data.length - 1][5]
                                    + " MB</p><div id='Monitor"
                                    + pnd.getId()
                                    + "_2" // id_2 presents memery info
                                    + "' class='plotpic'> </div></a></li>"
                                    + "<li class='span3'>"
                                    + "<a class='thumbnail'><p>Disk Info: Total Disk "
                                    + monitor_data[monitor_data.length - 1][9]
                                    + " GB</p><div id='Monitor"
                                    + pnd.getId()
                                    + "_3" // id_3 presents disk info
                                    + "' class='plotpic'> </div></a></li>"
                                    + "<li class='span3'>"
                                    + "<a class='thumbnail'><p>Network Info: Bandwidth "
                                    + monitor_data[monitor_data.length - 1][10]
                                    + " Mbps</p><div id='Monitor"
                                    + pnd.getId()
                                    + "_4" // id_4 presents network info
                                    + "' class='plotpic'> </div></a></li></ul></div></div>";

                            pnode_id_list += pnd.getId() + ";";
                        }

                    } else if (queryMap.containsKey("pnode_id")) {
                        // show one pnode monitor info

                        Pnode pnd = Pnode.findById(Integer.parseInt(queryMap
                                .get("pnode_id")));

                        monitor_data = RRDTools.getMonitorInfo((int) pnd
                                .getId());

                        pnode_monitor_show += "<div class='row'><div class='span12'><h3>Worker "
                                + pnd.getId()
                                + "<small>"
                                + "  - Ip Address: "
                                + pnd.getIp()
                                + ";</small>"
                                + "<small>"
                                + "&nbsp;&nbsp;&nbsp;&nbsp;Status: "
                                + pnd.getStatus()
                                + ";</small></h3> <ul class='thumbnails'><li class='span3'>"
                                + "<a class='thumbnail'><p>CPU Info: "
                                + (int) monitor_data[monitor_data.length - 1][2]
                                + " Cores, "
                                + monitor_data[monitor_data.length - 1][1]
                                + " Mhz</p><div id='Monitor"
                                + pnd.getId()
                                + "_1" // id_1 presents cpu info
                                + "' class='plotpic'> </div></a></li>"
                                + "<li class='span3'>"
                                + "<a class='thumbnail'><p>Memory Info: Total Memory "
                                + monitor_data[monitor_data.length - 1][5]
                                + " MB</p><div id='Monitor"
                                + pnd.getId()
                                + "_2" // id_2 presents memery info
                                + "' class='plotpic'> </div></a></li>"
                                + "<li class='span3'>"
                                + "<a class='thumbnail'><p>Disk Info: Total Disk "
                                + monitor_data[monitor_data.length - 1][9]
                                + " GB</p><div id='Monitor"
                                + pnd.getId()
                                + "_3" // id_3 presents disk info
                                + "' class='plotpic'> </div></a></li>"
                                + "<li class='span3'>"
                                + "<a class='thumbnail'><p>Network Info: Bandwidth "
                                + monitor_data[monitor_data.length - 1][10]
                                + " Mbps</p><div id='Monitor"
                                + pnd.getId()
                                + "_4" // id_4 presents network info
                                + "' class='plotpic'> </div></a></li></ul></div></div>";

                        pnode_id_list += pnd.getId() + ";";
                    }

                    if (!pnode_id_list.equals("")) {
                        pnode_id_list = pnode_id_list.substring(0,
                                pnode_id_list.length() - 1);
                    }

                    values.put("pnode_monitor_show", pnode_monitor_show);
                    values.put("pnode_id_list", pnode_id_list);
                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "monitor.html");
                }

                // --------------------------- http request from monitor
                // page
                // --------------------------
                else if (!userprivilege.equals("Normal")
                        && act.equals("getMonitorData")) {
                    double[][] monitor_data;

                    monitor_data = RRDTools.getMonitorInfo(Integer
                            .parseInt(queryMap.get("pnode_id")));

                    String ret = "";

                    if (monitor_data != null) {
                        for (int i = 0; i < monitor_data.length; i++) {
                            for (int j = 0; j < monitor_data[i].length; j++) {
                                if (j < monitor_data[i].length - 1) {
                                    ret += Util
                                            .formatDouble(monitor_data[i][j]) + ',';
                                } else {
                                    ret += Util
                                            .formatDouble(monitor_data[i][j]) + ';';
                                }
                            }
                        }
                    }

                    if (ret.length() != 0)
                        ret = ret.substring(0, ret.length() - 1);

                    return ret;
                }

                // --------------------------- http request from account
                // page
                // --------------------------
                else if (!userprivilege.equals("Normal")
                        && act.equals("account") || act.equals("add_user")
                        || act.equals("stop_user") || act.equals("active_user")
                        || act.equals("delete_user")
                        || act.equals("pass_modify")) {

                    if (userprivilege.equals("Root") && act.equals("add_user")) {
                        if (Users.findByName(queryMap.get("username")) != null) {
                            String adduser_error = "alert('The User Name has exist!')";
                            values.put("adduser_error", adduser_error);
                        } else if (Users.findByEmail(queryMap.get("email")) != null) {
                            String adduser_error = "alert('The Email Address has exist!')";
                            values.put("adduser_error", adduser_error);
                        } else {
                            String adduser_privilege = queryMap
                                    .get("privilege");
                            Users.user_type usertype = null;

                            if (adduser_privilege.equals("Root")
                                    || adduser_privilege.equals("Individual")
                                    || adduser_privilege.equals("Enterprise")) {
                                if (adduser_privilege.equals("Root")) {
                                    usertype = Users.user_type.Root;
                                } else if (adduser_privilege
                                        .equals("Enterprise")) {
                                    usertype = Users.user_type.Enterprise;
                                } else if (adduser_privilege
                                        .equals("Individual")) {
                                    usertype = Users.user_type.Individual;
                                }

                                new AddUserHandler().handleMessage(
                                        new AddUserMessage(queryMap
                                                .get("username"), queryMap
                                                .get("email"), queryMap
                                                .get("password"), usertype,
                                                "true", session_ip_loginuser
                                                        .get(remote_ipaddr)
                                                        .getId()), null, null,
                                        null);
                            }
                        }
                    }

                    else if (userprivilege.equals("Enterprise")
                            && act.equals("add_user")) {
                        if (Users.findByName(queryMap.get("username")) != null) {
                            String adduser_error = "alert('The User Name has exist!')";
                            values.put("adduser_error", adduser_error);
                        } else if (Users.findByEmail(queryMap.get("email")) != null) {
                            String adduser_error = "alert('The Email Address has exist!')";
                            values.put("adduser_error", adduser_error);
                        } else {
                            new AddUserHandler().handleMessage(
                                    new AddUserMessage(
                                            queryMap.get("username"), queryMap
                                                    .get("email"), queryMap
                                                    .get("password"),
                                            Users.user_type.Normal, "true",
                                            session_ip_loginuser.get(
                                                    remote_ipaddr).getId()),
                                    null, null, null);
                        }
                    }

                    else if (act.equals("active_user")) {
                        new ActiveUserHandler().handleMessage(
                                new ActiveUserMessage(Integer.parseInt(queryMap
                                        .get("user_id"))), null, null, null);

                    }

                    else if (act.equals("stop_user")) {
                        new StopUserHandler().handleMessage(
                                new StopUserMessage(Integer.parseInt(queryMap
                                        .get("user_id"))), null, null, null);
                    }

                    else if (act.equals("delete_user")) {
                        new DeleteUserHandler().handleMessage(
                                new DeleteUserMessage(Integer.parseInt(queryMap
                                        .get("user_id"))), null, null, null);
                    }

                    else if (act.equals("pass_modify")) {
                        Users user = session_ip_loginuser.get(remote_ipaddr);
                        if (user.getPassword().equals(queryMap.get("oldpass"))) {
                            new ModifyUserPassHandler().handleMessage(
                                    new ModifyUserPassMessage(user.getId(),
                                            queryMap.get("newpass")), null,
                                    null, null);
                        } else {
                            String pass_error = "alert('Old Password Wrong!')";
                            values.put("pass_error", pass_error);
                        }
                    }

                    // list all users
                    String user_show = "";
                    List<Users> users = new ArrayList<Users>();
                    if (userprivilege.equals("Root")) {
                        users = Users.all();
                    } else if (userprivilege.equals("Enterprise")) {
                        for (UserRelations urre : UserRelations
                                .getByAdminUserId(session_ip_loginuser.get(
                                        remote_ipaddr).getId())) {
                            users.add(Users.findById(urre.getNormalUserId()));
                        }
                    }

                    if (userprivilege.equals("Individual")) {
                        user_show = "Individual user has no users control right";
                        String hide_head = "<!--";
                        String hide_tail = "-->";
                        values.put("user_hide_head", hide_head);
                        values.put("user_hide_tail", hide_tail);
                    } else if (users == null) {
                        user_show = "None normal users";
                    } else {
                        for (Users user : users) {
                            user_show = user_show
                                    + "<tr><td>"
                                    + user.getId()
                                    + "</td><td>"
                                    + user.getName()
                                    + "</td><td>"
                                    + user.getEmail()
                                    + "</td><td>"
                                    + user.getPassword()
                                    + "</td><td>"
                                    + user.getPrivilege()
                                    + "</td><td>"
                                    + user.getActivated()
                                    + "</td><td><div class='btn-group'><button class='btn btn-danger dropdown-toggle' "
                                    + "data-toggle='dropdown'> Action <span class='caret'></span></button>"
                                    + "<ul class='dropdown-menu'> "
                                    + "<li><a href='active_user?user_id="
                                    + user.getId() + "'> Active </a></li>"
                                    + "<li><a href='stop_user?user_id="
                                    + user.getId() + "'> Stop </a></li>"
                                    + "<li><a href='delete_user?user_id="
                                    + user.getId() + "'> Delete</a></li>"
                                    + "</ul></div></td></tr>";
                        }
                    }

                    values.put("user_show", user_show);

                    if (userprivilege.equals("Root")) {
                        values.put("user_type_op",
                                "<option>Root</option><option>Enterprise</option><option>Individual</option>");
                    } else if (userprivilege.equals("Enterprise")) {
                        values.put("user_type_op", "<option>Normal</option>");
                    }

                    fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                            "account.html");
                }
            }
        }

        // 如果登录
        if (session_ip_islogin.get(remote_ipaddr)) {
            // ----------------- 初始化overview html显示变量 --------------------
            int user_total = 0;
            int root_user_total = 0;
            int enterprise_user_total = 0;
            int individual_user_total = 0;
            int not_activated_user_total = 0;

            for (Users user : Users.all()) { // *********** users var **********
                user_total++;

                if (user.getPrivilege().equals("Root")) {
                    root_user_total++;
                } else if (user.getPrivilege().equals("Enterprise")) {
                    enterprise_user_total++;
                } else if (user.getPrivilege().equals("Normal")) {
                    individual_user_total++;
                }

                if (user.getActivated().equals("0")) {
                    not_activated_user_total++;
                }
            }

            if (session_ip_loginuser.get(remote_ipaddr).getPrivilege()
                    .equals("Root")) {
                values.put("user_total", user_total);
                values.put("root_user_total", root_user_total);
                values.put("enterprise_user_total", enterprise_user_total);
                values.put("individual_user_total", individual_user_total);
                values.put("not_activated_user_total", not_activated_user_total);
            } else {
                values.put("hide_head", "<!--");
                values.put("hide_tail", "-->");
            }

            int pmachine_total = 0;
            int working_pmachine_toal = 0;
            int failed_pmachine_total = 0;
            int retired_pmachine_total = 0;
            for (Pnode pnode : Pnode.all()) { // ************* pmachine var
                                              // **********
                pmachine_total++;
                if (pnode.getStatusCode().equals("RUNNING")) {
                    working_pmachine_toal++;
                } else if (pnode.getStatusCode().equals("CONNECT_FAILURE")) {
                    failed_pmachine_total++;
                } else if (pnode.getStatusCode().equals("RETIRED")) {
                    retired_pmachine_total++;
                }
            }
            values.put("pmachine_total", pmachine_total);
            values.put("working_pmachine_toal", working_pmachine_toal);
            values.put("failed_pmachine_total", failed_pmachine_total);
            values.put("retired_pmachine_total", retired_pmachine_total);

            int vcluter_num = 0;
            for (Vcluster vcluster : Vcluster.all()) { // *********** vcluster
                vcluster.toString(); // var **********
                vcluter_num++;
            }
            values.put("vcluster_num", vcluter_num);

            int vmachine_num = 0;
            for (Vnode vnode : Vnode.all()) { // *********** vnode var
                vnode.toString(); // **********
                vmachine_num++;
            }
            values.put("vmachine_num", vmachine_num);
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
