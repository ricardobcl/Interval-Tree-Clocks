all :itc.o main.c queue.o itc.o causalh.o BitArray.o
	gcc -o main main.c itc.o causalh.o queue.o BitArray.o -lm -g

test : causalh.o test.c
	gcc -o teste test.c causalh.o

BitArray.o : BitArray.c BitArray.h
	gcc -c BitArray.c BitArray.h

queue.o : queue.c queue.h
	gcc -c queue.c queue.h

itc.o : itc.h itc.c
	gcc -c itc.c itc.h 

causalh.o : causalh.h causalh.c
	gcc -c causalh.c

clean :
	rm -f *.o *.gch este *.out binaryfile filebin main tempfile teste1 bfile ficheiro tmp.file
	