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
  if (argc < 3) {
    printf("usage: vm_daemon <storage_server> <vm_dir>\n");
    printf("depends on ruby, and must run with 'vm_daemon_helper.rb' in same dir!\n");
    return 1;
  } else {
    char* storage_server = argv[1];
    char* vm_dir = argv[2];

    // there's no need to free those malloc'd pointers
    char* cmd = (char *) malloc(sizeof(char) * (strlen(vm_dir) * 2 + strlen(storage_server) * 2 + 200));
    char* pid_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* status_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* status_info = (char *) malloc(sizeof(char) * 100);

    int pid = getpid();
    FILE* fp = NULL;

    cmd[0] = '\0';
    pid_fn[0] = '\0';
    status_fn[0] = '\0';

    printf("This is vm_daemon!\n");
    printf("Running with pid = %d\n", pid);

    sprintf(pid_fn, "%s/vm_daemon.pid", vm_dir);

    fp = fopen(pid_fn, "w");
    if (fp == NULL) {
      printf("error: cannot open pid file %s!\n", pid_fn);
      exit(1);
    }
    fprintf(fp, "%d", pid);
    fclose(fp);

    // forever loop, read current vm status, determine what to do next

    // vm statuses:
    // 1 preparing: downloading vdisks
    // 2 using: running/suspended
    // 3 saving: uploading changed resource
    // 4 destroyed: vm destroyed, remove all resources
    // 5 failed: failed to start for some reason, should be moved to 'broken' directory?

    sprintf(cmd, "./vm_daemon_helper.rb %s %s prepare 2>&1 >> %s/raw_exec_output.log", storage_server, vm_dir, vm_dir);
    printf("[cmd] %s\n", cmd);
    system(cmd);

    // after vm booted, we don't do anything for 30 seconds
    // this is necessary, because libvirt takes some time do boot the vm
    sleep(30);

    sprintf(status_fn, "%s/status", vm_dir);
    for (;;) {
      FILE* status_fp = NULL;
      sprintf(cmd, "./vm_daemon_helper.rb %s %s poll 2>&1 >> %s/raw_exec_output.log", storage_server, vm_dir, vm_dir);
      printf("[cmd] %s\n", cmd);
      system(cmd);

      // check if to be destroyed (read 'status' file)
      status_fp = fopen(status_fn, "r");
      if (status_fp != NULL) {
        fscanf(status_fp, "%s", status_info);
        if (strcmp(status_info, "destroyed") == 0) {
          printf("vm destroyed detected!\n");
          break;
        }
        fclose(status_fp);
      } else {
        // cannot read file, print warning, remove the vm
        printf("warning: cannot read 'status' file, consider vm as destroyed!\n");
        break;
      }

      sleep(1); // sleep 1 sec between each polling round
    }

    // clean up resource
    sprintf(cmd, "./vm_daemon_helper.rb %s %s cleanup 2>&1 >> %s/raw_exec_output.log", storage_server, vm_dir, vm_dir);
    printf("[cmd] %s\n", cmd);
    system(cmd);

    return 0;
  }
}
