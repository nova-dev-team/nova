package nova.worker.api.messages;

/**
 * message for revoke image files and clean up pool
 * 
 * @author shayf
 * 
 */
public class RevokeImageMessage {
    public RevokeImageMessage() {
        super();
    }

    public RevokeImageMessage(String name) {
        super();
        this.name = name;
    }

    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
