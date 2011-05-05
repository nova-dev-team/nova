package nova.storage.ftp;

import java.util.ArrayList;
import java.util.List;

import nova.common.util.Conf;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;

public class FtpAnonymousUser extends BaseUser {

	public static final String ANONYMOUS_USERNAME = "anonymous";

	Conf conf = null;

	public FtpAnonymousUser(Conf conf) {
		this.conf = conf;

		List<Authority> authorities = new ArrayList<Authority>();

		int maxLogin = 1000;
		int maxLoginPerIP = 10;
		authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP));

		int uploadRate = 0;
		int downloadRate = 0;

		authorities.add(new TransferRatePermission(downloadRate, uploadRate));

		super.setName(ANONYMOUS_USERNAME);
		super.setAuthorities(authorities);
	}

	@Override
	public int getMaxIdleTime() {
		return conf.getInteger("storage.ftp.idle_time");
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public String getHomeDirectory() {
		return conf.getString("storage.ftp.home");
	}

}
