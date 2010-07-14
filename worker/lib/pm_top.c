// This is the performance monitor for physical machines.
// It polls the performance meters and writes to a log file.
//
// Author:: Zhao Xun

#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <unistd.h>
#include <signal.h>

#define NCPUSTATES 5
#define DEFAULT_LOG_MAX_COUNT 1000

static long cp_time[NCPUSTATES];
static long cp_old[NCPUSTATES];
static long cp_diff[NCPUSTATES];
int cpu_states[NCPUSTATES];
char buffer[4096+1];

/*Skip spaces & some words*/
static inline char * skip_token(const char *p)
{
  while (isspace(*p)) p++;
  while (*p && !isspace(*p)) p++;
  return (char *)p;
}

/*Calculate the using percentages of CPU*/
long percentages_cpu(int cnt, int *out, register long *new, register long *old, long *diffs)
{
  /*Initialization*/
  register int i;
  register long change;
  register long total_change;
  register long *dp;
  long half_total;

  total_change = 0;
  dp = diffs;

  /*Calculate changes for each state and the overall change*/
  for (i = 0; i < cnt; i++)
  {
  if ((change = *new - *old) < 0)
    change = (int)((unsigned long)*new-(unsigned long)*old);
    total_change += (*dp++ = change);
    *old++ = *new++;
  }

  /*Avoid divide by zero potential*/
  if (total_change == 0)
  total_change = 1;

  /*Calculate percentages based on overall change, rounding up*/
  half_total = total_change / 2;
  half_total = 0;
  for (i = 0; i < cnt; i++)
    *out++ = (int)((*diffs++ * 1000 + half_total) / total_change);

  /*Return the total in case the caller wants to use it*/
  return(total_change);
}

// command line args:
// pm_top <pid_file> <log_file> [update_interval] [log_max_count]
int main(int argc, char* argv[])
{

  // default sleep interval is 1 second
  int update_interval = 1;
  char* pid_file;
  char* log_file;

  /*Initialization*/
  int fd, len;
  FILE * fw;
  char *p;
  int i, k, j = 0;
  long mem_total, mem_free, buffers, cached;
  long rece_total, tran_total, rece_old, tran_old;
  double rece_diff, tran_diff;
  int log_max_count = DEFAULT_LOG_MAX_COUNT;

  printf("This is pm_top!\n");
  if (argc < 3 || argc > 5) {
    printf("Usage: pm_top <pid_file> <log_file> [update_interval] [log_max_count]\n");
    return 1;
  } else {
    pid_file = argv[1];
    log_file = argv[2];
  }

  if (argc >= 4) {
    update_interval = atoi(argv[3]);
  }
  if (argc >= 5) {
    log_max_count = atoi(argv[4]);
  }

  // prevent zombie child process
  signal(SIGCHLD, SIG_IGN);
  int pid = fork();
  if (pid == 0) {
    // child process, do nothing, falls through
  } else {
    // parent process
    FILE* pid_fp = fopen(pid_file, "w");
    fprintf(pid_fp, "%d", pid);
    fclose(pid_fp);
    printf("Background daemon running with pid=%d\n", pid);
    exit(0);
  }

  for(i = 0; i < NCPUSTATES; i++){
    cpu_states[i] = 0;
    cp_diff[i] = 0;
  }
  rece_diff = 0;
  tran_diff = 0;

  while(1){
    char time_str[32];
    struct tm* tm_struct;
    time_t tm_val = time(NULL);
    tm_struct = localtime(&tm_val);
    strftime(time_str, sizeof(time_str), "%Y%m%d-%H%M%S", tm_struct);

    /*Record number*/
    j++;
    
    /*Read information of CPU from "/proc/stat"*/
    fd = open("/proc/stat", O_RDONLY);
    len = read(fd, buffer, sizeof(buffer)-1);
    close(fd);
    buffer[len] = '\0';

    p = skip_token(buffer);
    cp_time[0] = strtoul(p, &p, 0);
    cp_time[1] = strtoul(p, &p, 0);
    cp_time[2] = strtoul(p, &p, 0);
    cp_time[3] = strtoul(p, &p, 0);
    cp_time[4] = strtoul(p, &p, 0);

    percentages_cpu(NCPUSTATES, cpu_states, cp_time, cp_old, cp_diff);
    if(j % log_max_count == 0)
      fw = fopen(log_file,"w+");
    else
      fw = fopen(log_file,"a+");
    if(j != 1) {
      //fprintf(stderr, "cpu  used:%4.1f nice:%4.1f sys:%4.1f idle:%4.1f iowait:%4.1f   ",cpu_states[0]/10.0,cpu_states[1]/10.0,cpu_states[2]/10.0,cpu_states[3]/10.0,cpu_states[4]/10.0);
      fprintf(fw, "Time: %s\t", time_str);
      fprintf(fw, "CPU: %5.1f\t",100-cpu_states[3]/10.0);
    }

    /*Read information of memory from "/proc/meminfo"*/
 		fd = open("/proc/meminfo", O_RDONLY);
    len = read(fd, buffer, sizeof(buffer)-1);
    close(fd);
    buffer[len] = '\0';

    p = skip_token(buffer);
    mem_total = strtoul(p, &p, 0);
  
    while(*p != '\n') p++;
    p++;
    p = skip_token(p);
    mem_free = strtoul(p, &p, 0);

    /*while(*p != '\n') p++;
    p++;
    p = skip_token(p);
    buffers = strtoul(p, &p, 0);

    while(*p != '\n') p++;
    p++;
    p = skip_token(p);
    cached = strtoul(p, &p, 0);*/
               	
    if(j != 1)
      fprintf(fw, "memTotal: %ldMB\tmemFree: %ldMB\t", mem_total/1024, mem_free/1024);
    
    /*Read information of network from "/proc/nev/dev"*/
    fd = open("/proc/net/dev", O_RDONLY);
    len = read(fd, buffer, sizeof(buffer)-1);
    close(fd);
    buffer[len] = '\0';

    p = (char*)buffer;
    /*Skip the fisrt three lines of the file*/
    while(*p != '\n') p++;
    p++;
    while(*p != '\n') p++;
    p++;
    while(*p != '\n') p++;
    p++;
    while(*p != ':') p++;
    p++;		
  
    rece_total = strtoul(p, &p, 0);
    
    /*Skip seven words*/
    for(k = 0; k < 7; k++)	
      strtoul(p, &p, 0);
        
    tran_total = strtoul(p, &p, 0);
    
    if(j != 1)
    {
      rece_diff = (double)rece_total-rece_old;
      tran_diff = (double)tran_total-tran_old;
      
      if(rece_diff < 1024 && tran_diff < 1024)
        fprintf(fw, "Rece:%6.1fB/s\tTran:%6.1fB/s\n", rece_diff, tran_diff);
      else if(rece_diff >= 1024 && tran_diff < 1024)
        fprintf(fw, "Rece:%6.1fkB/s\tTran:%6.1fB/s\n", rece_diff/1024, tran_diff);
      else if(rece_diff < 1024 && tran_diff >= 1024)
        fprintf(fw, "Rece:%6.1fB/s\tTran:%6.1fkB/s\n", rece_diff, tran_diff/1024);
      else
        fprintf(fw, "Rece:%6.1fkB/s\tTran:%6.1fkB/s\n", rece_diff/1024, tran_diff/1024);
    }
    fclose(fw);
    rece_old = rece_total;
    tran_old = tran_total;
    sleep(update_interval);
  }
return 0;
}

