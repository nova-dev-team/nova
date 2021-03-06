package nova.agent.appliance;

public class Appliance {

    public String name = "";

    Status status = Status.NOT_INSTALLED;

    String info = "";

    public Appliance() {
    }

    public Appliance(String name) {
        this.name = name;
    }

    public synchronized Status getStatus() {
        return this.status;
    }

    public synchronized void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Appliance) {
            Appliance app = (Appliance) o;
            return this.name.equals(app.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "{" + this.name + "," + this.status.toString() + "}";
    }

    public enum Status {

        NOT_INSTALLED,

        DOWNLOAD_PENDING,

        DOWNLOADING,

        FIRST_INSTALL,

        FIRST_INSTALLING,

        INSTALL_PENDING,

        INSTALLING,

        INSTALLED,

        CANCELLED,

        DOWNLOAD_FAILURE,

        INSTALL_FAILURE
    };
}
