#!/bin/sh

rake backgroundrb:setup

rake db:migrate RAILS_ENV=production
rake db:migrate RAILS_ENV=development

rake db:migrate:reset RAILS_ENV=production
rake db:migrate:reset RAILS_ENV=development

rake db:fixtures:load RAILS_ENV=production
rake db:fixtures:load RAILS_ENV=development
