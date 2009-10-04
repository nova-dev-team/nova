require "net/ftp"
require "uri"
require "utils"

module ImageResource

  def ImageResource.list_resource uri
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split uri  # parse URI information
    resource_list = []
    if scheme == "file"
      resource_list = Dir.entries(path).collect
    elsif scheme == "ftp"
      username, password = Util::split_userinfo userinfo
      Net::FTP.open(host, username, password) do |ftp|
        ftp.chdir path
        resource_list = ftp.list("*")
      end
    else
      raise "Unknown resource scheme '#{scheme}'!"
    end
    return resource_list
  end

end
