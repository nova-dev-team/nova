import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import nova.common.util.Utils;

public class test1 {
    /** * @param args */
    public static void main(String[] args) {
        String path = "/home/xiaohan/nxvnc-10.sh";
        String examplePath = Utils.pathJoin(Utils.NOVA_HOME, "script",
                "nxvnc-example.sh");
        String content = "";
        String c = null;
        File f = new File(path);
        try {
            f.createNewFile();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            BufferedReader br0 = new BufferedReader(new FileReader(examplePath));
            while ((c = br0.readLine()) != null) {
                System.out.println(c);
                if (c.equals("VNC_PORT=5904"))
                    c = "VNC_PORT=" + 5909;
                content = content + c + "\n";
            }
        } catch (Exception efile) {
        }
        try {
            FileWriter fw = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(fw);
            // bw.newLine();
            bw.write(content);
            bw.close();
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}