

metaphone.so:
	g++ -Iinclude -O3 -shared -o metaphone.so metaphone.cpp 

clean:
	rm metaphone.so

install: clean metaphone.so
	mv metaphone.so ../../lib/Linux_i386
