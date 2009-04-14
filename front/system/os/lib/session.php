<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

class session {
	
	private $os;
	
	public $user_name;
	
	public function __construct($os){
		$this->os = $os;
	}
	
	
	
	/** get_id() Returns the id of the current session.
	  *
	  * @access private
	  **/
	public function get_id(){
		if(isset($_COOKIE["sessionId"])){
			return $_COOKIE["sessionId"];
		}else{
		    return null;
		}
	} // end get_id()
	
	
	
	/** get_member_id() Returns the id of member from the current session.
	  *
	  * @param $session_id string
	  **/
	public function get_member_id(){
		$response = '';
		
		$session_id = $this->get_id();
		
		
		
		if($session_id != ""){
			
			$sql = "select
				qo_members_id as id
				from
				qo_sessions
				where
				id = '".$session_id."'";
		

				
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				$response = $row['id'];
			}
			
		}
		
		return $response;
	} // end get_member_id()
	
	
	
	/** get_group_id() Returns the member's group id for this session.
	  * 
	  * @access public
	  * @param $member_id
	  **/
	public function get_group_id(){
		$response = '';
		$session_id = $this->get_id();
		
		if($session_id != ""){
			$sql = "select
				qo_groups_id as id
				from
				qo_sessions
				where
				id = '".$session_id."'";
			
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				$response = $row['id'];
			}
		}
		
		return $response;
	} // end get_group_id()
	
	
	
	/** get_group_name() Returns the member's group name for this session.
	  *
	  * @access public
	  **/
	public function get_group_name(){
		$response = '';
		$group_id = $this->get_group_id();
		
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
	} // end get_group_name()
	
	
	/** exists()
	  *
	  * @param $session_id string
	  **/
	function exists($session_id=""){
		$response = false;
		$session_id = $session_id != "" ? $session_id : $this->get_id();
		
		if($session_id != ""){
			// query the db for the session id
			$sql = "select
				qo_members_id
				from
				qo_sessions
				where
				id = '".$session_id."'";
			
			// if a record is found, they are logged in
			if(mysql_num_rows($result = mysql_query($sql)) > 0)	{
				$response = true;
			}
		}
		
		return $response;
	} // end exists()
	
	
	
	/** login()
	  * 
	  * @access public
	  *	@param $module string
	  * @param $user string
	  * @param $pass string 
	  **/
	public function login($user, $pass, $group_id=""){
		$response = "{success: false}";
		
		if(!isset($user)||!strlen($user)){
			$response = "{errors:[{id:'user', msg:'Required Field'}]}";
		}elseif(!isset($pass)||!strlen($pass)){
			$response = "{errors:[{id:'pass', msg:'Required Field'}]}";
		}else if(!$this->os->member->exists($user)){
			$response = "{errors:[{id:'user', msg:'User not found'}]}";
		}else if(!$this->os->member->is_active($user)){
			$response = "{errors:[{id:'user', msg:'This account is not active'}]}";
		}else{
			// check password
			$sql = "SELECT
				id,
				email_address
				FROM
				qo_members
				WHERE
				email_address = '".$user."'
				AND
				password = '".$pass."'";
			
			if(mysql_num_rows($result = mysql_query($sql)) < 1){
				$response = "{errors:[{id:'pass', msg:'Incorrect Password'}]}";
			}else{
				// successful login, check for groups
				
				// get member id
				$row = mysql_fetch_assoc($result);
				$member_id = $row['id'];
				
				// if group id was not supplied
				if($group_id == ""){
					
					// get members active groups
					$sql = "SELECT
						G.id,
						G.name
						FROM
						qo_groups_has_members GM
							-- Groups Join --
							INNER JOIN qo_groups AS G ON G.id = GM.qo_groups_id AND G.active = 1
						WHERE
						qo_members_id = ".$member_id."
						ORDER BY
						G.name";
					
					$result = mysql_query($sql);
					
					if(mysql_num_rows($result = mysql_query($sql)) > 1){
						$groups = array();
						
						while($row = mysql_fetch_assoc($result)){
							$groups[] = $row;
						}
						
						$response = "{success:true, groups: ".json_encode($groups)."}";
					
					}else{
						
						$row = mysql_fetch_assoc($result);
						
						// log member in with this group id
						$group_id = $row["id"];
						
					}
					
				}
				
				// if group id was supplied
				if($group_id != ""){
				
					/* delete existing session
					$sql = "DELETE FROM
						qo_sessions
						WHERE
						qo_members_id = ".$member_id;
					
					if(!mysql_query($sql)){
						$response = "{errors:[{id:'user', msg:'Login Failed'}]}";
					}else{ */
					
						// get our random session id
						$session_id = $this->os->build_random_id();
						
						// save temporary session id to our db
						$sql = "INSERT INTO qo_sessions (
							qo_members_id,
							qo_groups_id,
							id,
							ip,
							date
							) VALUES (
							".$member_id.",
							".$group_id.",
							'".$session_id."',
							'".$_SERVER['REMOTE_ADDR']."',
							'".date("Y-m-d H:i:s")."')";
						
						// attempt to save login session
						if(!mysql_query($sql)){
							
							$response = "{errors:[{id:'user', msg:'Login Failed'}]}";
							
						}else{
							// successful login
							
							$this->user_name = $user;
							
							
							
							
							
							$sql = "insert into currentlyloggedin(
								session_id,
								member_id,
								group_id
							) values(
								'" . $session_id . "',
								" . $member_id . ",
								" . $group_id . "
							)";
							
							
							$response = "{success:true, sessionId:'".$session_id."'}\n";
							
							mysql_query($sql);
							
							//$response = "{errors:[{id:'user', msg:'Login Failed: ". $this->user_name  . "'}]}";
						}
					//}					
				}
			}
		}
		return $response;
	} // end login()
	
	
	
	/** logout()
	  *
	  * @access public
	  **/
	public function logout(){
		$session_id = $this->get_id();
		
		if(isset($session_id)){
			$sql = "delete
				from
				qo_sessions
				where
				id = '".$session_id."'";
			
			if(mysql_query($sql)){
				session_destroy();
				
				// clear the cookie
				setcookie("sessionId", "");
				
			    // redirect to login page
				header('Location: '.$this->os->get_login_url());
				
				$sql = "delete from currentlyloggedin where session_id = '" . $session_id ."'";
				
				mysql_query($sql);
				
			}
		}
	} // end logout()
}
?>