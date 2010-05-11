SHELL_EXPECT = '/usr/bin/expect'
SSH_EXPECT = File.dirname(__FILE__) + '/ssh.exp'

require 'fileutils'

module SSHKeyGenerator
	def SSHKeyGenerator.generate(base_path)
		begin
			FileUtils.mkdir_p(base_path)
		rescue
		end		
		filename = base_path + '/id_rsa'
		system "expect #{SSH_EXPECT} #{filename}"
	end
end

=begin
SSHKeyGenerator.generate('/var/vm1')
=end
