package nova.common.service.protocol;

import java.util.LinkedList;

public interface QueryApplianceStatusProtocol {
	public void sendSoftwareList(LinkedList<String> installSoftList);
}
