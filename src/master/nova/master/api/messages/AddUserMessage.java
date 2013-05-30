package nova.master.api.messages;

public class AddUserMessage {
	
	public AddUserMessage() {
		
	}

	public AddUserMessage(String user_name, String user_email, String user_password, 
			String user_privilege, String user_actived) {

		this.user_name = user_name;
		this.user_email = user_email;
		this.user_password = user_password;
		this.user_privilege = user_privilege;
		this.user_actived = user_actived;
		
	}
	
	public String user_name;
	
	public String user_email;
	
	public String user_password;
	
	public String user_privilege;
	
	public String user_actived;
	
}
