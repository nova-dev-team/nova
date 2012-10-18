package nova.master.api;

/**
 * port map info
 * 
 * @author eaglewatcher
 */
public class Route {
    public Route() {
    }

    String LocalIP = "";
    int LocalPort = 0;
    String DestHost = "";
    int DestPort = 0;
    String AllowClient = "";

    public String toString() {
        StringBuffer stb = new StringBuffer();
        stb.append(" LocalADD  " + LocalIP);
        stb.append(" :" + LocalPort);
        stb.append(" --->DestHost " + DestHost);
        stb.append(" :" + DestPort);
        stb.append("   (AllowClient) " + AllowClient);
        return stb.toString();
    }
}
