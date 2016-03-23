#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <cutils/log.h>
#include <cutils/properties.h>
#include "logtool.h"
#include <string.h>

#define LOG_TAG "LogTool"
char gszLogRootDir[BUF_SIZE]={0};
char gszLogDir[BUF_SIZE]={0};
void dumpLog(char* data);
#define MAIN_LOG 1
#define KERNEL_LOG 2
#define RADIO_LOG 3
#define EVENT_LOG 4
#define COMBINE_LOG 5
#define TCP_DUMP 6

#define SDK_4_3 18


#define LOG_COMBINE_MAIN 	1
#define LOG_COMBINE_SYSTEM 	2
#define LOG_COMBINE_KERNEL 	4
#define LOG_COMBINE_RADIO  8
#define LOG_COMBINE_EVENTS 16
#define DEFAULT_LOG_SIZE 20000
#define LOG_FILE_DIR    "/dev/log/"
int execLogCmd(char * logOpt, char *cmd,char *storePath,char * gszLogDir,int size);

int main(int argc, char **argv)
{
	int mode=-1;
	if(argc==1){
		printf("error parameter is too few");
		exit(-1);
	}else if(argc==2 && 0 == strcmp(argv[1], "main")){
		SLOGI("main log");
		mode=MAIN_LOG;
	}else if(argc==2 && 0 == strcmp(argv[1], "kernel")){
		SLOGI("kernel log");
		mode=KERNEL_LOG;
	}else if(argc==2 && 0 == strcmp(argv[1], "radio")){
		SLOGI("radio log");
		mode=RADIO_LOG;
	}else if(argc==2 && 0 == strcmp(argv[1], "event")){
		SLOGI("event log");
		mode=EVENT_LOG;
	}else if(argc==2 && 0 == strcmp(argv[1], "combine")){
		SLOGI("combine log");
		mode=COMBINE_LOG;
	}else if(argc==2 && 0 == strcmp(argv[1], "tcpdump")){
		SLOGI("tcp log");
		mode=TCP_DUMP;
	}else{
		printf("error parameter error");
		exit(-1);
	}

	char szValue[PROPERTY_VALUE_MAX]={0};
	char szDebug[255]={0};
	char szDate[BUF_TIME_SIZE]={0};
        char saveDir[BUF_TIME_SIZE]={0};////////////////
	getLogRootPath(gszLogRootDir);

	char storePath[BUF_SIZE]={0};

	int returnCode=createDir(gszLogRootDir,false,false);
	SLOGI("gszLogRootDir=%s,returnCode=%d",gszLogRootDir,returnCode);
	sprintf(szDebug,"echo createDir_path=%s,returncode=%d >> /data/logtool_dump.txt",gszLogRootDir,returnCode);
	system(szDebug);

	if(returnCode==ERROR_BOOTSTRAP_UNMOUNT){
		//waiting mkdir success //user mode discovery boot mount slowly
		exit(-1);
		//createDirWaitMount(gszLogRootDir);
	}
	if(mode==TCP_DUMP && isSaveMicroSD()==false){
		sprintf(gszLogDir,	"%s%s/"		,gszLogRootDir,DIR_NAME_TCPDUMP);
	}else{
		getDumpDate(szDate);
                property_get(KEY_SAVE_DIR,saveDir,"1111");///////////////////////
		if(strlen(szDate)==0)
		{
			printf("date no ready leave");
			exit(-1);
		}
		sprintf(gszLogDir,		"%s%s/"		,gszLogRootDir,szDate);
		SLOGI("gszLogDir=%s",gszLogDir);
		SLOGI("=======data is %s",szDate);
                SLOGI("saveDir is %s",saveDir);
	}
	 returnCode=createDir(gszLogDir,false,false);
	 SLOGI("gszLogDir=%s,returnCode=%d",gszLogDir,returnCode);
	 if(returnCode==ERROR_BOOTSTRAP_UNMOUNT){
		 exit(-1);
	 }


	int execReturn=0;
	switch(mode){
	case MAIN_LOG:
		execReturn=execLogCmd("Main", "asuslogcat -b main -b system  %s -v time -f",storePath,gszLogDir,DEFAULT_LOG_SIZE);
		break;
	case KERNEL_LOG:
		if(isFileExist("/dev/log/kernel")==false){

			if(isFileExist("/system/bin/logkmsg")==false){
				SLOGI("*******************************/proc/kmsg log");
				onHandelLogPath(false, storePath,gszLogDir,"Kernel");
				execReturn=execCmd("cat /proc/kmsg > ",storePath);
			}else{
				SLOGI("*******************************logkmsg");
				execReturn=execLogCmd("Kernel", "logkmsg -b kernel  %s -v uptime -f",storePath,gszLogDir,DEFAULT_LOG_SIZE);
			}
		}else {
			execReturn=execLogCmd("Kernel", "asuslogcat -b kernel  %s -v time -f",storePath,gszLogDir,DEFAULT_LOG_SIZE);
		}

		break;
	case RADIO_LOG:
		execReturn=execLogCmd("Radio", "asuslogcat -b radio  %s -v time -f",storePath,gszLogDir,DEFAULT_LOG_SIZE);
		break;
	case EVENT_LOG:
		execReturn=execLogCmd("Event", "asuslogcat -b events  %s -v time -f",storePath,gszLogDir,DEFAULT_LOG_SIZE);
		break;
	case COMBINE_LOG:

		property_get(KEY_COMBINE_CONFIG,szValue,"");
		if(strlen(szValue)==0){//select all
			execReturn=execLogCmd("Combine", "asuslogcat -b main -b system -b events -b kernel -b radio %s -v time -f",storePath,gszLogDir,DEFAULT_LOG_SIZE);
		}else{
			int digit=atoi(szValue);
			char szCombincmd[260]="asuslogcat";
			bool mainLog=((digit&LOG_COMBINE_MAIN)==LOG_COMBINE_MAIN)?true:false;
			bool systemLog=((digit&LOG_COMBINE_SYSTEM)==LOG_COMBINE_SYSTEM)?true:false;
			bool kernelLog=((digit&LOG_COMBINE_KERNEL)==LOG_COMBINE_KERNEL)?true:false;
			bool radioLog=((digit&LOG_COMBINE_RADIO)==LOG_COMBINE_RADIO)?true:false;
			bool eventsLog=((digit&LOG_COMBINE_EVENTS)==LOG_COMBINE_EVENTS)?true:false;
			if(mainLog){
				strcat(szCombincmd," -b main");
			}
			if(systemLog){
				strcat(szCombincmd," -b system");
			}
			if(kernelLog){
				strcat(szCombincmd," -b kernel");
			}
			if(radioLog){
				strcat(szCombincmd," -b radio");
			}
			if(eventsLog){
				strcat(szCombincmd," -b events");
			}
			strcat(szCombincmd," %s -v time -f");
			execReturn=execLogCmd("Combine", szCombincmd,storePath,gszLogDir,80000);
		}

		break;
	case TCP_DUMP:
		char szTcpDumpPath[PATH_SIZE]={0};
		char szDate[PATH_SIZE]={0};
		getDate(szDate,PATH_SIZE);
		sprintf(szTcpDumpPath,	"%s%s_%s.pcap"		,gszLogDir,FILE_NAME_TCPDUMP,szDate);
		SLOGI("====tcpdump is %s",szTcpDumpPath);
		execReturn=execCmd("tcpdump -i any -p -s 0 -W 2 -C 100 -w",szTcpDumpPath);
		break;
	}
	SLOGI("leave mode:%d,execReturn=%d",mode,execReturn);
	sprintf(szDebug,"echo execCmd_returncode=%d >> /data/logtool_dump.txt",execReturn);
	system(szDebug);
}



int execLogCmd(char * logOpt, char *cmd,char *storePath,char * gszLogDir,int size){
	char szLogCmd[BUF_SIZE]={0};
	char rotatesize[BUF_SIZE]={0};
		if(getBuildSDK()==SDK_4_3){
			sprintf(rotatesize,"%s %d","-r",size);//-r 20000
		}else{
			sprintf(rotatesize,"%s%d","-r",size);//-r20000
		}
	onHandelLogPath(false, storePath,gszLogDir,logOpt);
	sprintf(szLogCmd,cmd,rotatesize);
	SLOGI("====logdumps_Logcommand is %s",szLogCmd);
	SLOGI("====logdumps_storepath is %s",storePath);
	int result=execCmd(szLogCmd,storePath);
	return result;
}


void dumpLog(char* data){
	char szDebug[255]={0};
	sprintf(szDebug,"echo %s >> /data/logtool_dump.txt",data);
	system(szDebug);
}
