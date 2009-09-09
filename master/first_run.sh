#! /bin/sh

# triggers 'first_run.sh' script for Ceil component
lib/ceil/server/first_run.sh

rake db:migrate RAILS_ENV=production
rake db:migrate RAILS_ENV=development

rake db:migrate:reset RAILS_ENV=production
rake db:migrate:reset RAILS_ENV=development

rake db:fixtures:load RAILS_ENV=production
rake db:fixtures:load RAILS_ENV=development
