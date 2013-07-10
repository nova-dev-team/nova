package nova.master.api.messages;

public class DeleteUserMessage {

    public DeleteUserMessage() {

    }

    public DeleteUserMessage(int id) {
        this.id = id;
    }

    public long id;

}
