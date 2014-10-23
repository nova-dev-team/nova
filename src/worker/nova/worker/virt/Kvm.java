package nova.worker.virt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.worker.NovaWorker;

import org.apache.log4j.Logger;

/**
 * Interfacing to the KVM hypervisor.
 * 
 * @author santa
 * 
 */
public class Kvm {

    /**
     * Log4j logger.
     */
    static Logger log = Logger.getLogger(Kvm.class);

    /**
     * Emit libvirt domain definitions.
     * 
     * @param params
     *            VM parameters.
     * @return Emitted XML domain definition.
     */
    public static String emitDomain(HashMap<String, Object> params) {
        String templateFpath = Utils.pathJoin(Utils.NOVA_HOME, "conf", "virt",
                "kvm-domain-template.xml");
        String strWorkerIP = NovaWorker.getInstance().getAddr().getIp();
        if ((params.get("hdaImage") != null)
                && !params.get("hdaImage").toString().equals("")) {
            params.put("sourceFile", Utils.pathJoin(Utils.NOVA_HOME, "run",
                    params.get("name").toString(), params.get("hdaImage")
                            .toString()));
            if (Conf.getString("storage.engine").equalsIgnoreCase("pnfs")) {
                params.put("sourceFile", Utils.pathJoin(Utils.NOVA_HOME, "run",
                        "run", strWorkerIP + "_"
                                + params.get("name").toString(),
                        params.get("hdaImage").toString()));
            }
        } else {
            params.put("sourceFile", Utils.pathJoin(Utils.NOVA_HOME, "run",
                    params.get("name").toString(), "linux.img"));
            if (Conf.getString("storage.engine").equalsIgnoreCase("pnfs")) {
                params.put("sourceFile", Utils.pathJoin(Utils.NOVA_HOME, "run",
                        "run", strWorkerIP + "_"
                                + params.get("name").toString(), "linux.img"));
            }
        }

        if ((params.get("cdImage") != null)
                && (!params.get("cdImage").toString().equals(""))) {
            params.put("bootDevice", "cdrom");
        } else {
            params.put("bootDevice", "hd");
        }

        if (params.get("runAgent").toString().equalsIgnoreCase("true")) {
            params.put("bootDevice", "hd");
            // agent cdImage put in NOVA_HOME/run/agentcd
            params.put("cdromPath", Utils.pathJoin(Utils.NOVA_HOME, "run",
                    params.get("name").toString(), "agentcd", "agent-cd.iso"));
            if (Conf.getString("storage.engine").equalsIgnoreCase("pnfs")) {
                params.put("cdromPath", Utils.pathJoin(Utils.NOVA_HOME, "run",
                        "run", strWorkerIP + "_"
                                + params.get("name").toString(), "agentcd",
                        "agent-cd.iso"));
            }
            params.put("determinCdrom", "<disk type='file' device='cdrom'>"
                    + "\n    <source file='"
                    + params.get("cdromPath").toString() + "'/>"
                    + "\n    <target dev='hdc'/>"// +
                                                 // "\n    <boot order='2'/>"

                    + "\n  </disk>");
        }
        /*
         * else if ((params.get("cdImage") != null) &&
         * (!params.get("cdImage").toString().equals(""))) { // cdImage put in
         * NOVA_HOME/run params.put("cdromPath", Utils.pathJoin(Utils.NOVA_HOME,
         * "run", params.get("cdImage").toString())); if
         * (Conf.getString("storage.engine").equalsIgnoreCase("pnfs")) {
         * params.put("cdromPath", Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
         * strWorkerIP + "_" + params.get("name").toString(),
         * params.get("cdImage").toString())); } params.put("determinCdrom",
         * "<disk type='file' device='cdrom'>" + "\n    <source file='" +
         * params.get("cdromPath").toString() + "'/>" +
         * "\n    <target dev='hdc'/>" + "\n    <readonly/>" + "\n  </disk>"); }
         */
        else {
            params.put("determinCdrom", "");
        }

        String vmNetworkInterface = Conf.getString("vm_network_interface");
        String vmNetworkBridge = Conf.getString("vm_network_bridge");
        String fixVncMousePointer = Conf.getString("fix_vnc_mouse_pointer");
        System.out.println("vmNetworkInterface is "
                + vmNetworkInterface.equals(""));
        System.out.println("vmNetworkBridge is " + vmNetworkBridge.equals(""));
        System.out.println("fixVncMousePointer is " + fixVncMousePointer);

        // if ((!vmNetworkInterface.equals("")) &&
        // (!vmNetworkBridge.equals(""))) {
        params.put("interfaceType", "bridge");
        params.put("sourceBridge", vmNetworkBridge);
        params.put(
                "macAddress",
                "54:7E:" + Integer.toHexString((int) (Math.random() * 256))
                        + ":"
                        + Integer.toHexString((int) (Math.random() * 256))
                        + ":"
                        + Integer.toHexString((int) (Math.random() * 256))
                        + ":"
                        + Integer.toHexString((int) (Math.random() * 256)));
        params.put(
                "determinNetwork",
                "<interface type='" + params.get("interfaceType").toString()
                        + "'>" + "\n    <source bridge='"
                        + params.get("sourceBridge").toString() + "'/>"
                        + "\n    <mac address='"
                        + params.get("macAddress").toString() + "'/>"
                        + "\n  </interface>");
        // } else {
        // params.put("interfaceType", "network");
        // params.put("sourceNetwork", "default");
        // params.put(
        // "macAddress",
        // "54:7E:" + Integer.toHexString((int) (Math.random() * 256))
        // + ":"
        // + Integer.toHexString((int) (Math.random() * 256))
        // + ":"
        // + Integer.toHexString((int) (Math.random() * 256))
        // + ":"
        // + Integer.toHexString((int) (Math.random() * 256)));
        // params.put(
        // "determinNetwork",
        // "<interface type='"
        // + params.get("interfaceType").toString()
        // + "'>\n    <source network='"
        // + params.get("sourceNetwork").toString() + "'/>"
        // + "\n    <mac address='"
        // + params.get("macAddress").toString() + "'/>"
        // + "</interface>");
        // }
        if (fixVncMousePointer.equals("true")) {
            params.put("inputType", "tablet");
            params.put("bus", "usb");
            params.put("determinVnc", "<input type='"
                    + params.get("inputType").toString() + "' bus='"
                    + params.get("bus").toString() + "'/>");
        } else {
            params.put("determinVnc", "");
        }
        String strVNCPort = String.valueOf(Utils.getFreePort());
        String ss = params.get("uuid").toString();
        System.out.println(ss);
        Utils.WORKER_VNC_MAP.put(params.get("uuid").toString(), strVNCPort);
        params.put("vncport", strVNCPort);
        // write nova.agent.ipaddress.properties file
        File confFile = null;
        if (Conf.getString("storage.engine").equalsIgnoreCase("pnfs")) {
            params.put("cdromPath", Utils.pathJoin(Utils.NOVA_HOME, "run",
                    "run", strWorkerIP + "_" + params.get("name").toString(),
                    "agentcd", "agent-cd.iso"));
            confFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                    strWorkerIP + "_" + params.get("name").toString(),
                    "conf.xml"));
        } else {
            confFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "run", params
                    .get("name").toString(), "conf.xml"));
        }
        if (!confFile.exists()) {
            try {
                confFile.createNewFile();
            } catch (IOException e1) {
                log.error("create conf.xml fail!", e1);
            }
        }
        String rt = Utils.expandTemplateFile(templateFpath, params);

        try {
            PrintWriter outpw = new PrintWriter(new FileWriter(confFile));
            outpw.println(rt);
            outpw.close();
        } catch (IOException e1) {
            log.error("write conf.xml file fail!", e1);
        }
        return rt;
    }
}
