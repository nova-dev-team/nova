package nova.agent.api.messages;

import java.util.LinkedList;

/**
 * Request installation of softwares message save softwares' names in LinkedList
 * Using getInstallSoftList() to get will be installed softwares' names
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class QueryApplianceStatusMessage {
	private LinkedList<String> softList = new LinkedList<String>();

	public QueryApplianceStatusMessage() {

	}

	public QueryApplianceStatusMessage(LinkedList<String> installSoftList) {
		this.softList = installSoftList;
	}

	public LinkedList<String> getInstallSoftList() {
		return this.softList;
	}
}
