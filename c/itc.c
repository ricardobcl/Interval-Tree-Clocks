#include "itc.h"

stamp* newStamp(){
	return (stamp*) malloc(sizeof(stamp));
}


/**********************************************************************/

int id_split(Id* i, Id** il, Id** ir){
	if (!i){perror("id_split "); exit(0);}
	
	if (i->n == 0){
		*il = newId(0);
		*ir = newId(0);
	}else if (i->n == 1){
		*il = newId(-1);
		*ir = newId(-2);
	}else{
		if(i->i1->n != 0 && i->i2->n == 0){
			Id* al = newId(-3);
			Id* ar = newId(-3);
			id_split(i->i1, &al, &ar);
			*il = newId(-3);
			(*il)->i1 = al;
			(*il)->i2 = newId(0);
			*ir = newId(-3);
			(*ir)->i1 = ar;
			(*ir)->i2 = newId(0);			
		}else if(i->i1->n == 0 && i->i2->n != 0){
			Id* al = newId(-3);
			Id* ar = newId(-3);
			id_split(i->i2, &al, &ar);
			*il = newId(-3);
			(*il)->i1 = newId(0);
			(*il)->i2 = al;
			*ir = newId(-3);
			(*ir)->i1 = newId(0);
			(*ir)->i2 = ar;
		}else if(i->i1->n != 0 && i->i2->n != 0){
			*il = newId(-3);
			(*il)->i1 = dupId(i->i1);
			(*il)->i2 = newId(0);
			
			*ir = newId(-3);
			(*ir)->i1 = newId(0);
			(*ir)->i2 = dupId(i->i2);
		}else{printf("Bug id_split i %d - i1 %d - i2 %d\n",i->n, i->i1->n, i->i2->n);}
	}
}

Id* id_norm(Id* i1, Id* i2){
	Id* new = (Id*) malloc(sizeof(Id));
	
	if (i1->n == 0 && i2->n == 0){
		new = newId(0);
		return new;
	}else if(i1->n == 1 && i2->n == 1){
		new = newId(1);
		return new;
	}else{
		return NULL;
	}
}

int base(Event* e){ // stupid
	return e->n;
}

Event* drop(int m, Event* e){
	if (!e) {perror("drop "); exit(0);}
	
	int r = 0;
	if (m <= ev_n(e->n)) r = ev_n(e->n) -m;
	else r = ev_n(e->n);
	
	if (e->n < 0) r = -r-1;
	
	Event* new = newEvent(r);
	new->e1 = e->e1; // even if they are null
	new->e2 = e->e2;
	return new;
}

int height(Event* e){
	if (!e){perror("altura erro "); exit(0);}
	if(e->n >= 0){
		return e->n + max(height(e->e1) ,height(e->e2));
	}else{
		return -(e->n) -1;
	}
}

Event* event_norm(Event* e){
	Event* new;
	if (!e) {perror("norm_event "); exit(0);}
	
	if ((e->n >= 0) && (e->e1->n < 0) && (e->e2->n == e->e1->n)){
		new = newEvent(-(ev_n(e->n) + ev_n(e->e2->n))-1); // coise ...
	}else if ((e->n >= 0)){// && (e->e1->n >= 0) && (e->e2->n >= 0)
		int tmp = min(ev_n(base(e->e1)), ev_n(base(e->e2)));
		
		new = newEvent(ev_n(e->n)+tmp);
		new->e1 = drop(tmp, e->e1);
		new->e2 = drop(tmp, e->e2);
		
	}else{printf("out the same %d, %d, %d\n", e->n, e->e1->n, e->e2->n); new = e;}
	return new;
}

Id* id_sum(Id* i1, Id* i2){
	if (!i1) {perror("i1 error ");exit(0);}
	if (!i2) {perror("i2 error ");exit(0);}

	Id* new;
	if (i1->n < 0 && i2->n < 0){
		new = newId(-1);
		new->i1 = id_sum(i1->i1, i2->i1);
		new->i2 = id_sum(i1->i2, i2->i2);
		Id* aux = id_norm(new->i1, new->i2);
		if (aux){
			new = aux;
		}
	}else{
		if (i1->n != 0 && i2->n == 0){
			new = dupId(i1);
		}else if(i1->n == 0 && i2->n != 0){
			new = dupId(i2);
		}else if (i1->n == 0 && i2->n == 0){
			new = newId(0);
		}else{
			printf("Bug id_sim i1.n %d - i2.n %d\n", i1->n, i2->n );
		}
	}
	
	return new;
}

