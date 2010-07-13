#include<stdio.h>   
#include<stdlib.h>
#include<ctype.h> 
#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h> 
#include<unistd.h>

#define NCPUSTATES 5
#define LOGSIZE 1000

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

int main(void)
{ 
	/*Initialization*/
	int fd, len; 
	FILE * fw;
	char *p; 
	int i, k, j = 0; 
	long mem_total, mem_free, buffers, cached;
	long rece_total, tran_total, rece_old, tran_old;
	double rece_diff, tran_diff;

	for(i = 0; i < NCPUSTATES; i++){ 
		cpu_states[i] = 0; 
		cp_diff[i] = 0; 
	} 
	rece_diff = 0;
	tran_diff = 0;

	while(1){ 
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
		if(j%LOGSIZE == 0)
			fw = fopen("log.txt","w+"); 
		else
			fw = fopen("log.txt","a+");
		if(j != 1)
		//fprintf(stderr, "cpu  used:%4.1f nice:%4.1f sys:%4.1f idle:%4.1f iowait:%4.1f   ",cpu_states[0]/10.0,cpu_states[1]/10.0,cpu_states[2]/10.0,cpu_states[3]/10.0,cpu_states[4]/10.0); 
		fprintf(fw, "CPU using:%5.1f   ",100-cpu_states[3]/10.0); 

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
		fprintf(fw, "memTotal:%ldMB  memFree:%ldMB   ", mem_total/1024, mem_free/1024);
		
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
				fprintf(fw, "Rece:%6.1fB/s Tran:%6.1fB/s   \n", rece_diff, tran_diff);
			else if(rece_diff >= 1024 && tran_diff < 1024)
				fprintf(fw, "Rece:%6.1fkB/s Tran:%6.1fB/s   \n", rece_diff/1024, tran_diff);
			else if(rece_diff < 1024 && tran_diff >= 1024)
				fprintf(fw, "Rece:%6.1fB/s Tran:%6.1fkB/s   \n", rece_diff, tran_diff/1024);
			else
				fprintf(fw, "Rece:%6.1fkB/s Tran:%6.1fkB/s   \n", rece_diff/1024, tran_diff/1024);
		}
		fclose(fw);
		rece_old = rece_total;
		tran_old = tran_total;
		sleep(1);
	} 
return 0;
}

