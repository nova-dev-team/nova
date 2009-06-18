<?php
/*
 * qWikiOffice Desktop 0.8.1
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

// This classes methods (actions) will be called by connect.php
class QoPreferences {
	
	private $os;

	public function __construct($os){
		$this->os = $os;
	}
	
	// begin public module actions
	
	public function saveAppearance(){
		print $this->save('appearance');
	}
	
	public function saveAutorun(){
		print $this->save('autorun');
	}
	
	public function saveBackground(){
		print $this->save('background');
	}
	
	public function saveQuickstart(){
		print $this->save('quickstart');
	}
	
	public function saveShortcut(){
		print $this->save('shortcut');
	}
	
	public function viewThemes(){
		print $this->os->preference->get_theme_thumbs();
	}
	
	public function viewWallpapers(){
		print $this->os->preference->get_wallpaper_thumbs();
	}
	
	// end public module actions

	private function save($what){
		$success = "{'success': false}";
	
		switch(true){
			case ($what == "autorun" || $what == "quickstart" || $what == "shortcut"):
				
				// clear old launcher data for member (based on group)
				if($this->os->launcher->clear("member", $what)){					
					$ids = $_POST["ids"];
					
					$ids = json_decode(get_magic_quotes_gpc() ? stripslashes($ids) : $ids);
					
					// if ids are found
					if(count($ids) > 0){
						$member_id = $this->os->session->get_member_id();
						$group_id = $this->os->session->get_group_id();
				
						// os will decode the ids
						if($this->os->launcher->set($member_id, $group_id, $ids, $what)){
							$success = "{'success': true}";
						}
					}else{
						$success = "{'success': true}";
					}
				}
				
				break;
			case ($what == "appearance"  || $what == "background"):
	
				$styles = array(
					'backgroundcolor' => $_POST["backgroundcolor"],
					'fontcolor' => $_POST["fontcolor"],
					'theme_id' => $_POST["theme"],
					'transparency' => $_POST["transparency"],
					'wallpaper_id' => $_POST["wallpaper"],
					'wallpaperposition' => $_POST["wallpaperposition"]
				);
				
				if($styles['backgroundcolor'] != "" && $styles['fontcolor'] != "" && $styles['theme_id'] != "" && $styles['transparency'] != "" && $styles['wallpaper_id'] != "" && $styles['wallpaperposition'] != ""){
					if($this->os->preference->set_styles($styles)){
						$success = "{'success': true}";
					}
				}
				
				break;
		}
		
		return $success;
	}
}
?>