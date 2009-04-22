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
    $email = $this->os->session->get_member_email();
    $core_reply = file_get_contents("http://localhost:3000/vcluster/create_and_add_to/" .   $email.'/'.  $_REQUEST['vcluster_name']);

    echo $core_reply;
  }
  
  public function removeCluster() {
    $email = $this->os->session->get_member_email();
    $core_reply = file_get_contents("http://localhost:3000/user/remove_vcluster/" . $email. '/' . $_REQUEST['vcluster_cid']);
    echo $core_reply;
  }
  
  
  public function newVM() {
    
    $core_reply = file_get_contents("http://localhost:3000/vcluster/add_new_vm/" . $_REQUEST['vcluster_cid']);

    echo $core_reply;
  }
  
  public function removeVM() {
    $core_reply = file_get_contents("http://localhost:3000/vcluster/remove_vmachine_ex/" . $_REQUEST['vm_vid']);
    echo $core_reply;
  }
  
  
    public function startVM() {
    echo "{success:true, msg:'start'}";
        // TODO
  }
  
  
  
    public function stopVM() {
    echo "{success:true, msg:'stop'}";
        // TODO
  }
  
  
    public function pauseVM() {
    echo "{success:true, msg:'pause'}";
        // TODO
  }
  
    public function resumeVM() {
    echo "{success:true, msg:'resume'}";
        // TODO
  }
  
    public function infoVM() {
    
    echo "{success:true, info:'TODO: Detail info of "   . $_REQUEST['vm_id']  .".'}"; // TODO

  }


public function listVM() {
    
    $core_reply = file_get_contents("http://localhost:3000/vcluster/info_vm_list/" . $_REQUEST['cid']);
    //echo $core_reply;
    
    $lst = json_decode($core_reply);

    echo "{'success':true, 'all_vms':[";
    
    
    $total = sizeof($lst);
    for ($i = 0; $i < $total; $i+=4) {
      echo "{'vm_id':'" . $lst[$i] ."', 'vm_ip':'" . $lst[$i + 1] . "', 'vm_image':'" . $lst[$i + 2] 
        ."', 'status':'"  . $lst[$i + 3] . "'}";
      
      if ($i < $total - 4) {
        echo ",";
      }
    }
    
    echo "]}";
  
}

}


?>
