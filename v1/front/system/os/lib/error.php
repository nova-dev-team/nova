<?php
/*
 * qWikiOffice Desktop 1.0
 * Copyright(c) 2007-2008, Integrated Technologies, Inc.
 * licensing@qwikioffice.com
 * 
 * http://www.qwikioffice.com/license
 */

class error {

	private $os;

	public function __construct($os){
		$this->os = $os;
	}
	
	
	
	/** log() Records an error log to the the qo_error_log table
	  * 
	  * @param {array} $errors An array of error messages
	  **/
	public function log($errors){
	    for($i = 0, $len = count($errors); $i < $len; $i++){
		$sql = "INSERT INTO qo_error_log (text, timestamp) VALUES ('".$errors[$i]."', '".date("Y-m-d H:i:s")."')";
			mysql_query($sql);
		}
	}
}
?>