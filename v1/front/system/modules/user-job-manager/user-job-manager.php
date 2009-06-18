<?php

class UserJobManager {

  private $os;

  public function __construct($os) {
    $this->os = $os;
  }

  public function dummyTest() {
    echo "{success:true, msg:'haha->Member id : " . $this->os->session->get_member_id() ."!!!'}";
  }


  public function listCluster() {
  
    $email = $this->os->session->get_member_email();

    $core_reply = file_get_contents("http://localhost:3000/user/info_vclusters/" . $email);

    $lst = json_decode($core_reply);

    echo "{'success':true, 'all_clusters':[";

    $total = sizeof($lst);
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
  
    $add_opt = "mem=" . $_REQUEST['mem'] . "&img=" . $_REQUEST['img'] . "&vcpu=" . $_REQUEST['vcpu'];
    if ($add_opt == "")
      $core_reply = file_get_contents("http://localhost:3000/vcluster/add_new_vm/" . $_REQUEST['vcluster_cid']);
    else
      $core_reply = file_get_contents("http://localhost:3000/vcluster/add_new_vm/" . $_REQUEST['vcluster_cid'] . "?" . $add_opt);

    echo $core_reply;
  }
  
  public function removeVM() {
    $core_reply = file_get_contents("http://localhost:3000/vcluster/remove_vmachine_ex/" . $_REQUEST['vm_vid']);
    echo $core_reply;
  }
  
  
  public function startVM() {
    $core_reply = file_get_contents("http://localhost:3000/vmachine/start/" . $_REQUEST['vm_vid']);
    echo $core_reply;
  }
  
  
  
  public function stopVM() {
    $core_reply = file_get_contents("http://localhost:3000/vmachine/stop/" . $_REQUEST['vm_vid']);
    echo $core_reply;
  }
  
  
  public function pauseVM() {
    $core_reply = file_get_contents("http://localhost:3000/vmachine/suspend/" . $_REQUEST['vm_vid']);
    echo $core_reply;
  }
  
  public function resumeVM() {
    $core_reply = file_get_contents("http://localhost:3000/vmachine/resume/" . $_REQUEST['vm_vid']);
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

  public function listImage() {
    $core_reply = file_get_contents("http://localhost:3000/vimage/short_list/");
    $lst = json_decode($core_reply);

    echo "{'success':true, 'imglist':[";

//    echo "{success:true, imglist:['hadoop-slave.img', 'intrepid2.img', 'f8.img']}";
    $total = sizeof($lst);
    for ($i = 0; $i < $total; $i++) {
      echo "'" . $lst[$i] . "'";
      if ($i < $total - 1) {
        echo ",";
      }
    }
    
    echo "]}";
  }

}


?>
