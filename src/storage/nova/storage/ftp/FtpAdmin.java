package nova.storage.ftp;

import java.util.ArrayList;
import java.util.List;

import nova.common.util.Conf;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

public class FtpAdmin extends BaseUser {

    public FtpAdmin() {

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());

        int maxLogin = 1000;
        int maxLoginPerIP = 10;
        authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP));

        int uploadRate = 0;
        int downloadRate = 0;

        authorities.add(new TransferRatePermission(downloadRate, uploadRate));

        super.setName(Conf.getString("storage.ftp.admin.username"));
        super.setAuthorities(authorities);
    }

    @Override
    public String getPassword() {
        return Conf.getString("storage.ftp.admin.password");
    }

    @Override
    public int getMaxIdleTime() {
        return Conf.getInteger("storage.ftp.idle_time");
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public String getHomeDirectory() {
        return Conf.getString("storage.ftp.home");
    }

}
