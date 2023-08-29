#include "operations.h"
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <errno.h>
#include <pthread.h>

int tfs_init() {
    state_init();

    /* create root inode */
    int root = inode_create(T_DIRECTORY);
    if (root != ROOT_DIR_INUM) {
        return -1;
    }

    return 0;
}

int tfs_destroy() {
    state_destroy();
    return 0;
}

static bool valid_pathname(char const *name) {
    return name != NULL && strlen(name) > 1 && name[0] == '/';
}


int tfs_lookup(char const *name) {
    if (!valid_pathname(name)) {
        return -1;
    }

    // skip the initial '/' character
    name++;

    return find_in_dir(ROOT_DIR_INUM, name);
}

int tfs_open(char const *name, int flags) {
    int inum;
    size_t offset;

    /* Checks if the path name is valid 
    * lock name */
    if (!valid_pathname(name)) {
        /* unlock name */
        return -1;
    }

    inum = tfs_lookup(name);
    if (inum >= 0) {
        /* The file already exists */
        inode_t *inode = inode_get(inum);
        if (inode == NULL) {
            return -1;
        }
        pthread_rwlock_wrlock(&inode->rwlock);
        /* Trucate (if requested) */
        if (flags & TFS_O_TRUNC) {
            if (inode->i_size > 0) {
                if (data_block_free(inode->i_data_block) == -1) {
                    pthread_rwlock_unlock(&inode->rwlock);
                    return -1;
                }
                inode->i_size = 0;
            }
        }
        /* Determine initial offset */
        if (flags & TFS_O_APPEND) {
            offset = inode->i_size;
        } else {
            offset = 0;
        }
        pthread_rwlock_unlock(&inode->rwlock);
    } else if (flags & TFS_O_CREAT) {
        /* The file doesn't exist; the flags specify that it should be created*/
        /* Create inode */
        inum = inode_create(T_FILE);
        if (inum == -1) {
            return -1;
        }
        /* Add entry in the root directory */
        if (add_dir_entry(ROOT_DIR_INUM, inum, name + 1) == -1) {
            inode_delete(inum);
            return -1;
        }
        offset = 0;
    } else {
        return -1;
    }
    /* Finally, add entry to the open file table and
     * return the corresponding handle */
    return add_to_open_file_table(inum, offset);

    /* Note: for simplification, if file was created with TFS_O_CREAT and there
     * is an error adding an entry to the open file table, the file is not
     * opened but it remains created */
}


int tfs_close(int fhandle) { return remove_from_open_file_table(fhandle); }

void allocate_blocks(open_file_entry_t *file ,inode_t *inode, size_t to_write){

    int i, amount = (int)((file->of_offset + to_write) / BLOCK_SIZE) + 1;

    for(i = 0; i < amount; i++){
        if (i <= 9){
            if (inode->d_data_block[i] == -1){
                inode->d_data_block[i] = data_block_alloc();
            }
        }
        else{   
                void *block = data_block_get(inode->i_data_block);
                if (((int*)block)[i-10] == 0)
                    ((int*)block)[i-10] = data_block_alloc();
        }
    }
}

void write_aux_rec(open_file_entry_t *file, inode_t *inode, void const *buffer, size_t to_write){
    size_t amount_left = inode->amount_written_last;
    void *block, *block_i;

    if (to_write == 0)
        return;
    
    int block_id = (int)(file->of_offset / BLOCK_SIZE);
    size_t block_offset = file->of_offset % BLOCK_SIZE;
    size_t fill_block = BLOCK_SIZE - amount_left;
    if (to_write > fill_block){
        if (block_id <= 9){
            block = data_block_get(inode->d_data_block[block_id]);
        }
        else {
            block_i = data_block_get(inode->i_data_block);
            block = data_block_get(((int*)block_i)[block_id-10]);
        }
        inode->amount_written_last = 0;
        memcpy(block + block_offset, buffer, fill_block);
        file->of_offset += fill_block;
        write_aux_rec(file,inode,buffer, to_write - fill_block);
    }
    else {
        if (block_id <= 9){
            block = data_block_get(inode->d_data_block[block_id]);
        }
        else {
            block_i = data_block_get(inode->i_data_block);
            block = data_block_get(((int*)block_i)[block_id-10]);
        }
        inode->amount_written_last = amount_left + to_write;
        memcpy(block + block_offset, buffer, to_write);
        file->of_offset += to_write;
        write_aux_rec(file,inode,buffer, 0);
    } 
}

