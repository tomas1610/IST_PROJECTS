#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include "player.h"



int fd,errcode, trials;
ssize_t n;
socklen_t addrlen;
struct addrinfo hints, *res;
struct sockaddr_in addr;
char GSIP[15], GSPORT[6], PLID[7];


int main(int argc, char *argv[]){

    char host[256];
    char *IP;
    int read = 1, hostname;
    struct sockaddr_storage their_addr;
    socklen_t addr_len = sizeof(their_addr);
    trials = 0;

    if (argc > 3){
        printf("Excessive Arguments");
        return -1;
    }
    else if (argc == 3){
        GSIP = malloc(strlen(argv[1] + 1));
        GSPORT = malloc(strlen(argv[2] + 1));
        strcpy(GSIP,argv[1]);
        strcpy(GSPORT,argv[2]);
    }
    else if (argc == 2){
        GSIP = malloc(strlen(argv[1] + 1));
        GSPORT = malloc(strlen("58038") + 1)
        strcpy(GSIP, argv[1]);
        strcpy(GSPORT,"58038");
    }
    else if (argc == 1){
        strcpy(GSIP,"localhost");
        strcpy(GSPORT,"58038");
    }
    else {
        printf("Too many arguments\n");
        return -1;
    }

    fd = socket(AF_INET,SOCK_DGRAM,0);
    if (fd == -1){
        printf("ERROR CREATING UDP SOCKET\n");
        exit(1);
    }

    memset(&hints,0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_DGRAM;

    errcode = getaddrinfo("localhost",GSPORT,&hints,&res);
    if (errcode != 0){
        printf("ERROR\n");
        exit(1);
    }
 
    while (handle_request()){}

    freeaddrinfo(res);
    close(fd);

    return 0;
}

int handle_request(){

    char command[11];
    scanf("%s",command);

    if (strcmp(command,"start") == 0 || strcmp(command,"sg") == 0){
        start_request();
        return 1;
    }
    else if (strcmp(command,"play") == 0 || strcmp(command,"pl") == 0){
        return 1;
    }
    else if (strcmp(command,"guess") == 0 || strcmp(command,"gw") == 0){
        printf("guess\n");
        return 1;
    }
    else if (strcmp(command,"scoreboard") == 0 || strcmp(command,"sb") == 0){
        printf("scoreboard\n");
        return 1;
    }
    else if (strcmp(command,"hint") == 0 || strcmp(command,"h") == 0){
        printf("hint\n");
        return 1;
    }
    else if (strcmp(command,"state") == 0 || strcmp(command,"st") == 0){
        printf("state\n");
        return 1;
    }
    else if (strcmp(command,"quit") == 0){
        printf("quit\n");
        return 1;
    }
    else if (strcmp(command,"exit") == 0){
        printf("exit\n");
        return 0;
    }
}

void start_request(){
    
    size_t total = sizeof(char) * 10;
    char buffer[10], read_buffer[10];
    scanf("%s",PLID);
    memset(buffer,'\0',sizeof(buffer));

    strcat(buffer,"SNG ");
    strcat(buffer,PLID);
    strcat(buffer,"\n");

    printf("Buffer Inicial: %s\n",buffer);
    
    

    n = sendto(fd,buffer,sizeof(buffer),0,res->ai_addr,res->ai_addrlen);
    if (n == -1){
        printf("ERROR SENDING MESSAGE\n");
        exit(1);
    } 

    addrlen = sizeof(addr);
    n = recvfrom(fd,read_buffer,128,0,(struct sockaddr *)&addr,&addrlen);
    if (n == -1){
        printf("ERROR RECEIVING MESSAGE\n");
        exit(1);
    }

    printf("BUFFER ENVIADO PELO SERVER: %s\n",read_buffer); 
}

/*void play_request(){
    
    char buffer[14];
    char letter;

    

    printf("%c\n",letter);

    memset(buffer,'\0',sizeof(buffer));
    sprintf(buffer,"PLG %s %c %d\n",PLID,letter,trials);

    printf("Buffer: %s\n",buffer);

    n = sendto(fd,buffer,sizeof(buffer),0,res->ai_addr,res->ai_addrlen);
    if (n == -1){
        printf("ERROR SENDING MESSAGE\n");
        exit(1);
    } 


}*/
