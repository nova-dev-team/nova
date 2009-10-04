require "net/ftp"
require "uri"
require "utils"

module ImageResource

  # get a list of resource at given URI
  # only support "file://" & "ftp://" scheme
  # eg:  "file:///home/santa/"
  #      "ftp://user:password@host/path"
  def ImageResource.list_resource uri
    scheme, userinfo, host, port, registry, path, opaque, query, fragment = URI.split uri  # parse URI information
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

  # TODO get resource from given uri, save it to given file path
  # a .copying lock file will be created, and will be deleted when copying finished, whether successful or not
  def ImageResource.get_resource from_uri, to_file
  end

  # TODO save resource from local file to given remote URI
  def ImageResource.put_resource from_file, to_uri
  end

  # TODO prepare resource for vmachine
  def ImageResource.prepare_resource vm_name, resource_name
  end

end
