#include <unistd.h>
#include <dirent.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <math.h>

// CONSTANTS
#define MAX_BUF_LEN 100
#define MAX_WORD_LEN 150
#define OPCODE_LEN 3
#define PLID_LENGTH 7
#define WORD_LEN 31


int fd , errcode, verbose;
int pos_left[31];
ssize_t n;
socklen_t addrlen;
struct addrinfo hints, *res, *p;
struct sockaddr_in addr, sv_addr;
char *word_path, *GSPORT, *opcode, *image_name, *PLID;
char buffer[MAX_BUF_LEN], verbose_msg[MAX_BUF_LEN];
char palavra[256], word[WORD_LEN], path_aux[22];

struct stat file_stat;

typedef struct{
    int playing;
    FILE *game;
    char path[22];
    char path_scores[13];
    char PLID[PLID_LENGTH];
    int max_errors;
    int errors;
    char word[WORD_LEN];
    char guessing_word[WORD_LEN];
    int len;
    int trials;
    char guesses[27];
}player;

player games[999999];

void init(){

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
        perror("FAIL BINDING (SERVER) \n");
        exit(1);
    }
}

void game_finished(int id, char code){

    char new_name[100];
    struct stat sb;
    DIR *dir;
    FILE *score_f;
    
    
    fclose(games[id].game);

    if (stat(games[id].path,&file_stat) != 0){
        perror("ERROR GETTING THE FILE INFORMATION\n");
        exit(1);
    }

    dir = opendir(games[id].path_scores);
    if (!dir){
        if (mkdir(games[id].path_scores,0700) != 0){
            printf("ERROR CREATING DIRECTORY\n");
            exit(1);
        }
    }
    else{
        closedir(dir);
    }
    
    time_t t = time(NULL);
    struct tm *tm = localtime(&t);

    char time_str[20];

    strftime(time_str,sizeof(time_str),"%Y%m%d_%H%M%S",tm); 

    sprintf(new_name,"GAMES/%s/%s_%c.txt", PLID,time_str,code);       

    if (rename(games[id].path, new_name) != 0){
        perror("Error renaming file");
        exit(1);
    }

    memset(new_name,'\0',sizeof(new_name));
    int n_succ = games[id].trials - games[id].errors;
    int score = (double)n_succ/ (float)games[id].trials * 100;
    char score_str[14];
    sprintf(score_str,"%03d",score);
    sprintf(new_name,"SCORES/%s_%s_%s.txt",score_str,PLID,time_str);
    score_f = fopen(new_name,"w");
    if (score_f == NULL){
        perror("ERROR CREATING SCORE FILE\n");
        exit(1);
    }

    fprintf(score_f,"%s %s %s %d %d\n",score_str,PLID,games[id].word,n_succ,games[id].trials);
    fclose(score_f);

    memset(games[id].path,'\0',sizeof(games[id].path));
    memset(games[id].PLID,'\0',PLID_LENGTH);
    games[id].max_errors = 0;
    memset(games[id].word,'\0',games[id].len);
    memset(games[id].guessing_word,'\0',games[id].len);
    memset(palavra,'\0',sizeof(palavra));
    games[id].errors = 0;
    games[id].len = 0;
    games[id].trials = 1;
    games[id].playing = 0;
    image_name = NULL;
    opcode = NULL;
    PLID = NULL;
    memset(games[id].guesses,'\0',27);
}

int present_array(char element, int id){

    size_t len = sizeof(games[id].guesses)/sizeof(int);
    int i;

    for (i = 0; i < len; i++){
        if (games[id].guesses[i] == element)
            return 1;
    }
    return 0;
}

int is_playing(const char *id){

    int  id_player = atoi(id);

    if (games[id_player].playing){
        return 1;
    }

    games[id_player].game = fopen(games[id_player].path,"r");
    if (games[id_player].game != NULL){
        fclose(games[id_player].game);
        return 1;
    }

    return 0;
}

int valid_plid(const char *id){

    int len = strlen(id), i;

    if (len < 6)
        return 0;

    for (i = 0; i < len; i++){
        if (id[i] < '0' || id[i] > '9')
            return 0;
    }
    
    return 1;
}

int check_if_win(int id){

    if (!strcmp(games[id].word,games[id].guessing_word))
        return 1;
    return 0;
}

int random_line(){

    FILE *f;
    int lines = 0;

    f = fopen(word_path,"r");
    if (f == NULL){
        perror("Unable to open the file\n");
        exit(1);
    }

    char line[256];

    while (fgets(line,sizeof(line),f) != NULL){
        lines++;
    }

    fclose(f);

    return rand() % lines;

}

