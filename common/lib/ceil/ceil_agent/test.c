#include <stdio.h>
#include <dirent.h>
#include <sys/types.h>
#include <unistd.h>


int main() {
  DIR *dd;
  struct dirent *dirp;
  int n;

  dd = opendir("./queue");
  if (dd == NULL) {
    printf("failed\n");
    exit(1);
  }

  while ((dirp = readdir(dd)) != NULL) {
    printf("%s\n", dirp->d_name);
    n = atoi(dirp->d_name);
    printf("%d\n", n);
  }
  mkdir("jj", 0755);
  rename("/tmp/big", "/home/fermion/biga");

  return 0;
}
