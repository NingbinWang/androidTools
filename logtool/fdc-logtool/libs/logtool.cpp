#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <fcntl.h>
#include <time.h>
#include <sys/stat.h>
#include <sys/statfs.h>
#include <sys/sysinfo.h>
#include <cutils/log.h>
#include <cutils/properties.h>
#include <string.h>

#include "logtool.h"
#define LOG_TAG "LogTool"

#define BUF_SIZE 256
#pragma GCC diagnostic ignored "-Wwrite-strings"


#ifdef __cplusplus
extern "C" {
#endif

void onHandleLogPathbyId(char *dir,char *fileNamePrevix,char *resultPath);
void deleteExistpathbyId(int idx ,char *filePath,char * fileName);
int find_Path_dirs(const char* dir,char*keyword,char *returnPath);
int getMaxRotateNum(bool bplog);
bool isUserBuild();
int rotateLogs(char *filePath,char * fileNamePrevix,int fd,bool bUpdateMtp)
{

	int err;

    // Can't rotate logs if we're not outputting to a file
    if (fileNamePrevix == NULL || filePath==NULL)
    {
        return 0;
    }
    close(fd);
    fd=0;
    char * pFilename=strrchr(filePath,(int)'/');
    char szDir[PATH_SIZE]={0};
    strncpy(szDir, filePath, pFilename-filePath);
    strcat(szDir,"/");
    char Path[PATH_SIZE]={0};


    onHandleLogPathbyId(szDir,fileNamePrevix,Path);
    fd = openfile (Path);

    if (fd < 0) {
        perror ("couldn't open output file");
        exit(-1);
    }


    return fd;

}


void onHandleLogPathbyId(char *dir,char *fileNamePrevix,char *resultPath)
{
	FILE * pTempFile;
	int mCurrent=1;
	long lSize;
	char tempPath[PATH_SIZE]={0};
	char output[256]={0};
	char fileNameSuffix[256]="log";
	sprintf(tempPath,"%s%s_RotateLastIdx",dir,fileNamePrevix);
	SLOGI("tempPath=%s",tempPath);
	pTempFile = fopen ( tempPath , "rb" );
	bool isBplog=false;
	if(strcmp(FILE_NAME_BPLOG,fileNamePrevix)==0){
		isBplog=true;
		strcpy(fileNameSuffix,"istp");
	}
	     if (pTempFile!=NULL)
	     {
			  char buffer[100]={0};
			  // obtain file size:
			  fseek (pTempFile , 0 , SEEK_END);
			  lSize = ftell (pTempFile);
			  rewind (pTempFile);
			  size_t result = fread (buffer,1,lSize,pTempFile);
			  fclose(pTempFile);

			  int current=atoi(buffer);
			  if(current >= getMaxRotateNum(isBplog)){
				  mCurrent=1;
			  }else{
				  mCurrent=current+1;
			  }
			  deleteExistpathbyId(mCurrent ,dir,fileNamePrevix);
	     }


	    sprintf(output,"echo %d >  %s",mCurrent,tempPath);
	    system(output);

	    char date[BUF_TIME_SIZE]={0};

	    getDate(date,BUF_TIME_SIZE);

	    if(mCurrent<10){
	    	 sprintf(resultPath,"%s%s_0%d_%s.%s",dir,fileNamePrevix,mCurrent,date,fileNameSuffix);//fileSuffix modem  .istp other log
	    }else{
	    	 sprintf(resultPath,"%s%s_%d_%s.%s",dir,fileNamePrevix,mCurrent,date,fileNameSuffix);
	    }
}

void deleteExistpathbyId(int idx ,char *szDir,char * fileName){
	//char * pFilename=strrchr(filePath,(int)'/');
	//char szDir[PATH_SIZE]={0};
	//strncpy(szDir, filePath, pFilename-filePath);
	const bool searchFile=false;
	char returnPath[PATH_SIZE]={0};
	char keyword[PATH_SIZE]={0};
	if(idx<10){
		sprintf(keyword,"%s_0%d",fileName,idx);
	}else{
		sprintf(keyword,"%s_%d",fileName,idx);
	}
	if(searchFile){
		find_Path_dirs(szDir,keyword,returnPath);
		int len=strlen(returnPath);
		if(len>0){
			 char szCmd[PATH_SIZE]={0};
			 sprintf(szCmd,"rm -f %s",returnPath);
			 system(szCmd);
		 }
	}else{
		sprintf(returnPath,"%s%s",szDir,keyword);
		char szCmd[PATH_SIZE]={0};
		sprintf(szCmd,"rm -rf %s*",returnPath);
		system(szCmd);
	}

}


int find_Path_dirs(const char* dir,char*keyword,char *returnPath)
  {
    if(returnPath == NULL || keyword==NULL){
    	return -1;
    }
	int dir_count = 0;
      struct dirent* dent;
      DIR* srcdir = opendir(dir);

      if (srcdir == NULL)
      {
          perror("opendir");
          return -1;
      }

      while((dent = readdir(srcdir)) != NULL)
      {
          struct stat st;

          if(strcmp(dent->d_name, ".") == 0 || strcmp(dent->d_name, "..") == 0)
              continue;

          if (fstatat(dirfd(srcdir), dent->d_name, &st, 0) < 0)
          {
              perror(dent->d_name);
              continue;
          }

          SLOGI(dent->d_name);
          if (strncmp (dent->d_name,keyword,strlen(keyword)) == 0)
		 {

        	  sprintf(returnPath,"%s/%s",dir,dent->d_name);
        	  break;
        	 // system("rm -rf ");
        	 // SLOGI("YA %s",dent->d_name);

		 }
          //if (S_ISDIR(st.st_mode)) dir_count++;
      }
      closedir(srcdir);
      return dir_count;
  }

bool isDataStrorage(char * path)
{

	if(strncmp(path,DIR_DATA,strlen(DIR_DATA)-1)==0)
	{
		return true;
	}else{
		return false;
	}
}

bool isExternelStrorage(char * path)
{

	if(strncmp(path,DIR_SDCARD,strlen(DIR_SDCARD)-1)==0)
	{
		return true;
	}else{
		return false;
	}
}

bool isMicroStrorage(char * path)
{

	if(strncmp(path,DIR_MICRO_SDCRAR,strlen(DIR_MICRO_SDCRAR)-1)==0)
	{
		return true;
	}else{
		return false;
	}

}


long getuptime()
{
	struct sysinfo info;
	sysinfo(&info);
	return info.uptime;
}




void waitMount(bool saveMicroSD,char* path)//store log data
{
	char szValue[256]={0};
	struct timespec start, stop;
	double accum;
	if( clock_gettime( CLOCK_REALTIME, &start) == -1 ) {
		  perror( "clock gettime" );
	}
	if(path!=NULL){
		SLOGI("waitMount %s",path);
	}
	while(true)
	{
		if(saveMicroSD){
			if(isSaveMicroSD()==false){// sdard remove
				break;
			}
			if(getuptime()>60){
				execCmd("mkdir"		,path);
				DIR* dir = opendir(path);

				 if(dir){
					 SLOGI("open dir success when wait mount");
					 closedir(dir);
					 break;
				 }
			}else{
				if(isBootMountEnable()){
					break;
				};
			}



		}else{
			execCmd("mkdir"		,path);
			DIR* dir = opendir(path);

			 if(dir){
				 closedir(dir);
				 break;
			 }

		}



		//close system plug usb

		sleep(1);
	}
	if( clock_gettime( CLOCK_REALTIME, &stop) == -1 ) {
				perror( "clock gettime" );
	}

	accum = ( stop.tv_sec - start.tv_sec ) + ( stop.tv_nsec - start.tv_nsec )/1000000000;
	char debug[255]={0};
	sprintf(debug,"echo waitingtime=%lf >> /data/logtool_dump.txt",accum);
	system(debug);
	sprintf(debug,"echo path=%s >> /data/logtool_dump.txt",path);
	system(debug);
}

 void onHandelLogPath(bool waitMircoSDBootMount,char * storePath,char * dirPath,char * fileNamePrevix )
{
	char szDate[BUF_TIME_SIZE]={0};
	char szPath[BUF_SIZE]={0};
	char szOldPath[BUF_SIZE]={0};
	if(waitMircoSDBootMount==true){
		//move dir to microSD
		//search date --1 /data/Asuslog/2014_0220_141312/Main_01_2014_0220_141313.log --2 /data/12121_02121_1212/

		char * pFilename=strrchr(storePath,(int)'/');//EX: /data/Asuslog/kernel/12121_02121_1212/kernel.log -> /kernel.log
		strncpy(szPath, storePath, pFilename-storePath);//EX:   /data/Asuslog/kernel/12121_02121_1212
		char * pDate=strrchr(szPath,(int)'/')+1;//EX: 12121_02121_1212
		sprintf(szDate,"%s",pDate);
		strncpy(szPath,storePath,pFilename-storePath+1);//EX: /data/Asuslog/kernel/12121_02121_1212/

		if(strcmp(FILE_NAME_TCPDUMP,fileNamePrevix)==0 || strcmp(FILE_NAME_BPLOG,fileNamePrevix)==0){//oldPath /data/Asuslog/kernel/
			sprintf(szPath,"%s%s/",dirPath,szDate);//EX: /Removable/MicroSD/Asuslog/kernel/12121_02121_1212/    //new version /data/Asuslog/2014_0220_141312
		}else{
			sprintf(szPath,"%s",dirPath);
		}

		strcat(szPath,(pFilename+1));// /Removable/MicroSD/Asuslog/kernel/12121_02121_1212/bootstrap_kernel.log
		strcat(szPath,"bootstrap_");
		strcpy(szOldPath,storePath);

		execCmd("chmod 777"	,	szOldPath);
	}else{
		getDate(szDate,BUF_TIME_SIZE);
	}


	 if(strcmp(FILE_NAME_TCPDUMP,fileNamePrevix)==0){
		 createLogData(storePath,dirPath,szDate,fileNamePrevix);
	 }else{
		 onHandleLogPathbyId(dirPath,fileNamePrevix,storePath);

	 }


	if(waitMircoSDBootMount==true){
		//data/Asuslog/kernel/12121_02121_1212/kernel.log ->
		// /Removable/MicroSD/Asuslog/kernel/12121_02121_1212/bootstrap_kernel.log
		//SLOGI("copy wait szOldPath=%s, szNewPath=%s ",szOldPath ,szPath);
		char szCmd[BUF_SIZE]={0};
		sprintf(szCmd, "cat %s > %s",szOldPath, szPath);
		system(szCmd);
		char * pFilename=strstr(szOldPath,szDate);
		memset(szPath,0,BUF_SIZE);
		strncpy(szPath, szOldPath, strlen(szOldPath)-strlen(pFilename));
		execCmd("rm -rf"	,	szPath);// rm /data/Asuslog/Modem
	}
}

bool isMtpUpdate(char * path)
{
	return false;//service update
	if(strncmp(path,DIR_DATA,strlen(DIR_DATA)-1)==0)
	{
		return true;
	}else{
		return false;
	}
}

void broadcastMountPath(char* filepath){
	char mountpath[PATH_SIZE]={0};
	sprintf(mountpath," file://%s",filepath);
	execCmd("am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d",	mountpath);
}

void broadcastMountDir(char* dirpath){
	char mountpath[PATH_SIZE]={0};
	sprintf(mountpath," file://%s",dirpath);
	execCmd("am broadcast -a android.intent.action.MEDIA_MOUNTED -d",	mountpath);
}

int closelogfd(int fd,char * dirPath,bool updateMtp)
{
	if(fd!=0){
		close(fd);
		execCmd("chmod -R 777"	,	dirPath);
		if(updateMtp){
			broadcastMountDir(dirPath);
		}
	}
	return 0;
}

int openfile (const char *pathname)
{
    return open(pathname, O_WRONLY | O_APPEND | O_CREAT, S_IRUSR | S_IWUSR);
}

long long getFileSize(int fd){
	struct stat statbuf;
	fstat(fd, &statbuf);
	return statbuf.st_size;
}

bool isFileExist(char * path)
{
	FILE* fp = fopen(path, "r");
	if (fp) {
	    // file exists
	    fclose(fp);
	    return true;
	}
	return false;
}

int createDir( char* dirpath,bool bchangePermission,bool updateMtp)
{
	 DIR* dir = opendir(dirpath);
	 int result=0;
	 if(dir){
	 		/* Directory exists. */
	 		 closedir(dir);
	 		 if(bchangePermission){
	 			execCmd("chmod -R 777"	,	dirpath);
	 		 }
	 		 if(updateMtp){
	 			broadcastMountDir(dirpath);
	 		 }
	 	}else{

	 		result=execCmd("mkdir -p"		,		dirpath);//65280
	 		SLOGI("mkdir path=%s,result=%d",dirpath,result);
	 		execCmd("chmod -R 777"	,	dirpath);
	 	}
	 return result;
}

void setBootMountEnable(bool enable){
	if(enable==true){
		property_set(KEY_BOOTMOUNT_STATE,"1");
	}else{
		property_set(KEY_BOOTMOUNT_STATE,"0");
	}
}

void getDumpDate(char * date)
{
	property_get(KEY_DATE_FOLDER,date,"");
	/*
	if(strlen(date)==0){
		getDate(date,strlen(date));
		property_set(KEY_DATE_FOLDER,date);
	}*/
}

int getBuildSDK(){
	char szSDK[PROPERTY_VALUE_MAX]={0};
	property_get("ro.build.version.sdk",szSDK,"19");
	int sdk=atoi(szSDK);
	return sdk;
}

int getMaxRotateNum(bool bplog){

	char szRotate[PROPERTY_VALUE_MAX]={0};
	char szDefault[PROPERTY_VALUE_MAX]={0};
	int rotate=50;
	if(bplog==true){
		property_get(MTS_ROTATE_NUM,szRotate,"3");
		rotate=atoi(szRotate);
		return rotate;
	}
	property_get(KEY_ROTATE_TOTAL,szRotate,"");
	if(strlen(szRotate)==0){
		if(isUserBuild()==true){
			rotate=20;
		}
		sprintf(szDefault,"%d",rotate);
		property_set(KEY_ROTATE_TOTAL,szDefault);
	}else{
		rotate=atoi(szRotate);
	}
	return rotate;
}

bool isUserBuild(){
	char szBuild[PROPERTY_VALUE_MAX]={0};
	property_get(KEY_IMAGE_TYPE,szBuild,"user");
	if(strcmp(szBuild,"user")==0){
		return true;
	}
	return false;
}

bool isBootMountEnable(){
	char szValue[PROPERTY_VALUE_MAX]={0};
	property_get(KEY_BOOTMOUNT_STATE,szValue,"");
	if(strcmp(szValue,ENABLE)==0){
		return true;
	}
	return false;
}

bool isSaveMicroSD(){
	char szValue[PROPERTY_VALUE_MAX]={0};
	property_get(KEY_SAVE_DIR,szValue,"");
	return isMicroStrorage(szValue);
}

int execCmd( char * prefix, char *posfix)
{
	char append[BUF_SIZE]={0};
	sprintf(append,"%s %s",prefix,posfix);
	SLOGI("====tcpdump is %s",append);
	return system(append);
}




void getDate(char* pszBuf,int len)
{// Get current date/time, format is YYYY-MM-DD.HH:mm:ss
	time_t	now = time(NULL);
	struct tm  tstruct= *localtime(&now);
	strftime(pszBuf, sizeof(pszBuf)*len, "%Y_%m%d_%H%M%S", &tstruct);

}


bool checkMemorySafe(char* path)
{

	struct statfs st;
	statfs(path, &st);
	int lowbound=((st.f_blocks*st.f_bsize/(1024*1024)/10/2));//total
	int value=((st.f_bavail*st.f_bsize/(1024*1024)));//MB
	if(value<=lowbound){
		return false;
	}
	return true;
}

bool isBuildNoApp(){
	char szValue[PROPERTY_VALUE_MAX]={0};
	property_get(KEY_BUILD_NOAPP,szValue,"");
	if(strlen(szValue)==0){
		return false;
	}
	if(strcmp(szValue,ENABLE)==0){
		return true;
	}
	return false;
}

void getLogRootPath(char* pszBuf)
{
	property_get(KEY_SAVE_DIR,pszBuf,"");
	if(strlen(pszBuf)==0 || strlen(pszBuf)==1)
	{
		if(isBuildNoApp()){
			property_set(KEY_SAVE_DIR,DIR_ROOT_NO_ZYGOTE);
			sprintf(pszBuf,"%s",DIR_ROOT_NO_ZYGOTE);
		}else{
			property_set(KEY_SAVE_DIR,DIR_ROOT);
			sprintf(pszBuf,"%s",DIR_ROOT);
		}

	}
}

void setLogRootPath(char * path)
{
	property_set(KEY_SAVE_DIR,path);
}

bool checkEnble(char*value){
	if(strcmp(value,ENABLE)==0){
		return true;
	}
	return false;
}


void createLogData(char * storePath,char * dirPath,char* date,char * filename)
{
	char szPath[BUF_SIZE]={0};
	sprintf(szPath,	"%s%s/"			,dirPath	,date);
	int result=execCmd("mkdir"	,szPath);
	lockUnMount(result,szPath);
	execCmd("chmod 777"	,szPath);
	sprintf(storePath,	"%s%s"		,szPath,filename);
}

void createDirWaitMount(char *path){

	int returnCode= createDir(path,true,false);
	SLOGI("createDir path=%s ,returnCode=%d",path,returnCode);
	lockUnMount(returnCode,path);
}

void lockUnMount(int returnCode,char * path){
	if(returnCode==ERROR_BOOTSTRAP_UNMOUNT){
		char debug[255]={0};
		SLOGE("wait_BOOTSTRAP_UNMOUNT_waitMount_path=%s",path);
		sprintf(debug,"echo wait_BOOTSTRAP_UNMOUNT_waitMount_path=%s >> /data/logtool_dump.txt",path);
		system(debug);
		waitMount(isSaveMicroSD(),path);
		SLOGE("leave_BOOTSTRAP_UNMOUNT_waitMount_path=%s",path);
		sprintf(debug,"echo waitMount_leave >> /data/logtool_dump.txt");
		system(debug);
	}
}
#ifdef __cplusplus
}
#endif
