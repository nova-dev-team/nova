// js for Nova master webui

//
// Global helpers
//

function do_message(type, title, msg) {
  jQuery.noticeAdd({
    text: "<table><tr><td rowspan='2' valign='top'><img src='/images/" + type + ".png'></td><td class='message_title'><font color='white'><b>" + title + "</b></font></td></tr><tr><td class='message_body'><font color='white'>" + msg + "</font></td></tr></table>"
  });
}

//
// "Users" page
//

function load_user_list(page, page_size) {
  if ($("#user_table_container").html() == "") {
    html = "<table id='user_table' width='100%'>";
    html += "<tr class='row_type_0'><td><b>#</b></td><td><b>Login</b></td><td><b>Privilege</b></td><td><b>Activated</b></td><td><b>Name</b></td><td><b>Email</b></td></tr>";
    for (i = 0; i < page_size; i++) {
      html += "<tr id='user_table-r" + i + "' class='row_type_" + ((i + 1) % 2) + "'>";
      html += "<td id='user_table-r" + i + "-id'>&nbsp;</td>";
      html += "<td id='user_table-r" + i + "-login'>&nbsp;</td>";
      html += "<td id='user_table-r" + i + "-privilege'>&nbsp;</td>";
      html += "<td id='user_table-r" + i + "-activated'>&nbsp;</td>";
      html += "<td id='user_table-r" + i + "-name'>&nbsp;</td>";
      html += "<td id='user_table-r" + i + "-email'>&nbsp;</td>";
      html += "</tr>";
    }
    html += "</table>";
    html += "<div id='user_table_pager'></div>";
    $("#user_table_container").html(html);
  }
  $("#user_table_container").block();
  $.ajax({
    url: "/users/list",
    type: "POST",
    dataType: "json",
    data: {
      page: page,
      page_size: page_size
    },
    success: function(result) {
      $("#user_table_container").unblock();
      if (result.success) {
        pages_total = result.pages_total;
        html = "<b>Pages:</b> "
        for (i = 1; i <= pages_total; i++) {
          if (i == page) {
            html += " " + i + " ";
          } else {
            html += " <a href='#' onclick='load_user_list(" + i + ", " + page_size + ")'>" + i + "</a> ";
          }
        }
        html += "&nbsp;&nbsp;<b><a href='#' onclick='load_user_list(" + page + "," + page_size + ")'>Refresh</a></b>"
        $("#user_table_pager").html(html);
        for (i = 0; i < result.users.length; i++) {
          u = result.users[i];
          $("#user_table-r" + i).show();
          $("#user_table-r" + i + "-id").html(u.id);
          $("#user_table-r" + i + "-login").html(u.login);
          $("#user_table-r" + i + "-name").html(u.name);
          $("#user_table-r" + i + "-email").html(u.email);
          $("#user_table-r" + i + "-privilege").html(u.privilege);

          if (u.activated) {
            html = "<input name='user_table-user-" + u.login + "-activated' " + (u.login == g_current_user ? "disabled" : "") + " type='checkbox' checked onclick='user_set_activated(\"" + u.login + "\")'/>";
          } else {
            html = "<input name='user_table-user-" + u.login + "-activated' " + (u.login == g_current_user ? "disabled" : "") + " type='checkbox' unchecked onclick='user_set_activated(\"" + u.login + "\")'/>";
          }
          $("#user_table-r" + i + "-activated").html(html);
        }
        while (i < page_size) {
          $("#user_table-r" + i).hide();
          i++;
        }
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#user_table_container").unblock();
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}


function user_set_activated(user_login) {
  activated = $("input[name='user_table-user-" + user_login + "-activated']").attr("checked");
  $.ajax({
    url: "/users/edit",
    type: "POST",
    dataType: "json",
    data: {
      login: user_login,
      activated: activated
    },
    success: function(result) {
      if (result.success) {
        do_message("success", activated ? "User activated" : "User deactivated", result.message);
      } else {
        do_message("failure", "Activate failed", result.message);
        $("input[name='user_table-user-" + user_login + "-activated']").attr("checked", !activated);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
      $("input[name='user_table-user-" + user_login + "-activated']").attr("checked", !activated);
    }
  });
}


//
// "System" page
//

function system_load_settings(setting_count) {
  if ($("#system_settings_container").html() == "") {
    html = "<table id='system_settings' width='100%'>";
    html += "<tr class='row_type_0'><td><b>Key</b></td><td><b>Value</b> (click to edit)</td><td><b>For worker?</b></td></tr>";
    for (i = 0; i < setting_count; i++) {
      html += "<tr id='system_settings-r" + i + "' class='row_type_" + ((i + 1) % 2) + "'>";
      html += "<td id='system_settings-r" + i + "-key'>&nbsp;</td>";
      html += "<td id='system_settings-r" + i + "-value'>&nbsp;</td>";
      html += "<td id='system_settings-r" + i + "-for_worker'>&nbsp;</td>";
      html += "</tr>";
    }
    html += "</table>";
    $("#system_settings_container").html(html);
  }
  $("#system_settings_container").block();
  $.ajax({
    url: "/settings/index",
    type: "POST",
    async: false,
    dataType: "json",
    data: {
      items: "key,value,editable,for_worker"
    },
    success: function(result) {
      $("#system_settings_container").unblock();
      if (result.success) {
        for (i = 0; i < result.data.length; i++) {
          s = result.data[i];
          $("#system_settings-r" + i + "-key").html(s.key);

          if (s.value != null) {
            s_value_html = s.value;
          } else {
            s_value_html = "<i>(none)</i>";
          }

          if (s.editable) {
            html = "<a href='#' onclick='system_setting_edit(\"" + s.key + "\")'><div id='system_settings-key-" + s.key + "'>" + s_value_html + "</div></a>"
            $("#system_settings-r" + i + "-value").html(html);
          } else {
            $("#system_settings-r" + i + "-value").html(s_value_html);
          }

          if (s.for_worker) {
            $("#system_settings-r" + i + "-for_worker").html("<input type='checkbox' disabled checked>");
          } else {
            $("#system_settings-r" + i + "-for_worker").html("<input type='checkbox' disabled unchecked>");
          }
        }
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#system_settings_container").unblock();
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}


function system_setting_edit(key) {
  old_value = $("#system_settings-key-" + key).html();
  if (old_value == "<i>(none)</i>") {
    old_value = "";
  }
  new_value = prompt("Please provide new value for '" + key + "'", old_value);
  if (new_value == null) {
    return;
  }
  $("#system_settings_container").block();
  $.ajax({
    url: "/settings/edit",
    type: "POST",
    dataType: "json",
    data: {
      key: key,
      value: new_value
    },
    success: function(result) {
      $("#system_settings_container").unblock();
      if (result.success) {
        $("#system_settings-key-" + key).html(new_value);
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#system_settings_container").unblock();
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function load_worker_machines() {
  html = "<table id='worker_machines' width='100%'>";
  html += "<tr class='row_type_0'><td><b>IP</b></td><td><b>Hostname</b></td><td><b>Status</b></td>";
  html += "<td><b>VM capacity</b> (click to change)</td><td><b>VM preparing</b></td><td><b>VM running</b></td><td><b>VM failure</b></td>";
  html += "<td><b>Actions</b></td></tr>";
  html += "</table>";
  $("#worker_machines_container").html(html);
  $("#worker_machines_container").block();
  $.ajax({
    url: "/pmachines/list.json",
    type: "POST",
    async: false,
    dataType: "json",
    success: function(result) {
      $("#worker_machines_container").unblock();
      if (result.success) {
        for (i = 0; i < result.data.length; i++) {
          ip = result.data[i].ip;
          hostname = result.data[i].hostname;
          pm_status = result.data[i].status;
          vm_capacity = result.data[i].vm_capacity;
          vm_failure = result.data[i].vm_failure;
          vm_preparing = result.data[i].vm_preparing;
          vm_running = result.data[i].vm_running;

          row_id = "worker_machines_rid_" + i;
          html = "<tr class='row_type_" + ((i + 1) % 2) + "' id='" + row_id + "'>";
          html += "<td>" + ip + "</td>";
          if (hostname == null) {
            html += "<td><i>(Unknown)</i></td>";
          } else {
            html += "<td>" + hostname + "</td>";
          }

          html += "<td>";
          if (pm_status == "failure") {
            html += "<b><font color='red'>failure</font></b>";
          } else if (pm_status == "retired") {
            html += "<font color='gray'>retired</font>";
          } else {
            html += pm_status;
          }
          html += "</td>";

          html += "<td><a href='#' onclick='change_worker_capacity(\"" + ip + "\", \"" + row_id + "-capacity\")' id='" + row_id + "-capacity'>" + vm_capacity + "</a></td>";
          html += "<td>" + vm_preparing + "</td>";
          html += "<td>" + vm_running + "</td>";
          html += "<td>" + vm_failure + "</td>";

          html += "<td>"
          if (pm_status == "pending") {
            html += "&nbsp";
          } else if (pm_status == "working") {
            html += "<button type='button' class='btn' onclick='retire_worker_machine(\"" + ip + "\")'><span><span>retire</span></span></button>";
          } else if (pm_status == "retired") {
            html += "<button type='button' class='btn' onclick='reconnect_worker_machine(\"" + ip + "\")'><span><span>reuse</span></span></button> ";
            html += "<button type='button' class='btn' onclick='delete_worker_machine(\"" + ip + "\")'><span><span><font color='red'>delete</font></span></span></button>";
          } else if (pm_status == "failure") {
            html += "<button type='button' class='btn' onclick='reconnect_worker_machine(\"" + ip + "\")'><span><span>reconnect</span></span></button> ";
            html += "<button type='button' class='btn' onclick='delete_worker_machine(\"" + ip + "\")'><span><span><font color='red'>delete</font></span></span></button>";
          }
          html += "</td></tr>";

          $("#worker_machines > tbody:last").append(html);
        }
      } else {
        do_message("failure", "Error occurred!", result.message);
      }
    },
    error: function() {
      $("#worker_machines_container").unblock();
      do_message("failure", "Request failed (worker machines)", "Please check your network connection!");
    }
  });
}

function retire_worker_machine(ip) {
  $("#worker_machines_container").block();
  $.ajax({
    async: false,
    url: "/pmachines/retire",
    type: "POST",
    dataType: "json",
    data: {
      ip: ip
    },
    success: function(result) {
      $("#worker_machines_container").unblock();
      if (result.success) {
        load_worker_machines();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#worker_machines_container").unblock();
      do_message("failure", "Request failed (retire worker)", "Please check your network connection!");
    }
  });
}

function delete_worker_machine(ip) {
  $("#worker_machines_container").block();
  $.ajax({
    async: false,
    url: "/pmachines/delete",
    type: "POST",
    dataType: "json",
    data: {
      ip: ip
    },
    success: function(result) {
      $("#worker_machines_container").unblock();
      if (result.success) {
        load_worker_machines();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#worker_machines_container").unblock();
      do_message("failure", "Request failed (delete worker)", "Please check your network connection!");
    }
  });
}

function reconnect_worker_machine(ip) {
  $("#worker_machines_container").block();
  $.ajax({
    async: false,
    url: "/pmachines/reconnect",
    type: "POST",
    dataType: "json",
    data: {
      ip: ip
    },
    success: function(result) {
      $("#worker_machines_container").unblock();
      if (result.success) {
        load_worker_machines();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#worker_machines_container").unblock();
      do_message("failure", "Request failed (reconnect worker)", "Please check your network connection!");
    }
  });
}

function change_worker_capacity(ip, elem_id) {
  old_val = $("#" + elem_id).html();
  new_val = prompt("Please provide new capacity:");
  $.ajax({
    url: "/pmachines/edit_capacity",
    type: "POST",
    dataType: "json",
    data: {
      ip: ip,
      vm_capacity: new_val
    },
    success: function(result) {
      if (result.success) {
        $("#" + elem_id).html(new_val);
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed (edit capacity)", "Please check your network connection!");
    }
  });
}

function pmachine_add() {
  var pm_ip = $("#new_worker_machine_ip").val();
  var vm_cap = $("#new_worker_machine_capacity").val();
  if (pm_ip == "" || pm_ip == null) {
    alert("Please provide 'Worker IP'!");
    return;
  } else if (vm_cap == "" || vm_cap == null) {
    alert("Please provide 'VM capacity'!");
    return;
  }
  $.ajax({
    url: "/pmachines/add",
    type: "POST",
    dataType: "json",
    data: {
      vm_capacity: vm_cap,
      ip: pm_ip
    },
    success: function(result) {
      if (result.success) {
        do_message("success", "Added new woker", result.message);
        load_worker_machines();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed (add new worker)", "Please check your network connection!");
    }
  });
}


function port_mapping_load() {
  html = "<table id='port_mappings' width='100%'>";
  html += "<tr class='row_type_0'><td><b>Proxy port</b> (click to delete)</td><td><b>Destination IP</b></td><td><b>Destination port</b></td></tr>";
  html += "</table>";
  $("#port_mappings_container").html(html);
  $("#port_mappings_container").block();
  $.ajax({
    url: "/misc/list_port_mapping",
    type: "POST",
    async: false,
    dataType: "json",
    success: function(result) {
      $("#port_mappings_container").unblock();
      if (result.success) {
        for (i = 0; i < result.data.length; i++) {
          ip = result.data[i].ip;
          dest_port = result.data[i].port;
          proxy_port = result.data[i].local_port;
          row_id = "port_mapping_rid_" + i;
          html = "<tr class='row_type_" + ((i + 1) % 2) + "' id='" + row_id + "'>";
          html += "<td><a href='#' onclick='del_port_mapping(\"" + ip + "\", \"" + dest_port + "\", \"" + row_id + "\")'>" + proxy_port + "</a></td>";
          html += "<td>" + ip + "</td>";
          html += "<td>" + dest_port + "</td>";
          html += "</tr>";
          $("#port_mappings > tbody:last").append(html);
        }
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#port_mappings_container").unblock();
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function del_port_mapping(dest_ip, dest_port, row_id) {
  $("#" + row_id).hide();
  $.ajax({
    url: "/misc/del_port_mapping",
    type: "POST",
    dataType: "json",
    data: {
      ip: dest_ip,
      port: dest_port
    },
    success: function(result) {
      if (result.success) {
        // do nothing
      } else {
        $("#" + row_id).show();
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function add_port_mapping() {
  var proxy_port = $("#port_mapping_proxy_port").val();
  var dest_port = $("#port_mapping_dest_port").val();
  var dest_ip = $("#port_mapping_ip").val();
  $.ajax({
    url: "/misc/add_port_mapping",
    type: "POST",
    dataType: "json",
    data: {
      ip: dest_ip,
      port: dest_port,
      local_port: proxy_port
    },
    success: function(result) {
      if (result.success) {
        port_mapping_load();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}


function load_software_packages() {
  html = "<table id='software_packages' width='100%'>";
  html += "<tr class='row_type_0'><td><b>Display name</b></td><td><b>File name</b></td><td><b>OS family</b></td><td><b>Description</b></td><td><b>Actions</b></td></tr>";
  html += "</table>";
  $("#software_packages_container").html(html);
  $("#software_packages").block();
  $.ajax({
    url: "/softwares/list",
    type: "POST",
    async: false,
    dataType: "json",
    success: function(result) {
      $("#software_packages").unblock();
      if (result.success) {
        for (i = 0; i < result.data.length; i++) {
          display_name = result.data[i].display_name;
          file_name = result.data[i].file_name;
          desc = result.data[i].description;
          if (desc == null) {
            desc = "";
          }
          os_family = result.data[i].os_family;

          row_id = "software_packages_rid_" + i;
          html = "<tr class='row_type_" + ((i + 1) % 2) + "' id='" + row_id + "'>";
          html += "<td>" + display_name + "</td>";
          html += "<td>" + file_name + "</td>";
          html += "<td>" + os_family + "</td>";
          html += "<td>" + desc + "</td>";
          html += "<td><button type='button' class='btn' onclick='soft_apply_to_vdisk(\"" + file_name + "\")'><span><span>Apply to Vdisks</span></span></button>&nbsp;<button type='button' class='btn' onclick='soft_delete(\"" + file_name + "\")'><span><span><font color='red'>Delete!</font></span></span></button></td>";
          html += "</tr>";
          $("#software_packages > tbody:last").append(html);
        }
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#software_packages").unblock();
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function soft_delete(soft_fname) {
  $.ajax({
    url: "/softwares/remove",
    type: "POST",
    async: false,
    dataType: "json",
    data: {
      file_name: soft_fname,
    },
    success: function(result) {
      if (result.success) {
        do_message("success", "Request successful", result.message);
        load_vdisk_images();
        load_software_packages();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function soft_apply_to_vdisk(soft_fname) {
  $.ajax({
    url: "/softwares/apply_to_vdisks",
    type: "POST",
    async: false,
    dataType: "json",
    data: {
      file_name: soft_fname,
    },
    success: function(result) {
      if (result.success) {
        do_message("success", "Request successful", result.message);
        load_vdisk_images();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function register_new_software() {
  disp_name = $("#new_soft_display").val();
  fname = $("#new_soft_fname").val();
  os_family = $("#new_soft_os").val();
  if (os_family == "none") {
    os_family = "";
  }
  desc = $("#new_soft_desc").val();
  $.ajax({
    url: "/softwares/register",
    type: "POST",
    async: false,
    dataType: "json",
    data: {
      display_name: disp_name,
      file_name: fname,
      os_family: os_family,
      description: desc
    },
    success: function(result) {
      if (result.success) {
        load_software_packages();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function white_spacing(count) {
  space = "";
  for (var j = 0; j < count; j++) {
    space += "&nbsp;";
  }
  return space;
}

function load_vdisk_images() {
  html = "<table id='vdisk_images_table' width='100%'><tr></tr>";
  html += "</table>";
  $("#vdisk_images_container").html(html);
  $("#vdisk_images_container").block();
  $.ajax({
    url: "/vdisks/list",
    type: "POST",
    async: false,
    dataType: "json",
    success: function(result) {
      $("#vdisk_images_container").unblock();
      if (result.success) {
        for (var i = 0; i < result.data.length; i++) {
          display_name = result.data[i].display_name;
          file_name = result.data[i].file_name;
          desc = result.data[i].description;
          if (desc == null) {
            desc = "";
          }
          img_format = result.data[i].disk_format;
          os_family = result.data[i].os_family;
          if (os_family == null) {
            os_family = "";
          }
          os_name = result.data[i].os_name;
          if (os_name == null) {
            os_name = "";
          }
          soft_list = result.data[i].soft_list;
          if (soft_list == null) {
            soft_list = "";
          }

          row_id = "vdisk_image_rid_" + i;
          html = "<tr class='row_type_" + (i % 2) + "'>";
          html += "<td>";

          html += "Display name: <b>" + display_name + "</b>" + white_spacing(5);
          html += "File name: <b>" + file_name + "</b>" + white_spacing(5);
          html += "Image format: <b>" + img_format + "</b>" + white_spacing(5);
          html += "OS family: <b>" + os_family + "</b>" + white_spacing(5);
          html += "OS name: <b>" + os_name + "</b>" + white_spacing(5) + "<br/>";
          html += "Description: " + desc + "<br/>";
          html += "Software packages: <input type='text' size=90 value='" + soft_list + "' id='" + row_id + "_soft_list'/>";
          html += white_spacing(3) + "<button type='button' class='btn' onclick='edit_vdisk_soft_list(\"" + file_name + "\", \"" + row_id + "\")'><span><span>Update</span></span></button> &nbsp;&nbsp; (separate with comma)";

          html += white_spacing(5) + "<a href='#' onclick='remove_vdisk(\"" + file_name + "\")'><font color='red'>Delete vdisk!</font></a>"

          html += "<br/>";
          html += "</td>";
          html += "</tr>";
          $("#vdisk_images_table > tbody:last").append(html);
        }
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      $("#vdisk_images_container").unblock();
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function register_new_vdisk() {
  var file_name = $("#new_vdisk_fname").val();
  var display_name = $("#new_vdisk_display").val();
  var format = $("#new_vdisk_format").val();
  var os_name = $("#new_vdisk_os_name").val();
  var os_family = $("#new_vdisk_os_family").val();
  if (os_family == "none") {
    os_family = null;
  }
  var desc = $("#new_vdisk_desc").val();

  $.ajax({
    url: "/vdisks/register.json",
    type: "POST",
    async: false,
    dataType: "json",
    data: {
      file_name: file_name,
      display_name: display_name,
      disk_format: format,
      os_name: os_name,
      os_family: os_family,
      description: desc
    },
    success: function(result) {
      if (result.success) {
        load_vdisk_images();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function remove_vdisk(vdisk_fname) {
  $.ajax({
    url: "/vdisks/remove",
    type: "POST",
    async: false,
    dataType: "json",
    data: {
      file_name: vdisk_fname,
    },
    success: function(result) {
      if (result.success) {
        location.reload();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function edit_vdisk_soft_list(vdisk_fname, row_id) {
  var soft_list = $("#" + row_id + "_soft_list").val();
  $.ajax({
    url: "/vdisks/edit_soft_list",
    type: "POST",
    async: false,
    dataType: "json",
    data: {
      file_name: vdisk_fname,
      soft_list: soft_list
    },
    success: function(result) {
      if (result.success) {
        load_software_packages();
      } else {
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

//
// "Account info" page
//

function user_update_info() {
  var name = $("#user_fullname").val();
  var email = $("#user_email").val();
  var email_regex = /[a-z0-9\._]+@[a-z0-9\._]+/;
  if (email == null || email_regex.test(email) == false) {
    do_message("failure", "Update failed", "You must provide a valid email address!");
    return;
  }
  if (name == null) {
    name = "";
  }
  $.ajax({
    url: "/users/edit",
    type: "POST",
    dataType: "json",
    data: {
      login: g_current_user,
      name: name,
      email: email
    },
    success: function(result) {
      if (result.success) {
        do_message("success", "Update done", result.message);
      } else {
        do_message("failure", "Update failed", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

function user_reset_password() {
  var old_password = $("#user_old_password").val();
  var new_password = $("#user_new_password").val();
  var new_password_confirm = $("#user_new_password_confirm").val();
  $.ajax({
    url: "/users/edit",
    type: "POST",
    dataType: "json",
    data: {
      login: g_current_user,
      old_password: old_password,
      new_password: new_password,
      new_password_confirm: new_password_confirm
    },
    success: function(result) {
      if (result.success) {
        do_message("success", "Reset done", result.message);
      } else {
        do_message("failure", "Reset failed", result.message);
      }
    },
    error: function() {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

