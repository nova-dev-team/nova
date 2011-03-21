package nova.storage.api;

import nova.common.service.SimpleProxy;

public class StorageProxy extends SimpleProxy {

	private StorageProxy() {

	}

	public static StorageProxy getProxy() {
		StorageProxy proxy = new StorageProxy();
		return proxy;
	}

}
