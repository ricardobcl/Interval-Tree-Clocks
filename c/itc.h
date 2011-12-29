#ifndef ITC_H
#define ITC_H

#include <stdlib.h>
#include <errno.h>
#include <stdio.h>
#include <strings.h>
#include <string.h>
#include <math.h>
#include "BitArray.h"

#define EV_N(v) -v-1
#define ID_N(v) -v-1

#define FORK 1
#define JOIN 3
#define EVENT 2
#define LEQ 4
#define SEED 0

typedef struct event{/* if n < 0 -> n' = -n-1, e1 = e2 = NULL else ... */
	int n;
	struct event* e1;
	struct event* e2;
} Event;

typedef struct id{/* if n >= 0 i1 = i2 = NULL else ... */
	char n;
	struct id* i1;
	struct id* i2;
} Id;

typedef struct stamp{
	Id* i;
	Event* e;
	int serial;
}stamp;

// aux functions
char compId(Id* i1, Id*i2);
Event* dupEvent(Event* e);
Id* dupId(Id* i);
char compEvent(Event* e1, Event* e2);
Event* newEvent(int val);
Id* newId(int val);

int max(int a, int b);
int min(int a, int b);

int id_n(int n);
int ev_n(int n);

void printId(Id* i);
void printEvent(Event* e);
void printStamp(stamp* s);

// MAIN FUNCTIONS
stamp* newStamp();

// usable
stamp* itc_seed();/* start stamp */
int itc_fork(stamp* s, stamp* rl, stamp* rr);
int itc_join(stamp* s1, stamp* s2, stamp* sr);
int itc_event(stamp* in, stamp* out);
int itc_peek(stamp* in, stamp* out);
char itc_leq(stamp* s1, stamp*s2);

int id_split(Id* i, Id** i1, Id** i2);

/* ****************************************************** */

typedef struct binEvent{
	int ub;
	int fb;
	int sb;
	void* array;
}binEvent;

typedef struct binId{
	int ub;
	int fb;
	int sb;
	void* array;
}binId;

typedef struct binStamp{
	int idsize;
	void* bid;
	int eventsize;
	void* bevent;
}binStamp;

binId* newbinid();
bitArray* id_enc(Id* i, bitArray* bi);
Id* id_dec(bitArray* bi);

binEvent* newbinevent();
bitArray* event_enc(Event* e, bitArray* be);
Event* event_dec(bitArray* be);

binStamp* encodeStamp(stamp* s);
stamp* decodeStamp(binStamp* bs);

void* Encode_Stamp(stamp* s, int* len);
stamp* Decode_Stamp(void*);

void* StampEncode(stamp* s, int* len);
stamp* StampDecode(void* array, int len);

#endif /* ITC_H */

char* dectobin(void* v, int len);
