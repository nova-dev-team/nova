<?php

class UserJobManager {

  private $os;

  public function __construct($os) {
    $this->os = $os;
  }

  public function dummyTest() {
//    	$user_id = $os->session->get_member_id();

    //echo "{success:true, user_id:" . $user_id ."}";
    echo "{success:true, msg:'haha->Member id : " . $this->os->session->get_member_id() ."!!!'}";
  }


  public function listCluster() {
  
    $email = $this->os->session->get_member_email();
    
//echo $email;

   $core_reply = file_get_contents("http://localhost:3000/user/info_vclusters/" . $email);
 // echo $core_reply;
  $lst = json_decode($core_reply);

//  echo sizeof($lst);
  
/*  for ($i = 0; $i < sizeof($lst); $i++) {
    echo $lst[$i];
}*/
//   $json = new Services_JSON();

    echo "{'success':true, 'all_clusters':[";
    
    // XXX DUMMY CODE
    
 /*   if ($_REQUEST["cid"] == "") {
  

    
    $total = 6;
    for ($i = 0; $i < $total; $i++) {
      echo "{'cluster_id':'c" . $i ."', 'cluster_name':'1.2.3.4'}";
      
      if ($i < $total - 1) {
        echo ",";
      }
    }
    
  }*/
  
  $total = sizeof($lst);
//  echo $total;
  for ($i = 0; $i < $total; $i += 2) {
    echo "{'cluster_id' :  '" . $lst[$i] . "', cluster_name:'" . $lst[$i + 1] ."'}";
if ($i < $total - 2) {
  echo ',';
}
}
  
  
      echo "]}";

  }
  
  
  public function addCluster() {
      
  }
  
   public function removeCluster() {
   
   
}


public function listVM() {
    
    $core_reply = file_get_contents("http://localhost:3000/vcluster/info_vm_list/" . $_REQUEST['cid']);
    //echo $core_reply;
    
    $lst = json_decode($core_reply);

    echo "{'success':true, 'all_vms':[";
    
    
    $total = sizeof($lst);
    for ($i = 0; $i < $total; $i+=4) {
      echo "{'vm_id':'" . $lst[$i] ."', 'vm_ip':'" . $lst[$i + 1] . "', 'vm_image':'" . $lst[$i + 2] 
        ."', 'create_time':'"  . $lst[$i + 3] . "'}";
      
      if ($i < $total - 4) {
        echo ",";
      }
    }
    
    echo "]}";
  
}

}


?>
