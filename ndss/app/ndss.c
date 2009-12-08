#include <stdio.h>
#include <string.h>
#include <assert.h>

#include "xmemory.h"

#include "ftp/ndss_ftp.h"

typedef int (*ndss_action)(int argc, char* argv[]);
typedef void (*ndss_action_help)();

// forward declaration
static int print_help(int argc, char* argv[]);
static void help_on_help();


char* g_actions[] = {

  // action_name, action_info, action_function, action_print_help_function
  // trick: function pointers are converted to char*

  "ftp", "Run as an ftp server", (char *) ndss_ftp, (char *) ndss_ftp_help, 

  "help", "Display help message", (char *) print_help, (char *) help_on_help,

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
    // only "ndss help" or "ndss"
    printf("usage: ndss COMMAND [ARGS]\n\nList of commands:\n");
    for (i = 0; g_actions[i] != NULL; i += 4) {
      int action_name_length = strlen(g_actions[i]);
      printf("  %s", g_actions[i]);
      for (j = max_action_name_length + 4; j > action_name_length; j--) {
        printf(" ");
      }
      printf("%s\n", g_actions[i + 1]);
    }
    printf("\nSee 'ndss help COMMAND' for more information on a specific command.\n");
    return 0;
  } else {
    // ndss help 'action'
    printf("[TODO] print help information on a specific command\n");

    for (i = 0; g_actions[i] != NULL; i += 4) {
      if (strcmp(g_actions[i], argv[2]) == 0) {
        ((ndss_action_help) g_actions[i + 3])();
        return 0;
      }
    }
    
    printf("ndss: '%s' is not a valid help topic. See 'ndss help'.\n", argv[2]);
    return 1;
  }
}

static int find_and_exec_action(int argc, char* argv[]) {
  int i;

  // TODO suggest best match when action not found
  for (i = 0; g_actions[i] != NULL; i += 4) {
    if (strcmp(g_actions[i], argv[1]) == 0) {
      return ((ndss_action) g_actions[i + 2])(argc, argv);
    }
  }

  printf("ndss: '%s' is not a valid command. See 'ndss help'.\n", argv[1]);
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

