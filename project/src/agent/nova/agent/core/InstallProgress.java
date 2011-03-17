package nova.agent.core;

import nova.common.interfaces.Cancelable;
import nova.common.interfaces.Progressable;

public class InstallProgress implements Progressable, Cancelable {

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

}
