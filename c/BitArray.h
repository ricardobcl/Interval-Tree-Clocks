#ifndef BIT_ARRAY
#define BIT_ARRAY

#include <stdlib.h>
#include <errno.h>
#include <stdio.h>
#include <strings.h>
#include <netinet/in.h>
#include <stdint.h>

typedef struct bitArray{
	int ub;
	int fb;
	int sb;
	void* array;
}bitArray;

bitArray* newbitArray();
int addbits(bitArray* be, int val, int n);
int readbits(bitArray* be, int n);

void* unify(bitArray* b);
bitArray* extract(void* array);

int saveFormated(FILE* fp, void* array, int len);
int loadFormated(FILE* fp, void** array, int* len);

int switchEndianess(void* in, void** out, int len);

#endif /* BIT_ARRAY*/