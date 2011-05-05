#include <stdio.h>
#include <dirent.h>
#include <unistd.h>
#include <sys/types.h>

#include "agent_common.h"


unsigned int find_min_dirname(char *path) {
  DIR *dd;
  struct dirent *dirp;
  unsigned int n, min;
  dd = opendir(path);
  if (dd == NULL) {
    perror("cannot open queue dir");
    return -1;
  }

  min = -1;
  while ((dirp = readdir(dd)) != NULL) {
    n = atoi(dirp->d_name);
    if (n > 0 && n < min) min = n;
  }
  return min;
}

void set_status(char *basepath, const char *status) {
  FILE *fp;
  char filename[LENGTH_PATH];
  bzero(filename, LENGTH_PATH);
  sprintf(filename, "%s/status", basepath);
  printf("status filename = %s\n", filename);
  fp = fopen(filename, "w");
  if (fp == NULL) {
    printf("cannot open status file!\n");
    return;
  }
  fprintf(fp, "%s", status);
  fclose(fp);
}

int get_folder_name(char *ftp_address, char *buffer) {
  int len;
  int i, j;
  len = strlen(ftp_address);
  printf("ftp address = %s, len = %d\n", ftp_address, len);

  for (i = len - 1; i > 0; --i)
    if (ftp_address[i] == '/') {
      printf("find ! j = %d\n", i);
      for (j = i + 1; j < len; ++j) buffer[j - i - 1] = ftp_address[j];
      buffer[j - i - 1] = '\0';
      return 0;
    }
  return -1;
}

int main() {
  while (1) {
    unsigned int min;
    int ret;
    char src_path[LENGTH_PATH];
    char dst_path[LENGTH_PATH];
    char cfg_path[LENGTH_PATH];
    char command_file[LENGTH_PATH];
    char command[LENGTH_LINE];
    char command_line[LENGTH_LINE];
    char app_name[LENGTH_PATH];
    FILE *fp;

    sleep(5);
    min = find_min_dirname(DIR_QUEUE);
    //1.find min dir
    bzero(src_path, LENGTH_PATH);
    bzero(dst_path, LENGTH_PATH);
    sprintf(src_path, "%s/%u", DIR_QUEUE, min);
    sprintf(dst_path, "%s/%u", DIR_HISTORY, min);


    printf("src path = %s\n", src_path);
    printf("dst path = %s\n", dst_path);
    ret = rename(src_path, dst_path);
    if (ret != 0) {
      perror("cannot move jobdir to workspace");
      continue;
    }
    //2.mv to history

    bzero(command, LENGTH_LINE);
    bzero(command_line, LENGTH_LINE);

    bzero(command_file, LENGTH_PATH);

    sprintf(command_file, "%s/command", dst_path);
    printf("opening %s\n", command_file);

    fp = fopen(command_file, "r");
    if (fp == NULL) {
      perror("cannot open command file");
      continue;
    }

    fscanf(fp, "%s", command);
    fclose(fp);

    printf("command = %s\n", command);

    //switch to working space
    //use wget -r -nH to download "virus" package
    bzero(app_name, LENGTH_PATH);
    get_folder_name(command, app_name);

    printf("app_name = [%s]\n", app_name);
    set_status(dst_path, "downloading");
    printf("2\n");
    sprintf(command_line, "cd %s && wget -r %s -nH", dst_path, command);
    system(command_line);


    set_status(dst_path, "configuring");
    bzero(cfg_path, LENGTH_PATH);
    sprintf(cfg_path, "%s/%s/config", dst_path, app_name);
    mkdir(cfg_path, 0755);
    bzero(command_line, LENGTH_LINE);
    sprintf(command_line, "cp node.list %s/", cfg_path);
    system(command_line);
    // mkdir dst_path/app_name/config
    // copy node.list to dir above
    //
    set_status(dst_path, "processing");

    bzero(command_line, LENGTH_LINE);
    sprintf(command_line, "cd %s/%s && /bin/sh ./entry.sh", dst_path, app_name);
    printf("command line = %s\n", command_line);
    system(command_line);

    set_status(dst_path, "finished");
    //3.process it
    // 3.1 read 'command' file
    // 3.2 set status "downloading"
    // 3.3 wget command . local
    // 3.4 set status "processing"
    // 3.5 run entry.sh
    // 3.6 set status "finished"


  }
  return 0;
}

