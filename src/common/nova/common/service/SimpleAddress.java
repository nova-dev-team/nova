package nova.common.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Address info of a component.
 * 
 * @author santa
 * 
 */
public class SimpleAddress {

    /**
     * Target ip address.
     */
    public String ip;

    /**
     * Target port.
     */
    public int port;

    /**
     * No-arg construcutor for gson.
     */
    public SimpleAddress() {

    }

    public SimpleAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public SimpleAddress(InetSocketAddress addr) {
        this.ip = addr.getAddress().getHostAddress();
        this.port = addr.getPort();
    }

    /**
     * Override string present.
     */
    @Override
    public String toString() {
        return this.ip + ":" + this.port;
    }

    /**
     * Override equal checker.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleAddress) {
            SimpleAddress other = (SimpleAddress) o;
            return this.ip.equals(other.ip) && this.port == other.port;
        }
        return false;
    }

    /**
     * Override hash function.
     */
    @Override
    public int hashCode() {
        return ip.hashCode() ^ port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(this.ip, this.port);
    }

    public InetAddress getInetAddress() {
        String[] ips = ip.split(".");
        byte[] bs = new byte[] { (byte) (int) Integer.valueOf(ips[0]),
                (byte) (int) Integer.valueOf(ips[1]),
                (byte) (int) Integer.valueOf(ips[2]),
                (byte) (int) Integer.valueOf(ips[3]) };
        try {
            return InetAddress.getByAddress(bs);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
