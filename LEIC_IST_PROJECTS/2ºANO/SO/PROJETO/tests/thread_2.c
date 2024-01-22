#include "../fs/operations.h"
#include <assert.h>
#include <string.h>
#include <pthread.h>

void* tfs_open_tr(){
    int *f = (int*)malloc(sizeof(int));
    char *path = "/f1";
    *f = tfs_open(path,TFS_O_CREAT);
    return (void*) f;
}

int main(){

    int* f;
    pthread_t th[40];

    assert(tfs_init() != -1);

    for (int i = 0; i < 40; i++){
        if (pthread_create(&th[i],NULL,&tfs_open_tr,NULL) != 0){
            return -1;
        }
    }

    for (int i = 0; i < 40; i++){
        if (pthread_join(th[i], (void**) &f) != 0)
            return -1;
        assert(*f != -1);
        tfs_close(*f);
    }
    /* é esperado que o programa falhe na 20º iteração do for */

    printf("Successful test.\n");

    return 0;
}
