package nova.master.api;

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * transfer I/O data between maped ports
 * 
 * @author eaglewatcher
 */
public class Transfer extends Thread {

    Route route = null;
    Socket socket;
    static private int TIMEOUT = 1000;
    static private int BUFSIZ = 1024;

    public Transfer(Socket s, Route route) {
        this.route = route;
        this.socket = s;
        this.start();
    }

    public void run() {
        Socket outbound = null;
        try {
            outbound = new Socket(route.DestHost, route.DestPort);
            socket.setSoTimeout(TIMEOUT);
            InputStream is = socket.getInputStream();
            outbound.setSoTimeout(TIMEOUT);
            OutputStream os = outbound.getOutputStream();
            pipe(is, outbound.getInputStream(), os, socket.getOutputStream());
        } catch (Exception e) {
            SysLog.severe(" transfer error:" + route.toString() + " :" + e);
        } finally {
            SysLog.warning("Disconnect :" + route.toString());
            closeSocket(outbound);
            closeSocket(socket);
        }
    }

    private void pipe(InputStream is0, InputStream is1, OutputStream os0,
            OutputStream os1) {
        try {
            int ir;
            byte bytes[] = new byte[BUFSIZ];
            while (true) {
                try {
                    if ((ir = is0.read(bytes)) > 0) {
                        os0.write(bytes, 0, ir);
                    } else if (ir < 0) {
                        break;
                    }
                } catch (InterruptedIOException e) {
                }
                try {
                    if ((ir = is1.read(bytes)) > 0) {
                        os1.write(bytes, 0, ir);
                        // if (logging) writeLog(bytes,0,ir,false);
                    } else if (ir < 0) {
                        break;
                    }
                } catch (InterruptedIOException e) {
                }
            }
        } catch (Exception e0) {
            SysLog.warning(" Method pipe" + this.route.toString() + " error:"
                    + e0);
        }
    }

    void closeSocket(Socket s) {
        try {
            s.close();
        } catch (Exception ef) {

        }
    }

}
