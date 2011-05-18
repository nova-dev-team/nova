package nova.master.api.messages;

import nova.common.service.SimpleAddress;

public class AddPnodeMessage {

	/**
	 * No-arg constructore for gson.
	 */
	public AddPnodeMessage() {

	}

	public AddPnodeMessage(SimpleAddress pAddr) {
		this.pAddr = pAddr;
		// System.out.println(pAddr.toString());
	}

	/**
	 * The {@link SimpleAddress}.
	 */
	public SimpleAddress pAddr;

}
