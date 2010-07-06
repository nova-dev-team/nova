#!/usr/bin/ruby

# This is the style checker for Nova project.
# Santa Zhang (santa1987@gmail.com)

require 'find'

load File.join File.dirname(__FILE__), 'style-check.conf'

NOVA_SRC_ROOT = "../.."

# check if should skip some files
def should_skip? f
  if defined? IGNORE_DIR
    IGNORE_DIR.each do |d|
      if f.start_with? File.join(NOVA_SRC_ROOT, d)
        # puts "[ignore] #{f}"
        return true
      end
    end
  end
  false
end

def drop_ext f
  ext = File.extname(f)
  return f[0..-ext.length - 1]
end

# report warning, 'f' is file, 'msg' is warning message
def report_warning f, msg
  puts "[warning] #{f}: #{msg}"
end

# report error, 'f' is file, 'msg' is error message
def report_error f, msg
  puts "[error] #{f}: #{msg}"
  exit 1
end

# check if a file contains trailing whitespace, and shows warning
def warn_trailing_whitespace f
  File.open(f).each_line do |line|
    line = line.chomp
    if line.end_with? " "
      report_warning f, "contains trailing whitespace"
      break
    end
  end
end

# check if a file contains indents with tabs, and shows warning
def warn_tab_indents f
  File.open(f).each_line do |line|
    line = line.chomp
    if line.start_with? "\t"
      report_warning f, "contains tab indents"
      break
    end
  end
end

# extension for C++ files
CXX_EXT = [".cc", ".cxx", ".cpp"]

# check if C/C++ source code has either main() or has .h file
def c_check_has_main_or_header f
  header_f = drop_ext(f) + ".h"
  return true if File.exist? header_f
  File.open(f).each_line do |line|
    return true if line =~ /int main\(/
  end
  report_error f, "does not have .h file and main() function"
  return false
end

# check if .h files has corresponding source code file
def c_check_has_source_file f
  ext_list = CXX_EXT
  ext_list << ".c"
  ext_list.each do |ext|
    src_fn = drop_ext(f) + ext
    return true if File.exist? src_fn
  end
  report_error f, "does not have corresponding source file"
  return false
end

def c_style_check f
  f_ext = File.extname f
  if f_ext == ".c"
    # C source code
    c_check_has_main_or_header f
  elsif CXX_EXT.include? f_ext
    # C++ source code
    c_check_has_main_or_header f
  elsif f_ext == ".h"
    # C/C++ header file
    c_check_has_source_file f
  end
  warn_trailing_whitespace f
  warn_tab_indents f
end

def ruby_style_check f
  warn_trailing_whitespace f
  warn_tab_indents f
end

def python_style_check f
  warn_trailing_whitespace f
  warn_tab_indents f
end

Find.find(NOVA_SRC_ROOT) do |f|
  next if should_skip? f
  if f.end_with? ".c" or f.end_with? ".h" or CXX_EXT.include? File.extname(f)
    c_style_check f
  elsif f.end_with? ".rb"
    ruby_style_check f
  elsif f.end_with? ".py"
    python_style_check f
  end
end

