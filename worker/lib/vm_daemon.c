// A daemon process that monitors the health status of created VM.
// It also takes care of resource preparing before VM creation.
//
// Author::   Santa Zhang (mailto:santa1987@gmail.com)
// Since::    0.3

#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char* argv[]) {
  if (argc < 2) {
    printf("usage: vm_daemon <vm_dir>\n");
    printf("depends on ruby, and must run with 'vm_deamon_helper.rb' in same dir!\n");
    return 1;
  } else {
    char* vm_dir = argv[1];

    // there's no need to free those malloc'd pointers
    char* cmd = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* pid_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* status_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));

    int pid = getpid();
    FILE* fp = NULL;

    cmd[0] = '\0';
    pid_fn[0] = '\0';

    printf("This is vm_daemon!\n");
    printf("Running with pid = %d\n", pid);

    sprintf(pid_fn, "%s/vm_daemon.pid", vm_dir);

    fp = fopen(pid_fn, "w");
    fprintf(fp, "%d", pid);
    fclose(fp);

    // forever loop, read current vm status, determine what to do next

    // vm statuses:
    // 1 preparing: downloading vdisks
    // 2 using: running/suspended
    // 3 saving: uploading changed resource
    // 4 destroyed: vm destroyed, remove all resources
    // 5 failed: failed to start for some reason, should be moved to 'broken' directory?

    sprintf(cmd, "./vm_daemon_helper.rb %s prepare", vm_dir);
    printf("[cmd] %s\n", cmd);
    system(cmd);

    for (;;) {
      sprintf(cmd, "./vm_daemon_helper.rb %s poll", vm_dir);
      printf("[cmd] %s\n", cmd);
      system(cmd);

      // TODO check if to be destroyed

      sleep(1); // sleep 1 sec between each polling round
    }

    return 0;
  }
}

