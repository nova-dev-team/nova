<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

class launcher {

	private $os;



	public function __construct($os){
		$this->os = $os;
	}
	
	
	
	/** get_id()
	  * 
	  * @param {string} launcher (e.g. autorun, contextmenu, quickstart, shortcut, startmenu)
	  **/
	private function get_id($launcher){
		// default
		$id = '';
		
		if($this->os->session->exists() && $launcher != ""){
			$sql = "select
				id
				from
				qo_launchers
				where
				name = '".$launcher."'";
			
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				$id = $row["id"];
			}
		}
		
		return $id;
	} // end get_id()
	
	
	
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
	public function set($member_id, $group_id, $ids, $launcher){
		// default
		$response = false;

		if($member_id != "" && $group_id != "" && count($ids) > 0 && $launcher != ""){
			
			// get the launcher's Db record id based on its launcher name
			$launcher_id = $this->get_id($launcher);

			if($launcher_id != ""){
				// initialize
				$sort_order = 0;

				// loop through ids array
				foreach($ids as $id){
					// get the module's Db record id based on its moduleId property
					$module_id = $this->os->module->get_id($id);
					
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
	
	
	
	/** get()
	  *
	  * @access public
	  * @param {integer} $member_id
	  * @param {integer} $group_id
	  **/
	public function get($member_id, $group_id){
		$response = array();
		
		if($member_id != "" && $group_id != ""){			
			// get the launchers
			$sql = "SELECT
				id,
				name
				FROM
				qo_launchers";
			
			if($result = mysql_query($sql)){
				while($row = mysql_fetch_assoc($result)){
					$response[$row["name"]] = $this->get_modules($member_id, $group_id, $row["id"]);
				}
			}
		}
		
		return $response;
	} // end get()
	
	
	
	/** get_all() Will load ALL the launchers associated
	  * with a member (in group) at once.  Includes the default launchers
	  *
	  * @access private
	  * @param $member_id int, the member id
	  **/
	public function get_all(){
		$member_id = $this->os->session->get_member_id();
		$group_id = $this->os->session->get_group_id();
		
		if($member_id != "" && $group_id != ""){
			// get system default			
			$l_default = $this->get("0", "0");
			
			// get member preferences
			$l_member = $this->get($member_id, $group_id);
		}
		
		// overwrite system default launchers with member preference
		if(count($l_member) > 0){
			$launchers = $this->os->overwrite_assoc_array($l_default, $l_member);
		}else{
			$launchers = $l_default;
		}
		
		return json_encode($launchers);
	} // end get_all()
	
	
	
	/** get_modules() Returns an array containing all modules for the passed in launcher.
	  * 
	  * @param {integer} $member_id
	  * @param {integer} $group_id
	  * @param {integer} $launcher_id
	  **/
	private function get_modules($member_id, $group_id, $launcher_id){
	    $response = array();
	    
	    if($member_id != '' && $group_id != '' && $launcher_id != ''){
	    	$sql = "SELECT
				M.module_id as moduleId
				FROM
				qo_members_has_module_launchers ML
					-- Modules --
					INNER JOIN qo_modules AS M ON M.id = ML.qo_modules_id
				WHERE
				qo_launchers_id = ".$launcher_id."
				AND
				qo_members_id = ".$member_id."
				AND
				qo_groups_id = ".$group_id."
				ORDER BY  ML.sort_order asc";
			
			if($result = mysql_query($sql)){
				while($row = mysql_fetch_assoc($result)){
			    	$response[] = $row["moduleId"];
				}
			}
	    }
	    
	    return $response;
	} // end get_modules()
}
?>