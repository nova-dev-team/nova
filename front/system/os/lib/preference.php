<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

class preference {

	private $os;



	public function __construct($os){
		$this->os = $os;
	}



	/** get_styles() Returns all the styles associated with a member (in group)
	  * for this session.
	  *
	  * @access public
	  **/
	public function get_styles(){
		$styles = '{}';
		
		$member_id = $this->os->session->get_member_id();
		$group_id = $this->os->session->get_group_id();
		
		if($member_id != "" && $group_id != ""){
			// get system default
			$s_default = $this->get_style("0", "0");
			
			// get group default
			$s_group = $this->get_style("0", $group_id);
			
			// get member preferences
			$s_member = $this->get_style($member_id, $group_id);
		}
		
		// default styles
		$styles = $s_default;
		
		// overwrite with group default
		if(count($s_group) > 0){
			$styles = $this->os->overwrite_assoc_array($styles, $s_group);
		}
		
		// overwrite with member preference
		if(count($s_member) > 0){
			$styles = $this->os->overwrite_assoc_array($styles, $s_member);
		}
		
		return json_encode($styles);
	} // end get_styles()
	
	
	
	/** get_style() Will return the style associated with a member (in group).
	  *
	  * @access private
	  * @param $member_id int, the member id
	  * @param $group_id int, the group id
	  **/
	private function get_style($member_id, $group_id){
		$response = array();
		
		if($member_id != "" && $group_id != ""){			
			// get system default
			$sql = "SELECT
				backgroundcolor,
				fontcolor,
				transparency,
				T.id AS themeid,
				T.name AS themename,
				T.path_to_file AS themefile,
				W.id AS wallpaperid,
				W.name AS wallpapername,
				W.path_to_file AS wallpaperfile,
				wallpaperposition
				FROM
				qo_styles S
					-- Themes --
					INNER JOIN qo_themes AS T ON T.id = S.qo_themes_id
					-- Wallpapers --
					INNER JOIN qo_wallpapers AS W ON W.id = S.qo_wallpapers_id
				WHERE
				qo_members_id = ".$member_id."
				AND
				qo_groups_id = ".$group_id;
			
			$result = mysql_query($sql);
			
			// if a record was returned
			if(mysql_num_rows($result = mysql_query($sql)) > 0){
				$row = mysql_fetch_assoc($result);
				
				$response["backgroundcolor"] = $row["backgroundcolor"];
				$response["fontcolor"] = $row["fontcolor"];
				$response["transparency"] = $row["transparency"];
				$response["theme"] = array(
					"id" => $row["themeid"],
					"name" => $row["themename"],
					"pathtofile" => $this->os->config->THEMES_DIR.$row["themefile"]
				);
				$response["wallpaper"] = array(
					"id" => $row["wallpaperid"],
					"name" => $row["wallpapername"],
					"pathtofile" => $this->os->config->WALLPAPERS_DIR.$row["wallpaperfile"]
				);
				$response["wallpaperposition"] = $row["wallpaperposition"];
			}
		}
		
		return $response;
	} // end get_style()
	
	
	
	/** set_style() Creates a new style record, or updates one if it already exists
	  * 
	  * @param {integer} $member_id
	  * @param {integer} $group_id
	  * @param {array} $styles
	  * @return {boolean}
	  **/
	public function set_styles($styles){
		$result = false;
		$member_id = $this->os->session->get_member_id();
		$group_id = $this->os->session->get_group_id();
		
		if($member_id != "" && $group_id != ""){			
			if($this->style_exists($member_id, $group_id)){
				$sql = "update
					qo_styles
					set
					backgroundcolor = '".$styles['backgroundcolor']."',
					fontcolor = '".$styles['fontcolor']."',
					qo_themes_id = ".$styles['theme_id'].",
					transparency = '".$styles['transparency']."',
					qo_wallpapers_id = ".$styles['wallpaper_id'].",
					wallpaperposition = '".$styles['wallpaperposition']."'
					where
					qo_members_id = ".$member_id." and
					qo_groups_id = ".$group_id;
			}else{
				$sql = "insert into qo_styles (
					qo_members_id,
					qo_groups_id,
					backgroundcolor,
					fontcolor,
					qo_themes_id,
					transparency,
					qo_wallpapers_id,
					wallpaperposition)
					values (
					".$styles['member_id'].",
					".$styles['group_id'].",
					'".$styles['backgroundcolor']."',
					'".$styles['fontcolor']."',
					".$styles['theme_id'].",
					'".$styles['transparency']."',
					".$styles['wallpaper_id'].",
					'".$styles['wallpaperposition']."')";
			}
			
			if(mysql_query($sql)){
				$result = true;
			}
		}
		
		return $result;
	} // end set_styles()
	
	
	
	/** style_exists() Returns true if a style record exists for the member id and group id.
	  * 
	  * @param {integer} $member_id
	  * $param {integer} $group_id
	  * @return boolean
	  **/
	private function style_exists($member_id, $group_id){
		$result = false;
		
		if($member_id != "" && $group_id != ""){				
			$sql = "select
				transparency
				from
				qo_styles
				where
				qo_members_id = ".$member_id." and
				qo_groups_id = ".$group_id;
		
			// if a record exists
			if(mysql_num_rows(mysql_query($sql)) > 0){
				$result = true;
			}
		}
		
		return $result;
	} // end style_exists()
	
	
	
	/** get_theme_thumbs()
	  **/
	public function get_theme_thumbs(){
	    $themes = "{'images': []}";
	    $member_id = $this->os->session->get_member_id();
	    
	    if($member_id != ''){
	        $sql = "select
				id,
				name,
				path_to_thumbnail as pathtothumbnail,
				path_to_file as pathtofile
				from
				qo_themes
				order by name";
		
			if($result = mysql_query($sql)){
				$items = array();
				$count = 0;
				$path = $this->os->config->THEMES_DIR;
				
				while($row = mysql_fetch_assoc($result)){
					$items[$count] = $row;
					$items[$count]["pathtothumbnail"] = $path.$items[$count]["pathtothumbnail"];
					$items[$count]["pathtofile"] = $path.$items[$count]["pathtofile"];
					
					$count++;
				}
				
				$themes = '{"images":'.json_encode($items).'}';
			}
	    }
	    
	    return $themes;
	} // end get_theme_thumbs()
	
	
	
	/** get_wallpaper_thumbs()
	  **/
	public function get_wallpaper_thumbs(){
	    $wallpapers = "{'images': []}";
	    $member_id = $this->os->session->get_member_id();
	    
	    if($member_id != ''){
	        $sql = "select
				id,
				name,
				path_to_thumbnail as pathtothumbnail,
				path_to_file as pathtofile
				from
				qo_wallpapers
				order by name";
		
			if($result = mysql_query($sql)){
				$items = array();
				$count = 0;
				$path = $this->os->config->WALLPAPERS_DIR;
				
				while($row = mysql_fetch_assoc($result)){
					$items[$count] = $row;
					$items[$count]["pathtothumbnail"] = $path.$items[$count]["pathtothumbnail"];
					$items[$count]["pathtofile"] = $path.$items[$count]["pathtofile"];
					
					$count++;
				}
				
				$wallpapers = '{"images":'.json_encode($items).'}';
			}
	    }
	    
	    return $wallpapers;
	} // end get_wallpaper_thumbs
}
?>