Event* lift(int c, Event* e){
	if (!e){perror("lift "); exit(0);}
	
	Event* new;
	if(e->n >= 0){ new = dupEvent(e); new->n += c; }
	else { new = newEvent(ev_n(e->n) + c); new->n = -new->n -1;}
	
	return new;
}

stamp* grow(Id* i, Event* e){ // ID->N is only used as a storage int, so id rules dont aplly
	if(!i || !e){perror("grow "); exit(0);}
	
	stamp* new = (stamp*) malloc(sizeof(stamp));
	if (i->n == 1 && e->n < 0){
		//printf("A\n");
		new->i = newId(0);
		new->e = dupEvent(e);
		if (e->n >= 0) new->e->n ++;
		else new->e->n --;
	}else if (i->n < 0 && (i->i1->n == 0 && e->n >= 0)){
	//printf("B\n");
		new = grow(i->i2, e->e2);
		
		new->i->n++;
			
		Event* tmp = new->e;
		new->e = newEvent(e->n);
		new->e->e1 = dupEvent(e->e1);
		new->e->e2 = tmp;
	}else if (i->n < 0 && (i->i2->n == 0 && e->n >= 0)){
	//printf("C\n");
		new = grow(i->i1, e->e1);
		
		new->i->n++;
		
		Event* tmp = new->e;
		new->e = newEvent(e->n);
		new->e->e1 = tmp;
		new->e->e2 = dupEvent(e->e2);
	}else if(i->n < 0 && e->n >= 0){
		//printf("D\n");
		stamp* l = grow(i->i1, e->e1);
		stamp* r = grow(i->i2, e->e2);
		//printf("%d < %d\n",id_n(l->i->n), id_n(r->i->n));
		if (l->i->n < r->i->n){
		//printf("D1\n");
			new->i = l->i;
			
			new->i->n += 1;

			new->e = newEvent(e->n);
			new->e->e1 = l->e;
			new->e->e2 = dupEvent(e->e2);
		}else{
		//printf("D2\n");
			new->i = r->i;

			new->i->n += 1;

			new->e = newEvent(e->n);
			new->e->e1 = dupEvent(e->e1);
			new->e->e2 = r->e;
		}
	}else if(e->n < 0){
	//printf("E\n");
		Event* tmp = newEvent(ev_n(e->n));
		tmp->e1 = newEvent(-1);
		tmp->e2 = newEvent(-1);
		new = grow(i, tmp);
		new->i->n += 1000;
	}else{
		printf("Bug Doom\n i.n %d, e.n %d\n", i->n, e->n);
		new->e = dupEvent(e);
	}
	//printEvent(new->e);printf("\n"); printEvent(new->e);printf("\n**\n");
	return new;
}

Event* fill(Id* i, Event* e){
	if (!i) { perror("not i "); }
	if (!e) { perror("not e "); }
	
	Event* new;
	if(i->n == 0){//printf("fill a\n");
		new = dupEvent(e);
		
	}else if(i->n == 1 && e->n >= 0){//printf("fill b\n");
		new = newEvent((abs(height(e))*-1) -1);
		
	}else if(e->n < 0){//printf("fill c\n");
		new = newEvent((e->n));
		
	}else if(i->n < 0 && i->i1->n == 1 && e->n >= 0){//printf("fill d\n");
		Event* tmp = fill(i->i2, e->e2);
		int aux = max(height(e->e1), base(tmp));
		new = newEvent(e->n);
		new->e1 = newEvent((-1*aux)-1);
		new->e2 = tmp;
		Event* new2 = event_norm(new);
		return new2;
		
	}else if(i->n < 0 && i->i2->n == 1 && e->n >= 0){//printf("fill e\n");
		Event* tmp = fill(i->i1, e->e1);
		int aux = max(height(e->e2), base(tmp));
		new = newEvent(e->n);
		new->e1 = tmp;
		new->e2 = newEvent((-1*aux)-1);
		Event* new2 = event_norm(new);
		return new2;
		
	}else if(i->n < 0 && e->n >= 0){//printf("fill f\n");
		new = newEvent(e->n);
		new->e1 = fill(i->i1, e->e1);
		new->e2 = fill(i->i2, e->e2);
		Event* new2 = event_norm(new);
		return new2;
		
	}else{
		return dupEvent(e);// new = newEvent((e->n));
	}
	return new;
}

