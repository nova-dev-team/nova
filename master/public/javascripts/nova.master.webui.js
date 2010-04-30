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
    html = "<table id='user_table' width='100%'>"
    html += "<tr class='row_type_0'><td><b>#</b></td><td><b>Login</b></td><td><b>Privilege</b></td><td><b>Activated</b></td><td><b>Name</b></td><td><b>Email</b></td></tr>"
    for (i = 0; i < page_size; i++) {
      html += "<tr id='user_table-r" + i + "' class='row_type_" + ((i + 1) % 2) + "'>"
      html += "<td id='user_table-r" + i + "-id'>&nbsp;</td>"
      html += "<td id='user_table-r" + i + "-login'>&nbsp;</td>"
      html += "<td id='user_table-r" + i + "-privilege'>&nbsp;</td>"
      html += "<td id='user_table-r" + i + "-activated'>&nbsp;</td>"
      html += "<td id='user_table-r" + i + "-name'>&nbsp;</td>"
      html += "<td id='user_table-r" + i + "-email'>&nbsp;</td>"
      html += "</tr>"
    }
    html += "</table>"
    html += "<div id='user_table_pager'></div>"
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
        $("#user_table_container").unblock();
        do_message("failure", "Error occurred", result.message);
      }
    },
    error: function(result) {
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
    error: function(result) {
      do_message("failure", "Request failed", "Please check your network connection!");
      $("input[name='user_table-user-" + user_login + "-activated']").attr("checked", !activated);
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
    error: function(result) {
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
    error: function(result) {
      do_message("failure", "Request failed", "Please check your network connection!");
    }
  });
}

