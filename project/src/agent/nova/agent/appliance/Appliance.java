package nova.agent.appliance;

public class Appliance {

	public String name;

	public enum Status {

		DOWNLOAD_PENDING,

		DOWNLOADING,

		INSTALL_PENDING,

		INSTALLING,

		INSTALLED,

		CANCELLED,

		DOWNLOAD_FAILURE,

		INSTALL_FAILURE
	};

	Status status;

	public Appliance(String name) {
		this.name = name;
		this.status = Status.DOWNLOAD_PENDING;
	}

	public synchronized Status getStatus() {
		return status;
	}

	public synchronized void setStatus(Status status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
