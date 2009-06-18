ActionController::Routing::Routes.draw do |map|
  map.list_vm("x/",
              :controller => 'x',
              :action => "list",
              :conditions => { :method => :get})
  map.check_vm("x/:id",
              :controller => 'x',
               :action => "check",
               :conditions => { :method => :get})
  map.create_vm("x/create",
              :controller => 'x',
               :action => "create",
               :conditions => { :method => :post})
  map.start_vm("x/:id/start",
              :controller => 'x',
               :action => "start",
               :conditions => { :method => :put})
  map.stop_vm("x/:id/stop",
              :controller => 'x',
               :action => "stop",
               :conditions => { :method => :put})
  map.suspend_vm("x/:id/suspend",
              :controller => 'x',
               :action => "suspend",
               :conditions => { :method => :put})
  map.resume_vm("x/:id/resume",
              :controller => 'x',
               :action => "resume",
               :conditions => { :method => :put})
  map.destroy_vm("x/:id/destroy",
              :controller => 'x',
               :action => "destroy",
               :conditions => { :method => :delete})
  map.check_vnc_port("x/:id/vnc_port",
                     :controller => 'x',
                     :action => "vnc_port",
                     :conditions => { :method => :get})


  # Storage Engine Routes
  map.stop_sengine("s/stop_se",
                   :controller => 's',
                   :action => 'stop_se',
                   :conditions => { :method => :put } )
  map.stop_sengine("s/restart_se",
                   :controller => 's',
                   :action => 'restart_se',
                   :conditions => { :method => :put } )
  map.list_se("s/list_se",
              :controller => 's',
              :action => 'list_se',
              :conditions => { :method => :get } )
  map.check_se("s/check_se",
              :controller => 's',
              :action => 'check_se',
              :conditions => { :method => :get } )

  # Update Engine Routes
  map.stop_uengine("s/stop_ue",
                    :controller => 's',
                    :action => 'stop_ue',
                    :conditions => { :method => :put } )
  map.restart_uengine("s/restart_ue",
                      :controller => 's',
                      :action => 'restart_ue',
                      :conditions => { :method => :put } )
  map.update_ue("s/update_ue",
             :controller => 's',
             :action => 'update_ue',
             :conditions => { :method => :put } )
  map.list_ue("s/list_ue",
              :controller => 's',
              :action => 'list_ue',
              :conditions => { :method => :get } )
  map.check_ue("s/check_ue",
              :controller => 's',
              :action => 'check_ue',
              :conditions => { :method => :get } )


  map.admin("a/",
            :controller => 'a',
            :action => 'admin',
            :conditions => { :method => :get } )
  map.auth("a/auth",
            :controller => 'a',
            :action => 'auth',
            :conditions => { :method => :post } )
  map.auth("a/post",
            :controller => 'a',
            :action => 'post',
            :conditions => { :method => :post } )


  # The priority is based upon order of creation: first created -> highest priority.

  # Sample of regular route:
  #   map.connect 'products/:id', :controller => 'catalog', :action => 'view'
  # Keep in mind you can assign values other than :controller and :action

  # Sample of named route:
  #   map.purchase 'products/:id/purchase', :controller => 'catalog', :action => 'purchase'
  # This route can be invoked with purchase_url(:id => product.id)

  # Sample resource route (maps HTTP verbs to controller actions automatically):
  #   map.resources :products

  # Sample resource route with options:
  #   map.resources :products, :member => { :short => :get, :toggle => :post }, :collection => { :sold => :get }

  # Sample resource route with sub-resources:
  #   map.resources :products, :has_many => [ :comments, :sales ], :has_one => :seller

  # Sample resource route with more complex sub-resources
  #   map.resources :products do |products|
  #     products.resources :comments
  #     products.resources :sales, :collection => { :recent => :get }
  #   end

  # Sample resource route within a namespace:
  #   map.namespace :admin do |admin|
  #     # Directs /admin/products/* to Admin::ProductsController (app/controllers/admin/products_controller.rb)
  #     admin.resources :products
  #   end

  # You can have the root of your site routed with map.root -- just remember to delete public/index.html.
  # map.root :controller => "welcome"

  # See how all your routes lay out with "rake routes"

  # Install the default routes as the lowest priority.
  # Note: These default routes make all actions in every controller accessible via GET requests. You should
  # consider removing the them or commenting them out if you're using named routes and resources.

  # map.connect ':controller/:action/:id'
  #  map.connect ':controller/:action/:id.:format'
end
