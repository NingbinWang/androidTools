#include <cutils/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/klog.h>
#include <linux/limits.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <time.h>
#include <sys/time.h>
#include <time.h>
#include <errno.h>
#include <signal.h>


#include <cutils/properties.h>
#include <cutils/sockets.h>
#include <stdlib.h>
#include <stdarg.h>


#include <assert.h>
#include <ctype.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <arpa/inet.h>

#include "logtool.h"
#define LOG_TAG "LogTool"
#define FILENAME_PREFIX     "kernel"

#define FLAG_FEED_LOGDOG     (1 << 0)

#define FLAG_BLOCK_FLUSH     (1 << 2)

#define DEFAULT_BUF_SIZE        16384
#define DEFAULT_SYNC_PERIOD     2
#define DEFAULT_KBUF_SIZE       16384
#define TIME_MARK_LEN 100
#define DEFAULT_LOG_ROTATE_SIZE_KBYTES 16
#define DEFAULT_MAX_ROTATED_LOGS 4
#define ARRAY_SIZE(x) (sizeof(x) / sizeof(x[0]))
#define UPTIME 1
#define BRIEF 2

static char * g_outputFileName = NULL;
static int g_logRotateSizeKBytes = 0;                   // 0 means "no log rotation"

static int g_outFD = -1;
static off_t g_outByteCount = 0;

static int g_LogFormat = UPTIME;
bool g_clearBuffer=false;
#define LOGGER_ENTRY_MAX_LEN		(5*1024)

static sigset_t g_signal_set;


static int openLogFile (const char *pathname)
{
    return open(pathname, O_WRONLY | O_APPEND | O_CREAT, S_IRUSR | S_IWUSR);
}


bool checkNameValidate(char* path)
{
		char * pFilename=strrchr(path,(int)'/')+1;
		char * result=strchr(pFilename,'_');
		int len=0;
		if(result!=NULL){
			len=strlen(pFilename)-strlen(result);
		}
		if(len==0){
			return false;
		}
		return true;
}

static void show_fail(const char *cmd){
	SLOGE("fail reseason : %s\n", cmd);
}


void getTimeMark(char * result,int len,int format,char* tag)
{

    struct tm* ptm;
    char timeBuf[32];

    char priChar;
    int prefixSuffixIsHeaderFooter = 0;
    char * ret = NULL;

    priChar = 'I';
    int pid=0;
    int tid=0;

   // struct timeval tv;

   // gettimeofday(&tv, NULL);
   // ptm = localtime(&tv.tv_sec);
    timespec ts;
   	clock_gettime(CLOCK_REALTIME, &ts);
    ptm = localtime(&ts.tv_sec);

    long uptime = getuptime();
    long tv_nsec=0;

    size_t prefixLen, suffixLen;

    switch (format)
    {
        case UPTIME:

            strftime(timeBuf, sizeof(timeBuf), "%Y-%m-%d %H:%M:%S", ptm);
            prefixLen = snprintf(result,len,
                "(%08ld)%s.%03ld %5d %5d %c %-8s: ", uptime, timeBuf, ts.tv_nsec / 1000000,
                pid, tid, priChar, tag);

            break;

        case BRIEF:
        default:
            prefixLen = snprintf(result, len,"%c/%-8s(%5d): ", priChar,tag, pid);
            break;
    }
}

static void rotateLogs_log()
{
   int err;

	if (g_outputFileName == NULL) {
		return;
	}

	char * pFilename=strrchr(g_outputFileName,(int)'/')+1;
	char * result=strchr(pFilename,'_');
	int len=0;
	if(result!=NULL){
		len=strlen(pFilename)-strlen(result);
	}
	char fileNamePrevix[260]={0};
	strncpy(fileNamePrevix,pFilename,len);
	g_outFD=rotateLogs(g_outputFileName,fileNamePrevix,g_outFD,false);
	g_outByteCount = 0;
}

static void setupOutput()
{

	if (g_outputFileName == NULL) {
		g_outFD = STDOUT_FILENO;

	} else {
		struct stat statbuf;

		g_outFD = openLogFile (g_outputFileName);

		if (g_outFD < 0) {
			perror ("couldn't open output file");
			exit(-1);
		}

		fstat(g_outFD, &statbuf);

		g_outByteCount = statbuf.st_size;
	}
}


int write_message(char *stamp, char *msg, int msg_size)
{

    int bytesWritten=0;

    bytesWritten=write(g_outFD, stamp, strlen(stamp));
    if(bytesWritten< 0 ){
    	 perror("output error");
    	 exit(-1);
    }

    bytesWritten+=write(g_outFD, msg, msg_size);

    g_outByteCount += bytesWritten;

	if (g_logRotateSizeKBytes > 0 && (g_outByteCount / 1024) >= g_logRotateSizeKBytes) {
		 rotateLogs_log();
	}

    return 0;
}

