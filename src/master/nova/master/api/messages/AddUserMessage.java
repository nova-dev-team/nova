package nova.master.api.messages;

import nova.master.models.Users;

public class AddUserMessage {

    public AddUserMessage() {

    }

    public AddUserMessage(String user_name, String user_email,
            String user_password, Users.user_type user_privilege,
            String user_actived, long create_userid) {

        this.user_name = user_name;
        this.user_email = user_email;
        this.user_password = user_password;
        this.user_privilege = user_privilege;
        this.user_actived = user_actived;
        this.create_userid = create_userid;

    }

    public String user_name;

    public String user_email;

    public String user_password;

    public Users.user_type user_privilege;

    public String user_actived;

    public Long create_userid;

}
