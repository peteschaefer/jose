

libjunixsocket.so:
	gcc -O3 -shared -o libjunixsocket.so junixsocket.c

clean:
	rm libjunixsocket.so

install: clean libjunixsocket.so
	mv libjunixsocket.so ../../lib/Linux_i386
 
