#include "causalh.h"
/*
typedef struct atom{
	int val;
	struct atom* next;
}atom;

typedef struct conjunto{
	int len;
	atom* list;
}conjunto;
*/
conjunto* novoConjunto(){
	conjunto* novo = (conjunto*) malloc(sizeof(conjunto));
	novo->len = 0;
	novo->list = NULL;
}

int insere(int v, conjunto* b){
	if(!b){perror("Insere : conjunto vazio "); exit(0);}
	
	atom** it = &(b->list);
	while(*it){
		if (v < (*it)->val) { break;}
		it = &((*it)->next);
	}

	atom* novo = (atom*) malloc(sizeof(atom));
	novo->val = v;
	novo->next = *it;
	*it = novo;
	
	b->len++;
	return 0;
}

conjunto* duplica(conjunto* conj){
	if(!conj){perror("Duplica : conjunto vazio "); exit(0);}
	
	conjunto* novo = novoConjunto();
	atom* it = conj->list;
	
	while(it){
		insere(it->val, novo);
		it = it->next;
	}
	
	return novo;
}


void imprimeConjunto(conjunto* conj){
	if(!conj){perror("Imprime : conjunto vazio "); exit(0);}
	
	atom* it = (conj->list);
	
	printf("> ");
	while(it){
		printf("%d ", (it)->val);
		it = (it)->next;
	}
	printf("\n");
}

//int remove(int v, conjunto* b);
int existe(int v, conjunto* b){ // 0 nao, 1 sim
	if(!b){perror("Existe : conjunto vazio "); exit(0);}
	
	atom** it = &(b->list);
	
	while(*it){
		if ((*it)->val == v) return 1;
		if ((*it)->val > v) return 0;
		it = &((*it)->next);
	}
	return 0;
}

int contido(conjunto* a, conjunto* b){
	if(!a){perror("Contido : left conjunto vazio "); exit(0);}
	if(!b){perror("Contido : right conjunto vazio "); exit(0);}
	
	atom** it = &(a->list);
	
	while(*it){
		if(!existe((*it)->val, b)) return 0;
		it = &((*it)->next);
	}
	
	return 1;
}

conjunto* reuniao(conjunto* a, conjunto* b){
	if(!a){perror("Reuniao : left conjunto vazio "); exit(0);}
	if(!b){perror("Reuniao : right conjunto vazio "); exit(0);}
	
	conjunto* novo = novoConjunto();
	
	atom* ita = a->list;
	atom* itb = b->list;
	
	while(ita){
		insere(ita->val, novo);
		ita = ita->next;
	}
	
	while(itb){
		if(!existe(itb->val, a)) insere(itb->val, novo);
		itb = itb->next;
	}
	
	return novo;
}
conjunto* interceccao(conjunto* a, conjunto* b){
	if(!a){perror("Interceccao : left conjunto vazio "); exit(0);}
	if(!b){perror("Interceccao : right conjunto vazio "); exit(0);}
	
	conjunto* novo = novoConjunto();
	atom* itb = b->list;
	
	while(itb){
		if(existe(itb->val, a)) insere(itb->val, novo);
		itb = itb->next;
	}
	
	return novo;
}
// ***************************************************************
/*
typedef int gerador;
typedef conjunto Cstamp;
*/
Cstamp* Cseed(gerador* gen){
	*gen = 1;
	
	Cstamp* novo = novoConjunto();
	insere((*gen)++, novo);
	
	return novo;
}

Cstamp* Cnovo(){
	Cstamp* novo = novoConjunto();
	return novo;
}

void Cfork(Cstamp* in, Cstamp** a, Cstamp** b){
	*a = duplica(in);
	*b = duplica(in);
}

Cstamp* Cjoin(Cstamp* a, Cstamp* b){
	Cstamp* res = reuniao(a, b);
	
	return res;
}

Cstamp* Cevent(Cstamp* in, gerador* gen){
	Cstamp* res = duplica(in);
	insere((*gen)++, res);
	
	return res;
}

int Cleq(Cstamp* a, Cstamp* b){
	return contido(a, b);
}

 