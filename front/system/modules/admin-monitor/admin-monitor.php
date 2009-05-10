<?php

class AdminMonitor {

  private $os;

  public function __construct($os) {
    $this->os = $os;
  }

  public function dummyTest() {
    echo "{success:true, msg:'haha->Member id : " . $this->os->session->get_member_id() ."!!!'}";
  }


  public function listCluster() {
  
    $core_reply = file_get_contents("http://localhost:3000/user/info_vclusters_ad");

    $lst = json_decode($core_reply);

    echo "{'success':true, 'all_clusters':[";

    $total = sizeof($lst);
    for ($i = 0; $i < $total; $i += 3) {
      echo "{'cluster_id' :  '" . $lst[$i] . "', cluster_name:'" . $lst[$i + 1] ."', owner_email:'" . $lst[$i + 2]  ."'}";
      if ($i < $total - 3) {
        echo ',';
      }
    }
    echo "]}";

  }
  
  public function removeCluster() {
    //$email = $this->os->session->get_member_email();
    $core_reply = file_get_contents("http://localhost:3000/user/remove_vcluster/" . $_REQUEST['owner_email'] . '/' . $_REQUEST['vcluster_cid']);
    echo $core_reply;
  }
  
  
  
  public function removeVM() {
    $core_reply = file_get_contents("http://localhost:3000/vcluster/remove_vmachine_ex/" . $_REQUEST['vm_vid']);
    echo $core_reply;
  }
  
  
  
  public function infoVM() {
    $core_reply = file_get_contents("http://localhost:3000/vmachine/detail_info/" . $_REQUEST['vm_id'] );
    // TODO Pretty reply
    echo $core_reply;
  }
  
  public function listVM() {
    
    $core_reply = file_get_contents("http://localhost:3000/vcluster/info_vm_list/" . $_REQUEST['cid']);
    
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
