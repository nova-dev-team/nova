package nova.common.service.protocol;

import java.util.LinkedList;

public interface RequestSoftwareProtocol {
	public void sendSoftwareList(LinkedList<String> installSoftList);
}
