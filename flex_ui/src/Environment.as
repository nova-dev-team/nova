
package
{
	import mx.rpc.http.mxml.HTTPService; 
	import mx.managers.CursorManager; 
	   
	public class Environment
	{
		
		public static function refreshMouseCursor():void
		{
			if(mouseCursorCount == 0)
			{
				CursorManager.removeBusyCursor();
			}	
			else
			{
				CursorManager.setBusyCursor();
			}
		}
		
		public static var mouseCursorCount:int = 0;		
		
//		public static var URL:String = "http://166.111.131.32:3000/";
//		public static var URL:String = "http://192.168.0.125:3000/";
		public static var URL:String = "http://192.168.0.100:3000/";
		public static var USER_URL:String = URL + "users";
		

		
		public static var PMACHINE:String = URL + "pmachines";
		public static var PMACHINE_NEW:String = PMACHINE + "/new";
		public static var PMACHINE_DETAILS:String = PMACHINE + "/show";
		
		public static var VCLUSTER:String = URL + "vclusters/";
		public static var VCLUSTER_VMACHINES:String = VCLUSTER + "vm_list/";
		public static var VCLUSTER_CREATE:String = VCLUSTER + "create";
		public static var VCLUSTER_DESTROY:String = VCLUSTER + "destroy/";
		
		public static var VMACHINE:String = URL + "vmachines/";
		public static var VMACHINE_DETAILS:String = URL + "vmachines/show/";
		public static var VMACHINE_START:String = URL + "vmachines/start?uuid=";
		public static var VMACHINE_STOP:String = URL + "vmachines/stop?uuid=";
		public static var VMACHINE_SUSPEND:String = URL + "vmachines/suspend?uuid=";
		public static var VMACHINE_RESUME:String = URL + "vmachines/resume?uuid=";
		
		public static var VDISK:String = URL + "vdisks/"; 
		public static var VDISK_INDEX:String = VDISK + "index";
		public static var VDISK_SOFT:String = VDISK + "soft_list/";
		
		public static var VMACHINE_VNC:String = VMACHINE+"observe/";
		
		public static var NETWORK_ERROR:String = "Network no response, please check your network settings!";
		
		public static var TIME_OUT:int = 5;
		
		
		public static var OverallService:HTTPService = new HTTPService();
		
		
		
		public static var test:int = 0;
		public static var width:int = 0;
		public static var height:int = 0;
	}
}