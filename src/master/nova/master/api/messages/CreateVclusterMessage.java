package nova.master.api.messages;

public class CreateVclusterMessage {
    public CreateVclusterMessage() {
    }

    public CreateVclusterMessage(String vclusterName, int vclusterSize,
            long user_id, String firstname) {
        System.out.println("CreateVclusterMessage");
        this.vclusterName = vclusterName;
        this.vclusterSize = vclusterSize;
        this.userId = user_id;
        this.firstname = firstname;
    }

    public String vclusterName;
    public int vclusterSize;
    public long userId;
    public String firstname;

}
