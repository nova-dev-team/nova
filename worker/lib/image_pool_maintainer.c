// This tool is used to prepare VM images in the background. It copies at very slow speed, reduces the impact on VM IO.
//
// Author::   Santa Zhang (mailto:santa1987@gmail.com)
// Since::    0.3

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <malloc.h>
#include <dirent.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <string.h>
#include <time.h>

// global copy speed limit
int g_mbps = 1;

// global sleep interval (in seconds)
int g_interval = 1;

// global image pool size
int g_pool_size = 5;

// copy with speed limit.
// params: old_fn = old file name
//         new_fn = new file name
//         mbps = speed limit (MB/s)
// return: 0 on success
//         -1 on failure
int copy_with_speed_limit(char* old_fn, char* new_fn, int mbps) {
  long long bytes_copied = 0;
  FILE* src;
  FILE* dst;
  int buf_size = 1024 * 16;
  char* buf = (char *) malloc(buf_size);
  double sleep_usec = 1;

  struct timeval begin_time;
  gettimeofday(&begin_time, NULL);

  src = fopen(old_fn, "rb");
  dst = fopen(new_fn, "wb");
  if (src == NULL || dst == NULL) {
    printf("error: cannot copy file!\n");
    return 1;
  }
  for (;;) {
    int cnt = fread(buf, sizeof(char), buf_size, src);
    if (cnt <= 0) {
      break;
    }
    bytes_copied += cnt;
    if (bytes_copied > mbps * 1024 * 1024) {
      // sleep only when copied enough data

      struct timeval end_time;
      long long usec;
      gettimeofday(&end_time, NULL);
      usec = (end_time.tv_sec - begin_time.tv_sec) * 1000 * 1000 + (end_time.tv_usec - begin_time.tv_usec);
      
      if (bytes_copied < 1.024 * 1.024 * mbps * usec) {
        // copying too slow
        sleep_usec /= 1.1;
      } else {
        // copying too fast
        sleep_usec *= 1.1;
      }
      usleep((int) sleep_usec);
    }
    fwrite(buf, sizeof(char), cnt, dst);
  }
  fclose(src);
  fclose(dst);

  free(buf);
  return 0;
}

// get a line from file, empty lines are discarded
// params: fp = file pointer
//         buf = string buffer
//         buf_size = size of string buffer
void fget_nonempty_line(FILE* fp, char* buf, int buf_size) {
  int i = 0;

  // whether we've got non crlf content
  int got_content = 0;
  while (i < buf_size - 1) {
    char ch = fgetc(fp);
    if (feof(fp)) {
      break;
    }
    if (got_content == 0) {
      if (ch != '\r' && ch != '\n') {
        got_content = 1;
      } else {
        continue;
      }
    }
    if (got_content == 1) {
      if (ch == '\r' || ch == '\n') {
        break;
      }
    }
    buf[i] = ch;
    i++;
  }
  buf[i] = '\0';
}

int text_starts_with(char* text, char* head) {
  int i;
  for (i = 0; text[i] != '\0' && head[i] != '\0'; i++) {
    if (text[i] != head[i]) {
      return 0;
    }
  }
  if (text[i] == '\0' && head[i] != '\0') {
    return 0;
  }
  return 1;
}

int text_ends_with(char* text, char* tail) {
  int len_text = strlen(text);
  int len_tail = strlen(tail);
  int i;

  if (len_tail > len_text) {
    return 0;
  }
  for (i = 0; i < len_tail && i < len_text; i++) {
    int pos_text = len_text - i - 1;
    int pos_tail = len_tail - i - 1;
    if (text[pos_text] != tail[pos_tail]) {
      return 0;
    }
  }

  return 1;
}

// reloads config file from disk
void refresh_config() {
  FILE* fp = fopen("image_pool_maintainer.conf", "r");
  if (fp != NULL) {
    int buf_size = 256;
    char* buf = (char *) malloc(sizeof(char) * buf_size);
    while (!feof(fp)) {
      fget_nonempty_line(fp, buf, buf_size);
      if (text_starts_with(buf, "#")) {
        continue;
      } else if (text_starts_with(buf, "sleep_interval=")) {
        g_interval = atoi(buf + 15);
        printf("g_interval set to %d\n", g_interval);
      } else if (text_starts_with(buf, "pool_size=")) {
        g_pool_size = atoi(buf + 10);
        printf("g_pool_size set to %d\n", g_pool_size);
      } else if (text_starts_with(buf, "copy_speed_limit=")) {
        g_mbps = atoi(buf + 17);
        printf("g_mbps set to %d\n", g_mbps);
      }
    }
    free(buf);
    fclose(fp);
  }
}

