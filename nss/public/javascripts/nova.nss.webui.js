
function list_filesystem(dir) {
  $("#fsdiv").block();
  $.ajax({
    url: "/fs/listdir",
    type: "POST",
    dataType: "json",
    data: {
      dir: dir
    },
    success: function(result) {
      if (result.success) {
        var html = "";
        html += "Current dir: <b>" + result.dir + "</b>, <a href='#' onclick='list_filesystem(\"" + result.dir + "/..\")'>Up one level</a><br/>";
        html += "<table width='100%'><tr><td>Filename</td><td>File size</td></tr>";
        var file_counter = 0;
        var dir_counter = 0;
        html += "<tr class='row_type_0'><td>";
        html += "<a href='#' onclick='list_filesystem(\"" + result.dir + "/..\")'>..</a>"
        html += "</td><td></td></tr>";
        for (i = 0; i < result.data.length; i++) {
          var fdata = result.data[i];
          html += "<tr class='row_type_" + ((i + 1)% 2) + "'>";
          html += "<td>";
          if (fdata.isdir) {
            html += "<a href='#' onclick='list_filesystem(\"" + result.dir + "/" + fdata.filename + "\")'>" + fdata.filename + "</a>";
            dir_counter += 1;
          } else {
            html += fdata.filename;
            file_counter += 1;
          }
          html += "</td>";
          html += "<td>" + fdata.fsize + "</td>";
          html += "</tr>";
        }
        html += "</table><br/>";
        html += dir_counter + " directories, " + file_counter + " files.<br/>";
        $("#fsdiv").html(html);
      } else {
        alert("Error message: " + result.message);
      }
      $("#fsdiv").unblock();
    },
    error: function() {
      $("#fsdiv").unblock();
      alert("Request failed!");
    }
  });
}

function load_vdisks() {
  $("#vdisks_div").block();
  $.ajax({
    url: "/vdisk_pool/list",
    type: "GET",
    dataType: "json",
    success: function(result) {
      if (result.success) {
        var html = "";
        html += "<table width='100%'><tr><td>Vdisk filename</td><td>Pool size</td><td>Actions</td></tr>";
        for (i = 0; i < result.data.length; i++) {
          var vd = result.data[i];
          html += "<tr class='row_type_" + (i % 2) + "'>";
          html += "<td>" + vd.basename + "</td>";
          html += "<td>" + vd.pool_size + "</td>";
          html += "<td>";
          html += "<button type='button' class='btn' onclick='edit_pool_size(\"" + vd.basename + "\")'><span><span>Edit pool size</span></span></button>";
          html += " &nbsp;&nbsp;";
          html += "<button type='button' class='btn' onclick='unregister_vdisk(\"" + vd.basename + "\")'><span><span><font color='red'>Unregister!</font></span></span></button>";
          html += "</td>";
          html += "</tr>";
        }
        html += "</table><br/>";
        html += result.data.length + " vdisks registered.<br/>";
        $("#vdisks_div").html(html);
      } else {
        alert("Error message: " + result.message);
      }
      $("#vdisks_div").unblock();
    },
    error: function() {
      $("#vdisks_div").unblock();
      alert("Request failed!");
    }
  });
}

function register_vdisk() {
  $.ajax({
    url: "/vdisk_pool/register",
    type: "POST",
    dataType: "json",
    data: {
      basename: $("#new_vdisk_name").val(),
      pool_size: $("#new_pool_size").val()
    },
    success: function(result) {
      if (result.success) {
        load_vdisks();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      alert("Request failed!");
    }
  });
}

function edit_pool_size(basename) {
  var pool_size = prompt("New pool size for vdisk '" + basename + "'?");
  if (pool_size != "" && pool_size != null) {
    $.ajax({
      url: "/vdisk_pool/edit",
      type: "POST",
      dataType: "json",
      data: {
        basename: basename,
        pool_size: pool_size
      },
      success: function(result) {
        if (result.success) {
          load_vdisks();
        } else {
          alert("Error message: " + result.message);
        }
      },
      error: function() {
        alert("Request failed!");
      }
    });
  }
}

function unregister_vdisk(basename) {
  $.ajax({
    url: "/vdisk_pool/unregister",
    type: "POST",
    dataType: "json",
    data: {
      basename: basename
    },
    success: function(result) {
      if (result.success) {
        load_vdisks();
      } else {
        alert("Error message: " + result.message);
      }
    },
    error: function() {
      alert("Request failed!");
    }
  });
}

