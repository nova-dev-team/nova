package nova.agent.core;

import java.io.IOException;

import nova.common.interfaces.Cancelable;
import nova.common.interfaces.Progressable;

public class InstallProgress implements Progressable, Cancelable, Runnable {

	private String softName;
	private String softPath;

	public InstallProgress(String softNm, String softPh) {
		this.softName = softNm;
		this.softPath = softPh;
	}

	@Override
	public void cancel() {
		// TODO @gaotao Auto-generated method stub

	}

	@Override
	public boolean isCanceled() {
		// TODO @gaotao Auto-generated method stub
		return false;
	}

	@Override
	public int getProgress() {
		// TODO @gaotao Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDone() {
		// TODO @gaotao Auto-generated method stub
		return false;
	}

	@Override
	public void run() {
		try {
			System.out.println(this.softPath + "\\" + this.softName);
			Runtime.getRuntime().exec(this.softPath + "\\" + this.softName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
