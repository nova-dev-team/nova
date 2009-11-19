CC=gcc
CCFLAGS=-Wall


all: ndss_ctl ndss_server


ndss_ctl: ndss_ctl.o
	$(CC) ndss_ctl.o -o ndss_ctl


ndss_server: ndss_server.o
	$(CC) ndss_server.o -o ndss_server


clean:
	rm -f *.o *~ ndss_server ndss_ctl


