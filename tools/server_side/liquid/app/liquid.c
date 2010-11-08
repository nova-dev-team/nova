/**
  @mainpage

  @section Introduction

  TODO intro doc

  @section Features

  TODO feat doc
*/

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>

#include "xmemory.h"
#include "xdef.h"
#include "xoption.h"
#include "xlog.h"

#include "ftp/liquid_ftp.h"
#include "dfs/liquid_dfs.h"
#include "dftp/liquid_dftp.h"
#include "node/liquid_node.h"
#include "imgstore/imgstore.h"
#include "imgdir/imgdir_server.h"
#include "imgmount/imgmount.h"

typedef xsuccess (*liquid_action)(int argc, char* argv[]);
typedef void (*liquid_action_help)();

// forward declaration
static xsuccess print_help(int argc, char* argv[]);
static void help_on_help();


void* g_actions[] = {

  // action_name, action_info, action_function, action_print_help_function
  // NOTE: sort these functions alphabetically
  "dfs", "Run as an distributed filesystem server", liquid_dfs, liquid_dfs_help,

  "dftp", "Run as an distributed FTP server", liquid_dftp, liquid_dftp_help,

  "ftp", "Run as an local FTP server", liquid_ftp, liquid_ftp_help,

  "help", "Display help message", print_help, help_on_help,

  "imgdir", "Serve as a VM disk info directroy", imgdir_server_main, imgdir_print_help,

  "imgmount", "Mount a VM disk filesystem", imgmount_main, imgmount_print_help,

  "imgstore", "Serve as a VM disk image storage node", liquid_imgstore, liquid_imgstore_help,

  "node", "Serve as a liquid DHT storage node", liquid_node, liquid_node_help,

  // terminated by NULL
  NULL
};

static void help_on_help() {
  printf("Help on help:\n");
  printf("\n");
  printf("usage: liquid help [topic]\n");
  printf("\n");
  printf("If [topic] is found, the help doc will be given.\n");
  printf("If [topic] is not given, list of commands will be given.\n");
}

static xsuccess print_help(int argc, char* argv[]) {
  int i, j;
  int max_action_name_length = 0; // for pretty printing
  for (i = 0; g_actions[i] != NULL; i += 4) {
    int action_name_length = strlen((char *) g_actions[i]);
    if (max_action_name_length < action_name_length) {
      max_action_name_length = action_name_length;
    }
  }

  if (argc <= 2) {
    // only "liquid help" or "liquid"
    printf("usage: liquid COMMAND [ARGS]\n\nList of commands:\n");
    for (i = 0; g_actions[i] != NULL; i += 4) {
      int action_name_length = strlen((char *) g_actions[i]);
      printf("  %s", (char *) g_actions[i]);
      for (j = max_action_name_length + 4; j > action_name_length; j--) {
        printf(" ");
      }
      printf("%s\n", (char *) g_actions[i + 1]);
    }
    printf("\nSee 'liquid help COMMAND' for more information on a specific command.\n");
    return XSUCCESS;
  } else {
    // liquid help 'action'
    for (i = 0; g_actions[i] != NULL; i += 4) {
      if (strcmp(g_actions[i], argv[2]) == 0) {
        ((liquid_action_help) g_actions[i + 3])();
        return XSUCCESS;
      }
    }

    printf("liquid: '%s' is not a valid help topic. See 'liquid help'.\n", argv[2]);
    return XFAILURE;
  }
}

static xsuccess find_and_exec_action(int argc, char* argv[]) {
  int i;

  // ENHANCE suggest best match when action not found
  for (i = 0; g_actions[i] != NULL; i += 4) {
    if (strcmp(g_actions[i], argv[1]) == 0) {
      return ((liquid_action) g_actions[i + 2])(argc, argv);
    }
  }

  printf("liquid: '%s' is not a valid command. See 'liquid help'.\n", argv[1]);
  return XFAILURE;
}

int main(int argc, char* argv[]) {
  int ret = 0;
  srand(time(NULL));
  xlog_init(argc, argv);
  if (argc == 1) {
    ret = print_help(argc, argv);
  } else {
    ret = find_and_exec_action(argc, argv);
  }
  if (xmem_usage(NULL) != 0) {
    xmem_usage(stdout);
  }
  return ret;
}

