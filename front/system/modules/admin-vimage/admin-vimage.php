<?php

class AdminVimage
{

    private $os;

    public function __construct($os)
    {
        $this->os = $os;
    }
    
    
    public function listImg() {
      $core_reply = file_get_contents("http://localhost:3000/vimage/list/");
      echo "{success:true, all_images:" . $core_reply ."}";
    }

    public function addImg() {
      echo "{success:true}";
    }

    public function delImg() {
      $iid = $_REQUEST["img_id"];
      $hidden = $_REQUEST["hidden"];
      if ($hidden == "true") {
        $core_reply = file_get_contents("http://localhost:3000/vimage/hide/" . $iid);
      } else {
        $core_reply = file_get_contents("http://localhost:3000/vimage/unhide/" . $iid);
      }
      echo $core_reply;
    }
    

    public function kickAss()
    {
    	$user_id = $_REQUEST['user_id'];
		
		
		$sql  = "select * from currentlyloggedin where member_id=".$user_id;
		
		$result = mysql_query($sql);
		
		$value_arr = mysql_fetch_assoc($result);
		
		$session_id = $value_arr['session_id'];
    	
		$sql = "Delete from currentlyloggedin where member_id=".$user_id;
		
		mysql_query($sql);
		
		$sql = "delete
				from
				qo_sessions
				where
				id = '".$session_id."'";
				
		mysql_query($sql);
		
        echo "{success:true}";
    }

    public function toggleActive()
    {


        $new_active = $_REQUEST['value'];

        $user_id = $_REQUEST['user_id'];

        if ($new_active != "" && user_id != "")
        {


            $new_active_val = 0;

            if ($new_active == "true")
            {
                $new_active_val = 1;
            }

            $sql = "update qo_members set active=".$new_active_val."
			where id=".$user_id;

            //print $sql;

            mysql_query($sql);

            echo "{success:true}";

        }

    }

    public function viewUserInfo()
    {
        $start = $_REQUEST['start'];
        $limit = $_REQUEST['limit'];


        $filter = " (qo_groups_id = 1 or qo_groups_id = 10000) ";

        echo "{";

        // only filter on Admin/user
        if ($_REQUEST['filter'] != "")
        {

            foreach ($_REQUEST['filter'] as $filter_setting)
            {

                if ($filter != "")
                {
                    $filter .= "  and ";
                }

                $filter_field = $filter_setting['field'];
                $filter_value = $filter_setting['data']['value'];

                // echo '"'.$filter_field.'":';
                //echo '"'.$filter_value.'",';

                if ($filter_field == "user_role")
                {
                    if ($filter_value == "User")
                    {
                        $filter .= " ( qo_groups_id = 10000  )";
                    } else if ($filter_value == "Administrator")
                    {
                        $filter .= " ( qo_groups_id = 1 )";
                    } else
                    {
                        // do nothing
                    }
                } else if ($filter_field == "is_active")
                {
                    if ($filter_value == "Yes")
                    {
                        $filter .= " ( M.active = 1  )";
                    } else if ($filter_value == "No")
                    {
                        $filter .= " ( M.active = 0 )";
                    } else
                    {
                        $filter .= " ( M.active = 0 OR M.active = 1)";
                    }
                }
            }

        }

        if ($filter == "")
        {
            $filter = " (qo_groups_id = 1 or qo_groups_id = 10000) ";
        }

        $sql = "select * from qo_members as M inner join  qo_groups_has_members as G on (M.id = G.qo_members_id)";


        if ($filter != "")
        {
            $sql .= "  where ".$filter;
        }

        $result = mysql_query($sql);

        $total_count = mysql_num_rows($result);

        print '"totalCount":"'.$total_count.'",';

        //print ' "sql1" : "'.$sql.'", ';

        $sql = "
				select M.id as m_id, qo_groups_id, first_name, last_name,
				M.active as m_active , email_address
				 from qo_members as M 
				 	inner join qo_groups_has_members as G on (M.id = G.qo_members_id)
			";

        if ($filter != "")
        {
            $sql .= "  where ".$filter;
        }

        //echo ' "sql2" : "' . $sql . '", ';


        echo ' "all_users":[';

        $sort_by = $_REQUEST['sort'];
        $direction = $_REQUEST['dir'];

        //			echo "sort = ".$sort_by;

        if ($sort_by != "")
        {


            if ($sort_by == "first_name")
            {
                $sql .= "  order by first_name ".$direction;
            } else if ($sort_by == "email")
            {
                $sql .= "  order by email_address ".$direction;
            } else if ($sort_by == "is_active")
            {
                $sql .= "  order by M.active ".$direction;


            } else if ($sort_by == "user_role")
            {
                $sql .= "  order by qo_groups_id ".$direction;
            } else
            {
                $sql .= "  order by ".$sort_by." ".$direction;
            }

        }

        if ($start != "" && $limit != "")
        {
            $sql .= "  limit ".$start." ,".$limit;
        }

        // echo $sql;

        $result2 = mysql_query($sql);



        $data_str = "";

        while ($row = mysql_fetch_array($result2))
        {

            if ($row['qo_groups_id'] == "1")
            {
                $user_role = "Administrator";
            } else if ($row['qo_groups_id'] == "10000")
            {
                $user_role = "User";
            } else if ($row['qo_groups_id'] == "1000")
            {
                $user_role = "Super Administrator";
            } else if ($row['qo_groups_id'] == "10")
            {
                $user_role = "Debug";
            } else
            {
                $user_role = "ERROR";
            }
			
			$another_sql = "select * from currentlyloggedin where member_id = ".$row['m_id'];
			
			$is_logged_in = 0;
			
			$result_new = mysql_query($another_sql);
			
			if (mysql_num_rows($result_new) > 0) {
				$is_logged_in = 1;
			}
			
			$is_logged_in_str = "";
			
			if ($is_logged_in == 1) {
				$is_logged_in_str = "true";
			} else  if ($is_logged_in == 0){
				$is_logged_in_str = "false";
			}
			

            $data_str .=
            '{"user_id":"'.$row['m_id'].
            '", "first_name":"'.$row['first_name'].
            '","last_name":"'.$row['last_name'].'","email":"'
            .$row['email_address'].'","user_role":"'.$user_role.
            '","is_active":"'.$row['m_active'].'",'.
            '"is_logged_in" :"'.  $is_logged_in_str  .'"'
            .'}';
            ;

            $data_str .= ",";

        }

        echo substr($data_str, 0, strlen($data_str)-1); // eat the last comma


        echo ']}';

    }


}


?>
