package nova.master.api.messages;

public class UnregisterApplianceMessage {

    public UnregisterApplianceMessage() {

    }

    public UnregisterApplianceMessage(long id) {
        this.id = id;
    }

    public long id;

}
