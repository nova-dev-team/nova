#include <cstdio>
#include <cstdlib>
#include <map>
#include <string>
#include <cstring>
#include <cstdarg>

#include <unistd.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <libvirt/libvirt.h>
#include <libvirt/virterror.h>

using namespace std;

// struct used to present image pool item info
// only used for VM disk image files
struct ImagePoolItemInfo {
  bool has_copying_lock;  // whether the item has a .copying file
  bool has_logfile;       // whether the item has a .log file
  int last_mtime;         // modification time in last scanning round
  int last_filesize;      // filesize in last scanning round

  ImagePoolItemInfo() {
    has_copying_lock = false;
    has_logfile = false;
    last_mtime = -1;
    last_filesize = -1;
  }
};

char* g_run_root;
char* hypervisor;
//argv[3], indicated by nova worker
//either 'xen' or 'kvm', if null, consider it's 'xen' by default

// listing of the image pool dir. filename -> item info
// only VM disk image files are included in the list
map<string, ImagePoolItemInfo> g_image_pool_dir;

// Map which stores VM's last ack time.
// If we lose contact to a VM for a long time, remove its resource.
map<string, int> g_vm_alive_time;

// connection to libvirt
virConnectPtr g_virt_conn = NULL;

FILE* g_log_fp = NULL;

