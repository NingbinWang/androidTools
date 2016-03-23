#include <errno.h>
#include <cutils/log.h>
#include <cutils/properties.h>

#include "detectprop.h"
#include "logdump.h"
#include "logtool.h"
//
#pragma GCC diagnostic ignored "-Wwrite-strings"

#define LOG_TAG "LogTool"

#define MAX_LOG_LEN		(5*1024+1)
#define MAX_LOGFILE_KBSIZE	20000
#define BUF_SIZE 256




//#define DIR_NAME_KERNEL 	"KernelLog"
//#define DIR_NAME_MAIN		"MainLog"
//#define DIR_NAME_EVENT  	"EventLog"
//#define DIR_NAME_RADIO  	"RadioLog"

char gszLogRootDir[BUF_SIZE]={0};
char gszLogKernelDir[BUF_SIZE]={0};
char gszLogMainDir[BUF_SIZE]={0};
char gszLogEventDir[BUF_SIZE]={0};
char gszLogRadioDir[BUF_SIZE]={0};

#define FILE_NAME_KERNEL "Kernel"
#define FILE_NAME_MAIN "Main"
#define FILE_NAME_RADIO "Radio"
#define FILE_NAME_EVENTS "Events"

FILE *g_pMain=NULL;
FILE *g_pEvent=NULL;
FILE *g_pRadio=NULL;
FILE *g_pKernel=NULL;
FILE *g_pWatchProp=NULL;

char gszKernelPath[BUF_SIZE]={0};
char gszMainPath[BUF_SIZE]={0};
char gszRadioPath[BUF_SIZE]={0};
char gszEventPath[BUF_SIZE]={0};

bool gbUpdateMtpPath=true;
bool gbkernelEnable=false;
bool gbMainEnable=false;
bool gbEventEnable=false;
bool gbRadioEnable=false;

bool gbkernelChange=false;
bool gbMainChange=false;
bool gbEventChange=false;
bool gbRadioChange=false;

bool gbkernelWaitMount=false;
bool gbMainWaitMount=false;
bool gbEventWaitMount=false;
bool gbRadioWaitMount=false;

bool gbWriteMain=false;
bool gbWriteKernel=false;
bool gbWriteEvent=false;
bool gbWriteRadio=false;

int gMainfd=0;
int gKernelfd=0;
int gRadiofd=0;
int gEventfd=0;

void resetLogPath();
int onOpenLogFile(int fd,bool waitMircoSDBootMount,char * storePath,char * dirPath,char * filename )
{
		if(fd!=0){
			close(fd);
			fd=0;
			execCmd("chmod -R 777"	,	dirPath);
		}
		onHandelLogPath( waitMircoSDBootMount,storePath, dirPath, filename );

		fd=openfile(storePath);
		if (fd < 0) {
			perror ("couldn't open output file");
			exit(-1);
		}

		return fd;
}



void* start_kernel_run(void * arg)
{

	char szKernelPath[BUF_SIZE]={0};
	//int fd=0;
	long byteCount=0;
	int ret;
	char szOutput[MAX_LOG_LEN]={0};
	while (fgets(szOutput, sizeof szOutput, g_pKernel))
	{
		if(gbkernelEnable){
			if(gbkernelChange==true)
			{//換路徑

				gKernelfd=onOpenLogFile( gKernelfd,gbkernelWaitMount,szKernelPath,gszLogKernelDir,FILE_NAME_KERNEL );
				byteCount = getFileSize( gKernelfd);
				gbkernelWaitMount=false;
				gbkernelChange=false;
				if(LOG_DEBUG==true){
					SLOGE("kernel path %s",szKernelPath);
				}
				if(gbkernelWaitMount){
					//move log to micro sdcard

					gbkernelWaitMount=false;
				}
			}

			if(gKernelfd!=0){
				gbWriteKernel=true;
				ret=write(gKernelfd, szOutput, strlen(szOutput));
				gbWriteKernel=false;
			}else{
				ret=0;
			}

			byteCount+=ret;
			if (MAX_LOGFILE_KBSIZE > 0 && (byteCount / 1024) >= MAX_LOGFILE_KBSIZE)
			{
				gKernelfd=rotateLogs(szKernelPath,FILE_NAME_KERNEL,gKernelfd,gbUpdateMtpPath);
					if(gKernelfd>=0){
						byteCount=0;
					}
			}
		}else{
			gKernelfd= closelogfd( gKernelfd,gszLogKernelDir,gbUpdateMtpPath);
		}
	}
	gKernelfd= closelogfd( gKernelfd,gszLogKernelDir,gbUpdateMtpPath);

	if(LOG_DEBUG==true){
		SLOGE("kernel leave");
	}

	return 0;
}



