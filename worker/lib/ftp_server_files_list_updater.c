// This tool is used to update files listing on FTP storage server.
// It depends on lftp.
//
// Usage: ftp_server_files_list_updater <run_root>
//        where <run_root> is the working dir of Nova worker module.
//
// Author::   Santa Zhang (mailto:santa1987@gmail.com)
// Since::    0.3

#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <string.h>

#include <unistd.h>
#include <signal.h>

void print_help() {
  printf("This is ftp_server_files_list_updater.\n");
  printf("It is a helper for Nova worker.\n");
  printf("\n");
  printf("Usage:\n");
  printf("    ftp_server_files_list_updater <pid_file> <run_root>\n");
  printf("    <pid_file> is the file which will contain the pid of this running deamon.\n");
  printf("    <run_root> is the working dir of Nova worker.\n");
}

int main(int argc, char* argv[]) {
  int pid;
  FILE* fp;
  char* list_fn = NULL; // will be malloc'ed later. don't use array, prevent buffer overflow
  char* run_root;
  char* pid_file;
  if (argc < 3) {
    print_help();
    return 1;
  }
  
  pid_file = argv[1];
  run_root = argv[2];

  // check if could write files_list
  // we don't free list_fn, since it will be recollected on exit
  list_fn = (char *) malloc(sizeof(char) * (strlen(run_root) + 50));
  sprintf(list_fn, "%s/ftp_server_files_list~", run_root);
  printf("checking if could write to %s\n", list_fn);
  fp = fopen(list_fn, "w");
  if (fp == NULL) {
    printf("error: cannot open file %s\n", list_fn);
  } else {
    fclose(fp);
  }

  signal(SIGCHLD, SIG_IGN); // prevent zombie process
  pid = fork();
  if (pid == 0) {
    // child process, persistantly polling ftp server for files list
    chdir(run_root);
    for (;;) {
      // The listing is first written to a "list~" file, and after all listing has been fetched, it is renamed to "list".
      // This prevents worker from reading incomplete file listing
      system("lftp -f ftp_server_files_list_updater_lftp_script 2>&1 | tee \"ftp_server_files_list~\" > /dev/null");
      rename("ftp_server_files_list~", "ftp_server_files_list");
      sleep(120); // sleep for 2 minutes
    }
  } else {
    // parent process, write pid, and exits
    fp = fopen(pid_file, "w");
    if (fp == NULL) {
      kill(pid, 9);
      printf("error: cannot open pid file %s!\n", pid_file);
      printf("ftp_server_files_list_updater terminated!\n");
      return 1;
    } else {
      fprintf(fp, "%d\n", pid);
      fclose(fp);
      printf("ftp_server_files_list_updater running with pid=%d\n", pid);
    }
  }

  return 0;
}

