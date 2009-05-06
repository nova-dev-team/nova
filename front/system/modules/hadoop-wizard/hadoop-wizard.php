<?php

class HadoopWizard {
  
  private $os;

  public function __construct($os) {
    $this->os = $os;
  }
  
  
  public function create() {
    $email = $this->os->session->get_member_email();
    $core_reply = file_get_contents("http://localhost:3000/batch/create_and_add_to/" . $email . "?cname=" . $_REQUEST["vcluster_name"] ."&csize=" . $_REQUEST["vcluster_size"]);
    $cid = $core_reply;
    $soft_arr = split("\n", $_REQUEST["software_list"]);
    echo sizeof($soft_arr);
    for ($i = 0; $i < sizeof($soft_arr); $i++) {
      $core_reply = file_get_contents("http://localhost:3000/batch/add_soft/" . $cid."/".$soft_arr[$i]);
    }
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