void* start_main_run(void * arg)
{

	char szMainPath[BUF_SIZE]={0};

	//int fd=0;
	long byteCount=0;
	int ret;
	char szOutput[MAX_LOG_LEN]={0};
	while (fgets(szOutput, sizeof szOutput, g_pMain))
	{
		if(gbMainEnable){
			if(gbMainChange==true)
			{//換路徑
				gMainfd=onOpenLogFile( gMainfd,gbMainWaitMount,szMainPath,gszLogMainDir,FILE_NAME_MAIN );
				byteCount = getFileSize( gMainfd);
				gbMainWaitMount=false;
				gbMainChange=false;
				if(LOG_DEBUG==true){
					SLOGE("main path %s",szMainPath);
				}

			}

			if(gMainfd!=0){
				gbWriteMain=true;
				ret=write(gMainfd, szOutput, strlen(szOutput));
				gbWriteMain=false;
			}else{
				ret=0;
			}

			byteCount+=ret;
			if ( (byteCount / 1024) >= MAX_LOGFILE_KBSIZE)
			{
				gMainfd=rotateLogs(szMainPath,FILE_NAME_MAIN,gMainfd,gbUpdateMtpPath);
					if(gMainfd>=0){
						byteCount=0;
					}
			}
		}else{
			gMainfd= closelogfd( gMainfd,gszLogMainDir,gbUpdateMtpPath);
		}

	}
	gMainfd= closelogfd( gMainfd,gszLogMainDir,gbUpdateMtpPath);
	if(LOG_DEBUG==true){
		SLOGE("main leave");
	}


	return 0;
}

void* start_radio_run(void * arg)
{

	char szRadioPath[BUF_SIZE]={0};
	//int fd=0;
	long byteCount =0;
	int ret;
	char szOutput[MAX_LOG_LEN]={0};
	while (fgets(szOutput, sizeof szOutput, g_pRadio))
	{
		if(gbRadioEnable){
			if(gbRadioChange==true)
			{//換路徑
				gRadiofd=onOpenLogFile( gRadiofd,gbRadioWaitMount,szRadioPath,gszLogRadioDir,FILE_NAME_RADIO );
				byteCount = getFileSize( gRadiofd);
				gbRadioWaitMount=false;
				gbRadioChange=false;
				if(LOG_DEBUG==true){
					SLOGE("radio path %s",szRadioPath);
				}

			}
			if(gRadiofd!=0){
				gbWriteRadio=true;
				ret=write(gRadiofd, szOutput, strlen(szOutput));
				gbWriteRadio=false;
			}else{
				ret=0;
			}

			byteCount+=ret;
			if (MAX_LOGFILE_KBSIZE > 0 && (byteCount / 1024) >= MAX_LOGFILE_KBSIZE)
			{
					gRadiofd=rotateLogs(szRadioPath,FILE_NAME_RADIO,gRadiofd,gbUpdateMtpPath);
					if(gRadiofd>=0){
						byteCount=0;
					}
			}
		}else{
			gRadiofd= closelogfd( gRadiofd,gszLogRadioDir,gbUpdateMtpPath);
		}
	}
	gRadiofd= closelogfd( gRadiofd,gszLogRadioDir,gbUpdateMtpPath);
	if(LOG_DEBUG==true){
		SLOGE("radio leave");
	}

	return 0;
}

