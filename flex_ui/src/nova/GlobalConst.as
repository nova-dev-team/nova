package nova
{
	public class GlobalConst
	{

		public static const urlBase:String = "http://localhost:3000";
		
		public static const logoutURL:String = urlBase + "/logout";
		
		public static const misc_WhoAmI:String = urlBase + "/misc/who_am_i.json";
		
		public static const user_ChangePwd:String = urlBase + "/users/edit.json";
		
		public static const user_UpdateProfile:String = urlBase + "/users/edit.json";
		
		[Bindable]
		public static var myLogin:String = "";
		
		[Bindable]
		public static var myPrivilege:String = "";
		
		[Bindable]
		public static var myEmail:String = "";
		
		[Bindable]
		public static var myFullname:String = "";

	}
}
