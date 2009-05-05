<?php

class HadoopWizard {
  
  private $os;

  public function __construct($os) {
    $this->os = $os;
  }
  
  
  public function create() {
    echo $_REQUEST["software_list"];
    // TODO send request to core
    echo "{success:true}";
  }
  
  
  public function progress() {
    // TODO: input = cid
    echo "{success:true}";
  }
  
}

?>

