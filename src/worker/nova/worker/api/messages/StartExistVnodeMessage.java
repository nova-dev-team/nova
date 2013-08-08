package nova.worker.api.messages;

public class StartExistVnodeMessage {

    public StartExistVnodeMessage() {

    }

    public StartExistVnodeMessage(String hyper, String uuid, long vnodeid) {
        this.hyper = hyper;
        this.uuid = uuid;
        this.vnodeid = vnodeid;
    }

    public String hyper, uuid;
    public long vnodeid;

}
