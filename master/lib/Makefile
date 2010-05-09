all: port_fwd_daemon ftp_server_files_list_updater

port_fwd_daemon: port_fwd_daemon.c
	gcc port_fwd_daemon.c -o port_fwd_daemon

ftp_server_files_list_updater: ftp_server_files_list_updater.c
	gcc ftp_server_files_list_updater.c -o ftp_server_files_list_updater

clean:
	rm -f port_fwd_daemon ftp_server_files_list_updater

