package nova.master.api.messages;

public class UnregisterVdiskMessage {

    public UnregisterVdiskMessage() {

    }

    public UnregisterVdiskMessage(long id) {
        this.id = id;
    }

    public long id;

}
