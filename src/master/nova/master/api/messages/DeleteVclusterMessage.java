package nova.master.api.messages;

public class DeleteVclusterMessage {

    public DeleteVclusterMessage() {

    }

    public DeleteVclusterMessage(long id) {
        this.id = id;
    }

    public long id;

}
