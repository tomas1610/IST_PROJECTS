#include "../fs/operations.h"
#include <assert.h>
#include <string.h>
#include <pthread.h>

char buffer[40];

void* tfs_write_tr(){
    ssize_t *r = (ssize_t*)malloc(sizeof(ssize_t));
    char *path = "/f1";
    char *str = "AAA!";
    int f = tfs_open(path, TFS_O_CREAT);
    *r = tfs_write(f,str,strlen(str));
    return (void*) r;
}

void* tfs_read_tr(){
    char *path = "/f1";
    int f = tfs_open(path,TFS_O_CREAT);
    tfs_read(f,buffer,sizeof(buffer) - 1);
    return 0;
}

int main(){

    size_t* r;
    pthread_t th[20];
    char *path = "/f1";
    char *str = "AAA!";

    assert(tfs_init() != -1);

    int f;

    f = tfs_open(path, TFS_O_CREAT);
    assert(f != -1);

    assert(tfs_close(f) != -1);


    for (int i = 0; i < 10; i++){
        if (pthread_create(&th[i],NULL,&tfs_write_tr,NULL) != 0){
            return -1;
        }
    }


    for (int i = 0; i < 10; i++){
        if (pthread_join(th[i], (void**) &r) != 0)
            return -1;
        assert(*r == strlen(str));
    }

    assert(tfs_close(f) != -1);

    for (int i = 10; i < 20; i++){
        if (pthread_create(&th[i],NULL,&tfs_read_tr,NULL) != 0){
            return -1;
        }
    }

    for (int i = 10; i < 20; i++){
        if (pthread_join(th[i],(void**) &r) != 0)
            return -1;
        assert(strcmp(buffer, str) == 0);
    }


    assert(tfs_close(f) != -1);

    printf("Successful test.\n");

    return 0;
}