Event* ev_join(Event* e1, Event* e2){
	if (!e1){perror("ev_join 1 "); exit(0);}
	if (!e2){perror("ev_join 2 "); exit(0);}
	//printEvent(e1);printf("\n");printEvent(e2);printf("**\n");
	//printf("ev-join\n");
	Event* new;
	int d = 0;
	if (e1->n >= 0 && e2->n >= 0){
		Event* tmp;
		if (e1->n > e2->n){	//printf("A\n");
			return ev_join(e2, e1);
		}else{ //printf("B\n");
			d = e2->n - e1->n;
			new = newEvent(e1->n);
			new->e1 = ev_join(e1->e1, lift(d, e2->e1));
			new->e2 = ev_join(e1->e2, lift(d, e2->e2));
			tmp = event_norm(new);new = tmp;
		}
	}else if (e1->n < 0 && e2->n < 0){// printf("C\n");
		new = newEvent(min(e1->n, e2->n));
	}else if (e1->n < 0 && e2->n >= 0){// printf("D\n");
		Event* aux = newEvent(ev_n(e1->n));
		aux->e1 = newEvent(-1);
		aux->e2 = newEvent(-1);
		new = ev_join(aux, e2);
	}else if (e1->n >= 0 && e2->n < 0){//printf("E\n");
		Event* aux = newEvent(ev_n(e2->n));
		aux->e1 = newEvent(-1);
		aux->e2 = newEvent(-1);
		new = ev_join(e1, aux);
	}else{ printf("Bug ev_join\n");}
	
	return new;
}

stamp* itc_seed(){
	stamp* s = (stamp*) malloc(sizeof(stamp));
	
	s->i = newId(1);
	s->e = newEvent(-1);
		
	return s;
}

int leq_ev(Event* e1, Event* e2){
	if (!e1){perror("leq_ev 1 "); exit(0);}
	if (!e2){perror("leq_ev 2 "); exit(0);}
	
	int a = 0, b = 0, c = 0;
	if (e1->n >= 0 && e2->n >= 0){//printf("A\n");
		b = leq_ev(lift(ev_n(e1->n), e1->e1), lift(ev_n(e2->n), e2->e1));
		c = leq_ev(lift(ev_n(e1->n), e1->e2), lift(ev_n(e2->n), e2->e2));
		return b && c && (ev_n(e1->n) <= ev_n(e2->n));
	}else if (e1->n >= 0 && e2->n < 0){//printf("B\n");
		b = leq_ev(lift(ev_n(e1->n), e1->e1), e2);
		c = leq_ev(lift(ev_n(e1->n), e1->e2), e2);
		return b && c && (ev_n(e1->n) <= ev_n(e2->n));
	}else{//printf("C\n");
		return (ev_n(e1->n) <= ev_n(e2->n));
	}
}

int itc_fork(stamp* s, stamp* rl, stamp* rr){
	if (!s || !s->e || !s->i){ perror("Fork "); exit(0);}
	
	rl->e = dupEvent(s->e);
	rr->e = dupEvent(rl->e);
	id_split(s->i, &(rl->i), &(rr->i));
}

int itc_peek(stamp* in, stamp* out){
	if (!in || !out){ perror("Peek "); exit(0); }
	
	out->i = newId(0);
	out->e = dupEvent(in->e);
}

int itc_join(stamp* s1, stamp* s2, stamp* sr){
	if (!s1) printf("not s1\n");
	if (!s2) printf("not s2\n");
	if (!sr) printf("not sr\n");
	sr->i = id_sum(s1->i, s2->i);
	sr->e = ev_join(s1->e, s2->e);
}

int itc_event(stamp* in, stamp* out){
	if (!in) {perror("event "); exit(0);}
	
	out->i = dupId(in->i);
	Event* e = fill(in->i, in->e);
	if (compEvent(e, in->e) == 1){
		stamp* s = grow(in->i, in->e);
		out->e = s->e;
	}else{
		out->e = e;
	}
}

char itc_leq(stamp* s1, stamp* s2){
	return leq_ev(s1->e, s2->e);
}

/* ****************************************************** */

