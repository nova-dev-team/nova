ActionController::Routing::Routes.draw do |map|
  map.resources :vmachine_infos

#  map.resources :vdisks

#  map.resources :vclusters

  # this is a stupid hack, to make "settings/edit" work good
#  map.connect 'settings/edit', :controller => 'settings', :action => 'edit'
#  map.resources :settings

#  map.resources :groups

#  map.resources :vmachines

  map.logout '/logout', :controller => 'sessions', :action => 'destroy'
  map.login '/login', :controller => 'sessions', :action => 'new'
  #map.register '/register', :controller => 'users', :action => 'create'
  #map.signup '/signup', :controller => 'users', :action => 'new'
#  map.resources :users

  map.resource :session

  map.home '', :controller => 'app', :action => 'home'

#  map.connect 'misc/verification_image.:format', :controller => 'misc', :action => 'verification_image'
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


  # default routing, also includes the first version of nova api
  # we have to use :path_prefix, so that the old api does not overshadow newer version api
  map.connect ':controller/:action', :path_prefix => '/'
  map.connect ':controller/:action/:id', :path_prefix => '/'
  map.connect ':controller/:action.:format', :path_prefix => '/'
  map.connect ':controller/:action/:id.:format', :path_prefix => '/'

  map.connect ':controller/:action', :path_prefix => '/api/v1'
  map.connect ':controller/:action/:id', :path_prefix => '/api/v1'
  map.connect ':controller/:action.:format', :path_prefix => '/api/v1'
  map.connect ':controller/:action/:id.:format', :path_prefix => '/api/v1'

  # versioned api
  map.namespace :api, :path_prefix => '/' do |api|

    api.namespace :v2 do |v2|
      v2.connect ':controller/:action'
      v2.connect ':controller/:action/:id'
      v2.connect ':controller/:action.:format'
      v2.connect ':controller/:action/:id.:format'
    end

    api.namespace :v3 do |v3|
      v3.connect ':controller/:action'
      v3.connect ':controller/:action/:id'
      v3.connect ':controller/:action.:format'
      v3.connect ':controller/:action/:id.:format'
    end
  end
  
end
