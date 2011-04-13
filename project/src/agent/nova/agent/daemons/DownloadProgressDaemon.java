package nova.agent.daemons;

import java.util.ArrayList;

import nova.common.util.SimpleDaemon;

public class DownloadProgressDaemon extends SimpleDaemon {

	private static DownloadProgressDaemon instance = null;

	private ArrayList<String> softList = new ArrayList<String>();

	public static void setSoftList(ArrayList<String> softlist)
	{
		intstanc.softList
	}

	@Override
	protected void workOneRound() {

	}

}