bitArray* id_enc(Id* i, bitArray* bi){
	if(!bi || !i){perror("addbitsi "); exit(0);}
	
	if ( i->n == 0 ){//printf("id enc a1\n");
		addbits(bi, 0, 3);
	}else if ( i->n > 0 ){//printf("id enc a2\n");
		addbits(bi, 0, 2);
		addbits(bi, 1, 1); 
	}else if (i->n < 0 && i->i1->n == 0 && i->i2->n != 0){//printf("id enc b\n");
		addbits(bi, 1, 2);
		id_enc(i->i2, bi);
	}else if (i->n < 0 && i->i1->n != 0 && i->i2->n == 0){//printf("id enc c\n");
		addbits(bi, 2, 2);
		id_enc(i->i1, bi);
	}else if (i->n < 0 && i->i1->n != 0 && i->i2->n != 0){//printf("id enc d\n");
		addbits(bi, 3, 2); 	
		id_enc(i->i1, bi);
		id_enc(i->i2, bi);
	}else { printf("->-> %d, %d, %d\n",i->n, i->i1->n, i->i2->n ); exit(0); }
	
	return bi;
}

Id* id_dec(bitArray* bi){
	Id* new;
	
	int val = readbits(bi, 2);
	if ( val == 0 ){//printf("Id dec a\n");
		int x = readbits(bi, 1);//printf("chceck\n");
		new = newId(x);
	}else if ( val == 1 ){//printf("Id dec b\n");
		new = newId(-3);
		new->i1 = newId(0);
		new->i2 = id_dec(bi);
	}else if ( val == 2 ){//printf("Id dec c\n");
		new = newId(-3);
		new->i1 = id_dec(bi);
		new->i2 = newId(0);
	}else if ( val == 3 ){//printf("Id dec d\n");
		new = newId(-3);
		new->i1 = id_dec(bi);
		new->i2 = id_dec(bi);
	}else{ perror("BUG id_decI "); exit(0); }

	return new;
}

int enc_n(bitArray* be, int val, int nb){
	//printf("enc %d %d\n", val, nb);
	if (val < (1 << nb)){
		addbits(be, 0, 1);
		//printf("%d\t enc %d %d\n", be->ub, val, nb);
		addbits(be, val, nb);
	}else{
		addbits(be, 1, 1);
		enc_n(be, val - (1 << nb), nb+1);
	}
	
	return 1;	
}

bitArray* event_enc(Event* e, bitArray* be){
	if(!be || !e){perror("event_enc "); exit(0);}
	if (e->n < 0 && !e->e1 && !e->e2){
		addbits(be, 1, 1);//printf("g\n");
		enc_n(be, ev_n(e->n), 2);
	} else if (ev_n(e->n) == 0 && e->e1->n == -1 && e->e2->n != -1){//printf("a\n");
		addbits(be, 0, 1);
		addbits(be, 0, 2);
		event_enc(e->e2, be);
	} else if (ev_n(e->n) == 0 && e->e1->n != -1 && e->e2->n == -1){//printf("b\n");
		addbits(be, 0, 1);
		addbits(be, 1, 2);
		event_enc(e->e1, be);
	} else if (ev_n(e->n) == 0 && e->e1->n != -1 && e->e2->n != -1){//printf("c\n");
		addbits(be, 0, 1);
		addbits(be, 2, 2);
		event_enc(e->e1, be);
		event_enc(e->e2, be);
	} else if (ev_n(e->n) > 0 && e->e1->n == -1 && e->e2->n != -1){//printf("d\n");
		addbits(be, 0, 1);
		addbits(be, 3, 2);
		addbits(be, 0, 1);
		addbits(be, 0, 1);
		enc_n(be, ev_n(e->n), 2);
		event_enc(e->e2, be);
	} else if (ev_n(e->n) > 0 && e->e1->n != -1 && e->e2->n == -1){//printf("e\n");
		addbits(be, 0, 1);
		addbits(be, 3, 2);
		addbits(be, 0, 1);
		addbits(be, 1, 1);
		enc_n(be, ev_n(e->n), 2);
		event_enc(e->e1, be);
	} else if (ev_n(e->n) > 0 && e->e1->n != -1 && e->e2->n != -1){//printf("f\n");
		addbits(be, 0, 1);
		addbits(be, 3, 2);
		addbits(be, 1, 1);
		enc_n(be, ev_n(e->n), 2);
		event_enc(e->e1, be);
		event_enc(e->e2, be);
	} else printf("\tOOOPS :%d, %d, %d\n", e->n, e->e1->n, e->e2->n);
	
	return be;
}

int dec_n(bitArray* be){
	if (!be){perror("readbitsi "); exit(0);}
	
	unsigned int n = 0;
	int b = 2;
	while(readbits(be, 1) == 1){
		n += (1 << b);
		//printf("%d\tdec %d %d\n",be->sb, n, b);
		b++;
	}
	
	int n2 = readbits(be, b);
	n+=n2;
	//printf("val %d %d -- %d\n", n2, b, be->sb);
	return n;
}

