package nova.worker.api.messages;

public class QueryVnodeIPMessage {
    public QueryVnodeIPMessage() {

    }

    public QueryVnodeIPMessage(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
        // this.vnodeid = vnodeid;
    }

    public String name;
    public String uuid;
    // public long vnodeid;

}
