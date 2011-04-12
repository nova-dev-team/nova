package nova.common.service.message;

import java.util.LinkedList;

/**
 * Request installation of softwares message save softwares' names in LinkedList
 * Using getInstallSoftList() to get will be installed softwares' names
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class RequestSoftwareMessage {
	private LinkedList<String> softList = new LinkedList<String>();

	public RequestSoftwareMessage() {

	}

	public RequestSoftwareMessage(LinkedList<String> installSoftList) {
		this.softList = installSoftList;
	}

	public LinkedList<String> getInstallSoftList() {
		return this.softList;
	}
}
