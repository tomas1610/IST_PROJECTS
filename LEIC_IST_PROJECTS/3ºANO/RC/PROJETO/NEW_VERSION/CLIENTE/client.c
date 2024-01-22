#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include "player.h"

// CONSTANTS
#define PLID_LENGTH 6
#define MAX_BUF_LEN 100

int fd,errcode, n_trial, word_size, max_errors, errs;
ssize_t n;
socklen_t addrlen;
struct addrinfo hints, *res;
struct sockaddr_in addr;
struct timeval time;
char *GSPORT, *GSIP;
char word[31];


void init_UDP(){

    fd = socket(AF_INET,SOCK_DGRAM,0);
    if (fd == -1){
        printf("ERROR CREATING UDP SOCKET\n");
        exit(1);
    }

    memset(&hints,0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_DGRAM;

    errcode = getaddrinfo(GSIP,GSPORT,&hints,&res);
    if (errcode != 0){
        printf("ERROR\n");
        exit(1);
    }
    
    time.tv_sec = 10;
    time.tv_usec = 0;
    if (setsockopt(fd,SOL_SOCKET,SO_RCVTIMEO,&time,sizeof(time)) < 0)
        exit(1);

}

void start_request(char PLID[]){

    char status[4],buffer[MAX_BUF_LEN];
     
    scanf("%s",PLID);
    memset(buffer,'\0',sizeof(buffer));
    sprintf(buffer,"SNG %s\n", PLID);

    n = sendto(fd,buffer,strlen(buffer),0,res->ai_addr,res->ai_addrlen);
    if (n == -1){
        printf("ERROR SENDING MESSAGE\n");
        exit(1);
    } 

    memset(buffer,'\0',sizeof(buffer));
    addrlen = sizeof(addr);
    n = recvfrom(fd,buffer,MAX_BUF_LEN,0,(struct sockaddr *)&addr,&addrlen);
    if (n == -1){
        printf("ERROR RECEIVING MESSAGE\n");
        exit(1);
    }


    sscanf(buffer,"RSG %s",status);

    if (!strcmp(status,"OK")){
        sscanf(buffer,"RSG %s %d %d",status,&word_size,&max_errors);
        memset(word,'_',word_size);
        printf("New game started (max %d errors): %s\n",max_errors,word);
    }
    else if (!strcmp(status,"NOK")){
        printf("Already Playing\n");
    }

}

void play_request(char PLID[]){

    char letter;
    char status[4],buffer[MAX_BUF_LEN];
    int offset, n_pos, i, pos,pos_aux,trial,count,aux;

    scanf(" %c",&letter);

    memset(buffer,'\0',sizeof(buffer));
    sprintf(buffer,"PLG %s %c %d\n",PLID,letter,n_trial);

    n = sendto(fd,buffer,strlen(buffer),0,res->ai_addr,res->ai_addrlen);
    if (n == -1){
        printf("ERROR SENDING MESSAGE\n");
        exit(1);
    } 

    memset(buffer,'\0',sizeof(buffer));
    addrlen = sizeof(addr);
    n = recvfrom(fd,buffer,MAX_BUF_LEN,0,(struct sockaddr *)&addr,&addrlen);
    if (n == -1){
        printf("ERROR RECEIVING MESSAGE\n");
        exit(1);
    }

    sscanf(buffer,"RLG %s",status);

    if (!strcmp(status,"OK")){
        i = 0;
        pos = 0;
        count = 0;
        aux = 0;
        sscanf(buffer,"RLG %s %d %d%n",status,&trial,&n_pos,&offset);
        offset++;
        while (count < n_pos){
            if (buffer[offset] >= '0' && buffer[offset] <= '9'){
                pos_aux = buffer[offset] - '0';
                if (aux){
                    pos = pos * 10 + pos_aux;
                }
                else{
                    pos = pos_aux;
                }
                aux = 1;
            }
            if ((buffer[offset] == ' ' || buffer[offset] == '\0' || buffer[offset] == '\n') && aux == 1){
                aux = 0;
                word[pos-1] = letter;
                count++;
                pos = 0;
            }
            offset++;
            i++;
        }
        n_trial++;
        printf("Yes, \"%c\" is part of the word: %s\n",letter,word);
    }


    if (!strcmp(status,"WIN")){
        for (i = 0;i < word_size; i++){
            if (word[i] == '_'){
                word[i] = letter;
            }
        }
        printf("WELL DONE! You guessed: %s in %d trials\n",word, n_trial);
        errs = 0;
        n_trial = 1;
    }
    else if (!strcmp(status, "NOK")){
        n_trial++;
        errs++;
        printf("No, \"%c\" is not part of word, You still have %d lives; word: %s\n",letter,max_errors - errs,word);
    }
    else if (!strcmp(status, "OVR")){
        n_trial = 1;
        errs = 0;
        printf("GAME OVER! You can start a new game\n");
    }
    else if (!strcmp(status,"DUP")){
        printf("Duplicate letter\n");
    }
    else if (!strcmp(status,"ERR")){
        printf("NO ACTIVE GAME\n");
    }

}

void guess_request(char PLID[]){

    char word_guess[31], status[4],buffer[MAX_BUF_LEN];

    scanf("%s",word_guess);

    memset(buffer,'\0',sizeof(buffer));
    sprintf(buffer,"PWG %s %s %d\n",PLID,word_guess,n_trial);

    n = sendto(fd,buffer,strlen(buffer),0,res->ai_addr,res->ai_addrlen);
    if (n == -1){
        printf("ERROR SENDING MESSAGE\n");
        exit(1);
    } 

    memset(buffer,'\0',sizeof(buffer));
    addrlen = sizeof(addr);
    n = recvfrom(fd,buffer,MAX_BUF_LEN,0,(struct sockaddr *)&addr,&addrlen);
    if (n == -1){
        printf("ERROR RECEIVING MESSAGE\n");
        exit(1);
    }

    sscanf(buffer,"RWG %s",status);
    if (!strcmp(status,"WIN")){
        errs = 0;
        printf("WELL DONE! You guessed: %s in %d trials\n",word_guess, n_trial);
        n_trial = 1;
    }
    else if (!strcmp(status,"NOK")){
        errs++;
        n_trial++;
        printf("Keep Trying, you still has %d lives: word = %s\n",max_errors - errs,word);
    }
    else if (!strcmp(status,"OVR")){
        printf("No more lives! GAME OVER\n");
        errs = 0;
        n_trial = 1;
    }
    else if (!strcmp(status,"ERR")){
        printf("NO ACTIVE GAME\n");
    }
    else if (!strcmp(status,"INV")){
        printf("TRIAL ERROR\n");
    }
}

void quit_request(char PLID[]){

    char status[4],buffer[MAX_BUF_LEN];


    memset(buffer,'\0',sizeof(buffer));
    sprintf(buffer,"QUT %s\n",PLID);
    n = sendto(fd,buffer,strlen(buffer),0,res->ai_addr,res->ai_addrlen);
    if (n == -1){
        printf("ERROR SENDING MESSAGE\n");
        exit(1);
    } 


    memset(buffer,'\0',sizeof(buffer));
    addrlen = sizeof(addr);
    n = recvfrom(fd,buffer,MAX_BUF_LEN,0,(struct sockaddr *)&addr,&addrlen);
    if (n == -1){
        printf("ERROR RECEIVING MESSAGE\n");
        exit(1);
    }

    sscanf(buffer,"RQT %s\n",status);

    if (!strcmp(status,"OK")){
        n_trial = 1;
        printf("Game Ended for %s\n",PLID);
    }
    else if (!strcmp(status,"ERR")){
        printf("NO ACTIVE GAME\n");
    }

}

void rev_request(char PLID[]){

    char status[4], opcode[4], word_rev[word_size + 1],buffer[MAX_BUF_LEN];

    memset(buffer,'\0',sizeof(buffer));
    sprintf(buffer,"REV %s\n",PLID);

    n = sendto(fd,buffer,strlen(buffer),0,res->ai_addr,res->ai_addrlen);
    if (n == -1){
        printf("ERROR SENDING MESSAGE\n");
        exit(1);
    } 

    errs = 0;
    memset(buffer,'\0',sizeof(buffer));
    addrlen = sizeof(addr);
    n = recvfrom(fd,buffer,MAX_BUF_LEN,0,(struct sockaddr *)&addr,&addrlen);
    if (n == -1){
        printf("ERROR RECEIVING MESSAGE\n");
        exit(1);
    }

    n = sscanf(buffer,"RRV %s",word_rev);

    if (!strcmp(word_rev,"ERR")){
        printf("NO ACTIVE GAME\n");
    }
    else {
        printf("Your word was %s\n",word_rev);
    }

}

void handle_scoreboard(char PLID[]){

    ssize_t n;
    int tcp_fd, tcp_err, offset;
    socklen_t tcp_addrlen;
    struct addrinfo tcp_hints,*tcp_res;
    struct sockaddr_in tcp_addr;
    char buffer[MAX_BUF_LEN+1], status[6],filename[25], filesizestr[10];
    size_t bytes_read , bytes_write, file_size, total;
    FILE *sb;


    tcp_fd = socket(AF_INET,SOCK_STREAM,0);
    if (tcp_fd == -1){
        perror("ERROR CREATING TCP CONNECTION\n");
        exit(1);
    }

    memset(&tcp_hints,0,sizeof(tcp_hints));
    tcp_hints.ai_family = AF_INET;
    tcp_hints.ai_socktype = SOCK_STREAM;

    tcp_err = getaddrinfo(GSIP,GSPORT,&tcp_hints,&tcp_res);
    if (tcp_err != 0){
        perror("ERROR GETTING ADRRESS\n");
        exit(1);
    }

    n = connect(tcp_fd,tcp_res->ai_addr,tcp_res->ai_addrlen);
    if (n == -1){
        perror("ERROR CONNECTING\n");
        exit(1);
    }

    n = write(tcp_fd,"GSB\n",4);
    if (n == -1){
        perror("ERROR SENDING MESSAGE FROM SCOREBOARD\n");
        exit(1);
    }

    memset(buffer,'\0',sizeof(buffer));
    bytes_read = read(tcp_fd,buffer,sizeof(buffer));
    if (bytes_read == -1){
        perror("ERROR RECEIVING MESSAGE FROM SCOREBOARD\n");
        exit(1);
    }

    sscanf(buffer,"RSB %s",status);

    if (!strcmp(status,"EMPTY")){
        printf("No games have been played yet\n");
        return;
    }
    else if (!strcmp(status,"OK")){
        memset(buffer,'\0',sizeof(buffer));
        bytes_read = read(tcp_fd,buffer,sizeof(buffer));
        if (bytes_read < 0){
            perror("ERROR READING BUFFER\n");
            exit(1);
        }   
        sscanf(buffer,"%s %s %n",filename,filesizestr,&offset);
        total = 0;
        bytes_read = bytes_read - (strlen(filename) + strlen(filesizestr) + 2);
        sb = fopen(filename,"w");
        if (sb == NULL){
            perror("ERROR OPENING FILE FOR SCOREBOARD\n");
            exit(1);
        }
        file_size = atoi(filesizestr); 
        bytes_write = fwrite(buffer+offset,1,bytes_read,sb);
        printf("%s",buffer+offset);
    
        total += bytes_write;

       while (total < file_size && bytes_read > 0){
            memset(buffer,'\0',sizeof(buffer));
            if (total + MAX_BUF_LEN > file_size){
                bytes_read = read(tcp_fd,buffer,file_size-total);
                bytes_write = fwrite(buffer,1,bytes_read,sb);
                total += bytes_write;
            }
            else{
                bytes_read = read(tcp_fd,buffer,sizeof(buffer));
                bytes_write = fwrite(buffer,1,bytes_read,sb);
                total += bytes_write;
            }
            printf("%s",buffer);
        }
    }
    else{
        printf("ERR: MESSAGE SENT WASN'T RECOGNIZED\n");
    }

    fclose(sb);
    freeaddrinfo(tcp_res);
    close(tcp_fd);

}

void handle_hint(char PLID[]){

    int tcp_fd, tcp_err, offset, len;
    socklen_t tcp_addrlen;
    struct addrinfo tcp_hints,*tcp_res;
    struct sockaddr_in tcp_addr;
    size_t bytes_read, bytes_write, total, file_max;
    char filename[25], filesize[11], status[4], PLID_AUX[PLID_LENGTH+1], msg[12], image[256],buffer[MAX_BUF_LEN+1];
    FILE *ht;

    
    tcp_fd = socket(AF_INET,SOCK_STREAM,0);
    if (tcp_fd == -1){
        perror("ERROR CREATING TCP CONNECTION\n");
        exit(1);
    }

    memset(&tcp_hints,0,sizeof(tcp_hints));
    tcp_hints.ai_family = AF_INET;
    tcp_hints.ai_socktype = SOCK_STREAM;

    tcp_err = getaddrinfo(GSIP,GSPORT,&tcp_hints,&tcp_res);
    if (tcp_err != 0){
        perror("ERROR GETTING ADRRESS\n");
        exit(1);
    }

    n = connect(tcp_fd,tcp_res->ai_addr,tcp_res->ai_addrlen);
    if (n == -1){
        perror("ERROR CONNECTING\n");
        exit(1);
    }

    sprintf(msg,"GHL %s\n",PLID);                // PODEMOS CONSIDERAR QUE TEMOS SEMPRE UM PLID LIDO ?
    n = write(tcp_fd,msg,strlen(msg));
    if (n == -1){
        perror("ERROR SENDING MESSAGE FROM HINT\n");
        exit(1);
    }

    memset(buffer,'\0',sizeof(buffer));
    bytes_read = read(tcp_fd,buffer,7);
    if (bytes_read <= 0){
        perror("ERROR RECEIVING MESSAGE FROM HINT\n");
        exit(1);
    }

    sscanf(buffer,"RHL %s",status);
    memset(buffer,'\0',sizeof(buffer));

    if (!strcmp(status,"OK")){
        
        strcpy(PLID_AUX,PLID);

        bytes_read = read(tcp_fd,buffer,256);

        if (bytes_read <= 0){
            perror("ERROR READING BUFFER\n");
            exit(1);
        }  

        sscanf(buffer,"%s %s %n",filename,filesize,&offset);


        ht = fopen(filename,"wb");
        if (!ht){
            freeaddrinfo(tcp_res);
            close(tcp_fd);
            perror("ERROR OPENING FILE\n");
            exit(1);
        }

        n = 1;
        total = 0;
     
        bytes_read = bytes_read - (strlen(filename) + strlen(filesize) + 2);
    
        file_max = atoi(filesize); 
        bytes_write = fwrite(buffer+offset,1,bytes_read,ht);
    
        total += bytes_write;
        memset(buffer,'\0',sizeof(buffer));


        memset(image,'\0',sizeof(image));
        while (total < file_max && bytes_read > 0){
            
            if (total + 256 > file_max){
                bytes_read = read(tcp_fd,image,file_max-total);
                bytes_write = fwrite(image,1,bytes_read,ht);
                total += bytes_write;
            }
            else{
                bytes_read = read(tcp_fd,image,256);
                bytes_write = fwrite(image,1,bytes_read,ht);
                total += bytes_write;
            }
            memset(image,'\0',sizeof(image));
        }

       //strcpy(PLID,PLID_AUX);

        printf("filename = %s (%ld bytes)\n",filename,file_max);
    }
    else if (!strcmp(status,"NOK")){
        printf("No hint to show\n");
    }


    fclose(ht); 
    freeaddrinfo(tcp_res);
    close(tcp_fd);
}

void handle_state(char PLID[]){

    int tcp_fd, tcp_err, offset;
    socklen_t tcp_addrlen;
    struct addrinfo tcp_hints,*tcp_res;
    struct sockaddr_in tcp_addr;
    char status[4], msg[12], buffer[MAX_BUF_LEN+1], filename[25], filesizestr[11];
    size_t bytes_write, bytes_read, total, file_size;
    FILE *st;


    tcp_fd = socket(AF_INET,SOCK_STREAM,0);
    if (tcp_fd == -1){
        perror("ERROR CREATING TCP CONNECTION\n");
        exit(1);
    }

    memset(&tcp_hints,0,sizeof(tcp_hints));
    tcp_hints.ai_family = AF_INET;
    tcp_hints.ai_socktype = SOCK_STREAM;

    tcp_err = getaddrinfo(GSIP,GSPORT,&tcp_hints,&tcp_res);
    if (tcp_err != 0){
        perror("ERROR GETTING ADRRESS\n");
        exit(1);
    }

    n = connect(tcp_fd,tcp_res->ai_addr,tcp_res->ai_addrlen);
    if (n == -1){
        perror("ERROR CONNECTING\n");
        exit(1);
    }

    sprintf(msg,"STA %s\n",PLID);                // PODEMOS CONSIDERAR QUE TEMOS SEMPRE UM PLID LIDO ?
    n = write(tcp_fd,msg,strlen(msg));
    if (n == -1){
        perror("ERROR SENDING MESSAGE FROM SCOREBOARD\n");
        exit(1);
    }

    memset(buffer,'\0',sizeof(buffer));
    n = read(tcp_fd,buffer,MAX_BUF_LEN);
    if (n == -1){
        perror("ERROR RECEIVING MESSAGE FROM SCOREBOARD\n");
        exit(1);
    }

    memset(status,'\0',sizeof(status));
    sscanf(buffer,"RST %s",status);

    if (!strcmp(status,"ACT")){
        
        memset(buffer,'\0',sizeof(buffer));
        bytes_read = read(tcp_fd,buffer,sizeof(buffer));
        if (bytes_read < 0){
            perror("ERROR READING BUFFER\n");
            exit(1);
        }   
        sscanf(buffer,"%s %s %n",filename,filesizestr,&offset);
        total = 0;
        bytes_read = bytes_read - (strlen(filename) + strlen(filesizestr) + 2);
        st = fopen(filename,"w");
        if (st == NULL){
            perror("ERROR OPENING FILE FOR SCOREBOARD\n");
            exit(1);
        }
        file_size = atoi(filesizestr); 
        bytes_write = fwrite(buffer+offset,1,bytes_read,st);
        printf("%s",buffer+offset);
    
        total += bytes_write;

       while (total < file_size && bytes_read > 0){
            memset(buffer,'\0',sizeof(buffer));
            if (total + MAX_BUF_LEN > file_size){
                bytes_read = read(tcp_fd,buffer,file_size-total);
                bytes_write = fwrite(buffer,1,bytes_read,st);
                total += bytes_write;
            }
            else{
                bytes_read = read(tcp_fd,buffer,sizeof(buffer));
                bytes_write = fwrite(buffer,1,bytes_read,st);
                total += bytes_write;
            }
            printf("%s",buffer);
        }

    }
    else if (!strcmp(status,"FIN")){
        
        memset(buffer,'\0',sizeof(buffer));
        bytes_read = read(tcp_fd,buffer,sizeof(buffer));
        if (bytes_read < 0){
            perror("ERROR READING BUFFER\n");
            exit(1);
        }   
        sscanf(buffer,"%s %s %n",filename,filesizestr,&offset);
        total = 0;
        bytes_read = bytes_read - (strlen(filename) + strlen(filesizestr) + 2);
        st = fopen(filename,"w");
        if (st == NULL){
            perror("ERROR OPENING FILE FOR SCOREBOARD\n");
            exit(1);
        }
        file_size = atoi(filesizestr); 
        bytes_write = fwrite(buffer+offset,1,bytes_read,st);
        printf("%s",buffer+offset);
    
        total += bytes_write;

       while (total < file_size && bytes_read > 0){
            memset(buffer,'\0',sizeof(buffer));
            if (total + MAX_BUF_LEN > file_size){
                bytes_read = read(tcp_fd,buffer,file_size-total);
                bytes_write = fwrite(buffer,1,bytes_read,st);
                total += bytes_write;
            }
            else{
                bytes_read = read(tcp_fd,buffer,sizeof(buffer));
                bytes_write = fwrite(buffer,1,bytes_read,st);
                total += bytes_write;
            }
            printf("%s",buffer);
        }

    }
    else if(!strcmp(status,"NOK")){
        printf("No register of game was found\n");
        return;
    }
    else if (!strcmp(status,"ERR")){
        printf("Other Error happened : State");
        return;
    }

    fclose(st);
    freeaddrinfo(tcp_res);
    close(tcp_fd);

}

int handle_request(){

    char command[11];
    char PLID[7];
    scanf("%s",command);

    if (strcmp(command,"start") == 0 || strcmp(command,"sg") == 0){
        errs = 0;
        start_request(PLID);
    }
    else if (strcmp(command,"play") == 0 || strcmp(command,"pl") == 0){
        play_request(PLID);
    }
    else if (strcmp(command,"guess") == 0 || strcmp(command,"gw") == 0){
        guess_request(PLID);
    }
    else if (strcmp(command,"scoreboard") == 0 || strcmp(command,"sb") == 0){
        handle_scoreboard(PLID);
    }
    else if (strcmp(command,"hint") == 0 || strcmp(command,"h") == 0){
        handle_hint(PLID);
    }
    else if (strcmp(command,"state") == 0 || strcmp(command,"st") == 0){
        handle_state(PLID);
    }
    else if (strcmp(command,"rev") == 0){
        rev_request(PLID);
    }
    else if (strcmp(command,"quit") == 0){
        quit_request(PLID);
    }
    else if (strcmp(command,"exit") == 0){
        quit_request(PLID);
        return 0;
    }

    return 1;
} 


/*--------------------------------------------------------------------------
---------------------------------MAIN---------------------------------------
--------------------------------------------------------------------------*/

int main(int argc, char *argv[]){

    if (argc > 5){
        printf("Excessive Arguments");
        return -1;
    }
    else if (argc == 5){
        GSIP = malloc(strlen(argv[2]) + 1);
        GSPORT = malloc(strlen(argv[4]) + 1);
        strcpy(GSIP,argv[2]);
        strcpy(GSPORT,argv[4]);
    }
    else if (argc == 3){
        if (!strcmp(argv[1],"-n")){
            GSIP = malloc(strlen(argv[2]) + 1);
            strcpy(GSIP,argv[2]);
            GSPORT = malloc(strlen("58038") + 1);
            strcpy(GSPORT,"58038");
        }
        else if (!strcmp(argv[1],"-p")){
            GSIP = malloc(strlen("localhost") + 1);
            strcpy(GSIP,"localhost");
            GSPORT = malloc(strlen(argv[2]) + 1);
            strcpy(GSPORT,argv[2]);
        }
    }
    else if (argc == 1){
        GSIP = malloc(strlen("localhost") + 1);
        GSPORT = malloc(strlen("58038") + 1);
        strcpy(GSIP,"localhost");
        strcpy(GSPORT,"58038");
    }
    else {
        printf("Input Wrong\n");
        return -1;
    }

    init_UDP();

    n_trial = 1;    

    while(handle_request()){}

    freeaddrinfo(res);
    close(fd);
    free(GSPORT);
    free(GSIP);

    printf("END GAME\n");

    return 0;


}