void* start_Events_run(void * arg)
{

	char szEventPath[BUF_SIZE]={0};

	long byteCount =0;

	int ret;
	char szOutput[MAX_LOG_LEN]={0};
	while (fgets(szOutput, sizeof szOutput, g_pEvent))
	{
		if(gbEventEnable)
		{
			if(gbEventChange==true)
			{//換路徑
				gEventfd=onOpenLogFile( gEventfd,gbEventWaitMount,szEventPath,gszLogEventDir,FILE_NAME_EVENTS );
				byteCount = getFileSize( gEventfd);
				gbEventWaitMount=false;
				gbEventChange=false;
				if(LOG_DEBUG==true)
				{
					SLOGE("Events path %s",szEventPath);
				}

			}
			if(gEventfd!=0){
				gbWriteEvent=true;
				ret=write(gEventfd, szOutput, strlen(szOutput));
				gbWriteEvent=false;
			}else{
				ret=0;
			}


			byteCount+=ret;
			if (MAX_LOGFILE_KBSIZE > 0 && (byteCount / 1024) >= MAX_LOGFILE_KBSIZE)
			{
					gEventfd=rotateLogs(szEventPath,FILE_NAME_EVENTS,gEventfd,gbUpdateMtpPath);
					if(gEventfd>=0){
						byteCount=0;
					}
			}
		}else{
			gEventfd= closelogfd( gEventfd,gszLogEventDir,gbUpdateMtpPath);
		}
	}

	gEventfd= closelogfd( gEventfd,gszLogEventDir,gbUpdateMtpPath);
	if(LOG_DEBUG==true){
		SLOGE("Events leave");
	}

	return 0;
}



void startKernel(){
	if(LOG_DEBUG==true){
		SLOGE("startKernel");
	}

	gbkernelChange=true;
	gbkernelEnable=true;
	 pthread_t id;
	 if ((g_pKernel = popen("logcat -b kernel -v uptime", "r")) == NULL){
	 					SLOGE("kernelcmd popen error %s" , strerror(errno));
	 }
	 int ret=pthread_create(&id,NULL,start_kernel_run,NULL);
	 if(ret!=0){
		 printf ("Create pthread error!\n");
		 exit (1);
	 }

}

void startMain(){
	if(LOG_DEBUG==true){
		SLOGE("startMain");
	}

	gbMainChange=true;
	gbMainEnable=true;
	pthread_t id;

	if ((g_pMain = popen("logcat -b main -b system -v uptime", "r")) == NULL){
						SLOGE("kernelcmd popen error %s" , strerror(errno));
	}
	 int ret=pthread_create(&id,NULL,start_main_run,NULL);
	 if(ret!=0){
		 printf ("Create pthread error!\n");
		 exit (1);
	 }

}

void startRadio(){
	if(LOG_DEBUG==true){
		SLOGE("startRadio");
	}

	gbRadioChange=true;
	gbRadioEnable=true;
	pthread_t id;
	if ((g_pRadio = popen("logcat -b radio -v uptime", "r")) == NULL){
						SLOGE("kernelcmd popen error %s" , strerror(errno));
	}
	 int ret=pthread_create(&id,NULL,start_radio_run,NULL);
	 if(ret!=0){
		 printf ("Create pthread error!\n");
		 exit (1);
	 }

}

void startEvent(){
	if(LOG_DEBUG==true){
		SLOGE("startEvent");
	}

	gbEventChange=true;
	gbEventEnable=true;
	pthread_t id;
	if ((g_pEvent = popen("logcat -b events -v uptime", "r")) == NULL){
						SLOGE("kernelcmd popen error %s" , strerror(errno));
		}
	 int ret=pthread_create(&id,NULL,start_Events_run,NULL);
	 if(ret!=0){
		 printf ("Create pthread error!\n");
		 exit (1);
	 }
}

void startRecordLog()
{
	bool diskSpaceValid=checkMemorySafe(gszLogRootDir);
	if(isMainSupport() && diskSpaceValid)
	{
		startMain();
	}

	if(isKernelSupport() && diskSpaceValid){
		startKernel();
	}

	if(isRadioSupport() && diskSpaceValid){
		startRadio();
	}

	if(isEventSupport() && diskSpaceValid ){
		startEvent();
	}
}

void setAllLogDir(char * rootlogdir){

	char szDate[BUF_TIME_SIZE]={0};
	getDumpDate(szDate);
	sprintf(gszLogKernelDir,	"%s%s/"		,rootlogdir,szDate);
	sprintf(gszLogMainDir,		"%s%s/"		,rootlogdir,szDate);
	sprintf(gszLogEventDir,		"%s%s/"		,rootlogdir,szDate);
	sprintf(gszLogRadioDir,		"%s%s/"		,rootlogdir,szDate);


}

