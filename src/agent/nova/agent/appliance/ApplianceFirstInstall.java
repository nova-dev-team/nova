package nova.agent.appliance;

import nova.agent.NovaAgent;

/**
 * Install appliances when one vm first start up
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ApplianceFirstInstall implements Runnable {

    private String[] appsInstall;

    public ApplianceFirstInstall(String[] args) {
        this.appsInstall = args;
    }

    @Override
    public void run() {
        for (String appName : appsInstall) {
            if (NovaAgent.getInstance().getAppliances().containsKey(appName)) {
                Appliance app = NovaAgent.getInstance().getAppliances()
                        .get(appName);
                app.setStatus(Appliance.Status.FIRST_INSTALL);
            } else {
                Appliance app = new Appliance(appName);
                NovaAgent.getInstance().getAppliances().put(appName, app);
                app.setStatus(Appliance.Status.FIRST_INSTALL);

            }
        }
    }
}