void signal_handler(int signum)
{
    if (signum == SIGTERM || signum == SIGINT || signum == SIGQUIT) {

        if (g_outFD>0) {
            close(g_outFD);
        }
        _exit(0);
    }
}

static int sigs[] = {SIGTERM, SIGALRM, SIGINT, SIGHUP, SIGUSR1, SIGUSR2, SIGQUIT};

void setup_signals() {
    struct sigaction act;
    unsigned int i;
    memset(&act, 0, sizeof(act));

    /* Create a mask set of signals to block/unblock */
    sigemptyset(&g_signal_set);
    for (i=0; i < ARRAY_SIZE(sigs); i++) {
        sigaddset(&g_signal_set, sigs[i]);
    }

    /* All of our signals in 'sigs' go to the handler, and we block further
     * instances of all those signals while the handler is running */
    act.sa_handler = signal_handler;
    act.sa_mask = g_signal_set;
    act.sa_flags = 0;
    for (i=0; i < ARRAY_SIZE(sigs); i++) {
        sigaction(sigs[i], &act, NULL);
    }
}

void log_loop(void)
{
    char tbuffer[TIME_MARK_LEN]={0};

    int max_buf_sz;
    char kbuffer[4096*2]={0};
    size_t buf_sz=sizeof(kbuffer);
    time_t old;

    while (1) {
        int bytes_read=0;
        char *cur=NULL;
        int i, offset;
        time_t now;


        bytes_read = klogctl(2, kbuffer, buf_sz);

        if (bytes_read < 0) {
            if (errno == EINTR) {
                continue;
            } else {
            	SLOGE("klogctl(2)");
                exit(-1);
            }
        }

        getTimeMark(tbuffer,TIME_MARK_LEN,g_LogFormat,"Kernel");

        cur = &kbuffer[0];
        for (i = 0; i < bytes_read; i++) {
            if (kbuffer[i] == '\n' || i == (bytes_read - 1)) {
                i++;
                int size = (int)(&kbuffer[i] - cur);
                if (write_message(tbuffer, cur, size) != 0) {
                    /* Something failed; rest before we go again */
                    sleep(1);
                }
                cur = &kbuffer[i];
            }
        }
        old = now;

    }
}

void usage(void)
{
	fprintf(stderr, "options include:\n"
        "  -s              Set default filter to silent.\n"
        "                  Like specifying filterspec '*:s'\n"
        "  -f <filename>   Log to file. Default to stdout\n"
        "  -r [<kbytes>]   Rotate log every kbytes. (16 if unspecified). Requires -f\n"
        "  -n <count>      Sets max number of rotated logs to <count>, default 4\n"
        "  -v <format>     Sets the log print format, where <format> is one of:\n\n"
        "                  brief process tag thread raw time threadtime long\n\n"
        "  -c              clear (flush) the entire log and exit\n"
        "  -d              dump the log and then exit (don't block)\n"
        "  -t <count>      print only the most recent <count> lines (implies -d)\n"
        "  -g              get the size of the log's ring buffer and exit\n"
        "  -b <buffer>     Request alternate ring buffer, 'main', 'system', 'radio'\n"
        "                  or 'events'. Multiple -b parameters are allowed and the\n"
        "                  results are interleaved. The default is -b main -b system.\n"
        "  -B              output the log in binary");
}


int main(int argc, char **argv)
{
    char currpath[PATH_MAX], *pathpiece;
    char logpath[PATH_MAX];
    struct stat st;
    int ret;
    int err;
    int hasSetLogFormat = 0;

    SLOGE("kernel user main");

    while (1) {
        int opt = getopt(argc, argv, "f:r:b:v:hc");

        if (opt < 0)
            break;

        switch (opt)
        {
        case 'f':

        	if(checkNameValidate(optarg)==false){
				show_fail("path invalidate!!!");
				exit(-1);
        	}
        	            	//temp
        	g_outputFileName = optarg;
            break;
        case 'r':

			if (optarg == NULL) {
				g_logRotateSizeKBytes= DEFAULT_LOG_ROTATE_SIZE_KBYTES;
			} else {
				long logRotateSize;
				char *lastDigit;
				/*
				if (!isdigit(optarg)) {
					SLOGI("Invalid parameter to -r\n");
					usage();
					exit(-1);
				}*/
				g_logRotateSizeKBytes = atoi(optarg);
			}
			break;
        case 'b':
        	//only kernel
        	break;
        case 'v':
        	if(strcmp(optarg,"uptime")!=0){
        		g_LogFormat=BRIEF;
        	}
           break;
        case 'c':
            g_clearBuffer=true;
          break;

        case 'h':
            usage();
            exit(-1);
            break;
        }
    }

    if (geteuid() != 0) {
        SLOGE( "Must be root!\n");
        exit(-1);
    }

    setupOutput();
    log_loop();


    return 0;
}
