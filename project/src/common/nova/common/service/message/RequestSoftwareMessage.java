package nova.common.service.message;

import java.util.ArrayList;

public class RequestSoftwareMessage {
	private ArrayList<String> softList = new ArrayList<String>();

	public RequestSoftwareMessage(ArrayList<String> installSoftList) {
		this.softList = installSoftList;
	}

	public ArrayList<String> getInstallSoftList() {
		return this.softList;
	}
}
