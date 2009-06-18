<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

class theme {
    
    private $os;



	public function __construct($os){
		$this->os = $os;
	}
	
	
	
    /** get() Returns a string of the theme css to include
	  * 
	  * @param {integer} $member_id
	  * @param {integer} $group_id
	  * @param {string} $theme_dir
	  **/
	public function get(){
		$member_id = $this->os->session->get_member_id();
		$group_id = $this->os->session->get_group_id();
		$theme_dir = $this->os->get_theme_dir();
		
		// get members saved theme
		$theme = $this->get_link($member_id, $group_id, $theme_dir);
		
		if($theme == ''){
			// get the default
		    $theme = $this->get_link('0', '0', $theme_dir);
		}
		
		return $theme;
	} // end get()
	
	
	
	/** get_link()
	  * 
	  * @param {integer} $member_id
	  * @param {integer} $group_id
	  * @param {string} $theme_dir
	  **/
	private function get_link($member_id, $group_id, $theme_dir){
	    $theme = '';
		
		if($member_id != "" && $group_id != "" && $theme_dir){
			$sql = "SELECT
				path_to_file as path
				FROM
				qo_themes T
					INNER JOIN qo_styles AS S ON S.qo_themes_id = T.id
				WHERE
				qo_members_id = ".$member_id."
				AND
				qo_groups_id = ".$group_id;
			
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				$theme = '<link id="theme" rel="stylesheet" type="text/css" href="'.$theme_dir.$row["path"].'" />';
			}
		}
		
		return $theme;
	} // end get_link()
}
?>