

libunixutils.so:
	gcc -O3 -shared -o libunixutils.so unixutils.c

clean:
	rm libunixutils.so

install: clean libunixutils.so
	mv libunixutils.so ../../lib/Linux_i386
 