char *choose_word(){

    FILE *f;
    int n_line,i, count = 1;
    char line[256];

    n_line = random_line(word_path);

    f = fopen(word_path,"r");
    if (f == NULL){
        perror("Unable to open the file\n");
        exit(1);
    }


    while(fgets(line,sizeof(line),f) != NULL){
        if (count == n_line)
            strcpy(palavra,line);
        memset(line,'\0',sizeof(line));
        count++;
    }


    fclose(f);
    palavra[strlen(palavra)-1] = '\0';
    sscanf(palavra,"%s",word);
    image_name = malloc(strlen(palavra) - strlen(word));
    sscanf(palavra,"%s %s",word,image_name);

    return word;
}

int max_errors(char word[]){

    int len = strlen(word);
    int max_errors;

    if (len >= 3 && len <= 6)
        max_errors = 7;
    else if (len >= 7 && len <= 10)
        max_errors = 8;
    else    
        max_errors = 9;

    return max_errors;
}

void handle_start(){
    
    int i, id;
    PLID = malloc(PLID_LENGTH + 1);
    char status[4], path_aux[22];

    sscanf(buffer,"%s %s",opcode,PLID);
    printf("PLID START = %s\n",PLID);
    id = atoi(PLID);
    memset(path_aux,'\0',sizeof(path_aux));
    sprintf(path_aux,"GAMES/GAME_%s.txt",PLID);
    strcpy(games[id].path,path_aux);
    memset(path_aux,'\0',sizeof(path_aux));
    sprintf(path_aux,"GAMES/%s",PLID);
    strcpy(games[id].path_scores,path_aux);
    
    if (is_playing(PLID)){                 
        sprintf(buffer,"RSG NOK\n");
        sprintf(verbose_msg,"PLID = %s: already playing\n",PLID);
    }
    else{
        games[id].playing = 1;
        strcpy(games[id].word,choose_word());
        strcpy(games[id].PLID,PLID);
        games[id].len = strlen(games[id].word);
        games[id].max_errors = max_errors(games[id].word);
        games[id].errors = 0;
        games[id].trials = 1;
        memset(games[id].guessing_word,' ',games[id].len);
        games[id].guessing_word[games[id].len] = '\0';
        sprintf(buffer,"RSG OK %d %d\n",games[id].len,games[id].max_errors);
        sprintf(verbose_msg,"PLID=%s: new game; word = \"%s\" (%d letter)\n",PLID,games[id].word,games[id].len);
        games[id].game = fopen(games[id].path,"w");
        fprintf(games[id].game,"%s %s\n",games[id].word,image_name);
    }

    n = sendto(fd,buffer,strlen(buffer),0,(struct sockaddr *)&addr, addrlen);
    if (n == -1){
        perror("ERROR SENDING MESSAGE (SERVER)\n");
        exit(1);
    }

}

int n_occ_letter_in_word(char letter){

    size_t len = strlen(word);
    int i, occ = 0;

    for (i = 0; i < len; i++){
        if (word[i] == letter){
            occ++;
        }
    }

    return occ;
}

