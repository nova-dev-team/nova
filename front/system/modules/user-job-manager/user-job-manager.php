<?php

class UserJobManager
{

    private $os;

    public function __construct($os)
    {
        $this->os = $os;
    }

    public function dummyTest()
    {
//    	$user_id = $os->session->get_member_id();
		
        //echo "{success:true, user_id:" . $user_id ."}";
        echo "{success:true, msg:'haha->Member id : " . $this->os->session->get_member_id() ."!!!'}";
    }

}


?>
