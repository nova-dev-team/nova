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


function add_pmachine() {
  var ip_addr = $("#new_pm_ip").val();
  var pool_size = $("#new_pm_pool_size").val();

  // check args
  if (is_valid_ip(ip_addr) == false) {
    alert("Please provide a valid IP address!");
    return;
  }
  num_regex = /^[0-9]+$/;
  if (num_regex.test(pool_size) == false) {
    alert("Invalid pool size!");
    return;
  }
  $("#add_new_pmachine_div").block();

  $.ajax({
    url: "/pmachines/add.json",
    type: "POST",
    dataType: "json",
    data: {
      ip: ip_addr,
      pool_size: pool_size
    },
    success: function(result) {
      $("#add_new_pmachine_div").unblock();
      if (result.success) {
        window.location.reload();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      alert("Request failed!");
      $("#add_new_pmachine_div").unblock();
    }
  });
  
}

function pm_ajax(url, uuid) {
  $.ajax({
    url: url,
    type: "POST",
    dataType: "json",
    data: {
      ip: uuid
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

function delete_pmachine(ip_addr, uuid) {
  if (confirm("Are you sure to delete pmachine with IP=" + ip_addr + "?")) {
    pm_ajax("/pmachines/delete.json", ip_addr);
  }
}

function reuse_pmachine(ip_addr) {
  pm_ajax("/pmachines/reuse.json", ip_addr);
}

function retire_pmachine(ip_addr) {
  pm_ajax("/pmachines/retire.json", ip_addr);
}

function reconnect_pmachine(ip_addr) {
  pm_ajax("/pmachines/reconnect.json", ip_addr);
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

