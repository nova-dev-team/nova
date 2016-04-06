package nova.worker.virt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.log4j.Logger;

import nova.common.util.Conf;
import nova.common.util.Utils;
import nova.worker.NovaWorker;

/**
 * the Linux container interface
 * 
 * @author Tianyu Chen
 *
 */
public class Lxc {
    /**
     * log4j logger
     */
    static Logger logger = Logger.getLogger(Lxc.class);

    public static String emitDomain(HashMap<String, Object> params) {
        // get domain xml template path
        String templateFpath = Utils.pathJoin(Utils.NOVA_HOME, "conf", "virt",
                "lxc-domain-template.xml");
        String strWorkerIP = NovaWorker.getInstance().getAddr().getIp();

        // get image full path
        if ((params.get("hdaImage") != null)
                && (!params.get("hdaImage").toString().isEmpty())) {
            // if image file name is not null or empty
            String path = null;
            if (Conf.getString("storage.engine").equalsIgnoreCase("pnfs")) {
                path = Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                        strWorkerIP + "_" + params.get("name").toString(),
                        params.get("hdaImage").toString());
            } else {
                path = Utils.pathJoin(Utils.NOVA_HOME, "run",
                        params.get("name").toString(),
                        params.get("hdaImage").toString());
            }
            params.put("sourceFile", path);
        } else {
            // what if the image file name is empty?
            /**
             * TBD
             */
        }

        if ((params.get("cdImage") != null)
                && (!params.get("cdImage").toString().isEmpty())) {
            // if cd rom is not null or empty
            params.put("bootDevice", "cdrom");
        } else {
            params.put("bootDevice", "hd");
        }

        // write configuration xml file
        File conf = null;
        // get file path
        if (Conf.getString("storage.engine").equalsIgnoreCase("pnfs")) {
            conf = new File(Utils.pathJoin(Utils.NOVA_HOME, "run", "run",
                    strWorkerIP + "_" + params.get("name").toString(),
                    "conf.xml"));
        } else {
            conf = new File(Utils.pathJoin(Utils.NOVA_HOME, "run",
                    params.get("name").toString(), "conf.xml"));
        }
        // create file if not exists
        if (!conf.exists()) {
            try {
                conf.createNewFile();
            } catch (IOException ioe) {
                logger.error("create conf.xml fail! ", ioe);
            }
        }
        // write xml content
        String content = Utils.expandTemplateFile(templateFpath, params);
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(conf));
            pw.println(content);
            pw.close();
        } catch (IOException ioe) {
            logger.error("write conf.xml fail! ", ioe);
        }

        // return the content of the xml file
        return content;
    }
}