Event* event_dec(bitArray* be){
	if(!be){perror("event_dec "); exit(0);}
	Event* new;
	
	int val = readbits(be, 1);
	if (val == 1){//printf("g\n");
		new = newEvent(-(dec_n(be))-1);
		return new;
	}else if (val == 0){
		val = readbits(be, 2);
		if (val == 0){//printf("a\n");
			new = newEvent(0);
			new->e1 = newEvent(-1);
			new->e2 = event_dec(be);
			return new;
		}else if(val == 1){//printf("b\n");
			new = newEvent(0);
			new->e1 = event_dec(be);
			new->e2 = newEvent(-1);
			return new;
		}else if(val == 2){//printf("c\n");
			new = newEvent(0);
			new->e1 = event_dec(be);
			new->e2 = event_dec(be);
			return new;
		}else if(val == 3){
			val = readbits(be, 1);
			if ( val == 0 ){ // 0
				val = readbits(be, 1);
				if (val == 0){// printf("d\n");// 0
					new = newEvent(dec_n(be));
					new->e1 = newEvent(-1);
					new->e2 = event_dec(be);
					return new; 
				}else if (val == 1){//printf("e\n");
					new = newEvent(dec_n(be));
					new->e1 = event_dec(be);
					new->e2 = newEvent(-1);
					return new;
				}else printf("BUG type d : %d\n", val);
			}else if (val == 1){//printf("f\n");
					new = newEvent(dec_n(be));
					new->e1 = event_dec(be);
					new->e2 = event_dec(be);
					return new;
			}else printf("BUG type c : %d\n", val);
			
		}else printf("BUG type b : %d\n", val);
	}else printf("BUG type a : %d\n"	, val);
	
	return NULL;
}

//--------------------------------------------------------------------------------

binStamp* encodeStamp(stamp* s){
	if (!s){perror("Encode Stamp b"); exit(0);}
	
	binStamp* bs = (binStamp*) malloc(sizeof(binStamp));
	bitArray* be = newbitArray();
	bitArray* bi = newbitArray();
	
	id_enc(s->i, bi);
	event_enc(s->e, be);

	if (bi->sb == 0){
		bs->bid = bi->array;
		bs->idsize = bi->ub;
	}
	
	if (be->sb == 0){
		bs->bevent = be->array;
		bs->eventsize = be->ub;		
	}
	
	return bs;
}

void* Encode_Stamp(stamp* s, int* len){
	if (!s){perror("Encode Stamp "); exit(0);}
	
	bitArray* be = newbitArray();
	bitArray* bi = newbitArray();
	
	id_enc(s->i, bi);
	event_enc(s->e, be);
	
	int ilen = (bi->ub + bi->fb)/8;
	int elen = (be->ub + be->fb)/8;
	
	void* res = malloc(ilen + elen + 12);
	
	void* ibin = unify(bi);
	void* ebin = unify(be);
	
	bcopy(ibin, res, 6+ilen);
	bcopy(ebin, res+6+ilen, 6+elen);
	
	*len = ilen + elen + 12;
	
	return res;
}

void* StampEncode(stamp* s, int* len){
	if (!s){perror("Encode Stamp "); exit(0);}
	
	bitArray* ba = newbitArray();
	
	id_enc(s->i, ba);
	event_enc(s->e, ba);
	
	int alen = (ba->ub + ba->fb)/8;
	
	void* res = malloc(alen);
		
	*len = alen;
	
	return ba->array;
}

stamp* decodeStamp(binStamp* bs){	
	if (!bs){perror("Decode Stamp "); exit(0);}
	
	stamp* res = (stamp*) malloc(sizeof(stamp));
	bitArray* be = newbitArray();
	bitArray* bi = newbitArray();
	
	bi->array = bs->bid;
	bi->ub = bs->idsize;
	if (bs->idsize % 8 == 0)
		bi->fb = (int)  (((bs->idsize/8))*8) - bs->idsize;
	else
		bi->fb = (int)  (((bs->idsize/8)+1)*8) - bs->idsize;
	bi->sb = 0;
	
	be->array = bs->bevent;
	be->ub = bs->eventsize;
	if (bs->eventsize % 8 == 0)
		be->fb = (int) (((bs->eventsize/8))*8) - bs->eventsize;
	else 
		be->fb = (int) (((bs->eventsize/8)+1)*8) - bs->eventsize;
	be->sb = 0;

	res->i = id_dec(bi);
	res->e = event_dec(be);
	
	return res;
}

