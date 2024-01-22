#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include "server.h"

int fd , errcode;
ssize_t n;
socklen_t addrlen;
struct addrinfo hints, *svinfo, *res;
struct sockaddr_in addr;
FILE *fptr;

int flag = 0, playing = 0;
char GSPORT[6], word[31];


/*typedef struct player{
    int playing = 0;
    char word[31];
    int word_len;
    int max_errors;
    int trials;
};

struct player players[999999];   */

/*---------------------------------------------------------------------*/
/*--------------------------------MAIN---------------------------------*/
/*---------------------------------------------------------------------*/

int main(int argc, char *argv[]){

    char word_file_path[20], buffer[256];

    if (argc < 2){
        perror("Missing Arguments\n");
        exit(1);
    }
    else if (argc == 2){
        strcpy(word_file_path, argv[1]);
        strcpy(GSPORT,"58038");
    }
    else if (argc == 3){
        strcpy(word_file_path,argv[1]);
        if (strcmp(argv[2], "-v") == 0)
            flag = 1;
        else 
            strcpy(GSPORT,argv[2]);
    }
    else if (strcmp(argv[2], "-v") == 0 || strcmp(argv[3],"-v") == 0){
        flag = 1;
    }
    else if (argc == 4){
        strcpy(word_file_path, argv[1]);
        strcpy(GSPORT,argv[2]);
        flag = 1;
    }
    else{
        perror("Too many arguments\n");
        exit(1);
    }

    fopen(word_file_path,"r");

    fd = socket(AF_INET, SOCK_DGRAM,0);
    if (fd == -1){
        perror("ERROR OPENING SOCKET (SERVER)\n");
        exit(EXIT_FAILURE);
    }

    memset(&hints,0,sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_DGRAM;
    hints.ai_flags = AI_PASSIVE;

    errcode = getaddrinfo(NULL,GSPORT,&hints,&res);
    if (errcode != 0){
        perror("ERROR GETTING ADRESSING\n");
        exit(1);
    }
 
    n = bind(fd,res->ai_addr, res->ai_addrlen);
    if (n < 0){
        perror("FAILE BINDING (SERVER) \n");
        exit(1);
    }

    int j = 0, i;
    while (1){
        addrlen = sizeof(addr);
        memset(buffer,'\0',sizeof(buffer));
        n = recvfrom(fd,buffer,sizeof(char)*10,0,(struct sockaddr *)&addr, &addrlen);
        if (n == -1){
            perror("ERROR RECEIVING MESSAGE (SERVER)\n");
            exit(1);
        }
        while ((buffer[j] != ' ') && (buffer[j] != '\0')){
            j++;
        }

        char c,opcode[4];

        char id[5];
        char PLID[7];

        for (i = 0; i < j; i++){
            id[i] = buffer[i];
        }  
        if (!strcmp(id,"SNG")){
            sscanf(buffer,"%s %s",opcode,PLID);
            //handle_start(PLID);
        }
      /*  else if (!strcmp(id,"PLG")){

            int trials;
            char letter;
            sscanf(buffer,"%s %s %c %d",opcode,PLID,letter,trials);
            trials += 1;
            handle_play(letter,trials);
        }*/
    }

    freeaddrinfo(res);
    close(fd);

    exit(0);
}

/*void handle_play(char letter, int trials){

}*/

void select_word(const char *file_path){

    FILE *words;
    words = fopen(&file_path,"r");



}

void handle_start(char PLID[7]){

    int playing = 0;
    char buf[12*sizeof(char)];
    int id = atoi(PLID);
    /* fazemos a verificacao se o jogador tem algum jogo ativo */
    if (playing){
        memset(buf,'\0',sizeof(buf));
        strcpy(buf,"RSG NOK");
    } else {
        strcpy(word,"ornitorrinco");
        int word_len = strlen(word);
        int max_error = max_errors();
        memset(buf,'\0',sizeof(buf));
        sprintf(buf,"RSG OK %d %d\n",word_len,max_error);
    }

    n = sendto(fd,buf,sizeof(buf),0,(struct sockaddr *)&addr, addrlen);
    if (n == -1){
        perror("ERROR SENDING MESSAGE (SERVER)\n");
        exit(1);
    }
}

int max_errors(){

    int max_errors;
    int len = strlen(word);

    if ((len <= 6) && (len >=3)){
        max_errors = 7;
    }
    else if ((len >= 7) && (len <= 10)){
        max_errors = 8;
    }
    else if ((len >= 11)){
        max_errors = 9;
    }
    else{
        printf("Word is too short\n");
    }

    return max_errors;
}
/*
int n_occurences(char letter, char PLID[7]){
    
    int id = atoi(PLID);
    printf("ID = %d\n",id);
    int pos[players[id].word_len];

    for (int i = 0; i < players[id].word_len;i++){
        if (letter == players[id].word[i]){
            pos.append(i);
        }
    }
    return pos;
}*/