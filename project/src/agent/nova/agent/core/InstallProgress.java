package nova.agent.core;

import java.io.IOException;

import nova.common.interfaces.Cancelable;
import nova.common.interfaces.Progressable;

/**
 * Install one selected software
 * 
 * @author gaotao@gmail.com
 * 
 */
public class InstallProgress implements Progressable, Cancelable, Runnable {

	private String softName; // The name of installing software
	private String softPath;// The path of this installing software

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
			Runtime.getRuntime().exec(this.softPath + "\\" + this.softName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