stamp* Decode_Stamp(void* array){
	
	bitArray* be;
	bitArray* bi;
	
	stamp* res = newStamp();
	
	bi = extract(array);
	int len = (bi->ub + bi->fb)/8;
	be = extract(array+len+6);
	
	res->i = id_dec(bi);
	res->e = event_dec(be);
	
	return res;
}

stamp* StampDecode(void* array, int len){
	bitArray* ba = newbitArray();
	ba->ub = len*8;
	ba->array = array;
	
	stamp* res = newStamp();
	res->i = id_dec(ba);
	res->e = event_dec(ba);
	
	return res;
}

/*******************************************************/
/* AUXILIAR FUNCTIONS **********************************/
/*******************************************************/

int ev_n(int n){
	if (n < 0) return -n-1;
	else return n;
}

int id_n(int n){
	if (n < 0) return -n-1;
	else return n;
}

void printId(Id* i){
	if (!i){ perror("ups "); exit(0);}
	
	if (i->n >= 0){
		printf("%d", i->n);
	}else{
		printf("(");
		printId(i->i1);
		printf(",");
		printId(i->i2);
		printf(")");
	}
}

void printEvent(Event* e){
	if (!e) {perror("ups2 "); exit(0);}
	
	if (e->n < 0){
		printf("%d", -e->n -1);
	}else{
		printf("(");
		printf("%d, ",e->n );
		printEvent(e->e1);
		printf(",");
		printEvent(e->e2);
		printf(")");
	}
}

void printStamp(stamp* s){
	printf("(");
	printId(s->i);
	printf(",");
	printEvent(s->e);
	printf(" )\n");
}

int min(int a, int b){ if (a <= b) return a; else return b; }
int max(int a, int b){ if (a > b) return a; else return b; }

Id* dupId(Id* i){
	Id* new = (Id*) malloc(sizeof(Id));
	
	new->n = i->n;
	if (new->n < 0){
		new->i1 = dupId(i->i1);
		new->i2 = dupId(i->i2);
	}else{
		new->i1 = NULL;
		new->i2 = NULL;
	}
	return new;
}
Event* dupEvent(Event* e){
	Event* new = (Event*) malloc(sizeof(Event));
	new->n = e->n;
	if (new->n >= 0){
		new->e1 = dupEvent(e->e1);
		new->e2 = dupEvent(e->e2);
	}else{
		new->e1 = NULL;
		new->e2 = NULL;
	}
	return new;
}

Id* newId(int val){
	Id* i = (Id*) malloc(sizeof(Id));
	
	if (val == 0 || val == 1){
		i->n = val;
		i->i1 = i->i2 = NULL;
	}else if (val == -1){// l
		i->n = -1;
		i->i1 = newId(1);
		i->i2 = newId(0);
	}else if (val == -2){// r
		i->n = -1;
		i->i1 = newId(0);
		i->i2 = newId(1);
	}else{
		i->n = -1;
		i->i1 = i->i2 = NULL;
	}
			
	return i;
}
Event* newEvent(int val){
	Event* e = (Event*) malloc(sizeof(Event));
	
	e->n = val;
	e->e1 = e->e2 = NULL;
	return e;
}

char compEvent(Event* e1, Event* e2){
	
	if (e1->n < 0){
		if (e1->n == e2->n){ return 1; }
		else { return 0; }
	}else{
		if (e1->n == e2->n){
			return min(compEvent(e1->e1, e2->e1), compEvent(e1->e2, e2->e2));
		}else return 0;
	}
	
}

char compId(Id* i1, Id*i2){
	char res = 0;
	
	if(i1->n >= 0){
		if (i1->n == i2->n) res = 1;
		else res = 0;
	}else{
		res = min(compId(i1->i1, i1->i2), compId(i2->i1, i2->i2));
	}
	return res;
}

/* +++++++++++++++++++++++++++++++++++++++++ */

char* dectobin(void* v, int len){
	int aloc = 0;
	if (len%8) aloc = (len/8) +1;
	else aloc = len/8;
	
	char* res = calloc(len+1, sizeof(char));
	//memset(res, 'x', len);
	int i, j;
	unsigned char a = '\0';

	for(i = 0; i < aloc; i++){
		bcopy(v+i, (void*) &a, 1);
		for(j = 0; j < 8; j++){
			if ((a >> 7-j) & 0x01)
				res[8*(aloc-i-1)+j] = '1';
			else
				res[8*(aloc-i-1)+j] = '0'; 
		}
	}
	res[len+1] = '\0';
	return res;
}
