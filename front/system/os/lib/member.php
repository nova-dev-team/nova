<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

class member {

	private $os;



	public function __construct($os){
		$this->os = $os;
	}



	/** exits() Returns true if a record exists for the passed in email address.
	  * 
	  * @param {string} $email The members email address
	  * @return {boolean}
	  **/
	public function exists($email){
		
		$response = false;
		
		if($email != ''){
			$sql = "SELECT
				id
				FROM
				qo_members
				WHERE
				email_address = '".$email."'";
			
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$response = true;
			}
		}
		
		return $response;
	} // end exits()
	
	
	
	/** is_active()
	  * 
	  * @param {string} $email The members email address
	  * @return {boolean}
	  **/
	public function is_active($email){
		$response = false;
			
		if($email != ''){
			$sql = "SELECT
				active
				FROM
				qo_members
				WHERE
				email_address = '".$email."'";
				
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				
				if($row["active"] == 1){
					$response = true;
				}
			}
		}
		
		return $response;
	} // end is_active()
	
	
	
	/** get_name() Returns the name of a member.
	  *
	  * @param $member_id integer
	  **/
	function get_name($member_id){
		$response = '';
		
		if($member_id != ""){
			// query the db for the members name
			
			$sql = "SELECT
				first_name,
				last_name
				FROM
				qo_members
				WHERE
				id = " . $member_id;
			
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				$response = $row['first_name']." ".$row['last_name'];
			}
		}
		return $response;
	} // end get_name()
	
	
	//santa
	
	function get_id($email) {
		$response = '';
		
		if($email != ""){
			// query the db for the members name
			$sql = "SELECT
				id
				FROM
				qo_members
				WHERE
				email_address = '".$email ."'";
			
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				$response = $row['id'];
			}
		}
		
		return $response;
	}

	
	
	
	/** clear() Clears a members launcher
	  * 
	  * @access public
	  * @param {string} type ["system", "member"]
	  * @param {string} launcher (e.g. autorun, contextmenu, quickstart, shortcut, startmenu)
	  **/
	public function clear($type, $launcher){
		// default
		$response = false;
		
		// only if system or member type
		if($launcher != "" && ($type == "system" || $type == "member")){
			// if system
			if($type == "system"){
				$member_id = 0;
				$group_id = 0;
			}else{
				// else is member
				$member_id = $this->os->session->get_member_id();
				$group_id = $this->os->session->get_group_id();
			}
			
			if($member_id != "" && $group_id != ""){
				// get the launchers id
				$sql = "select id from qo_launchers where name = '".$launcher."'";

				if(mysql_num_rows($result = mysql_query($sql)) > 0){
					$row = mysql_fetch_assoc($result);
					
					// clear members launcher
					$sql = "DELETE
						FROM
						qo_members_has_module_launchers
						WHERE
						qo_members_id = ".$member_id."
						AND
						qo_groups_id = ".$group_id."
						AND
						qo_launchers_id = ".$row["id"];
					
					if(mysql_query($sql)){
						$response = true;
					}
				}
			}
		}
		
		return $response;
	} // end clear()
	
	
	
	/** set()
	  * 
	  * @access public
	  * @param {string} type The type of launcher ["system", "member"]
	  * @param {array} ids An array containing each module's moduleId property
	  * @param {string} launcher ["autorun", "contextmenu", "quickstart", "shortcut", "startmenu"]
	  * 
	  * @usage set("system", ["demo-grid", "tabs-grid"], "shortcut", 10);
	  **/
	public function set($type, $ids, $launcher){
		// default
		$response = false;

		if($launcher != "" && ($type == "system" || $type == "member")){
			// if system
			if($type == "system"){
				$member_id = 0;
				$group_id = 0;
			}else{
				// else is member
				$member_id = $this->os->session->get_member_id();
				$group_id = $this->os->session->get_group_id();
			}

			// get the launcher's Db record id based on its launcher name
			$launcher_id = $this->os->get_launcher_id($launcher);

			if($member_id != "" && $group_id != "" && $launcher_id != ""){
				// initialize
				$sort_order = 0;

				// loop through ids array
				foreach($ids as $id){
					// get the module's Db record id based on its moduleId property
					$module_id = $this->os->get_module_id($id);
					
					if($module_id != ""){
						$sql = "INSERT INTO
							qo_members_has_module_launchers
							(qo_members_id,
							qo_groups_id,
							qo_modules_id,
							qo_launchers_id,
							sort_order)
							VALUES
							(".$member_id.",
							".$group_id.",
							".$module_id.",
							".$launcher_id.",
							".$sort_order.")";
						
						mysql_query($sql);
						
						$response = true;
						
						/* ToDo: handle errors
						if(!mysql_query($sql))
						{
							$response = true;
						} */
					
						$sort_order++;
					}
				}
			}
		}
		
		return $response;
	} // end set()
}
?>