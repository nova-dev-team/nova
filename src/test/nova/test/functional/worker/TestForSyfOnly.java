package nova.test.functional.worker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nova.common.util.Utils;
import nova.worker.api.messages.StartVnodeMessage;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TestForSyfOnly {

    Logger log = Logger.getLogger(TestForSyfOnly.class);

    @Test
    public void test() {
        String apps[] = { "demo_appliance" };
        StartVnodeMessage msg = new StartVnodeMessage("kvm", "ubuntu_vm", null,
                false, "300000", "1", "ubuntu.img", false, apps,
                "192.168.0.200", "255.255.255.0", "192.168.0.1");
        // msg.setName("vm");

        msg.setUuid("0f7c794b-2e17-45ef-3c55-ece004e76aef");
        msg.setCdImage("");
        msg.setRunAgent(true);

        // write nova.agent.ipaddress.properties file
        File ipAddrFile = new File(Utils.pathJoin(Utils.NOVA_HOME, "conf",
                "nova.agent.ipaddress.properties"));
        System.out.println(ipAddrFile.getName());
        if (!ipAddrFile.exists()) {
            try {
                ipAddrFile.createNewFile();
            } catch (IOException e1) {
                log.error("create nova.agent.ipaddress.properties file fail!",
                        e1);
            }
        }

        try {
            PrintWriter out = new PrintWriter(new FileWriter(ipAddrFile));
            out.println("agent.bind_host=" + msg.getIpAddr());
            out.close();
        } catch (IOException e1) {
            log.error("write nova.agent.ipaddress.properties file fail!", e1);
        }
        // String[] ignore = { "shit1", "shit2" };
        // Utils.copyWithIgnore("fuck1", "fuck2", ignore);
    }

}
