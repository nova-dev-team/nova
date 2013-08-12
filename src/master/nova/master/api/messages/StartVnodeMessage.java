package nova.master.api.messages;

public class StartVnodeMessage {

    public StartVnodeMessage() {
    }

    public StartVnodeMessage(String uuid) {
        this.uuid = uuid;
    }

    public String uuid;

}