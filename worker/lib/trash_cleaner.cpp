#include <cstdio>
#include <cstdlib>
#include <map>
#include <string>
#include <cstring>

#include <unistd.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>

using namespace std;

// struct used to present image pool item info
// only used for VM disk image files
struct ImagePoolItemInfo {
  bool has_copying_lock;  // whether the item has a .copying file
  bool has_logfile;       // whether the item has a .log file
  int last_mtime;         // modification time in last scanning round
  int last_filesize;      // filesize in last scanning round
};

char* g_image_pool_path;

// listing of the image pool dir. filename -> item info
// only VM disk image files are included in the list
map<string, ImagePoolItemInfo> g_image_pool_dir;

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

bool is_vm_disk_image(char* filename) {
  if (text_ends_with(filename, (char *) ".qcow2")) {
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
  delete fname_copy;
  return false;
}

void cleanup_image_pool_dir() {
  DIR* p_dir = opendir(g_image_pool_path);
  if (p_dir == NULL) {
    printf("Error: cannot open directory '%s'!\n", g_image_pool_path);
    return;
  }
  struct dirent* p_dirent;
  while ((p_dirent = readdir(p_dir)) != NULL) {
    if (text_starts_with(p_dirent->d_name, (char *) ".")) {
      // skip files starting with "."
      continue;
    }
    if (is_vm_disk_image(p_dirent->d_name)) {
      ImagePoolItemInfo item_info;
      // TODO update values in item_info, or delete it according to current situation.
      g_image_pool_dir[p_dirent->d_name] = item_info;
      printf("found VM image: '%s'\n", p_dirent->d_name);
    }
  }
  closedir(p_dir);
}

int main(int argc, char* argv[]) {
  printf("This is trash_cleaner!\n");
  if (argc == 1) {
    printf("Usage: trash_cleaner <image_pool_path>\n");
    exit(0);
  }
  g_image_pool_path = argv[1];

  // ok, start to do cleanup work
  for (;;) {
    printf("Doing cleanup work\n");

    // first of all, cleanup image pool directory
    cleanup_image_pool_dir();

    // do cleanup every 3 minutes
    sleep(60 * 3);
  }
  return 0;
}

