# this makefile installes everything needed on Ubuntu to run bulletinboard.
# you must cd to the bulletinboard directory, and then run
# $ make
# afterwards you need to run
# $ source env.sh
# and then you can run the bulletinboard with
# $ rake run
all:
	sudo apt-get install libmysqlclient-dev -y
	sudo gem install bundle
	sudo apt-get install ruby1.9.1-dev -y
	bundle install
	sudo apt-get install mysql-server -y
	rake setup_mysql_first_time
	rake db:create
	rake db:setup
	RACK_ENV=test rake db:create
	RACK_ENV=test rake db:setup