void maintain_image_count(char* image_fn) {
  int copy_id;
  char* test_fn = (char *) malloc(sizeof(char) * (strlen(image_fn) + 32));
  struct stat st;
  for (copy_id = 1; copy_id <= g_pool_size; copy_id++) {
    sprintf(test_fn, "%s.pool.%d", image_fn, copy_id);
    printf("checking if has copy %s\n", test_fn);
    if (lstat(test_fn, &st) != 0) {
      FILE* fp;
      printf("copy %s not found, creating new copy...\n", test_fn);

      // create lock file
      sprintf(test_fn, "%s.pool.%d.copying", image_fn, copy_id);
      fp = fopen(test_fn, "w");
      fclose(fp);
      printf("created lock file %s\n", test_fn);
      
      sprintf(test_fn, "%s.pool.%d", image_fn, copy_id);
      printf("copying %s --> %s with speed limit %d MB/s\n", image_fn, test_fn, g_mbps);
      copy_with_speed_limit(image_fn, test_fn, g_mbps);

      // remove lock file
      sprintf(test_fn, "%s.pool.%d.copying", image_fn, copy_id);
      remove(test_fn);
      printf("removed lock file %s\n", test_fn);
    }
  }
  free(test_fn);
}

void maintain_pool_size() {
  DIR* p_dir;
  struct stat st;
  p_dir = opendir(".");
  if (p_dir == NULL) {
    printf("error: cannot read dir!\n");
    exit(1);
  } else {
    struct dirent* p_dirent;

    while ((p_dirent = readdir(p_dir)) != NULL) {
      printf("found dir entry: %s\n", p_dirent->d_name);
      if (text_starts_with(p_dirent->d_name, ".")) {
        continue;
      }
      if (text_ends_with(p_dirent->d_name, ".qcow2")) {
        char* buf = (char *) malloc(sizeof(char) * (strlen(p_dirent->d_name) + 32));
        printf("found qcow2 image: %s\n", p_dirent->d_name);
        printf("checking if %s has .copying lock\n", p_dirent->d_name);

        // check if original image has .copying lock
        sprintf(buf, "%s.copying", p_dirent->d_name);
        printf("test %s\n", buf);
        if (lstat(buf, &st) == 0) {
          printf("file %s has .copying lock, skip copying!\n", p_dirent->d_name);
        } else {
          printf("there is no .copying lock for %s, start copying\n", p_dirent->d_name);
          maintain_image_count(p_dirent->d_name);
        }
        free(buf);
      }
    }
  }
  closedir(p_dir);
}

int main(int argc, char* argv[]) {
  printf("This is image_pool_maintainer!\n");
  if (argc < 3) {
    printf("usage: image_pool_maintainer <pid_file> <image_pool_dir>\n");
    return 1;
  } else {
    char* pid_fn = argv[1];
    char* image_pool_dir = argv[2];
    int pid = fork();
    if (pid == 0) {
      // child process, do work
      if (chdir(image_pool_dir) != 0) {
        printf("error: cannot chdir to '%s'\n", image_pool_dir);
        exit(1);
      }

      // wait for 1 second, check if parent process could write pid file
      sleep(1);

      for (;;) {
        // reloads config data
        refresh_config();

        // make sure there's enough images in the pool
        maintain_pool_size();

        // sleep, wait for next round
        sleep(g_interval);
      }
    } else {
      // parent process, write pid file, and exits
      FILE* fp = fopen(pid_fn, "w");
      if (fp != NULL) {
        fprintf(fp, "%d", pid);
        fclose(fp);
      } else {
        // failed to open pid file, kill child process
        printf("error: cannot write pid file!\n");
        kill(pid, 9);
        exit(1);
      }
    }
    return 0;
  }
}

