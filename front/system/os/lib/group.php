<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

class group {

	private $os;



	public function __construct($os){
		$this->os = $os;
	}



	/** exits() Returns true if a record exists for the passed in Group name.
	  * 
	  * @param {string} $name
	  * @return {boolean}
	  **/
	public function exists($name){
		$response = false;
		
		if($name != ''){
			$sql = "SELECT
				id
				FROM
				qo_groups
				WHERE
				name = '".$name."'";
			
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$response = true;
			}
		}
		
		return $response;
	} // end exits()
	
	
	
	/** is_active()
	  * 
	  * @param {string} $group_id
	  * @return {boolean}
	  **/
	public function is_active($group_id){
		$response = false;
			
		if($group_id != ''){
			$sql = "SELECT
				active
				FROM
				qo_groups
				WHERE
				id = ".$group_id;
				
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				
				if($row["active"] == 1){
					$response = true;
				}
			}
		}
		
		return $response;
	} // end is_active()
	
	
	
	/** get_name()
	  *
	  * @param $group_id integer
	  **/
	function get_name($group_id){
		$response = '';
		
		if($group_id != ""){
			$sql = "SELECT
				name
				FROM
				qo_groups
				WHERE
				id = ".$group_id;
			
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				$response = $row['name'];
			}
		}
		
		return $response;
	} // end get_name()
	
	
	
	/** has_member()
	  *
	  * @param {integer} $member_id
	  * @param {string} $name The name of the group
	  * @return boolean
	  **/
	function has_member($member_id, $group_name){
		$response = false;
		
		if($member_id != '' && $group_name != ''){
			$sql = "SELECT
				name
				FROM
				qo_groups AS G
					INNER JOIN qo_groups_has_members AS GM ON G.id = GM.qo_groups_id
				WHERE
				qo_members_id = ".$member_id;
			
			if($result = mysql_query($sql)){
				while($row = mysql_fetch_assoc($result)){
					if(strcasecmp($row['name'], $group_name) == 0){ // case-insensitive string comparison
						$response = true;
					}
				}
			}
		}
	
		return $response;	
	} // end has_member()
}
?>