#define do_log(fmt, ...) {\
  do_log_start(); \
  printf(fmt, ## __VA_ARGS__);  \
  if (g_log_fp != NULL || 1) { \
    fprintf(g_log_fp, fmt, ## __VA_ARGS__); \
  } \
  do_log_end(fmt);  \
  fflush(g_log_fp); \
}

int text_starts_with(const char* text, const char* head) {
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

int text_ends_with(const char* text, const char* tail) {
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

void do_log_start() {
  char timestr_buf[32];
  struct tm* tm_struct;
  time_t tm_val = time(NULL);
  tm_struct = localtime(&tm_val);
  strftime(timestr_buf, sizeof(timestr_buf), "[%Y.%m.%d %H:%M:%S] ", tm_struct);
  printf("%s", timestr_buf);
  if (g_log_fp != NULL) {
    fprintf(g_log_fp, "%s", timestr_buf);
  }
}

void do_log_end(const char* fmt) {
  if (!text_ends_with(fmt, "\n")) {
    printf("\n");
    if (g_log_fp != NULL) {
      fprintf(g_log_fp, "\n");
    }
  }
}

bool is_vm_disk_image(const char* filename) {
  if (text_ends_with(filename, (char *) ".qcow2")) {
    return true;
  }
  if (text_ends_with(filename, (char *) ".img")) {
    return true;
  }

  // check if .qcow2.pool.X
  int fn_len = strlen(filename);
  char* fname_copy = new char[fn_len + 1];
  strcpy(fname_copy, filename);
  int idx = fn_len - 1;
  while (idx > 0) {
    if (fname_copy[idx] == '.') {
      break;
    }
    if (fname_copy[idx] < '0' || fname_copy[idx] > '9') {
      return false;
    }
    idx--;
  }
  fname_copy[idx] = '\0';
  if (text_ends_with(fname_copy, (char *) ".qcow2.pool")) {
    return true;
  }
  if (text_ends_with(fname_copy, (char *) ".img.pool")) {
    return true;
  }
  delete fname_copy;
  return false;
}

// my helper function, which wraps around lstat function.
// will return 0 if no error occurred
int my_stat(const char* dir, const char* base_fname, const char* ext_fname, struct stat* st) {
  char* fpath = new char[strlen(dir) + strlen(base_fname) + strlen(ext_fname) + 10];
  strcpy(fpath, dir);
  strcat(fpath, "/");
  strcat(fpath, base_fname);
  strcat(fpath, ext_fname);
  int ret = lstat(fpath, st);
  delete fpath;
  return ret;
}

int my_remove(const char* dir, const char* base_fname, const char* ext_fname) {
  char* fpath = new char[strlen(dir) + strlen(base_fname) + strlen(ext_fname) + 10];
  strcpy(fpath, dir);
  strcat(fpath, "/");
  strcat(fpath, base_fname);
  strcat(fpath, ext_fname);
  int ret = remove(fpath);
  delete fpath;
  return ret;
}

void cleanup_image_pool_dir() {
  do_log("Working in image pool...\n");
  char* image_pool_path = new char[strlen(g_run_root) + 20];
  strcpy(image_pool_path, g_run_root);
  strcat(image_pool_path, "/image_pool");
  do_log("Image pool: %s\n", image_pool_path);
  DIR* p_dir = opendir(image_pool_path);
  if (p_dir == NULL) {
    do_log("Error: cannot open directory '%s'!\n", image_pool_path);
  } else {
    struct dirent* p_dirent;
    while ((p_dirent = readdir(p_dir)) != NULL) {
      struct stat st;
      if (text_starts_with(p_dirent->d_name, (char *) ".")) {
        // skip files starting with "."
        continue;
      }
      if (text_ends_with(p_dirent->d_name, ".revoke")) {
        // check if the image to be revoked really exists
        char* revoked_image_fn = new char[strlen(image_pool_path) + strlen(p_dirent->d_name) + 10];
        strcpy(revoked_image_fn, image_pool_path);
        strcat(revoked_image_fn, "/");
        strcat(revoked_image_fn, p_dirent->d_name);
        revoked_image_fn[strlen(revoked_image_fn) - 7] = '\0'; // get the image to be revoked
        if (lstat(revoked_image_fn, &st) != 0) {
          // the image to be revoked does not exist, delete the trashed .revoke file
          my_remove(image_pool_path, p_dirent->d_name, "");
        }
        delete revoked_image_fn;
      }
      if (is_vm_disk_image(p_dirent->d_name)) {
        do_log("found VM image: '%s'\n", p_dirent->d_name);
        ImagePoolItemInfo item_info;
        if (my_stat(image_pool_path, p_dirent->d_name, ".copying", &st) == 0) {
          item_info.has_copying_lock = true;
          do_log(".copying lock found for '%s'\n", p_dirent->d_name);
        } else {
          item_info.has_copying_lock = false;
        }

        if (my_stat(image_pool_path, p_dirent->d_name, ".log", &st) == 0) {
          item_info.has_logfile = true;
          do_log("log file found for '%s'\n", p_dirent->d_name);
        } else {
          item_info.has_logfile = false;
        }

        if (my_stat(image_pool_path, p_dirent->d_name, "", &st) == 0) {
          if (g_image_pool_dir.find(p_dirent->d_name) != g_image_pool_dir.end()) {
            // file exists on last scanning round
            // check if this file is trash
            // if the file has not changed a bit since last scanning, and it has a .copying lock, it is surely trash.
            // so delete the file, and also delete the .copying lock file
            ImagePoolItemInfo last_info = g_image_pool_dir.find(p_dirent->d_name)->second;
            if ((st.st_mtime == last_info.last_mtime || st.st_size == last_info.last_filesize) && item_info.has_copying_lock) {
              do_log("removing '%s' since it is trash (copying failed)\n", p_dirent->d_name);
              char* fpath = new char[strlen(image_pool_path) + strlen(p_dirent->d_name) + 40];
              strcpy(fpath, image_pool_path);
              strcat(fpath, "/");
              strcat(fpath, p_dirent->d_name);
              remove(fpath);
              strcat(fpath, ".copying");
              remove(fpath);
              delete fpath;
            } else if (item_info.has_copying_lock == false && st.st_size == 0) {
              // delete images with 0 size
              do_log("removing '%s' since it is trash (zero file size)\n", p_dirent->d_name);
              char* fpath = new char[strlen(image_pool_path) + strlen(p_dirent->d_name) + 40];
              strcpy(fpath, image_pool_path);
              strcat(fpath, "/");
              strcat(fpath, p_dirent->d_name);
              remove(fpath);
              if (item_info.has_logfile) {
                strcat(fpath, ".log");
                remove(fpath);
              }
              delete fpath;
            } else {
              item_info.last_mtime = st.st_mtime;
              item_info.last_filesize = st.st_size;
              g_image_pool_dir[p_dirent->d_name] = item_info;
            }
          } else {
            item_info.last_mtime = st.st_mtime;
            item_info.last_filesize = st.st_size;
            g_image_pool_dir[p_dirent->d_name] = item_info;
          }

        } else {
          // well, I think this is not going to happen...
          do_log("Error: '%s' found but cannot stat it!\n", p_dirent->d_name);
        }

      }
    }
    closedir(p_dir);
  }
  delete image_pool_path;
  return;
}

void cleanup_vm_dir() {
  string connStr = "";
  if (hypervisor == NULL) connStr = "qemu:///system";
  if (strcmp(hypervisor, "xen") == 0) connStr = "xen:///";
  if (strcmp(hypervisor, "kvm") == 0) connStr = "qemu:///system";

  const char *conn = connStr.c_str();
  g_virt_conn = virConnectOpen(conn);
  if (g_virt_conn == NULL) {
    do_log("error: cannot open connection to '%s'", conn);
    return;
  }

  time_t now = time(NULL);
  char* vm_dir = new char[strlen(g_run_root) + 10];
  strcpy(vm_dir, g_run_root);
  strcat(vm_dir, "/");
  strcat(vm_dir, "vm");

  DIR* p_dir = opendir(vm_dir);
  if (p_dir != NULL) {
    struct dirent* p_dirent;
    while ((p_dirent = readdir(p_dir)) != NULL) {
      if (text_starts_with(p_dirent->d_name, ".")) {
        continue; // skip .* files
      }
      char* fpath = new char[(strlen(vm_dir) + strlen(p_dirent->d_name)) * 2 + 10];
      strcpy(fpath, vm_dir);
      strcat(fpath, "/");
      strcat(fpath, p_dirent->d_name);
      struct stat st;
      if (lstat(fpath, &st) != 0) {
        do_log("failed to stat on '%s'", fpath);
      }
      if (S_ISDIR(st.st_mode)) {
        do_log("Checking if VM '%s' exists", p_dirent->d_name);
        if (virDomainLookupByName(g_virt_conn, p_dirent->d_name) != NULL) {
          do_log("Domain '%s' is running", p_dirent->d_name);
          g_vm_alive_time[p_dirent->d_name] = now;
        } else {
          do_log("Domain '%s' is NOT running", p_dirent->d_name);
          if (g_vm_alive_time.find(p_dirent->d_name) == g_vm_alive_time.end()) {
            g_vm_alive_time[p_dirent->d_name] = now;
          }
          if (now - g_vm_alive_time[p_dirent->d_name] > 60 * 60) {
            // remove the vm if it is not detected for 1 hour
            do_log("Consider VM '%s' trashed since it is not detected for a long time", p_dirent->d_name);
            do_log("Remove all data of '%s' since it is trashed", p_dirent->d_name);
            char* rm_cmd = new char[strlen(fpath) + 40];
            sprintf(rm_cmd, "rm %s -rf", fpath);
            do_log("[cmd] %s", rm_cmd);
            system(rm_cmd);
            delete [] rm_cmd;
          }
        }
      }
      delete [] fpath;
    }
    closedir(p_dir);
  } else {
    do_log("failed to open dir '%s'\n", vm_dir);
  }

  delete [] vm_dir;
}

int main(int argc, char* argv[]) {
  printf("This is trash_cleaner!\n");
  if (argc < 3) {
    printf("Usage: trash_cleaner <log_folder> <run_root> <hypervisor>\n");
    exit(0);
  }

  if (fork() == 0) {
    char* pid_fn = new char[strlen(argv[1]) + 40];
    strcpy(pid_fn, argv[1]);
    strcat(pid_fn, "/");
    strcat(pid_fn, "trash_cleaner.pid");

    char* log_fn = new char[strlen(argv[1]) + 40];
    strcpy(log_fn, argv[1]);
    strcat(log_fn, "/");
    strcat(log_fn, "trash_cleaner.log");
    g_log_fp = fopen(log_fn, "a");
    if (g_log_fp == NULL) {
      printf("*** error: failed to open log file '%s'!\n", log_fn);
      exit(1);
    }
    delete [] log_fn;

    FILE* p_pidf = fopen(pid_fn, "w");
    if (p_pidf == NULL) {
      printf("Failed to create pid file '%s'!\n", pid_fn);
      exit(1);
    }
    fprintf(p_pidf, "%d", getpid());
    fclose(p_pidf);
    g_run_root = argv[2];
    hypervisor = argv[3];
    // ok, start to do cleanup work
    for (;;) {
      do_log("Doing cleanup work...\n");

      // first of all, cleanup image pool directory
      cleanup_image_pool_dir();

      // cleanup the running VMs
      // temperarily disabled..
      // cleanup_vm_dir();

      // do cleanup every 3 minutes
      sleep(3 * 60);
    }
  }
  return 0;
}

