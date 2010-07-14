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
  if (argc < 4) {
    printf("usage: vm_daemon <rails_root> <storage_server> <vm_dir> [running mode]\n");
    printf("depends on ruby, and must run with 'vm_daemon_helper.rb' in same dir!\n");
    printf("running mode = (NORMAL, RECOVER, RECEIVE), which can be empty, default is NORMAL\n");
    return 1;
  } else {
    char* rails_root = argv[1];
    char* storage_server = argv[2];
    char* vm_dir = argv[3];
    char* c_mode = NULL;
    // there's no need to free those malloc'd pointers
    char* cmd = (char *) malloc(sizeof(char) * (strlen(vm_dir) * 2 + strlen(storage_server) * 2 + 200));
    char* pid_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* status_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* status_info = (char *) malloc(sizeof(char) * 100);

    int pid = getpid();
    int mode = 0;
    // 0 -- NORMAL
    // 1 -- RECOVER
    // 2 -- RECEIVE

    FILE* fp = NULL;

    if (argc >= 5) {
      c_mode = argv[4];
      if (strcmp(c_mode, "NORMAL") == 0) {
        mode = 0;
      } else
      if (strcmp(c_mode, "RECOVER") == 0) {
        mode = 1;
      } else
      if (strcmp(c_mode, "RECEIVE") == 0) {
        mode = 2;
      }
    }

    cmd[0] = '\0';
    pid_fn[0] = '\0';
    status_fn[0] = '\0';

    printf("This is vm_daemon!\n");
    printf("Running with pid = %d\n", pid);
    printf("Running Mode(0-NORMAL, 1-RECOVER, 2-RECEIVE) == %d\n", mode);

    sprintf(pid_fn, "%s/vm_daemon.pid", vm_dir);
    if (mode != 2) {
      fp = fopen(pid_fn, "w");
      if (fp == NULL) {
        printf("error: cannot open pid file %s!\n", pid_fn);
        exit(1);
      }
      fprintf(fp, "%d", pid);
      fclose(fp);
    }

    // forever loop, read current vm status, determine what to do next
    // vm statuses:
    // 1 preparing: downloading vdisks
    // 2 using: running/suspended
    // 3 saving: uploading changed resource
    // 4 destroyed: vm destroyed, remove all resources
    // 5 failed: failed to start for some reason, should be moved to 'broken' directory?

    // NORMAL ROUTE:
    // 1 prepare
    // 2 poll
    // 3 clean

    // RECOVER ROUTE:
    // 1 poll
    // 2 clean

    // RECEIVE ROUTE:
    // 1 receive migrating vm from remote worker
    // 2 poll
    // 3 clean


    if (mode == 0) {
      sprintf(cmd, "./vm_daemon_helper.rb %s %s %s prepare 2>&1 >> %s/raw_exec_output.log", rails_root, storage_server, vm_dir, vm_dir);
      printf("[cmd] %s\n", cmd);
      system(cmd);
    // after vm booted, we don't do anything for 30 seconds
    // this is necessary, because libvirt takes some time do boot the vm
      sleep(30);
    }

    if (mode == 2) {
      sprintf(cmd, "./vm_daemon_helper.rb %s %s %s receive 2>&1 >> %s/raw_exec_output.log", rails_root, storage_server, vm_dir, vm_dir);
      printf("[cmd] %s\n", cmd);
      system(cmd);
    }

    sprintf(status_fn, "%s/status", vm_dir);
    for (;;) {
      FILE* status_fp = NULL;
      sprintf(cmd, "./vm_daemon_helper.rb %s %s %s poll 2>&1 >> %s/raw_exec_output.log", rails_root, storage_server, vm_dir, vm_dir);
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
    sprintf(cmd, "./vm_daemon_helper.rb %s %s %s cleanup 2>&1 >> %s/raw_exec_output.log", rails_root, storage_server, vm_dir, vm_dir);
    printf("[cmd] %s\n", cmd);
    system(cmd);

    return 0;
  }
}

