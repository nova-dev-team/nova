package nova
{
	public class GlobalConst
	{

		public static const urlBase:String = "http://localhost:3000";
		
		public static const logoutURL:String = urlBase + "/logout";
		
		public static const misc_WhoAmI:String = urlBase + "/misc/who_am_i.json";
		
		public static const misc_version:String = urlBase + "/misc/version.json";
		
		public static const user_ChangePwd:String = urlBase + "/users/edit.json";
		
		public static const user_UpdateProfile:String = urlBase + "/users/edit.json";
		
		public static const users_list:String = urlBase + "/users/list.json";
		
		public static const settings_list:String = urlBase + "/settings/index.json";
		
		public static const pmachine_list:String = urlBase + "/pmachines/list.json";
		
		public static const pmachine_add:String = urlBase + "/pmachines/add.json";
		
		public static const portmappings_list:String = urlBase + "/misc/list_port_mapping.json";
		
		public static const portmappings_add:String = urlBase + "/misc/add_port_mapping.json";
		
		public static const soft_packages_list:String = urlBase + "/softwares/list.json";
		
		public static const soft_packages_add:String = urlBase + "/softwares/register.json";
		
		public static const vdisks_list:String = urlBase + "/vdisks/list.json";
		
		public static const vdisks_add:String = urlBase + "/vdisks/register.json";
		
		public static const vclusters_list:String = urlBase + "/vclusters/list.json";
		
		public static const vclusters_show:String = urlBase + "/vclusters/show.json";
		
		public static const misc_overview:String = urlBase + "/misc/overview.json";
		
		[Bindable]
		public static var myLogin:String = "";
		
		[Bindable]
		public static var myPrivilege:String = "";
		
		[Bindable]
		public static var myEmail:String = "";
		
		[Bindable]
		public static var myFullname:String = "";
		
		[Bindable]
		public static var novaVersion:String = "";

	}
}
