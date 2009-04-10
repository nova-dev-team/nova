module UserHelper

  class Helper

    @@USER_EMAIL_MIN_LENGTH = 5 # the minimum length of email address

    # list all users
    def Helper.list
      list = []
      User.all.each {|user| list << user.email}
      return list
    end

    # add a new user
    def Helper.add user_email
      result = {}

      if user_email == nil # if did not given user email
        result[:success] = false
        result[:msg] = "User email address not provided!"

      elsif user_email.length < @@USER_EMAIL_MIN_LENGTH # check for email address length
        result[:success] = false
        result[:msg] = "Email address '#{user_email}' is too short!"

      else # user email address is too short

        if User.find_by_email user_email # check if user exists, to prevent duplicate users
          result[:succee] = false
          result[:msg] = "Email address '#{user_email}' already used!"

        else # if user does not exist
          u = User.new(:email => user_email)
          u.save
          result[:success] = true
          result[:msg] = "User '#{user_email}' successfully added."

        end
      end

      return result
    end

    # return the detailed info of a user
    def Helper.info user_email
      result = {}
      user = User.find_by_email user_email

      if user == nil # user not found
        result[:success] = false
        result[:msg] = "User '#{user_email}' not found!"

      else # user exists
        vc_list = []
        user.vclusters.each {|vcluster| vc_list << "c#{vcluster.id}"}

        result[:success] = true
        result[:email] = user_email
        result[:vclusters] = vc_list

      end

      return result
    end

    # add a vcluster to a user
    def Helper.add_vcluster user_email, vcluster_cid
      result = {}
      user = User.find_by_email user_email
      vcluster = Vcluster.find_by_id vcluster_cid[1..-1] if vcluster_cid != nil

      if user == nil # user not found
        result[:success] = false
        result[:msg] = "User '#{user_email}' not found!"

      elsif vcluster == nil # vcluster not found
        result[:success] = false
        result[:msg] = "Vcluster #{vcluster_cid} not found!"

      else # both user and vcluster are found

        if vcluster.user == nil # the vcluster is not assigned to any user
          user.vclusters << vcluster
          user.save
          vcluster.save

          result[:success] = true
          result[:msg] = "Added vcluster #{vcluster_cid} to user '#{user_email}'"

        elsif vcluster.user == user
          result[:success] = false
          result[:msg] = "Vcluster #{vcluster_cid} already belongs to user '#{user_email}'"

        else # vcluster belongs to other user
          result[:success] = false
          result[:msg] = "Vcluster #{vcluster_cid} already belongs to other user!"

        end
      end

      return result
    end

    # remove a vcluster from a user
    def Helper.remove_vcluster user_email, vcluster_cid
      result = {}
      user = User.find_by_email user_email

      if user == nil # user not found
        result[:success] = false
        result[:msg] = "User '#{user_email}' not found!"

      else # user found
        vcluster = user.vclusters.find_by_id vcluster_cid[1..-1] if vcluster_cid != nil

        if vcluster == nil
          result[:success] = false
          result[:msg] = "User '#{user_email}' does not have vcluster #{vcluster_cid}"

        else

          vcluster_under_use = false

          vcluster.vmachines.each do |vmachine|
            if vmachine.status != "not running"
              vcluster_under_use = true
              break
            end
          end

          if vcluster_under_use
            result[:success] = false
            result[:msg] = "Vcluster #{vcluster_cid} is under use!"

          else # safe to delete the cluster
            user.vclusters.delete vcluster
            user.save
            vcluster.save

            result[:success] = true
            result[:msg] = "Removed vcluster #{vcluster_cid} from user '#{user_email}'"

          end

        end
      end

      return result

    end
  
  end

end
