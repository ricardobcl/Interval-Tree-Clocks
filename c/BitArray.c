#include "BitArray.h"

/*typedef struct bitArray{
	int ub;
	int fb;
	int sb;
	void* array;
}bitArray;*/

bitArray* newbitArray(){
	bitArray* new = (bitArray*) malloc(sizeof(bitArray));
	new->ub = 0;
	new->fb = 0;
	new->sb = 0;
	new->array = NULL;

	return new;
}

int addbits(bitArray* be, int val, int n){
	if (!be) {perror("addbits "); exit(0);}
	
	unsigned int len = be->fb + be->ub;
	len /= 8; // len to bytes
	
	if (be->fb < n){
		int tlen = len;
		if (len == 0){
			len = 1;
		}else{
			len *= 2;
		}
		
		void* tmp = malloc(len);
		memset(tmp, '\0', len);
		bcopy(be->array, tmp, tlen);
		
		be->array = tmp;
		be->fb += (len-tlen) * 8;
	}
	
	unsigned int jump = (be->ub / 8);
	unsigned int bjump = be->ub % 8;
	unsigned int xxx = 0;
	unsigned int xx2 = 0;
	
	bcopy(be->array + jump, (void*) &xxx, min(len-jump, sizeof(xxx)));
	xx2 = (unsigned int) val;
	
	xx2 = xx2 << bjump;
	xxx = xx2 | xxx;
	
	bcopy((void*) &xxx, be->array + jump, min(len-jump, sizeof(xxx)));
	be->ub += n;
	be->fb -= n;
	
	return 1;
}

int readbits(bitArray* be, int n){
	if (!be) {perror("readbits "); exit(0);}
	
	unsigned int t = 0;
	
	unsigned int size = (be->ub + be->fb)/8;
	unsigned int jump = be->sb / 8;
	unsigned int bjump = be->sb % 8;
	
	bcopy(be->array + jump, (void*) &t, min(size - jump, sizeof(t)));
	unsigned int t2 ;
	
	if (n > 1) t2 = (1 << n) -1; 	
	else t2 = 1;
	
	t2 = t2 << bjump;
	t2 = t2 & t;
	t2 = t2 >> bjump;
	be->sb += n;
	
	return t2;	
}

void* unify(bitArray* b){
	unsigned int len = (b->ub + b->fb)/8;
	
	void* res = malloc(len+6);
	memset(res, '\0', len+6);
	
	uint16_t some_short = 0xffff;
	
	some_short = b->ub;
	some_short = htons(some_short);
	bcopy((void*) &some_short, res, 2);
	bcopy(res, (void*) &some_short, 2);
	
	some_short = b->fb;
	some_short = htons(some_short);
	bcopy((void*) &some_short, res+2, 2);
	
	some_short = b->sb;
	some_short = htons(some_short);
	bcopy((void*) &some_short, res+4, 2);
	
	bcopy(b->array, res+6, len);
	
	uint16_t shorty = 0x0000;
	bcopy(res, (void*) &shorty, 2);
	
	return res;
}

bitArray* extract(void* array){
	
	bitArray* res = newbitArray();
	
	uint16_t some_short = 0;
	
	bcopy(array, (void*) &some_short, 2);
	res->ub = (int) ntohs(some_short);
	
	bcopy(array+2, (void*) &some_short, 2);
	res->fb = (int) ntohs(some_short);
	
	bcopy(array+4, (void*) &some_short, 2);
	res->sb = (int) ntohs(some_short);
	
	printf("%d, %d, %d\n", res->ub, res->fb, res->sb);
	int len = (res->ub + res->fb)/8;
	res->array = malloc(len);

	res->array = array+6;
	
	return res;
}

int saveFormated(FILE* fp, void* array, int len){
	if (!fp){perror("save : Bad file pointer."); return -1;}
	
	uint32_t comp = (uint32_t) len;
	comp = htonl(comp);

	if (fwrite((void*) &comp, sizeof(uint32_t), 1, fp) != 1)
		{perror("save : Could not save length"); return -1;}
	
	int x;
	for (x = len-1; x >= 0; x--){
		if (fwrite(array+x, 1, 1, fp) != 1)
			{perror("save : Could not save data"); return -1;}
	}

	return 1;
}

int switchEndianess(void* in, void** out, int len){
	if(!in){perror("save : Bad file pointer."); return -1;}
	
	*out = malloc(len);
	
	int i;
	for (i = 0; i < len; i++){
		bcopy(in+i, (*out)+(len-1-i), 1);
	}
}

int loadFormated(FILE* fp, void** array, int* len){
	if (!fp){perror("load : Bad file pointer"); return -1;}
	
	uint32_t comp = 0x00000000;
	
	if (fread((void*) &comp, sizeof(uint32_t), 1, fp) != 1)
		{perror("load : Could not read length"); return -1;}
	comp = ntohl(comp);
	*len = (int) comp;
	
	*array = malloc(comp);
	int x;
	for(x = comp-1; x >= 0; x--){
		if(fread(*array+x, 1, 1, fp) != 1)
			{perror("load : Could not read data"); return -1;}
	}
	
	return 1;
}
