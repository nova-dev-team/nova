package nova.master.api;

import nova.common.service.MessageTooLongException;
import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;
import nova.common.service.SimpleServer;

public class MasterHttpProxy extends SimpleProxy {

    public MasterHttpProxy(SimpleAddress replyAddr) {
        super(replyAddr);
        // TODO Auto-generated constructor stub
    }

    public void sendRequest(Object req) {
        String message = (String) req;

        if (message.length() > SimpleServer.MAX_PACKET_SIZE) {
            throw new MessageTooLongException();
        }

        this.channel.getChannel().write(message);
    }
}
