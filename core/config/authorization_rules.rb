authorization do

  role :guest do
    has_permission_on :authorizeation_rules, :to => :read
  end

  role :user do
    has_permission_on :vmachine, :to => :create, :delete => :and do
      if_attribute :user => is {user}
      if_permitted_to :read, :vmachine
    end
    has_permission_on :vcluster, :to => :delete do
      if_attribute :user => is {user}
    end
  end
  
  role :admin do
    includes :user
    has_permission_on :user, :to => :delete do
      if_attribute :user => is {admin}
    end
  end
  
  role :root do
    includes :user
    has_permission_on [:pmachine, :vmachine, :vcluster], :to => :manage
    has_permission_on :authorizeation_rules, :to => :read
    has_permission_on :authorizeation_usages, :to => :read
  end  

end

privileges do
  privilege :manage, :includes => [:create, :read, :update, :delete]
  privilege :read, :includes => [:index, :show]
  privilege :create, :includes => :new
  privilege :update, :includes => :edit
  privilege :delete, :includes => :destroy
end
