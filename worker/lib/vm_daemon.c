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

#include <libvirt/libvirt.h>
#include <libvirt/virterror.h>

int main(int argc, char* argv[]) {
  if (argc < 5) {
    printf("usage: vm_daemon <rails_root> <storage_server> <vm_dir> <vm_name> [hypervisor]\n");
    printf("depends on ruby, and must run with 'vm_daemon_helper.rb' in same dir!\n");
    printf("hypervisor = (xen | kvm), default is kvm\n");
    return 1;
  } else {
    virConnectPtr g_virt_conn = NULL;
    char* rails_root = argv[1];
    char* storage_server = argv[2];
    char* vm_dir = argv[3];
    char* vm_name = argv[4];

    char* c_mode = NULL;

    // there's no need to free those malloc'd pointers
    char* cmd = (char *) malloc(sizeof(char) * (strlen(vm_dir) * 2 + strlen(storage_server) * 2 + 200));
    char* pid_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* lock_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* status_fn = (char *) malloc(sizeof(char) * (strlen(vm_dir) + 100));
    char* status_info = (char *) malloc(sizeof(char) * 100);

    int pid = getpid();
    int hypervisor = 0;
    // 0 -- KVM
    // 1 -- XEN
    
    FILE* fp = NULL;
    FILE* lfp = NULL;  //lock

    if (argc >= 6) {
      c_mode = argv[5];
      if (strcmp(c_mode, "kvm") == 0) {
        hypervisor = 0;
      } else
      if (strcmp(c_mode, "xen") == 0) {
        hypervisor = 1;
      } 
    }

    cmd[0] = '\0';
    pid_fn[0] = '\0';
    lock_fn[0] = '\0';
    status_fn[0] = '\0';
    
    printf("This is vm_daemon!\n");
    printf("Running with pid = %d\n", pid);
    printf("Hypervisor(0--KVM, 1--Xen) = %d\n", hypervisor);

    sprintf(pid_fn, "%s/vm_daemon.pid", vm_dir);
    sprintf(lock_fn, "%s/vm_daemon.lock", vm_dir);
    
    lfp = fopen(lock_fn, "w");
    if (lfp == NULL) {
      printf("error: cannot obtain daemon lock!\n");
      exit(1);
    }
    fprintf(lfp, "hahahahhhaa\n");
    
    fp = fopen(pid_fn, "w");
    if (fp == NULL) {
      printf("error: cannot open pid file %s!\n", pid_fn);
      exit(1);
    }

    fprintf(fp, "%d", pid);
    fclose(fp);

    if (hypervisor == 1)
      g_virt_conn = virConnectOpen("xen:///"); 
    else
      g_virt_conn = virConnectOpen("qemu:///system");

    if (g_virt_conn == NULL) {
      printf("error: cannot open connection to libvirt\n");
      return -1;
    }

    // forever loop, read current vm status, determine what to do next
    // vm statuses:
    // 1 preparing: downloading vdisks
    // 2 using: running/suspended
    // 3 saving: uploading changed resource
    // 4 destroyed: vm destroyed, remove all resources
    // 5 failed: failed to start for some reason, should be moved to 'broken' directory?

    // running logic:
    // if vm doesn't appear in libvirt's list, go to clean phase
    // else, polling vm_daemon_helper.rb each XX sec
    // actions should be done by vm_daemon_helper

    for (;;) {
      if (virDomainLookupByName(g_virt_conn, vm_name) != NULL) {
        sprintf(cmd, "./vm_daemon_helper.rb %s %s %s 2>&1 >> %s/raw_exec_output.log", rails_root, storage_server, vm_dir, vm_dir);
        printf("[cmd] %s\n", cmd);
        system(cmd);
      } else {
        // vm not exists, call cleanup
        break;
      }
      sleep(1);
    }

    if (hypervisor == 0) { //KVM
      sprintf(cmd, "./vm_daemon_helper.rb %s %s %s cleanup 2>&1 >> %s/raw_exec_output.log", rails_root, storage_server, vm_dir, vm_dir);
      printf("[cmd] %s\n", cmd);
      system(cmd);
    }
    fclose(lfp);
    return 0;
  }

/*

    //old routing

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
  */
}

