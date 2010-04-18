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
  } else {
    disable("new_vm_cd_image");
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
    disable("new_vm_cd_image");
  } else {
    disable("new_vm_ip_addr");
    disable("new_vm_subnet_mask");
    disable("new_vm_gateway");
    disable("new_vm_dns");
    disable("new_vm_agent_packages");
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
  var arch = $("#new_vm_arch").val();
  var hda_image = $("#new_vm_hda_image").val();
  var hda_save_to = $("#new_vm_hda_save_to").val();
  var run_agent = $("input[name=chk_agent_cd]").is(":checked");
  var mount_iso = $("input[name=chk_mount_iso]").is(":checked");
  var agent_hint = "";    // ip, subnet mask, gateway, dns, packages, etc...
  var uuid = $("#new_vm_uuid").val();
  var cd_image = $("#new_vm_cd_image").val();

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
      agent_hint += "agent_packages=" + agent_packages;
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
      cd_image: cd_image
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

function destroy_vmachine(name, uuid) {
  if (confirm("Are you sure to destroy vmachine named '" + name + "'?")) {
    vm_ajax("/vmachines/destroy.json", uuid);
  }
}

function resume_vmachine(uuid) {
  vm_ajax("/vmachines/resume.json", uuid);
}

function suspend_vmachine(uuid) {
  vm_ajax("/vmachines/suspend.json", uuid);
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

