#ifndef CAUSAL_HISTORY
#define CAUSAL_HISTORY

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

typedef struct atom{
	int val;
	struct atom* next;
}atom;

typedef struct conjunto{
	int len;
	atom* list;
	int serial;
}conjunto;
			
conjunto* novoConjunto();
void imprimeConjunto(conjunto* conj);
int insere(int v, conjunto* b);
//int remove(int v, conjunto* b);
int existe(int v, conjunto* b);

int contido(conjunto* a, conjunto* b);
conjunto* reuniao(conjunto* a, conjunto* b);
conjunto* interceccao(conjunto* a, conjunto* b);
conjunto* duplica(conjunto* conj);

// Mecanismo de história causal
typedef int gerador;
typedef conjunto Cstamp;

Cstamp* Cseed(gerador* gen);
Cstamp* Cnovo();

void Cfork(Cstamp* in, Cstamp** a, Cstamp** b);
Cstamp* Cjoin(Cstamp* a, Cstamp* b);
Cstamp* Cevent(Cstamp* in, gerador* gen);
int Cleq(Cstamp* a, Cstamp* b);

#endif /* CAUSAL_HISTORY */
