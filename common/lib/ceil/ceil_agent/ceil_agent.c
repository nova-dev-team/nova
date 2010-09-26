#include <stdio.h>
#include <dirent.h>
#include <unistd.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include "agent_common.h"

void error(char *msg) {
  perror(msg);
  exit(1);
}

void split_cmd(char *buffer, char *jobname) {
  int i, sp; 
  bzero(jobname, LENGTH_JOB);
  for (sp = 0; sp < strlen(buffer); ++sp) 
    if (buffer[sp] == ' ') break;
  strcpy(jobname, &buffer[sp + 1]);
}

int str_startwith(char *mother, char *child) {
  int a, b, i;
  a = strlen(mother);
  b = strlen(child);
  if (a < b)
    return -1;
  for (i = 0; i < b; ++i)
    if (mother[i] != child[i]) return -1;
  return 0;
}

int valid_dir(const char *dirname) {
  if (strcmp(dirname, ".")) return -1;
  if (strcmp(dirname, "..")) return -1;
  return 0;
}

int fd_send(int sockfd, char *msg) {
  return write(sockfd, msg, strlen(msg));
}

int find_max_dirname(char *path) {
  DIR *dd;
  struct dirent *dirp;
  int n, max;
  dd = opendir(path);
  if (dd == NULL) {
    perror("cannot open queue dir");
    return -1;
  }

  max = 0;
  while ((dirp = readdir(dd)) != NULL) {
    n = atoi(dirp->d_name);
    if (n > max) max = n;
  }
  return max;
}

int write_file(char *basepath, char *filename, char *content) {
  char pbuf[LENGTH_PATH]; 
  FILE *fp;
  bzero(pbuf, LENGTH_PATH);
  sprintf(pbuf, "%s/%s", basepath, filename);
  fp = fopen(pbuf, "w");
  if (fp == NULL) return -1;
  fputs(content, fp);
  fclose(fp);
}

int addjob(char *basepath, char *jobname, char *command) {
  int max, ret;
  char pbuf[LENGTH_PATH]; 
  max = find_max_dirname(basepath);
  max = max + 1;
  bzero(pbuf, LENGTH_PATH);
  sprintf(pbuf, "%s/%d", basepath, max);
  
  //mkdir pbuf
  ret = mkdir(pbuf, 0755);
  if (ret == 0) {
    //write file pbuf/jobname -> jobname
    //write file pbuf/command -> command
    ret = write_file(pbuf, "jobname", jobname);
    if (ret != 0) {
      perror("failed on writing jobname");
      return -1;
    }
    ret = write_file(pbuf, "command", command);
    if (ret != 0) {
      perror("failed on writing command");
      return -1;
    }
  } else {
    perror("failed on creating job folder");
    return -1;
  }
}

int deljob(char *basepath, char *jobname) {
  //TODO delete a job
  return 0;
}

//walk through the DIR_QUQUE dir
//for every job_dir, read the 'status' file

char *get_status(char *basepath) {
  char *buffer;
  char jobname[LENGTH_JOB];
  char status[LENGTH_LINE];
  char pbuf[LENGTH_PATH];

  DIR *dd;
  struct dirent *dirp;
  dd = opendir(basepath);
  if (dd == NULL) {
    perror("cannot open queue dir");
    return NULL;
  }
  while ((dirp = readdir(dd)) != NULL) {
    if (valid_dir(dirp->d_name)) {
      FILE *fp;
      //open basepath/dirp->d_name/status and jobname
      bzero(pbuf, LENGTH_PATH);
      bzero(jobname, LENGTH_JOB);
      sprintf(pbuf, "%s/%s/jobname", basepath, dirp->d_name);
      fp = fopen(pbuf, "r");
      fscanf(fp, "%s", jobname);
      fclose(fp);
      
      bzero(pbuf, LENGTH_PATH);
      bzero(status, LENGTH_LINE);
      sprintf(pbuf, "%s/%s/status", basepath, dirp->d_name);
      fp = fopen(pbuf, "r");
      fscanf(fp, "%s", status);
      fclose(fp);

      printf("[%s]%s\n", jobname, status);
    }
  }
  return 0;

}

int main(int argc, char *argv[]) {
  int sockfd, clientfd, portno;
  char buffer[LENGTH_LINE];
  char jobname[LENGTH_JOB];
  char password[LENGTH_PASSWORD];

  int n, ret, reuse;
  struct sockaddr_in server_addr, client_addr;
  int client_len;
  int pid;

  portno = 32167;
  sockfd = socket(AF_INET, SOCK_STREAM, 0);
  if (sockfd < 0)
    error("ERROR on opening socket");
  bzero((char *) &server_addr, sizeof(server_addr));
  
  server_addr.sin_family = AF_INET;
  server_addr.sin_addr.s_addr = INADDR_ANY;
  server_addr.sin_port = htons(portno);

  setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse));

  if (bind(sockfd, (struct sockaddr *) &server_addr, sizeof(server_addr)) < 0)
    error("ERROR on binding");

  listen(sockfd, 5);
  client_len = sizeof(client_addr);
  bzero(password, LENGTH_PASSWORD);
  sprintf(password, "12345678");

  while (1) {
    clientfd = accept(sockfd, (struct sockaddr *) &client_addr, &client_len);
    if (clientfd < 0) {
      perror("ERROR on accept");
    } else {
      bzero(buffer, 256);
      n = read(clientfd, buffer, 255);
      if (n < 0) {
        perror("ERROR reading from socket");
      } else {
        //new command
        printf("[MSG]%s\n", buffer);
        if (strcmp(buffer, password) != 0) {  
          perror("password not match");
          n = fd_send(clientfd, "access denied");
          close(clientfd);
        } else {
          n = fd_send(clientfd, "access permited");
          bzero(buffer, 256);
          n = read(clientfd, buffer, 255);

          //doing things here
          if (strcmp(buffer, "status") == 0) {
            // TODO return working status
          } else 
          if (str_startwith(buffer, "addjob ") == 0) {
            // TODO
            split_cmd(buffer, jobname);
            n = read(clientfd, buffer, LENGTH_LINE - 1);
            // buffer = an address, usually, which need to download and execute
            ret = addjob("./queue", jobname, buffer);
            if (ret == 0) {
              n = fd_send(clientfd, "job enqueue success");
            } else {
              n = fd_send(clientfd, "job enqueue failure");
            }
          } else
          if (str_startwith(buffer, "deljob ") == 0) {
            // TODO
            split_cmd(buffer, jobname);
            ret = deljob("./queue", jobname);
            if (ret == 0) {
              n = fd_send(clientfd, "job dequeue success");
            } else {
              n = fd_send(clientfd, "job dequeue failure");
            }
          }
        }
      }

      close(clientfd);
    }
  }

  return 1;
}


