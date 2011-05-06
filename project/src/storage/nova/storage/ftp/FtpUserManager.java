package nova.storage.ftp;

import nova.storage.NovaStorage;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;

public class FtpUserManager extends AbstractUserManager {

	static User admin = new FtpAdmin(NovaStorage.getInstance().getConf());

	static User anonUser = new FtpAdmin(NovaStorage.getInstance().getConf());

	public FtpUserManager() {
		super(admin.getName(), new Md5PasswordEncryptor());
	}

	@Override
	public User authenticate(Authentication auth)
			throws AuthenticationFailedException {

		if (auth instanceof AnonymousAuthentication) {
			return anonUser;
		} else if (auth instanceof UsernamePasswordAuthentication) {
			UsernamePasswordAuthentication uAuth = (UsernamePasswordAuthentication) auth;
			if (uAuth.getUsername().equals(admin.getName())
					&& uAuth.getPassword().equals(admin.getPassword())) {
				return admin;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public void delete(String arg0) throws FtpException {
		// do nothing
	}

	@Override
	public boolean doesExist(String name) throws FtpException {
		if (name.equals(admin.getName())) {
			return true;
		} else if (name.equals(anonUser.getName())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String[] getAllUserNames() throws FtpException {
		return new String[] { admin.getName(), anonUser.getName() };
	}

	@Override
	public User getUserByName(String name) throws FtpException {
		if (name.equals(admin.getName())) {
			return admin;
		} else if (name.equals(anonUser.getName())) {
			return anonUser;
		} else {
			return null;
		}
	}

	@Override
	public void save(User user) throws FtpException {
		// do nothing
	}

}
