<?php
// get the os
require_once ("../os/os.php");
if (class_exists('os'))
{
    $os = new os();

    $module = $_POST['module'];

    if ($module == "")
    {
        die ("success: false");
    }

    if ($module == 'login')
    {
        print $os->session->login($_POST['user'], $_POST['pass'], $_POST['group']);
    } else if ($module == 'signup')
    {	
		if($os->member->exists($_REQUEST['email'])) {
			
			print "{errors:[{id:'email-already-used', msg:'Email already used: " . $_REQUEST['email'] ."'}]}";	
			return;
			
		} else {
			
			$sql = "
				insert into qo_members (
					first_name,
					last_name,
					email_address,
					password,
					active
				) values (
					'". $_REQUEST['first_name']."',
					'". $_REQUEST['last_name']."',
					'". $_REQUEST['email']."',
					'". $_REQUEST['password']."',
					0
				);
			";

			if (!mysql_query($sql)) {
				print "{success: false}";
				return;
			}
			
			$id = $os->member->get_id($_REQUEST['email']);
			$group_id = 0;
			$is_admin = 0;
			
			// add to group
			if ($_REQUEST['role'] == "User") {
				$group_id = 2;
			} else if($_REQUEST['role'] == "Administrator") {
				$group_id = 1;
				$is_admin = 1;
			} else {
				print "{success: false}";
				return;
			}
			
			$sql = "
				insert into qo_groups_has_members (
					qo_groups_id, qo_members_id,
					active, admin
				) values (
					" . $group_id  .",
					" . $id . ",
					1,
					" . $is_admin ."
				)
			";
			
			if (!mysql_query($sql)) {
				print "{errors:[{id:'failed-join-group', msg:'Failed to join a group'}]}";
				return;
			}
			
			$sql = "
				insert into qo_styles (
					qo_members_id, qo_groups_id, fontcolor
				) values (
					" . $id .",
					" . $group_id .",
					000000
				)
			";
			
			if (!mysql_query($sql)) {
				print "{errors:[{id:'failed-set-style', msg:'Failed to set the style'}]}";
				return;
			}
			
		}
		print "{success: true}";
	
    } else if ($module == 'forgotPassword')
    {
		if($os->member->exists($_REQUEST['user'])) {
			
			// TODO send mail
	        print "{success: true}";
		} else {
	        print "{errors:[{id:'no-such-user', msg:'No such user: " . $_REQUEST['user'] ."'}]}";			
		}
		
    } else
    {
        print "{success: false}";
    }
}
?>
