CC=gcc
CFLAGS=-Wall -I../include

all: ndss

ndss: attr.o entry.o fetch.o main.o put.o remove.o rename.o serve.o 
	$(CC) $(CFLAGS) *.o -o $@

clean:
	rm -f *.o *~ ndss

