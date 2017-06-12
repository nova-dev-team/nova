package nova.worker.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleHandler;
import nova.master.api.MasterProxy;
import nova.worker.NovaWorker;
import nova.worker.api.messages.QueryVnodeIPMessage;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class QueryVnodeIPHandler implements SimpleHandler<QueryVnodeIPMessage> {

    class Virbr {

        String ip_address;

        String mac_address;

        String hostname;

        String client_id;

        String expiry_time;

    }

    Logger logger = Logger.getLogger(QueryVnodeIPHandler.class);

    @Override
    public void handleMessage(QueryVnodeIPMessage msg,
            ChannelHandlerContext ctx, MessageEvent e, SimpleAddress xreply) {

        String mac = ReadMacaddress(msg.name);
        logger.info("hhhhhh:  " + mac + " hhhhh " + msg.name);
        String RealIP = null;
        // 列表/array 数据
        File file = new File("/var/lib/libvirt/dnsmasq/virbr0.status");
        // String
        // str="[{'id': '1','code': 'bj','name': '北京','map': '39.90403, 116.40752599999996'}, {'id': '2','code': 'sz','name': '深圳','map': '22.543099, 114.05786799999998'}, {'id': '9','code': 'sh','name': '上海','map': '31.230393,121.473704'}, {'id': '10','code': 'gz','name': '广州','map': '23.129163,113.26443500000005'}]";
        String str = null;

        try {
            str = FileUtils.readFileToString(file);
            // System.out.print(str);
            StringBuilder strBuilder = new StringBuilder(str);
            for (int i = 0; i < str.length(); i++) {

                if (str.charAt(i) == '-') {
                    strBuilder.setCharAt(i, '_');
                }
            }
            str = strBuilder.toString();

        } catch (IOException e2) {
            e2.printStackTrace();
        }

        Gson gson = new Gson();

        List<Virbr> rs = new ArrayList<Virbr>();

        Type type = new TypeToken<ArrayList<Virbr>>() {
        }.getType();

        rs = gson.fromJson(str, type);

        for (Virbr o : rs) {
            logger.info("Finally  " + o.mac_address);
            if (mac.equals(o.mac_address)) {
                RealIP = o.ip_address;
                logger.info("Finally get: " + o.ip_address);
                break;
            }

        }

        // Return RealIP
        MasterProxy master = NovaWorker.getInstance().getMaster();

        master.sendVnodeIP(RealIP, msg.uuid);

    }

    public String ReadMacaddress(String name) {
        String macaddress = null;
        try {
            FileInputStream in = new FileInputStream("/etc/libvirt/qemu/"
                    + name + ".xml");
            InputStreamReader inReader = new InputStreamReader(in);
            BufferedReader bufReader = new BufferedReader(inReader);
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                if (line.contains("mac address")) {
                    int m = 0;
                    int n = 0;
                    for (int j = 0; j < line.length(); j++) {
                        if (line.charAt(j) == '\'' && m == 0)
                            m = j;
                        if (line.charAt(j) == '\'' && m != 0)
                            n = j;
                    }
                    // System.out.println(m+"  cs   "+n);
                    macaddress = line.substring(m + 1, n);
                    System.out.println(macaddress);
                    break;
                }

            }
            bufReader.close();
            inReader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            // System.out.println("读取" + filename + "出错！");
        }
        return macaddress;
    }

}
