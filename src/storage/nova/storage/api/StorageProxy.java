package nova.storage.api;

import java.net.InetSocketAddress;

import nova.common.service.SimpleAddress;
import nova.common.service.SimpleProxy;

public class StorageProxy extends SimpleProxy {

    public StorageProxy(InetSocketAddress replyAddr) {
        super(replyAddr);
    }

    public StorageProxy(SimpleAddress replyAddr) {
        super(replyAddr);
    }

}
