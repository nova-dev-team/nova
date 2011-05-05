function disable(item_name) {
  $("#" + item_name).attr("disabled", true);
  $("#" + item_name).addClass("disabled_input");
}

function enable(item_name) {
  $("#" + item_name).attr("disabled", false);
  $("#" + item_name).removeClass("disabled_input");
}

function toggle_mount_iso() {
  var iso_chk = $("input[name=chk_mount_iso]");
  var agent_chk = $("input[name=chk_agent_cd]");
  if (iso_chk.is(":checked") == true) {
    agent_chk.attr("checked", false);
    enable("new_vm_cd_image");
    disable("new_vm_ip_addr");
    disable("new_vm_subnet_mask");
    disable("new_vm_gateway");
    disable("new_vm_dns");
    disable("new_vm_agent_packages");
    disable("new_vm_nodelist");
    disable("new_vm_cluster_name");
  } else {
    disable("new_vm_cd_image");
  }
}
function toggle_kernel_options() {
  var kernel_chk = $("input[name=chk_kernel_options]");
  if (kernel_chk.is(":checked")  == true) {
    enable("new_vm_kernel");
    enable("new_vm_initrd");
    enable("new_vm_hda_dev");
  } else {
    disable("new_vm_kernel");
    disable("new_vm_initrd");
    disable("new_vm_hda_dev");
  }
}
function toggle_agent_cd() {
  var iso_chk = $("input[name=chk_mount_iso]");
  var agent_chk = $("input[name=chk_agent_cd]");
  if (agent_chk.is(":checked") == true) {
    iso_chk.attr("checked", false);
    enable("new_vm_ip_addr");
    enable("new_vm_subnet_mask");
    enable("new_vm_gateway");
    enable("new_vm_dns");
    enable("new_vm_agent_packages");
    enable("new_vm_nodelist");
    enable("new_vm_cluster_name");
    disable("new_vm_cd_image");
  } else {
    disable("new_vm_ip_addr");
    disable("new_vm_subnet_mask");
    disable("new_vm_gateway");
    disable("new_vm_dns");
    disable("new_vm_agent_packages");
    disable("new_vm_nodelist");
    disable("new_vm_cluster_name");
  }
}

function is_valid_ip(ip) {
  if (ip == null) {
    return false;
  }
  var dot_count = 0;
  var seg_count = 0;
  for (i = 0; i < ip.length; i++) {
    var ch = ip[i];
    if ('0' <= ch && ch <= '9') {
      seg_count++;
    } else if (ch == '.') {
      if (seg_count == 0) {
        return false;
      }
      dot_count++;
      seg_count = 0;
    } else {
      return false;
    }
    if (dot_count > 3 || seg_count > 3) {
      return false;
    }
  }
  if (dot_count != 3) {
    return false;
  }
  return true;
}


