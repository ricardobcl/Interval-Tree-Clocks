#ifndef QUEUE_H
#define QUEUE_H

#include <stdlib.h>
#include <errno.h>
#include <time.h>
#include <stdint.h>

#include "itc.h"
#include "BitArray.h"

#define DEF_SIZE 100

typedef struct elem{
	int i;
	void* st;
	
	struct elem* next;
}elem;

typedef struct queue{
	int n;
	
	elem* list;
}queue;

queue* newQueue();
void* dequeue(queue* q, char type, int* ind);
int enqueue(queue* el, void* st, int indice);
int verify(queue* q);

int saveQueue(queue* q, char* fname);
int loadQueue(queue* q, char* fname);

void* getLast(queue* q);
void* getN(queue*q, int n);

#endif /* QUEUE_H */
