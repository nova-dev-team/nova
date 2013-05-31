package nova.master.api;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * PortMap server, act as a watchdog, wait for connections, one route is
 * corresponding to one Server
 * 
 * @author eaglewatcher
 */
public class Server extends Thread {

    public Server(Route route) {
        this.route = route;
        connectionQueue = new Vector<Transfer>();
        start();
    }

    public void closeServer() {
        isStop = true;
        if (null != myServer) {
            closeServerSocket();
        }
        while (this.connectionQueue.size() > 0) {
            Transfer tc = (Transfer) connectionQueue.remove(0);
            tc.closeSocket(tc.socket);
            tc = null;
        }
    }

    public void run() {
        SysLog.info(" start Transfer......:" + route.toString());
        ServerSocket myServer = null;
        try {
            InetAddress myAD = Inet4Address.getByName(route.LocalIP);
            myServer = new ServerSocket(route.LocalPort, 4, myAD);
        } catch (Exception ef) {
            SysLog.severe("Create Server " + route.toString() + " error:" + ef);
            closeServerSocket();
            return;
        }
        SysLog.info("Transfer Server : " + route.toString() + " created OK");
        while (!isStop) {

            try {
                Socket sock = myServer.accept();

                sock.setSoTimeout(0);
                // connCounter++;
                Transfer myt = new Transfer(sock, route);
                connectionQueue.add(myt);

            } catch (Exception ef) {
                SysLog.severe(" Transfer Server : " + route.toString()
                        + " accept error" + ef);
            }
        }
    }

    private void closeServerSocket() {
        try {
            this.myServer.close();
        } catch (Exception ef) {
        }
    }

    private ServerSocket myServer = null;
    private boolean isStop = false;
    private Vector<Transfer> connectionQueue = null;
    // private int connCounter = 0;
    private Route route = null;
}
