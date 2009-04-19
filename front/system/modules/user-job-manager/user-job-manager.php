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
    $id = $this->os->session->get_member_id();
    
    // XXX DUMMY CODE
    
    if ($_REQUEST["cid"] == "") {
  
    echo "{'success':true, 'all_clusters':[";
    
    $total = 6;
    for ($i = 0; $i < $total; $i++) {
      echo "{'cluster_id':'c" . $i ."', 'cluster_name':'1.2.3.4'}";
      
      if ($i < $total - 1) {
        echo ",";
      }
    }
    
    echo "]}";
  }

  }
  
  
  public function addCluster() {
  
  }
  
   public function removeCluster() {
   
   
}


public function listVM() {



  // XXX Dummy code  , will be removed
  if ($_REQUEST["cid"] == "") {
  
    echo "{'success':true, 'all_vms':[";
    
    $total = 10;
    for ($i = 0; $i < $total; $i++) {
      echo "{'vm_id':'v" . $i ."', 'vm_ip':'1.2.3.4', 'vm_image':'ubuntu', 'create_time':'yesterday-once-more'}";
      
      if ($i < $total - 1) {
        echo ",";
      }
    }
    
    echo "]}";
  } else {
  
    echo "{'success':true, 'all_vms':[";
    
    $total = 8;
    for ($i = 0; $i < $total; $i++) {
      echo "{'vm_id':'XXv" . $i ."', 'vm_ip':'1.2.asdfasdf3.4', 'vm_image':'ubuntu', 'create_time':'yesterday-once-more'}";
      
      if ($i < $total - 1) {
        echo ",";
      }
    }
    
    echo "]}";
}
  
}

}


?>
