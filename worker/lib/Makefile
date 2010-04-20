all: ftp_server_files_list_updater trash_cleaner vm_daemon image_pool_maintainer

ftp_server_files_list_updater: ftp_server_files_list_updater.c
	gcc ftp_server_files_list_updater.c -o ftp_server_files_list_updater

trash_cleaner: trash_cleaner.cpp
	g++ trash_cleaner.cpp -o trash_cleaner -lvirt

vm_daemon: vm_daemon.c
	gcc vm_daemon.c -o vm_daemon

image_pool_maintainer: image_pool_maintainer.c
	gcc image_pool_maintainer.c -o image_pool_maintainer

clean:
	rm -f ftp_server_files_list_updater trash_cleaner vm_daemon image_pool_maintainer

