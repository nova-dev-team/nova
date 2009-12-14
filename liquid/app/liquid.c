/**
  @mainpage

  @section Introduction

  TODO

  @section Features
  
  TODO
*/

#include <stdio.h>
#include <string.h>
#include <assert.h>

#include "xmemory.h"

#include "ftp/liquid_ftp.h"
#include "gateway/liquid_gw.h"
#include "serv/liquid_serv.h"

typedef int (*liquid_action)(int argc, char* argv[]);
typedef void (*liquid_action_help)();

// forward declaration
static int print_help(int argc, char* argv[]);
static void help_on_help();


char* g_actions[] = {

  // action_name, action_info, action_function, action_print_help_function
  // trick: function pointers are converted to char*

  "ftp", "Run as an ftp server", (char *) liquid_ftp, (char *) liquid_ftp_help, 

  "gw", "Serve as gateway", (char *) liquid_gw, (char *) liquid_gw_help,

  "help", "Display help message", (char *) print_help, (char *) help_on_help,

  "serv", "Serve as storage node", (char *) liquid_serv, (char *) liquid_serv_help,

  // terminated by NULL
  NULL
};

static void help_on_help() {
  printf("TODO HELP ON HELP\n");
}

static int print_help(int argc, char* argv[]) {
  int i, j;
  int max_action_name_length = 0; // for pretty printing
  for (i = 0; g_actions[i] != NULL; i += 4) {
    int action_name_length = strlen(g_actions[i]);
    if (max_action_name_length < action_name_length) {
      max_action_name_length = action_name_length;
    }
  }

  if (argc <= 2) {
    // only "liquid help" or "liquid"
    printf("usage: liquid COMMAND [ARGS]\n\nList of commands:\n");
    for (i = 0; g_actions[i] != NULL; i += 4) {
      int action_name_length = strlen(g_actions[i]);
      printf("  %s", g_actions[i]);
      for (j = max_action_name_length + 4; j > action_name_length; j--) {
        printf(" ");
      }
      printf("%s\n", g_actions[i + 1]);
    }
    printf("\nSee 'liquid help COMMAND' for more information on a specific command.\n");
    return 0;
  } else {
    // liquid help 'action'
    printf("[TODO] print help information on a specific command\n");

    for (i = 0; g_actions[i] != NULL; i += 4) {
      if (strcmp(g_actions[i], argv[2]) == 0) {
        ((liquid_action_help) g_actions[i + 3])();
        return 0;
      }
    }
    
    printf("liquid: '%s' is not a valid help topic. See 'liquid help'.\n", argv[2]);
    return 1;
  }
}

static int find_and_exec_action(int argc, char* argv[]) {
  int i;

  // TODO suggest best match when action not found
  for (i = 0; g_actions[i] != NULL; i += 4) {
    if (strcmp(g_actions[i], argv[1]) == 0) {
      return ((liquid_action) g_actions[i + 2])(argc, argv);
    }
  }

  printf("liquid: '%s' is not a valid command. See 'liquid help'.\n", argv[1]);
  return 1;
}

int main(int argc, char* argv[]) {
  int ret = 0;
  if (argc == 1) {
    ret = print_help(argc, argv);
  } else {
    ret = find_and_exec_action(argc, argv);
  }
  assert(xmem_usage() == 0);
  return ret;
}

