package nova.master.api.messages;

public class ModifyUserPassMessage {

    public long userid;

    public String newpass;

    public ModifyUserPassMessage(long userid, String newpass) {
        this.userid = userid;
        this.newpass = newpass;
    }

}
