#!/usr/bin/ruby

require 'rubygems'
require 'sinatra'

set :environment, :development

set :app_file, __FILE__
set :reload, true


configure do
  # run once, on startup
end

helpers do
  def get_cpu_count
    return 2
  end
end

get '/' do
  "HI"
end

get '/hello' do
  hello_str = "HELLO\n"
  hello_str += "nova.pnode\n"
  hello_str += "version.1\n"
  hello_str += "fs.kfs\n"
  hello_str += "fs.nfs\n"
  hello_str += "cpu.#{get_cpu_count}\n"
  hello_str += "mem.2048\n"
  hello_str += "env.development\n"

  hello_str
end
