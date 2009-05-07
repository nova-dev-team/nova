<?php

class HadoopWizard {
  
  private $os;

  public function __construct($os) {
    $this->os = $os;
  }
  
  public function soft_list() {
    $core_reply = file_get_contents("http://localhost:3000/software/list");
    echo $core_reply;
  }
  
  public function create() {
    $email = $this->os->session->get_member_email();
    $core_reply = file_get_contents("http://localhost:3000/batch/create_and_add_to/" . $email . "?cname=" . $_REQUEST["vcluster_name"] ."&csize=" . $_REQUEST["vcluster_size"]);
    $cid = $core_reply;
    $soft_arr = split("\n", $_REQUEST["software_list"]);

    for ($i = 0; $i < sizeof($soft_arr); $i++) {
      $core_reply = file_get_contents("http://localhost:3000/batch/add_soft/" . $cid."/".$soft_arr[$i]);
    }
    
    // CHANGE Settings
    $core_reply = file_get_contents("http://localhost:3000/batch/change_setting/" . $cid."?item=vcpu&value=" . $_REQUEST["vcpu"]);

    $core_reply = file_get_contents("http://localhost:3000/batch/change_setting/" . $cid."?item=mem&value="  . $_REQUEST["mem_size"]);
    
    // image will be mapped
    $core_reply = file_get_contents("http://localhost:3000/batch/change_setting/" . $cid."?item=img&value=" . $_REQUEST["app_name"]);
    
//    echo "http://localhost:3000/batch/change_setting/" . $cid."?item=vcpu&value=4";
    // TODO add soft list
    // TODO start cluster
    echo "{success:true}";
  }
  
  
  public function progress() {
    // TODO: input = cid
    echo "{success:true}";
  }
  
}

?>

