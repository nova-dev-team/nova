// This tool provides timeout on a port forwarder.
// Usage: port_fwd_daemon <timeout_minutes> <local_port> <remote_ip[:remote_port]> <log_folder> <path_to_port_mapper>
//
// Author::   Santa Zhang (santa1987@gmail.com)
// Since::    0.3

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#ifndef __APPLE__
#include <malloc.h>
#endif  // __APPLE__

#include <signal.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <time.h>

void print_help() {
  printf("Usage: port_fwd_daemon <timeout_minutes> <local_port> <remote_ip[:remote_port]> <log_folder> <path_to_port_mapper>\n");
  printf("  If you do not want time out, set 'timeout_minutes' to 0.\n");
  printf("\n");
}


// convert 1.2.3.4_80 to 1.2.3.4:80
void to_ip(char* str) {
  int i;
  for (i = 0; str[i] != '\0'; i++) {
    if (str[i] == '_') {
      str[i] = ':';
    }
  }
}


// convert 1.2.3.4:80 to 1.2.3.4_80
void to_fn(char* str) {
  int i;
  for (i = 0; str[i] != '\0'; i++) {
    if (str[i] == ':') {
      str[i] = '_';
    }
  }
}


int main(int argc, char* argv[]) {
  int timeout_minutes;
  char* local_port;
  char* fwd_addr;
  char* log_folder;
  char* path_to_port_mapper;
  char* fpath = (char *) malloc(sizeof(char) * (100 + 2 * strlen(log_folder)));
  int pid;
  printf("This is port_fwd_daemon!\n");
  if (argc < 5) {
    print_help();
    exit(0);
  }
  timeout_minutes = atoi(argv[1]);
  local_port = argv[2];
  fwd_addr = argv[3];
  log_folder = argv[4];
  path_to_port_mapper = argv[5];

  // prevent zombies
  signal(SIGCHLD, SIG_IGN);

  // first fork, make this process deamonized
  pid = fork();
  if (pid != 0) {
    // parent process, exit now
    exit(0);
  } else {
    pid = fork();
    if (pid == 0) {
      // child, run port forewarder
      execl(path_to_port_mapper, path_to_port_mapper, "-p", local_port, "-d", fwd_addr, (char *) NULL);
    } else {
      // parent, monitors the running status of child process

      FILE *fp;
      // write pid file & port file
      to_fn(fwd_addr);
      sprintf(fpath, "%s/%s.pid", log_folder, fwd_addr);
      fp = fopen(fpath, "w");
      fprintf(fp, "%d", pid);
      fclose(fp);
      sprintf(fpath, "%s/%s.local_port", log_folder, fwd_addr);
      fp = fopen(fpath, "w");
      fprintf(fp, "%s", local_port);
      fclose(fp);

      // polling the port file
      for(;;) {
        struct stat st;
        int now = time(NULL);
        if (lstat(fpath, &st) != 0 || kill(pid, 0) != 0) {
          break;
        }
        if (timeout_minutes != 0 && now - st.st_mtime > 60 * timeout_minutes) {
          break;
        }
        sleep(10);
      }

      // clean up the log files
      sprintf(fpath, "%s/%s.pid", log_folder, fwd_addr);
      remove(fpath);
      sprintf(fpath, "%s/%s.local_port", log_folder, fwd_addr);
      remove(fpath);
      sleep(10);
      kill(pid, 9);
    }
  }
  return 0;
}

