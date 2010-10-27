#!/usr/bin/env python

import urllib2

def version_to_array(ver_text):
  ver_arr = []
  splt_arr = []
  seg = ""
  mode = None
  for c in ver_text:
    if c.isdigit():
      if mode == "number":
        seg += c
      else:
        splt_arr += seg,
        seg = c
        mode = "number"
    else:
      if mode == "number":
        splt_arr += seg,
        seg = c
        mode = "text"
      else:
        seg += c
  splt_arr += seg,
  for seg in splt_arr:
    if len(seg) == 0:
      continue
    elif seg.isdigit():
      ver_arr += int(seg),
    else:
      ver_arr += seg,
  return ver_arr

def list_rubygems_org_versions(gem_name):
  page_url = "http://rubygems.org/gems/%s/versions" % gem_name
  html_text = ""
  try:
    html_text = urllib2.urlopen(page_url).read()
  except:
    print "[exception] %s" % page_url

  # parse and find the versions
  ver_list = []
  idx = html_text.index("<div class=\"versions\">")
  idx2 = html_text.index("</ol>")
  search_text = html_text[idx:idx2]
  idx = 0
  while True:
    idx = search_text.find("<a href=", idx)
    if idx == -1:
      break
    idx = search_text.find(">", idx) + 1
    idx2 = search_text.index("</a>", idx)
    version_text = search_text[idx:idx2]
    ver_list += version_text,
    idx = idx2
  return ver_list

def parse_rubygems_org_page(gem_name, gem_version = None, mode = "development"):
  info_hash = {}
  if gem_version != None:
    page_url = "http://rubygems.org/gems/%s/versions/%s" % (gem_name, gem_version)
  else:
    page_url = "http://rubygems.org/gems/%s" % gem_name
  html_text = ""
  try:
    html_text = urllib2.urlopen(page_url).read()
  except:
    print "[exception] %s" % page_url

  # parse download location
  idx = html_text.index("<div class=\"admin\">")
  idx = html_text.index("<a href=\"", idx)
  idx2 = html_text.index("\"", idx + 9)
  info_hash["gem_url"] = "http://rubygems.org" + html_text[(idx + 9):idx2]

  # parse runtime/development dependency
  def _parse_dependency(mode):
    info_hash["%s_dependency" % mode] = []
    idx = html_text.find("id=\"%s_dependencies\">" % mode)
    if idx != -1:
      idx2 = html_text.index("</ol>", idx)
      search_text = html_text[idx:idx2]
      idx = 0
      while True:
        idx = search_text.find("<strong>", idx)
        if idx == -1:
          break
        idx2 = search_text.index("</strong>", idx)
        depend_gem_name = search_text[(idx + 8):idx2]
        idx = search_text.index("</a>", idx2)
        splt = search_text[idx2:idx].split()
        depend_op = splt[-2]
        depend_gem_version = splt[-1]
        info_hash["%s_dependency" % mode] += depend_gem_name + " " + depend_op + " " + depend_gem_version,

  _parse_dependency("runtime")
  if mode == "development":
    _parse_dependency("development")
  return info_hash

def rubygems_infer_dependency(ver_info, mode = "development", visited_list = []):
  gem_name, dep_op, gem_version = ver_info.split()
  if dep_op == ">=" or dep_op == "~>":
    ver_list = list_rubygems_org_versions(gem_name)
    target_ver_arr = version_to_array(gem_version)
    usable_ver_list = []
    for ver in ver_list:
      ver_arr = version_to_array(ver)
      is_stable_release = True
      for c in ver:
        if c == "." or c.isdigit():
          pass
        else:
          is_stable_release = False
          break
      if ver_arr >= target_ver_arr and is_stable_release == True:
        usable_ver_list += ver,
    chosen_version = None
    if dep_op == ">=":
      gem_version = usable_ver_list[0]
    elif dep_op == "~>":
      gem_version = usable_ver_list[-1]


  def _package_visited(package):
    pkg_name = package.split()[0]
    for visited in visited_list:
      if visited.startswith(pkg_name):
        return True
    return False

  visited_list += "%s %s %s" % (gem_name, dep_op, gem_version),
  info = parse_rubygems_org_page(gem_name, gem_version, mode)
  print gem_name, gem_version, info["gem_url"]
#  print "runtime dependency:",
#  for dep in info["runtime_dependency"]:
#    print dep,
#  print
  for dep in info["runtime_dependency"]:
    if not _package_visited(dep):
      rubygems_infer_dependency(dep, mode, visited_list)
  if mode == "development":
#    print "development dependency:",
#    for dep in info["development_dependency"]:
#      print dep,
#    print
    for dep in info["development_dependency"]:
      if not _package_visited(dep):
        rubygems_infer_dependency(dep, mode, visited_list)

if __name__ == "__main__":
  mode = "runtime"
  visited_list = []
  rubygems_infer_dependency("rails = 3.0.1", mode, visited_list)
  rubygems_infer_dependency("mongrel >= 0", mode, visited_list)
  rubygems_infer_dependency("devise >= 0", mode, visited_list)
  rubygems_infer_dependency("rack >= 0", mode, visited_list)
  rubygems_infer_dependency("uuidtools >= 0", mode, visited_list)
  rubygems_infer_dependency("xml-simple >= 0", mode, visited_list)
  rubygems_infer_dependency("sqlite3-ruby >= 0", mode, visited_list)
  rubygems_infer_dependency("rest-client >= 0", mode, visited_list)
  rubygems_infer_dependency("daemons >= 0", mode, visited_list)
  rubygems_infer_dependency("ruby-libvirt >= 0", mode, visited_list)
  rubygems_infer_dependency("posixlock >= 0", mode, visited_list)

