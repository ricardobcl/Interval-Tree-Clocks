#include <stdio.h>
#include <stdlib.h>

#define ITERATIONS 500

#include "itc.h"
#include "queue.h"
#include "causalh.h"

int main(){
	printf("ITC ********\n");
	srand(time(NULL));
	int i, r = -1, ind = 0;
	int counter = 0;
	
	queue* q = newQueue();
	
	gerador gen;
	queue* bag = newQueue();
	
	stamp* seed = (stamp*) malloc(sizeof(stamp));
	seed = itc_seed(seed);
	enqueue(q, (void*)seed, 0);
	
	Cstamp* seedb = Cseed(&gen);
	enqueue(bag, seedb, 0);
	
	stamp* a = (stamp*) malloc(sizeof(stamp));
	stamp* b = (stamp*) malloc(sizeof(stamp));
	stamp* x = (stamp*) malloc(sizeof(stamp));
	stamp* z = (stamp*) malloc(sizeof(stamp));
	
	// stats
	int forks = 0;
	int events = 0;
	int joins = 0;
	
	int act_ind = 0;
	
	int operation;
	int serStamp = 0;
	for ( i = 0; i < ITERATIONS; i++){
		operation = 0;
		
		if (q->n <= 1) operation = FORK;
		else operation = rand() % 10+1;
		
		//printf("****************** iteration %d %d\n", i, operation);
		
		if(operation < 4){// FORK
			forks++;
			
			int serialA = serStamp++;
			int serialB = serStamp++;
			//printf("forking %d %d\n", serialA, serialB);
			
			int ind = rand() % (q->n + 1);
			
			// ITC mechanism
			a = (stamp*) malloc(sizeof(stamp));
			b = (stamp*) malloc(sizeof(stamp));
			
			x = (stamp*) dequeue(q, ind, &act_ind);
			//printStamp(x);
			itc_fork(x, a, b);
			a->serial = serialA;
			//printStamp(a);
			b->serial = serialB;
			//printStamp(b);
			
			enqueue(q, (void*)a, i);
			enqueue(q, (void*)b, i);
			free(x);
			
			// causal history mechanism
			Cstamp* ca;
			Cstamp* cb;
			Cstamp* cx = Cnovo();
			
			cx = (Cstamp*) dequeue(bag, ind, &act_ind);
			//imprimeConjunto(cx);
			Cfork(cx, &ca, &cb);
			//imprimeConjunto(ca);
			ca->serial = serialA;
			//imprimeConjunto(cb);
			cb->serial = serialB;
			
			enqueue(bag, (void*) ca, i);
			enqueue(bag, (void*) cb, i);
			
		}else if(operation >=4 && operation < 7){//JOIN
			joins++;
			
			int serialA = serStamp++;
			//printf("joining %d   -> %d\n", q->n, serialA);
			
			// first out
			int inda = rand() % (q->n + 1);
			// ITC mechanism
			a = (stamp*) dequeue(q, inda, &act_ind);
			// causal history mechanism
			Cstamp* ca = Cnovo();
			ca = (Cstamp*) dequeue(bag, inda, &act_ind);
			//imprimeConjunto(ca);
			
			// second out
			int indb = rand() % (q->n + 1);
			// ITC mechanism
			b = (stamp*) dequeue(q, indb, &act_ind);			
			//printStamp(a);
			//printStamp(b);
			
			x = (stamp*) malloc(sizeof(stamp));
			itc_join(a, b, x);
			x->serial = serialA;
			//printStamp(x);
			
			enqueue(q, (void*)x, i);
			free(a);
			free(b);
			
			// causal history mechanism
			Cstamp* cb = Cnovo();
			cb = (Cstamp*) dequeue(bag, indb, &act_ind);
			//imprimeConjunto(cb);
			Cstamp* cx = Cjoin(ca, cb);
			cx->serial = serialA;
			//imprimeConjunto(cx);
			enqueue(bag, (void*)cx, i);
			
		}else if(operation >= 7){//EVENT
			events++;
			
			int serialA = serStamp++;
			//printf("eventing %d\n", serialA);
			
			int ind = rand() % (q->n + 1);
			
			// ITC mechanism
			x = (stamp*)dequeue(q, ind, &act_ind);
			//printStamp(x);
			z = (stamp*) malloc(sizeof(stamp));
			
			itc_event(x, z);
			z->serial = serialA;
			//printStamp(z);
			
			enqueue(q, (void*)z, i);
			free(x);
			
			// causal history mechanism
			Cstamp* cx = Cnovo();
			cx = (Cstamp*) dequeue(bag, ind, &act_ind);
			//imprimeConjunto(cx);
			
			Cstamp* cz = Cevent(cx, &gen);
			cz->serial = serialA;
			//imprimeConjunto(cz);
			enqueue(bag, (void*)cz, i);
		}else{
			printf	("%d\n", operation);
		}
		
		stamp* tmp = (stamp*) malloc(sizeof(stamp));
		tmp = (stamp*) getLast(q);
		Cstamp* tmpb = Cnovo();
		tmpb = (Cstamp*) getLast(bag);
		int len = q->n;
		int n;
		//counter = 0;
		
		for (n = 0; n < len-1; n++){
			
			if (len < 3) break;
			
			int comp = 0;
			void* ax1 = StampEncode((stamp*) getN(q, n), &comp);
			stamp* aux = StampDecode(ax1, comp);
			Cstamp* auxb = (Cstamp*) getN(bag, n);
			//printf(" | %d : %d/%d | serials %d %d | sub serials %d %d\n", q->n == bag->n, n, q->n, tmp->serial, tmpb->serial, aux->serial, auxb->serial);
			
			int a = itc_leq(tmp, aux);
			int b = Cleq(tmpb, auxb);
			if ((a && b) || (!a && !b)){
				//System.out.println("Check    "+ a + " " + b);
				//System.out.println(tmp.tostring());
				//System.out.println(((CStamp) saco.getInd(n)).tostring());
				//System.out.println(tmpb.tostring());
				//System.out.println(((Stamp) bag.getInd(n)).tostring());
			}else{
				//printf("\n");
				//printf("%d Bug : %d, %d | %d : %d/%d\n", n, a, b, q->n == bag->n, n, q->n);
				//printStamp(tmp);
				//printStamp(aux);
				//imprimeConjunto(tmpb);
				//imprimeConjunto(auxb);
				//printf("__________________________________________________________\n");
				
				//exit(0);
				counter++;
			}
		}
	}
	printf("BUGS > %d\n", counter);
	
	printf("************************************************\n");

	printf("forks %d\njoins %d\nevents %d\n", forks, joins, events);
	printf(" queue len %d\n", q->n);
	
	/*int coise;
	for(i = q->n-1; i >= 0; i--){
		stamp* omg = dequeue(q, i, &coise);
		printStamp(omg);
		
		int len = 0;
		void* aux = StampEncode(omg, &len);
		printStamp(StampDecode(aux, len));
		printf("\n");
	}*/
	
	//printQueue(q);
	
	char* fname = "binaryfile";
	printf("%s\n", fname);
	//saveQueue(q, fname);
	
	queue* q2 = newQueue();
	loadQueue(q2, fname);printf("checking\n");
//	printQueue(q2);

	printf("...\n");
}
