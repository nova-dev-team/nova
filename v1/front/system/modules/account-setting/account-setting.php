<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

// This classes methods (actions) will be called by connect.php
class AccountSetting {
	
	private $os;
	
	private $member_id;

	public function __construct($os){
		$this->os = $os;
		$this->member_id = $os->session->get_member_id();
	}
	
	public function viewAccount() {
		$sql = "select * from qo_members as M inner join  qo_groups_has_members as G on (M.id = G.qo_members_id) where M.id=" . $this->member_id;
		
		$result = mysql_query($sql);
		
		$val_arr = mysql_fetch_assoc($result);
		
		$user_role  = "";
		
		if ($val_arr['qo_groups_id'] == 1) {
			$user_role = "Administrator";
		} else if ($val_arr['qo_groups_id'] == 10) {
			$user_role = "Debug";
		} else if ($val_arr['qo_groups_id'] == 1000) {
			$user_role = "Super Administrator";
		} else if ($val_arr['qo_groups_id'] == 10000) {
			$user_role = "User";
		}
		
		echo "{success:true,
			first_name:'" .  $val_arr['first_name'] . "',
			last_name:'" .  $val_arr['last_name'] . "',
			email_address:'" .  $val_arr['email_address'] . "',
			user_role:'" .  $user_role .  "'
		}";
	}	
	
	public function updateAccount() {
		
		
		$old_pwd = $_REQUEST['old_password'];
		$new_pwd = $_REQUEST['new_password'];
		$new_fn = $_REQUEST['new_first_name'];
		$new_ln = $_REQUEST['new_last_name'];
		
		$sql = "
			select * from qo_members where 
			id="  . $this->member_id ." and
			password='"  .$old_pwd."'
		";
		
		$result = mysql_query($sql);
		
		if (mysql_num_rows($result) >0) {
			
			$sql = "
				update qo_members
					set 
					
					
					first_name='" .$new_fn ."',
					last_name='" .$new_ln ."',
					password='" .$new_pwd ."'
					
					
				 where 
				id="  . $this->member_id ."
			";
			
			$result = mysql_query($sql);
		
			echo "{success:true}";	
			
		} else {
			echo "{success:false, pwd_wrong:true}";	
		}
		
		
	}

}
?>