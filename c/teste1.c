#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <stdint.h>

#include<netdb.h>
#include<errno.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<sys/un.h>
#include<signal.h>

#include "queue.h"
#include "itc.h"

#define SENDER_SOCKET "/tmp/sender_socket"

#define BUFF_SIZE 256

// connection sockets
int clinsock, s_socket;

void sighand(int sig){
	
	unlink(SENDER_SOCKET);

	close(clinsock);
	close(s_socket);
	printf("Dying: client\n") ;
	exit(0);
}

int main(){
	//stamp* seed = (stamp*) malloc(sizeof(stamp)); // criar função que cria novo stamp
	
	// sig hadller
	signal(SIGINT, sighand );
	
	stamp* seed = itc_seed(seed);
	
	stamp* a = newStamp();
	stamp* b = newStamp();
	stamp* c = newStamp();
	
	itc_fork(seed, a, b);
	
	itc_event(a, a);
	itc_event(b, b);
	
	itc_fork(a, a, c);
	itc_event(b, b);
	
	itc_event(a, a);
	itc_join(b, c, b);
	
	itc_fork(b, b, c);
	
	itc_join(a, b, a);
	
	itc_event(a, a);

	printStamp(a);
	int len;
	void* cois = StampEncode(a, &len);
	
	uint16_t shorty = 0x0000;
	bcopy(cois, (void*) &shorty, 2);
	
	stamp* res = newStamp();
	res = StampDecode(cois, len);
	
	queue* saco = newQueue();
	itc_fork(a, a, b);
	itc_fork(a, a, c);
	itc_event(a, a);
	itc_event(a, a);
	enqueue(saco, (void*) a, -1);
	
	printStamp(res);
	
	
	// --------------------------------
	// connect to java
	
	// remote client socket
	int s_port, n; 
	struct sockaddr_in s_addr; 
	struct hostent *server = NULL; 
	char* buffer = (char*)calloc(BUFF_SIZE, sizeof(char));
	
	s_socket = socket(PF_INET, SOCK_STREAM, 0); 
			
	char* host = "localhost";
	
	server = gethostbyname(host);
	s_port = 6666;
	
	
	if (!server) {
		perror("no server : client "); 
		sighand(0); 
	} 
	
	memset(&s_addr, '\0', sizeof(s_addr));
	s_addr.sin_family = AF_INET; 
	s_addr.sin_addr =*((struct in_addr*)server->h_addr);
	s_addr.sin_port = htons(s_port);
	
	if (connect(s_socket,(struct sockaddr*) &s_addr,sizeof(struct sockaddr)) < 0){ 
		perror("connect remote : client "); 
		sighand(0); 
	}
	
	uint32_t length;
	void* tosend = StampEncode(a, &length);
	void* coisoo;
	switchEndianess(tosend, &coisoo, length);
	int comp = length;
	length = htonl(length);
	printf("%d\n", length);
	void* def = malloc(comp + 4);

	bcopy((void*)&length, def, 4);
	bcopy(coisoo, def+4, comp);
	printStamp(a);
	comp = send(s_socket,def, comp+4,0); 
	if (comp < 0){
		perror("connection sending : client ");
		sighand(0);
	}
	//---------------------------------
	/*FILE* fp = fopen("bfile", "wb");
	
	saveQueue(saco, "bfile");
	
	queue* saco2 = newQueue();
	loadQueue(saco2, "filebin");*/
	//int ccc;
	//printStamp(dequeue(saco2, -1, &ccc));
	//printQueue(saco2);
	
	/*uint32_t n = 1;
	fwrite((void*) &n, sizeof(uint32_t), 1, fp);
	
	uint32_t lenn = 0x00000000;
	void* ax = StampEncode(res, (int*) &lenn);
	
	saveFormated(fp, ax, lenn);
	fclose(fp);*/
	
	/*fp = fopen("binaryfile", "rb");
	uint32_t nn = 0x00000000;
	fread((void*) &nn, sizeof(uint32_t), 1, fp);
	
	int lenn2 = ntohl(nn);printf("check1 %d\n", lenn2);
	void* xa = NULL;
	loadFormated(fp, &xa, &lenn2);printf("check2 %d\n", lenn2);
//	fclose(fp);
	
	printStamp(StampDecode(xa, lenn2));*/
	
	return 0;
}