void callback(char * name,char *value){

	if(LOG_DEBUG==true){
		SLOGI("logcat name=%s, value=%s",name,value);
	}

	if(strcmp(name,KEY_ENABLE_MAIN)==0)
	{//main

		if(checkEnble(value))
		{//start
			char szDate[BUF_TIME_SIZE]={0};
			getDumpDate(szDate);
			sprintf(gszLogMainDir,		"%s%s/"	,gszLogRootDir,szDate);
			if(g_pMain==NULL)
			{
				startMain();
			}else{
				gbMainEnable=true;
			}
		}else{//stop

			gbMainChange=true;
			gbMainEnable=false;

			if(LOG_DEBUG==true){
				SLOGI("gbMainChange stop");
			}

		}
	}else if(strcmp(name,KEY_ENABLE_KERNEL)==0)//kernel
	{
		if(checkEnble(value))
		{//start
			char szDate[BUF_TIME_SIZE]={0};
			getDumpDate(szDate);
			sprintf(gszLogKernelDir,		"%s%s/"	,gszLogRootDir,szDate);
			if(g_pKernel==NULL){
				startKernel();
			}else{
				gbkernelEnable=true;
			}
		}else{//stop

			gbkernelChange=true;
			gbkernelEnable=false;
			if(LOG_DEBUG==true){
				SLOGI("g_pKernel stop");
			}

		}
	}
	else if(strcmp(name,KEY_ENABLE_RADIO)==0)
	{//radio
			if(checkEnble(value))
			{//start
				char szDate[BUF_TIME_SIZE]={0};
				getDumpDate(szDate);
				sprintf(gszLogRadioDir,		"%s%s/"	,gszLogRootDir,szDate);
				if(g_pRadio==NULL){
					startRadio();
				}else{
					gbRadioEnable=true;
				}
			}else{//stop
				gbRadioChange=true;
				gbRadioEnable=false;
				gRadiofd=closelogfd(gRadiofd,gszLogRadioDir,gbUpdateMtpPath);
				if(LOG_DEBUG==true){
					SLOGI("g_pRadio=NULL");
				}

			}
	}
	else if(strcmp(name,KEY_ENABLE_EVENT)==0)
	{//event
				if(checkEnble(value))
				{//start
					char szDate[BUF_TIME_SIZE]={0};
					getDumpDate(szDate);
					sprintf(gszLogEventDir,		"%s%s/"	,gszLogRootDir,szDate);
					if(g_pEvent==NULL){
						startEvent();
					}else{
						gbEventEnable=true;
					}
				}else{//stop
					gbEventEnable=false;
					gbEventChange=true;
					gEventfd=closelogfd(gEventfd,gszLogEventDir,gbUpdateMtpPath);
					if(LOG_DEBUG==true){
						SLOGI("g_pEvent=NULL");
					}

				}
	}
	else if(strcmp(name,LOG_PATH_CHANGE)==0){
		int i=0;
		while(gbWriteMain){//avoid is writing log
			i++;
		}
		gMainfd=closelogfd(gMainfd,gszLogMainDir,gbUpdateMtpPath);
		while(gbWriteKernel){
			i++;
		}
		gKernelfd=closelogfd(gKernelfd,gszLogKernelDir,gbUpdateMtpPath);
		while(gbWriteRadio){
			i++;
		}
		gRadiofd=closelogfd(gRadiofd,gszLogRadioDir,gbUpdateMtpPath);
		while(gbWriteEvent){
			i++;
		}
		if(LOG_DEBUG==true){
			SLOGI("close wait result=%d",i);
		}
		gEventfd=closelogfd(gEventfd,gszLogEventDir,gbUpdateMtpPath);

		execCmd("chmod -R 777"	,	gszLogRootDir);
		sprintf(gszLogRootDir,"%s",value);
		gbUpdateMtpPath=isMtpUpdate(gszLogRootDir);
		resetLogPath();
	}
}

