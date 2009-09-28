#!/bin/sh

# triggers 'first_run.sh' script for Ceil component
#cd lib/ceil/server
#./first_run.sh
#cd ../../..

rake backgroundrb:setup

rake db:migrate RAILS_ENV=production
rake db:migrate RAILS_ENV=development

rake db:migrate:reset RAILS_ENV=production
rake db:migrate:reset RAILS_ENV=development

rake db:fixtures:load RAILS_ENV=production
rake db:fixtures:load RAILS_ENV=development
