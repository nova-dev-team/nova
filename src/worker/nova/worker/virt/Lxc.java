package nova.worker.virt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.log4j.Logger;

import nova.common.util.Conf;
import nova.common.util.Utils;

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

    /**
     * generate a random mac address with the given head
     * 
     * @author Tianyu Chen
     * @param head
     * @return generated address
     */
    private static String generateMacAddr(String head) {
        return (head + Integer.toHexString((int) (Math.random() * 256)) + ":"
                + Integer.toHexString((int) (Math.random() * 256)) + ":"
                + Integer.toHexString((int) (Math.random() * 256)) + ":"
                + Integer.toHexString((int) (Math.random() * 256)));
    }

    /**
     * generate the libvirt domain xml for a container
     * 
     * @author Tianyu Chen
     * @param params
     *            the parameters to substitute the fields in the template xml
     *            file ($NOVA_HOME/conf/virt/lxc-domain-template)
     * @return the domain xml as a string
     */
    public static String emitDomain(HashMap<String, Object> params) {
        // get domain xml template path
        String templateFpath = Utils.pathJoin(Utils.NOVA_HOME, "conf", "virt",
                "lxc-domain-template.xml");

        // get image full path
        if ((params.get("hdaImage") != null)
                && (!params.get("hdaImage").toString().isEmpty())) {
            // if image file name is not null or empty
            String path = Utils.pathJoin("/home/vm",
                    params.get("name").toString(), "rootfs");
            params.put("sourceFile", path);
        } else {
            // what if the image file name is empty?
            /**
             * TBD
             */
        }

        // specify boot device here
        if ((params.get("cdImage") != null)
                && (!params.get("cdImage").toString().isEmpty())) {
            // if cd rom is not null or empty
            params.put("bootDevice", "cdrom");
        } else {
            params.put("bootDevice", "hd");
        }

        // determine network bridging options
        // set bridging
        params.put("interface", "bridge");
        params.put("bridge", Conf.getString("vm_network_bridge"));
        // set mac address
        params.put("macaddr", generateMacAddr("fe:13:"));

        // write configuration xml file
        File conf = new File(Utils.pathJoin("/home/vm",
                params.get("name").toString(), "conf.xml"));
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
