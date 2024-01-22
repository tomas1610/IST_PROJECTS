//test.c

#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
extern int errno;

int main(void)
{
char buffer[128];

if(gethostname(buffer,128)==-1)
	fprintf(stderr,"error: %s\n",strerror(errno));

else printf("host name: %s\n",buffer);
exit(0);
}
