#!/usr/bin/env python

import urllib2

def list_rubygems_org_versions(gem_name):
  page_url = "http://rubygems.org/gems/%s/versions" % gem_name
  html_text = urllib2.urlopen(page_url).read()

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
  html_text = urllib2.urlopen(page_url).read()

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

def rubygems_infer_dependency(ver_info, mode = "development"):
  gem_name, dep_op, gem_version = ver_info.split()
  if dep_op == "=":
    info = parse_rubygems_org_page(gem_name, gem_version, mode)
    print info
  elif dep_op == ">=" or dep_op == "~>":
    ver_list = list_rubygems_org_versions(gem_name)
    print ver_list

if __name__ == "__main__":
  rubygems_infer_dependency("rails = 3.0.1")
  rubygems_infer_dependency("warden = 1.0.1")
  rubygems_infer_dependency("devise = 1.1.3")
  rubygems_infer_dependency("bundler ~> 1.0.0")

