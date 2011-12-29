#include "queue.h"

queue* newQueue(){
	queue* res = (queue*) malloc(sizeof(queue));
	res->n = 0;
	res->list = NULL;
	
	return res;
}

void* dequeue(queue* q, char type, int* ind){
	if (!q){perror("not q "); return NULL;}
	if (!q->list){perror("not q->list "); return NULL;}
	if (type > q->n){perror("cannot remove "); return NULL;}
	
	int n = 0;
	if (type >= 0){
		n = type;
	}else{
		n =  rand() % (q->n + 1);
	}
	
	n--;
	elem** it = &(q->list);
	while(n > 0){// && (*it)->next){
		it = &(*it)->next;
		n--;
	}
	elem* res = (*it);
	(*it) = res->next;
	q->n--;
	if (!q->list && q->n > 0){ perror("error too short "); exit(0);}
	
	*ind = res->i;
	return res->st;
}

int enqueue(queue* el, void* st, int indice){
	if (!el) return -1;
	
	elem* novo = (elem*) malloc(sizeof(elem));
	novo->st = st;
	novo->next = el->list;
	novo->i = indice;
	
	el->n++;
	el->list = novo;
}

int verify(queue* q){
	if (!q){perror("not q "); return -1;}
	int n = q->n, i = 0;
	elem* it = q->list;
	
	while(n > 0){
		if (! it) {
			printf("does not compute ... n : %d\n", i);
			break;
		}
		n--;
		i++;
	}
	return 0;
}

void* getLast(queue* q){
	return q->list->st;
}

void* getN(queue* q, int n){
	if (!q){perror("not q "); return NULL;}
	if (!q->list){perror("not q->list "); return NULL;}
	if ((n-1) > q->n){perror("not big enough "); return NULL;}
	
	n;
	elem** it = &(q->list);
	it = &(*it)->next;
	while(n > 0 && *it){// && (*it)->next){
		if (!(*it)) printf("BUUUUGGGGGGGGGG************\n");
		it = &(*it)->next;
		n--;
	}
	
	return (*it)->st;
}

// ************************ itc based functions *** //

void printQueue(queue* q){
	if (!q){perror("not q "); return;}
	
	int i = 0;
	elem** it = &(q->list);
	while((*it)->next){
		printf("elem %d\n", i);
		printStamp((stamp*) (*it)->st);
		it = &(*it)->next;
		i++;
	}
}

int saveQueue(queue* q, char* fname){
	FILE* fp = fopen(fname, "wb");
	if(!fp){perror("Save file : Could not open file"); return -1;}
	
	uint32_t n = (uint32_t) q->n; 
	int len = q->n;
	n = htonl(n);
	if (fwrite((void*) &n, sizeof(uint32_t), 1, fp) != 1)
		{perror("Save file : Could not save length"); return -1;}
	
	int i;
	for(i = 0; i < len; i++){
		uint32_t lenn = 0x00; int st = 0;
		void* aux;
		
		stamp* s = dequeue(q, q->n , &st);
		aux = StampEncode((stamp*) s, (int*) &lenn);
		saveFormated(fp, aux, lenn);
	}
	
	fclose(fp);
}

int loadQueue(queue* q, char* fname){
	FILE* fp = fopen(fname, "rb");
	if(!fp){perror("Load file : Could not open file"); return -1;}
	
	uint32_t n = 0x00;
	if(fread((void*) &n, sizeof(uint32_t), 1, fp) != 1)
		{perror("Load file : Could not load length"); return -1;}
	n = ntohl(n);
	
	int i;
	for (i = 0; i < n; i++){
		uint32_t len = 0x00;
		void* aux;
		
		loadFormated(fp, &aux, (int*) &len);
		enqueue(q, (void*) StampDecode(aux, len), i);
	}
}