void handle_play(){

    int occ;
    char letter;
    int n_trial, id;
    char status[4];

    sscanf(buffer,"%s %s %c %d",opcode,PLID,&letter,&n_trial);
    id = atoi(PLID);

    if (!is_playing(PLID) || !valid_plid(PLID)){
        sprintf(buffer,"RLG ERR\n");
        n = sendto(fd,buffer,strlen(buffer),0,(struct sockaddr *)&addr, addrlen);
        if (n == -1){
            perror("ERROR SENDING MESSAGE (SERVER)\n");
            exit(1);
        }
        sprintf(verbose_msg,"Player %s doesnt have an active game\n",PLID);
        return;
    }   
    else if (present_array(letter,id)){
        sprintf(buffer,"RLG DUP %d\n",games[id].trials);
        n = sendto(fd,buffer,strlen(buffer),0,(struct sockaddr *)&addr, addrlen);
        if (n == -1){
            perror("ERROR SENDING MESSAGE (SERVER)\n");
            exit(1);
        }
        sprintf(verbose_msg,"Player %s has already sent the letter %c\n",PLID,letter);
        return;
    }
    else if (n_trial != games[id].trials){
        sprintf(buffer,"RLG INV %d\n",games[id].trials);
        n = sendto(fd,buffer,strlen(buffer),0,(struct sockaddr *)&addr, addrlen);
        if (n == -1){
            perror("ERROR SENDING MESSAGE (SERVER)\n");
            exit(1);
        }
        sprintf(verbose_msg,"Client number of trials is different from server trials\n");
        return;
    }

    games[id].guesses[n_trial] = letter;
    occ = n_occ_letter_in_word(letter);

    if (occ > 0){
        sprintf(buffer,"RLG OK %d %d",games[id].trials,occ);
        int len = games[id].len; \
        int i;
        char pos_matched[4];

        for (i = 0; i < len; i++){
            if (letter == games[id].word[i]){
                sprintf(pos_matched," %d",i+1);
                strcat(buffer,pos_matched);
                memset(pos_matched,'\0',sizeof(pos_matched));
                games[id].guessing_word[i] = letter;
            }
        }

        strcat(buffer,"\n");
        if (check_if_win(id)){
            memset(buffer,'\0',sizeof(buffer));
            sprintf(buffer,"RLG WIN %d\n",games[id].trials);
            sprintf(verbose_msg,"PLID=%s: play letter \"%c\" - %d hits; WIN (game ended)\n",PLID,letter,occ);
            fprintf(games[id].game,"T %c\n",letter);
            game_finished(id,'W');
        }
        else {
            fprintf(games[id].game,"T %c\n",letter);
            sprintf(verbose_msg,"PLID=%s: play letter \"%c\" - %d hits; word not guessed\n",PLID,letter,occ);
        }
    }
    else {
        games[id].errors++;
        if (games[id].errors < games[id].max_errors){
            sprintf(buffer,"RLG NOK %d\n",games[id].trials);
            fprintf(games[id].game,"T %c\n",letter);
            sprintf(verbose_msg,"PLID=%s: play letter \"%c\" - %d hits; word not guessed\n",PLID,letter,occ);
        }
        else {
            sprintf(buffer,"RLG OVR %d\n",games[id].trials);
            sprintf(verbose_msg,"PLID=%s: play letter \"%c\" - %d hits; OVER (game ended)\n",PLID,letter,occ);
            fprintf(games[id].game,"T %c\n",letter);
            game_finished(id,'F');
        }
    }
    games[id].trials++;
    n = sendto(fd,buffer,strlen(buffer),0,(struct sockaddr *)&addr, addrlen);
    if (n == -1){
        perror("ERROR SENDING MESSAGE (SERVER)\n");
        exit(1);
    }

}

void handle_guess(){

    int occ;
    int n_trial;
    char status[4], guess_word[31];

    sscanf(buffer,"%s %s %s %d",opcode,PLID,guess_word,&n_trial);
    memset(buffer,'\0',sizeof(buffer));

    int id = atoi(PLID);

    if (!is_playing(PLID) || !valid_plid(PLID)){
        sprintf(buffer,"RWG ERR\n");
        n = sendto(fd,buffer,sizeof(buffer),0,(struct sockaddr *)&addr, addrlen);
        if (n == -1){
            perror("ERROR SENDING MESSAGE (SERVER)\n");
            exit(1);
        }
        sprintf(verbose_msg,"Player %s doesnt have an active game\n",PLID);
        return;
    } 
    else if (n_trial != games[id].trials){
        sprintf(buffer,"RWG INV %d\n",games[id].trials);
        n = sendto(fd,buffer,sizeof(buffer),0,(struct sockaddr *)&addr, addrlen);
        if (n == -1){
            perror("ERROR SENDING MESSAGE (SERVER)\n");
            exit(1);
        }
        sprintf(verbose_msg,"Client number of trials is different from server trials\n");
        return;
    }

    if (!strcmp(games[id].word,guess_word)){
        sprintf(buffer,"RWG WIN %d\n",games[id].trials);
        sprintf(verbose_msg,"Player %s: Correct Guess, word was %s\n",PLID,games[id].word);
        fprintf(games[id].game,"G %s\n",guess_word);
        game_finished(id,'W');
    }
    else {
        games[id].errors++;
        if (games[id].errors < games[id].max_errors){
            sprintf(buffer,"RWG NOK %d\n",games[id].trials);
            sprintf(verbose_msg,"Player %s: Guess was not correct\n",PLID);
            fprintf(games[id].game,"G %s\n",guess_word);
        }
        else {
            sprintf(verbose_msg,"Player %s: Guess was not correct and no left attempts\n",PLID);
            sprintf(buffer,"RWG OVR %d\n",games[id].trials);
            fprintf(games[id].game,"G %s\n",guess_word);
            game_finished(id,'F');
        }
    
    //games[id].game = fopen(games[id].path,"w");
    //fclose(games[id].game);

    }

    games[id].trials++;
    n = sendto(fd,buffer,strlen(buffer),0,(struct sockaddr *)&addr, addrlen);
    if (n == -1){
        perror("ERROR SENDING MESSAGE (SERVER)\n");
        exit(1);
    }

}