function add_vmachine() {
  var cpu_count = $("#new_vm_cpu_count").val();
  var mem_size = $("#new_vm_mem_size").val();
  var name = $("#new_vm_name").val();
  var hypervisor = $("#new_vm_hypervisor").val();
  
  var use_hvm = "";
  if (hypervisor == "xen") {
    if ($("#chk_use_hvm").is(":checked") == true) {
      use_hvm = true;
    } else {
      use_hvm = false;
    }
  }

  var arch = $("#new_vm_arch").val();
  var hda_image = $("#new_vm_hda_image").val();
  var hda_save_to = $("#new_vm_hda_save_to").val();
  var run_agent = $("input[name=chk_agent_cd]").is(":checked");
  var mount_iso = $("input[name=chk_mount_iso]").is(":checked");
  
  var ex_kernel = $("input[name=chk_kernel_options]").is(":checked");

  var agent_hint = "";    // ip, subnet mask, gateway, dns, packages, nodelist, etc...
  var uuid = $("#new_vm_uuid").val();
  var cd_image = $("#new_vm_cd_image").val();
  
  var kernel = "";
  var initrd = "";
  var hda_dev = "";

  // check args
  if (hda_image == null || hda_image == "") {
    alert("You must pick an hda image!");
    return;
  }
  if (mem_size == null || mem_size == "") {
    alert("You must provide memory size!");
    return;
  }
  mem_size_regex = /^[0-9]+$/;
  if (mem_size_regex.test(mem_size) == false) {
    alert("Invalid memory size!");
    return;
  }
  vmachine_name_regex = /^([a-z])+([a-z0-9_\.\-])*$/;
  if (name == null || name == "" || vmachine_name_regex.test(name) == false) {
    alert("Invalid vmachine name!");
    return;
  }
  if (name.length > 20) {
    alert("Vmachine name too long! The limit is 20 characters.");
    return;
  }
  uuid = uuid.toLowerCase();
  uuid_regex = /^([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$/
  if (uuid == null || uuid == "" || uuid_regex.test(uuid) == false) {
    alert("Invalid UUID!");
    return;
  }

  if (ex_kernel == true) {
    kernel = $("#new_vm_kernel").val();
    initrd = $("#new_vm_initrd").val();
    hda_dev = $("#new_vm_hda_dev").val();
  }

  if (mount_iso == true) {
    if (cd_image == null || cd_image == "") {
      alert("Please provide cdrom image, if you want to mount .iso files!");
      return;
    }
  } else if (run_agent == true) {
    var ip_addr = $("#new_vm_ip_addr").val();
    var subnet_mask = $("#new_vm_subnet_mask").val();
    var gateway = $("#new_vm_gateway").val();
    var dns = $("#new_vm_dns").val();
    var agent_packages = $("#new_vm_agent_packages").val();
    var nodelist = $("#new_vm_nodelist").val();
    var cluster_name = $("#new_vm_cluster_name").val();
    
    if (is_valid_ip(ip_addr) == false) {
      alert("Invalid IP address!");
      return;
    }
    agent_hint += "ip=" + ip_addr + "\n";

    if (is_valid_ip(subnet_mask) == false) {
      alert("Invalid subnet mask!");
      return;
    }
    agent_hint += "subnet_mask=" + subnet_mask + "\n";

    if (is_valid_ip(gateway) == false) {
      alert("Invalid gateway!");
      return;
    }
    agent_hint += "gateway=" + gateway + "\n";

    if (dns != null && dns != "") {
      if (is_valid_ip(dns) == false) {
        alert("Invalid DNS!");
        return;
      }
      agent_hint += "dns=" + dns + "\n";
    }

    if (agent_packages != null) {
      agent_hint += "agent_packages=" + agent_packages + "\n";
    }

    if (nodelist != null && nodelist != "") {
      agent_hint += "nodelist=" + nodelist + "\n";
    }

    if (cluster_name != null && cluster_name != "") {
      agent_hint += "cluster_name=" + cluster_name + "\n";
    }
  }
  
  $("#add_new_vmachine_div").block();

  $.ajax({
    url: "/vmachines/start.json",
    type: "POST",
    dataType: "json",
    data: {
      cpu_count: cpu_count,
      mem_size: mem_size,
      name: name,
      hypervisor: hypervisor,
      arch: arch,
      hda_image: hda_image,
      hda_save_to: hda_save_to,
      run_agent: run_agent,
      mount_iso: mount_iso,
      agent_hint: agent_hint,
      uuid: uuid,
      cd_image: cd_image,
      kernel: kernel,
      initrd: initrd,
      hda_dev: hda_dev,
      use_hvm: use_hvm
    },
    success: function(result) {
      $("#add_new_vmachine_div").unblock();
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      alert("Request failed!");
      $("#add_new_vmachine_div").unblock();
    }
  });
  
}

function vm_ajax(url, uuid) {
  $.ajax({
    url: url,
    type: "POST",
    dataType: "json",
    data: {
      uuid: uuid
    },
    success: function(result) {
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      alert("Request failed!");
    }
  });
}

function vm_ajax2(url, name) {
  $.ajax({
    url: url,
    type: "POST",
    dataType: "json",
    data: {
      name: name
    },
    success: function(result) {
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      alert("Request failed!");
    }
  });
}

function restart_vmachine(name) {
  vm_ajax2("/vmachines/restart.json", name);
}

function destroy_vmachine(name) {
  if (confirm("Are you sure to destroy vmachine named '" + name + "'?")) {
    vm_ajax2("/vmachines/destroy.json", name);
  }
}

function resume_vmachine(name) {
  vm_ajax2("/vmachines/resume.json", name);
}

function suspend_vmachine(name) {
  vm_ajax2("/vmachines/suspend.json", name);
}

function power_off_vmachine(name) {
  vm_ajax2("/vmachines/power_off.json", name);
}

function change_system_setting(key) {
  old_value = $("#sys_setting_holder_" + key).html();
  new_value = prompt("Input the new value for key '" + key + "'.", old_value);
  if (new_value && new_value != "" && new_value != old_value) {
    $("#sys_settings_panel").block();
    $.ajax({
      url: "/settings/edit.json",
      type: "POST",
      dataType: "json",
      data: {
        key: key,
        value: new_value
      },
      success: function(result) {
        $("#sys_settings_panel").unblock();
        if (result.success) {
          $("#sys_setting_holder_" + key).html(new_value);
        } else {
          alert("Error message: " + result.message);
        }
      },
      error: function() {
        $("#sys_settings_panel").unblock();
        alert("Request failed!");
      }
    });
  }
}

function revoke_vm_image() {
  img_name = $("#revoke_vm_image_name").val();
  $.ajax({
    url: "/misc/revoke_vm_image.json",
    type: "POST",
    dataType: "json",
    data: {
      image_name: img_name
    },
    success: function(result) {
      $("#sys_settings_panel").unblock();
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      $("#sys_settings_panel").unblock();
      alert("Request failed!");
    }
  });
}

function revoke_vm_package() {
  pkg_name = $("#revoke_package_name").val();
  $.ajax({
    url: "/misc/revoke_package.json",
    type: "POST",
    dataType: "json",
    data: {
      package_name: pkg_name
    },
    success: function(result) {
      $("#sys_settings_panel").unblock();
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      $("#sys_settings_panel").unblock();
      alert("Request failed!");
    }
  });
}


function change_hda_save_to(vm_name, old_value) {
  new_value = prompt("Input the new hda_save_to value (input nothing to disable saving)", old_value);
  if (new_value == null || new_value == old_value) {
    return;
  }
  $.ajax({
    url: "/vmachines/change_hda_save_to.json",
    type: "POST",
    dataType: "json",
    data: {
      name: vm_name,
      hda_save_to: new_value
    },
    success: function(result) {
      $("#sys_settings_panel").unblock();
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      $("#sys_settings_panel").unblock();
      alert("Request failed!");
    }
  });
}

function add_package(vm_name) {
  pkg_list = prompt("Input software package list(separated by space)", "");
  if (pkg_list == null) {
    return;
  }
  $.ajax({
    url: "/vmachines/add_package.json",
    type: "POST",
    dataType: "json",
    data: {
      name: vm_name,
      pkg_list: pkg_list
    },
    success: function(result) {
      $("#sys_settings_panel").unblock();
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      $("#sys_settings_panel").unblock();
      alert("Request failed!");
    }
  });

}

function live_migrate_to(vm_name) {
  target_addr = prompt("Input destination worker's addr", "destintaion");
  if (target_addr == null) {
    return;
  }

  $.ajax({
    url: "/vmachines/live_migrate_to.json",
    type: "POST",
    dataType: "json",
    data: {
      name: vm_name,
      migrate_dest: target_addr
    },
    success: function(result) {
      $("#sys_settings_panel").unblock();
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      $("#sys_settings_panel").unblock();
      alert("Request failed!");
    }
  });
}


function hotbackup_to(vm_name) {
  target_addr = prompt("Input hotbackup slave worker's addr", "destintaion");
  if (target_addr == null) {
    return;
  }

  $.ajax({
    url: "/vmachines/hotbackup_to.json",
    type: "POST",
    dataType: "json",
    data: {
      name: vm_name,
      hotbackup_dest: target_addr
    },
    success: function(result) {
      $("#sys_settings_panel").unblock();
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      $("#sys_settings_panel").unblock();
      alert("Request failed!");
    }
  });
}