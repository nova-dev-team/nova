<?php
	//===========================================================================================
	// templateModule.php
	// PHP file for the TemplateModule Module, designed for a qWikiOffice web desktop.

	// This file is current not called by the TemplateModule so it is only presented as an example
	// PHP File



	// This file will read/save the Field Values from the TemplateForm Table of the qWikiOffice
	// Database using the JSON data format.

	// This file should be in the following dir:
	// templateForm/

	// This file is designed to handle request calls from the following:
	// - templateForm/templateForm.js

	// NOTE: The $_REQUEST variables should be able to handle both GET and POST request.
	// Field Names - No special characters in field names.  Strictly personal reasons.

	// -- The FORM fields.
	// -- $fFirstName	= $_REQUEST['firstName'];
	// -- $fLastName	= $_REQUEST['lastName'];
	// -- $fEmailAddress	= $_REQUEST['emailAddress'];
	// -- $fPassword	= $_REQUEST['password'];

	// Javascript Local Store Combo Box Value.
	// -- $fActive		= $_REQUEST['active'];

	//===========================================================================================

	//===========================================================================================
	// 0.0.0 Initial Creation Date (YMD) - 2008/12/08
	//  Description: See templateForm.js file for Module description and link.
	//
	//  Credits: .
	//  RoadMap: .
	//===========================================================================================


// This classes methods (actions) will be called by connect.php
class TemplateModule {
	
	/** PHP4 UNSUPPORTED
	 * Private or Public method
	 *	private $os;
	 */

	var $os;


	/** PHP4 UNSUPPORTED
	 * Private or Public method
	 *	public function __construct($os){
	 */

	function TemplateModule(&$os){
		$this->os =& $os;
	}
	

	// begin public module actions
	function doTask() {
		$response = "{success: false}";

		$task = ($_REQUEST['task']) ? ($_REQUEST['task']) : null;

		if($os->is_member_logged_in()) {
			$fMemberId = $os->get_member_id();

			switch($task) {
				case "read":
					$response = getFormDataTemplateForm($fMemberId);
					break;
				case "save":
					$response = saveFormDataTemplateForm($fMemberId);
					break;
				default:
					break;
			}//end switch
		} else {
			$response = "{success: false, msg: 'Member Not Logged In'}";
		}

		print $response;
	}


	//------ Functions Below ------

	// ============================================================================
	// Get Form Data Functions
	// ============================================================================

	function SQLerr($Code, $Text, $Stmt) {
		$SQLmsg = "SQL ErrorCode: " . addslashes($Code);
		// $SQLmsg .= " - SQL ErrorText: " . addslashes($Text);
		$SQLmsg .= " - SQL Request: " . addslashes($Stmt);

		$SQLmsg .= ".";
		return $SQLmsg;
	}


	function getFormDataTemplateForm ($fMemberId) {
		$response = "{success: false}";
	
		// For SQL Error Reporting.
		$sqlErrorCode = 0;
		$sqlErrorText = '';
		$sqlStmt      = '';

		// Check that a "templateForm" table exists.
		$sql = 'select * from `templateForm`';

	        if ($result = mysql_query($sql)) {
			$sql2 = 'select `first_name`, `last_name`, `email_address`, `password`, `active` 
				from `templateForm`
				where id = '.$fMemberId;
	
			if($result2 = mysql_query($sql2))
			{
				$row = mysql_fetch_assoc($result2);
				$response = '{success: true, "data":'.json_encode($row).'}';
			} else {
				$sqlErrorCode = mysql_errno();
				$sqlErrorText = mysql_error();
				$sqlStmt      = $sql;

				$response = "{success: false, msg: '" . SQLerr($sqlErrorCode, $sqlErrorText, $sqlStmt) . "'}";
			}
		} else {
			$sqlErrorCode = mysql_errno();
			$sqlErrorText = mysql_error();
			$sqlStmt      = $sql;

			$response = "{success: false, msg: '" . SQLerr($sqlErrorCode, $sqlErrorText, $sqlStmt) . "'}";
		}
		return $response;
	}


	// ============================================================================
	// Save Form Data Functions
	// ============================================================================

	function saveFormDataTemplateForm($fMemberId) {
		$response = "{success: false}";
	
		// For SQL Error Reporting.
		$sqlErrorText = '';
		$sqlErrorCode = 0;
		$sqlStmt      = '';

		// Re-assign all the incoming fields
		$fFirstName 	= $_REQUEST['firstName'];
		$fLastName 	= $_REQUEST['lastName'];
		$fEmailAddress 	= $_REQUEST['emailAddress'];
		$fPassword 	= $_REQUEST['password'];

		// Local Store Combo Box Values.
		$fActive	= $_REQUEST['active'];
	

		// Check incoming variables for required values.
		if(!isset($fFirstName)||!strlen($fFirstName)){
			$response = "{success: false, msg: 'Missing required field', field: 'firstName', errors:[{id:'firstName', msg:'Required Field'}]}";
		}elseif(!isset($fLastName)||!strlen($fLastName)){
			$response = "{success: false, msg: 'Missing required field', field: 'lastName', errors:[{id:'lastName', msg:'Required Field'}]}";
		}elseif(!isset($fEmailAddress)||!strlen($fEmailAddress)){
			$response = "{success: false, msg: 'Missing required field', field: 'emailAddress', errors:[{id:'emailAddress', msg:'Required Field'}]}";
		}elseif(!isset($fPassword)||!strlen($fPassword)){
			$response = "{success: false, msg: 'Missing required field', field: 'password', errors:[{id:'password', msg:'Required Field'}]}";
		}elseif(!isset($fActive)||!strlen($fActive)){
			$response = "{success: false, msg: 'Missing required field', field: 'active', errors:[{id:'active', msg:'Required Field'}]}";
		}else{
			// Incoming values passed required value test.

			// Make all the incoming strings safe
			$fFirstName 	= '\''.mysql_real_escape_string($_REQUEST['firstName']).'\'';
			$fLastName 	= '\''.mysql_real_escape_string($_REQUEST['lastName']).'\'';
			$fEmailAddress 	= '\''.mysql_real_escape_string($_REQUEST['emailAddress']).'\'';
			$fPassword 	= '\''.mysql_real_escape_string($_REQUEST['password']).'\'';

			// Local Store Combo Box Values.
			$fActive	= '\''.mysql_real_escape_string($_REQUEST['active']).'\'';
	


			$sql = 'select * from `templateForm`';
        		if ($result = mysql_query($sql)) {
				$sql2 = 'UPDATE `templateForm`
					SET `first_name` = '.$fFirstName.'
					  , `last_name` = '.$fLastName.'
					  , `email_address` = '.$fEmailAddress.'
					  , `password` = '.$fPassword.'
					  , `active` = '.$fActive.'
					WHERE id = '.$fMemberId;
	
				if (mysql_query($sql2)) {
					$response = "{success:true, msg: 'Save Complete'}";
				} else {
					$sqlErrorCode = mysql_errno();
					$sqlErrorText = mysql_error();
					$sqlStmt      = $sql;

					$response = "{success: false, msg: '" . SQLerr($sqlErrorCode, $sqlErrorText, $sqlStmt) . "'}";
				}
			} else {
				$sqlErrorCode = mysql_errno();
				$sqlErrorText = mysql_error();
				$sqlStmt      = $sql;

				$response = "{success: false, msg: '" . SQLerr($sqlErrorCode, $sqlErrorText, $sqlStmt) . "'}";
			}
		}
		return $response;
	}
}
?>