void handle_quit(){

    char PLID_AUX[7];
    int id;
    sscanf(buffer,"QUT %s",PLID_AUX);
    memset(buffer,'\0',sizeof(buffer));
    PLID = PLID_AUX;
    id = atoi(PLID);
    if (is_playing(PLID)){  
        sprintf(buffer,"RQT OK\n");
        sprintf(verbose_msg,"Player %s: Quitted Game\n",PLID);
        game_finished(id,'Q');
    }
    else {
        sprintf(verbose_msg,"Player %s: doesnt have an active game to quit\n",PLID);
        sprintf(buffer,"RQT ERR\n");
    }

    n = sendto(fd,buffer,strlen(buffer),0,(struct sockaddr *)&addr, addrlen);
    if (n == -1){
        perror("ERROR SENDING MESSAGE (SERVER)\n");
        exit(1);
    }
}

void handle_rev(){

    sscanf(buffer,"REV %s",PLID);
    int id = atoi(PLID);
    memset(buffer,'\0',sizeof(buffer));

    if (is_playing(PLID)){
        sprintf(buffer,"RRV %s/OK\n",games[id].word);
        sprintf(verbose_msg,"Player %s: REV command, word was %s\n",PLID,games[id].word);
    }
    else {
        sprintf(buffer,"RRV ERR\n");
        sprintf(verbose_msg,"Player %s: Has no active game\n",PLID) ;
    }

    n = sendto(fd,buffer,strlen(buffer),0,(struct sockaddr *)&addr, addrlen);
    if (n == -1){
        perror("ERROR SENDING MESSAGE (SERVER)\n");
        exit(1);
    }
}

int main(int argc, char *argv[]){

    if (argc == 1){
        perror("MISSING ARGUMENTS (WORD_FILE)\n");
        exit(0);
    }
    else if (argc == 2){      
        word_path = malloc(strlen(argv[1]) + 1); 
        GSPORT = malloc(strlen("58038") + 1);
        strcpy(word_path,argv[1]);
        strcpy(GSPORT,"58038");
        verbose = 0;

    }
    else if (argc > 2 && argc < 5){
        if (!strcmp(argv[2],"-v")){
            word_path = malloc(strlen(argv[1]) + 1); 
            strcpy(word_path,argv[1]);
            verbose = 1;
            GSPORT = malloc(strlen("58038") + 1);
            strcpy(GSPORT,"58038");
        }
        else if (argc == 4){
            verbose = 0;
            word_path = malloc(strlen(argv[1]) + 1);
            GSPORT = malloc(strlen(argv[3]) + 1);
            strcpy(word_path,argv[1]);
            strcpy(GSPORT,argv[3]);
        }
    }
    else if (argc == 5){
        word_path = malloc(strlen(argv[1]) + 1);
        GSPORT = malloc(strlen(argv[3]) + 1);
        strcpy(word_path,argv[1]);
        strcpy(GSPORT,argv[3]);
        verbose = 1;
    }
    else {
        perror("TOO MANY ARGUMENTS\n");
        exit(0);
    }

    printf("PORT = %s\n",GSPORT);
    init();
    printf("SERVER RUNNING\n");

    addrlen = sizeof(addr);


    while(1){
        opcode = malloc(OPCODE_LEN + 1);
        memset(buffer,'\0',sizeof(buffer));
        memset(verbose_msg,'\0',sizeof(verbose_msg));

        n = recvfrom(fd,buffer,sizeof(buffer),0,(struct sockaddr *)&addr, &addrlen);
        if (n == -1){
            perror("ERROR RECEIVING MESSAGE (SERVER)\n");
            exit(1);
        }

        sscanf(buffer,"%s",opcode);

        if (!strcmp(opcode,"SNG")){
            handle_start();
        }
        else if (!strcmp(opcode,"PLG")){
            handle_play();
        }
        else if (!strcmp(opcode,"PWG")){
            handle_guess();
        }
        else if (!strcmp(opcode,"QUT")){
            handle_quit();
        }
        else if (!strcmp(opcode,"REV")){
            handle_rev();
        }

        if (verbose){
            printf("IP = %s ; PORT = %s\n",inet_ntoa(addr.sin_addr),GSPORT);
            printf("%s",verbose_msg);
        }

    }

    freeaddrinfo(res);
    free(PLID);
    free(image_name);
    free(GSPORT);
    free(word_path);
    free(opcode);

    return 0;
}