void resetLogPath(){
	setAllLogDir(gszLogRootDir);
	createDirWaitMount(gszLogRootDir);
	createDirWaitMount(gszLogKernelDir);
	createDirWaitMount(gszLogMainDir);
	createDirWaitMount(gszLogEventDir);
	createDirWaitMount(gszLogRadioDir);
	//createDir(gszLogRootDir,false,gbUpdateMtpPath);
	//createDir(gszLogKernelDir,true,gbUpdateMtpPath);
	//createDir(gszLogMainDir,true,gbUpdateMtpPath);
	//createDir(gszLogEventDir,true,gbUpdateMtpPath);
	//createDir(gszLogRadioDir,true,gbUpdateMtpPath);
	gbRadioChange=true;
	gbEventChange=true;
	gbkernelChange=true;
	gbMainChange=true;
}

void* waitMountRun(void * arg){
	SLOGI("waitMountRun before");

	waitMount( true,gszLogRootDir);
	//mount
	if(isSaveMicroSD()){
		getLogRootPath(gszLogRootDir);

		SLOGI("waitMountRun after");
		bool diskSpaceValid=checkMemorySafe(gszLogRootDir);
		if(isMainSupport() && diskSpaceValid)
		{
			gbMainWaitMount=true;
		}

		if(isKernelSupport() && diskSpaceValid){
			gbkernelWaitMount=true;
		}

		if(isRadioSupport() && diskSpaceValid){
			gbRadioWaitMount=true;
		}

		if(isEventSupport() && diskSpaceValid){
			gbEventWaitMount=true;
		}
		resetLogPath();

	}
	return 0;
}

bool init()
{


	char szValue[256]={0};
	property_get(DUMP_ENABLE,szValue,NO_EXIST);
	if(strcmp(szValue,NO_EXIST)==0){
		//user do nothing
		SLOGE("DUMP_ENABLE NO_EXIST exit");
		exit(0);
	}
	bool result=true;
	getLogRootPath(gszLogRootDir);

	gbUpdateMtpPath=isMtpUpdate(gszLogRootDir);
	//system("echo 34 > /data/bbb.txt");
	//system("mkdir  /data/ppp22");
	int returnCode=createDir(gszLogRootDir,false,gbUpdateMtpPath);
   // system("echo 33 > /data/aaa.txt");
    char debug[255]={0};
    sprintf(debug,"echo returncode=%d >> /data/logtool_dump.txt",returnCode);
    system(debug);

	if(isMicroStrorage(gszLogRootDir) && returnCode==ERROR_BOOTSTRAP_UNMOUNT){
		//write log
		sprintf(debug,"echo mircorsd >> /data/logtool_dump.txt");
		system(debug);
		setBootMountEnable(false);
		sprintf(gszLogRootDir,"%s",DIR_MICRO_WAIT_MOUNT_DIR);
		createDir(gszLogRootDir,false,gbUpdateMtpPath);
		result=false;
	}else if(returnCode==ERROR_BOOTSTRAP_UNMOUNT){
		//waiting mkdir success //user mode discovery boot mount slowly
		createDirWaitMount(gszLogRootDir);
	}
	setAllLogDir(gszLogRootDir);
	//returnCode=createDir(gszLogRootDir,false,gbUpdateMtpPath);


	createDirWaitMount(gszLogRootDir);
	createDirWaitMount(gszLogKernelDir);
	createDirWaitMount(gszLogMainDir);
	createDirWaitMount(gszLogEventDir);
	createDirWaitMount(gszLogRadioDir);

	return result;
}



int main(int argc, char *argv[]){

	 if(LOG_DEBUG==true){
		 SLOGI("logcat init");
	}

	if(getuptime()<300){
		char szDate[BUF_TIME_SIZE]={0};
		getDate(szDate,BUF_TIME_SIZE);
		property_set(KEY_DATE_FOLDER,szDate);
	}


	bool result=init();
	 if(LOG_DEBUG==true){
		 SLOGI("startRecordLog");
	}

	startRecordLog();
	if(result==false){
		pthread_t id;
		int ret=pthread_create(&id,NULL,waitMountRun,NULL);
		 if(ret!=0){
			 printf ("Create pthread error!\n");
			 exit (1);
		 }
	}
	 if(LOG_DEBUG==true){
		 SLOGI("---enter detectProp---");
	}

	detectProp();//block message

	if(LOG_DEBUG==true){
		SLOGI("leave");
	}


	return 0;


 }