ssize_t tfs_write(int fhandle, void const *buffer, size_t to_write) {
    open_file_entry_t *file = get_open_file_entry(fhandle);
    if (file == NULL) {
        return -1;
    }
    pthread_mutex_lock(&file->file_lock);
    /* From the open file table entry, we get the inode */
    inode_t *inode = inode_get(file->of_inumber);
    if (inode == NULL) {
        pthread_mutex_unlock(&file->file_lock);
        return -1;
    }
    pthread_rwlock_wrlock(&inode->rwlock);
    /* Determine how many bytes to write */
    if (to_write + file->of_offset > (BLOCK_SIZE/sizeof(int) + 10) * BLOCK_SIZE) {
        to_write = (BLOCK_SIZE/sizeof(int) + 10) * BLOCK_SIZE - file->of_offset;
    }

    if (to_write > 0) {      

        if (inode->i_size == 0){
            inode->i_data_block = data_block_alloc();
        }


        allocate_blocks(file,inode,to_write);
        write_aux_rec(file,inode,buffer,to_write);
        
        /* The offset associated with the file handle is
         * incremented accordingly */
        if (file->of_offset > inode->i_size) {
            inode->i_size = file->of_offset;
        }
    }

    pthread_rwlock_unlock(&inode->rwlock);
    pthread_mutex_unlock(&file->file_lock);
    return (ssize_t)to_write;
}

void read_aux(open_file_entry_t *file, inode_t *inode, void *buffer, size_t to_read){

    size_t block_offset = (file->of_offset % BLOCK_SIZE);
    int block_index = (int)(file->of_offset / BLOCK_SIZE);
    void *block, *i_block;

    if (block_offset + to_read <= BLOCK_SIZE){
        if (block_index <= 9){
            block = data_block_get(inode->d_data_block[block_index]);
        }
        else{
            i_block = data_block_get(inode->i_data_block);
            block = data_block_get(((int*)i_block)[block_index-10]);
        }
        memcpy(buffer, block + block_offset, to_read);
        file->of_offset += to_read;
        return;
    }
    else{
        size_t fill_block = BLOCK_SIZE - block_offset;
        if (block_index <= 9){
            block = data_block_get(inode->d_data_block[block_index]);
        }
        else{
            i_block = data_block_get(inode->i_data_block);
            block = data_block_get(((int*)i_block)[block_index-10]);
        }
        memcpy(buffer, block + block_offset, fill_block);
        file->of_offset += fill_block;
        read_aux(file,inode,buffer, to_read - fill_block);
    }
}

ssize_t tfs_read(int fhandle, void *buffer, size_t len) {
    open_file_entry_t *file = get_open_file_entry(fhandle);
    if (file == NULL) {
        return -1;
    }
    pthread_mutex_lock(&file->file_lock);

    /*From the open file table entry, we get the inode*/
    inode_t *inode = inode_get(file->of_inumber);
    if (inode == NULL) {
        pthread_mutex_unlock(&file->file_lock);
        return -1;
    }
    pthread_rwlock_rdlock(&inode->rwlock);

    /*Determine how many bytes to read*/
    size_t to_read = inode->i_size - file->of_offset;
    if (to_read > len) {
        to_read = len;
    }

    if (to_read > 0) {
        void *block = data_block_get(inode->i_data_block);
        if (block == NULL) {
            pthread_rwlock_unlock(&inode->rwlock);
            pthread_mutex_unlock(&file->file_lock);
            return -1;
        }

        /*Perform the actual read*/
        read_aux(file, inode, buffer, to_read);


        /*The offset associated with the file handle is
          incremented accordingly*/
    }

    pthread_rwlock_unlock(&inode->rwlock);
    pthread_mutex_unlock(&file->file_lock);
    return (ssize_t)to_read;
}

int tfs_copy_to_external_fs(char const *source_path, char const *dest_path){

    FILE *fp;
    /* lock source_path */
    if (!valid_pathname(source_path) || tfs_open(source_path,0) == -1)
        return -1;
    /* lock dest_path*/
    fp = fopen(dest_path,"w");
    /* lock fp */
    if (fp == NULL){
        return -1;
    }
    fclose(fp);
    int inumber_source = tfs_lookup(source_path);
    open_file_entry_t *source = get_open_file_entry(inumber_source);
    inode_t *inode = inode_get(source->of_inumber);
    size_t size = inode->i_size;
    char input[size];
    tfs_open(source_path,0);
    tfs_read(inumber_source, input,size);
    printf("%d %ld %ld\n",source->of_inumber,sizeof(input), source->of_offset);
    tfs_close(inumber_source);
    fp = fopen(dest_path,"w");
    fwrite(input, 1, size, fp);
    fclose(fp);

    return 0;
}