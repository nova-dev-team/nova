package nova.master.handler;

import java.io.File;
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
import nova.master.models.Pnode;
import nova.master.models.Vcluster;
import nova.master.models.Vnode;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;

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

    /**
     * Handle a request message, render result pages.
     */
    @Override
    public void handleMessage(DefaultHttpRequest req,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {
        log.info("New HTTP request from " + e.getChannel().getRemoteAddress());
        super.handleMessage(req, ctx, e, xreply);
    }

    /**
     * Render the result pages.
     */
    @Override
    public String renderResult(DefaultHttpRequest req) {
        // TODO @zhaoxun render results
        String fpath = Utils.pathJoin(Utils.NOVA_HOME, "www", "master",
                "index.html");
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("content", "NOVA MASTER");
        // Pnode pnode = new Pnode();
        // pnode.setIp("0.0.0.0");

        /*
         * MasterProxy mp = new MasterProxy(new SimpleAddress(
         * Conf.getString("master.bind_host"), Integer.parseInt(Conf
         * .getString("master.bind_port")))); mp.connect(new
         * SimpleAddress(Conf.getString("master.bind_host"),
         * Integer.parseInt(Conf.getString("master.bind_port")))
         * .getInetSocketAddress()); System.out.println(new SimpleAddress(
         * Conf.getString("master.bind_host"), Integer.parseInt(Conf
         * .getString("master.bind_port"))).toString());
         */

        URL url = null;
        try {
            url = new URL("http://127.0.0.1:3000" + req.getUri());
        } catch (MalformedURLException e) {
            log.error(e);
        }

        String act = getAction(url.getFile());
        values.put("act", "act:" + act);
        Map<String, String> queryMap = null;
        System.out
                .println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        if (act != null) {
            String query = url.getQuery();
            queryMap = getQueryMap(query);

            System.out.println(act + "avcccc");
            if (act.equals("add_pnode")) {
                // System.out.println(111);
                new AddPnodeHandler().handleMessage(
                        new AddPnodeMessage(new SimpleAddress(queryMap
                                .get("pnode_ip"), Conf
                                .getInteger("worker.bind_port"))), null, null,
                        null);
                // System.out.println(222);
                // values.put("pnode_ip", "ip:" + queryMap.get("pnode_ip"));
            } else if (act.equals("register_vdisk")) {
                new RegisterVdiskHandler().handleMessage(
                        new RegisterVdiskMessage(queryMap.get("display_name"),
                                queryMap.get("file_name"), queryMap
                                        .get("image_type"), queryMap
                                        .get("os_family"), queryMap
                                        .get("os_name"), queryMap
                                        .get("description")), null, null, null);
            } else if (act.equals("register_appliance")) {
                new RegisterApplianceHandler().handleMessage(
                        new RegisterApplianceMessage(queryMap
                                .get("display_name"),
                                queryMap.get("file_name"), queryMap
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

                System.out.println(queryMap.get("vcluster_name") + "       "
                        + Integer.parseInt(queryMap.get("vcluster_size")));

                new CreateVclusterHandler()
                        .handleMessage(
                                new CreateVclusterMessage(queryMap
                                        .get("vcluster_name"),
                                        Integer.parseInt(queryMap
                                                .get("vcluster_size"))), null,
                                null, null);

                String appsList = "hadoop";
                int clusterSize = Integer.parseInt(queryMap
                        .get("vcluster_size"));
                String clusterName = queryMap.get("vcluster_name");

                if (Utils.isUnix()) {
                    // we don't have to create new ssh key pair
                    if (clusterSize <= 10) {
                        String ftpDir = Utils.pathJoin(Utils.NOVA_HOME, "data",
                                "ftp_home", "ssh_keys");
                        Utils.mkdirs(Utils.pathJoin(ftpDir, Vcluster.last()
                                .getClusterName()));
                        for (int i = 0; i < clusterSize; i++) {
                            String vmName = "ubuntu" + (i + 1);
                            Utils.mkdirs(Utils.pathJoin(ftpDir, clusterName,
                                    vmName));
                            String tmpFile = "id_rsa";
                            Utils.copyOneFile(Utils.pathJoin(ftpDir, tmpFile),
                                    Utils.pathJoin(ftpDir, clusterName, vmName,
                                            tmpFile));
                            String tmpPubFile = "id_rsa.pub";
                            Utils.copyOneFile(Utils
                                    .pathJoin(ftpDir, tmpPubFile), Utils
                                    .pathJoin(ftpDir, clusterName, vmName,
                                            tmpPubFile));
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

                        File ipFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                                "run", "params", "clusterInfo.txt"));

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
                // for (int i = 0; i < Vcluster.last().getClusterSize(); i++) {
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
                // for (int i = 0; i < Vcluster.last().getClusterSize(); i++) {
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
                        String ftpDir = Utils.pathJoin(Utils.NOVA_HOME, "data",
                                "ftp_home", "ssh_keys");
                        Utils.mkdirs(Utils.pathJoin(ftpDir, Vcluster.last()
                                .getClusterName()));
                        for (int i = 0; i < Vcluster.last().getClusterSize(); i++) {
                            String vmName = queryMap.get("vnode_name"
                                    + String.valueOf(i + 1));
                            Utils.mkdirs(Utils.pathJoin(ftpDir, Vcluster.last()
                                    .getClusterName(), vmName));
                            String tmpFile = "id_rsa";
                            Utils.copyOneFile(Utils.pathJoin(ftpDir, tmpFile),
                                    Utils.pathJoin(ftpDir, Vcluster.last()
                                            .getClusterName(), vmName, tmpFile));
                            String tmpPubFile = "id_rsa.pub";
                            Utils.copyOneFile(Utils
                                    .pathJoin(ftpDir, tmpPubFile), Utils
                                    .pathJoin(ftpDir, Vcluster.last()
                                            .getClusterName(), vmName,
                                            tmpPubFile));
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
                    if (queryMap.get("appliance_list" + String.valueOf(i + 1))
                            .contains("hadoop")) {

                        File ipFile = new File(Utils.pathJoin(Utils.NOVA_HOME,
                                "run", "params", "clusterInfo.txt"));

                        try {
                            if (!ipFile.exists()) {
                                ipFile.createNewFile();
                            }
                            OutputStream os = new FileOutputStream(ipFile);
                            os.write(Vcluster.last().getFristIp().getBytes());
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

                    new CreateVnodeHandler().handleMessage(
                            new CreateVnodeMessage(queryMap.get("vnode_image"
                                    + String.valueOf(i + 1)), queryMap
                                    .get("vnode_name" + String.valueOf(i + 1)),
                                    Integer.parseInt(queryMap.get("cpu_count"
                                            + String.valueOf(i + 1))),
                                    Integer.parseInt(queryMap.get("memory_size"
                                            + String.valueOf(i + 1))), queryMap
                                            .get("appliance_list"
                                                    + String.valueOf(i + 1)),
                                    Integer.parseInt(queryMap.get("pnode_id"
                                            + String.valueOf(i + 1))), i,
                                    Vcluster.last().getClusterName()), null,
                            null, null);

                    System.out
                            .println(queryMap.get("vnode_image"
                                    + String.valueOf(i + 1))
                                    + "~~~~~~"
                                    + queryMap.get("vnode_name"
                                            + String.valueOf(i + 1))
                                    + "~~~~~~"
                                    + Integer.parseInt(queryMap.get("cpu_count"
                                            + String.valueOf(i + 1)))
                                    + "~~~~~~"
                                    + Integer.parseInt(queryMap
                                            .get("memory_size"
                                                    + String.valueOf(i + 1)))
                                    + "~~~~~~"
                                    + queryMap.get("appliance_list"
                                            + String.valueOf(i + 1)));

                }
            } else if (act.equals("delete_pnode")) {
                new DeletePnodeHandler().handleMessage(new DeletePnodeMessage(
                        Long.parseLong(queryMap.get("pnode_id"))), null, null,
                        null);

            } else if (act.equals("delete_vnode")) {
                new DeleteVnodeHandler().handleMessage(new DeleteVnodeMessage(
                        Long.parseLong(queryMap.get("vnode_id"))), null, null,
                        null);

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
                        new UnregisterApplianceMessage(Long.parseLong(queryMap
                                .get("appliance_id"))), null, null, null);
            } else if (act.equals("install_app")) {
                String[] appNames = queryMap.get("app_name").split(",");
                new MasterInstallApplianceHandler()
                        .handleMessage(
                                new MasterInstallApplianceMessage(Long
                                        .parseLong(queryMap.get("vnode_id")),
                                        appNames), null, null, null);
            } else if (act.equals("migrate")) {
                new MasterMigrateVnodeHandler().handleMessage(
                        new MasterMigrateVnodeMessage(Long.parseLong(queryMap
                                .get("vnode_id")), Long.parseLong(queryMap
                                .get("migration_from")), Long
                                .parseLong(queryMap.get("migrate_to"))), null,
                        null, null);
            }
        }

        values.put("pnode_info", Pnode.all());
        values.put("vnode_info", Vnode.all());
        values.put("vcluster_info", Vcluster.all());

        return Utils.expandTemplateFile(fpath, values);

    }

    public static Map<String, String> getQueryMap(String query) {
        if (query != null) {
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
