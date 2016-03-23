#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <stdbool.h>

#define KEY_SAVE_DIR 			"persist.asuslog.savedir"
#define DUMP_ENABLE 			"persist.asuslog.dump.enable"
#define KEY_ENABLE_TCPDUMP 		"persist.asuslog.tcpdump.enable"
#define KEY_ENABLE_MAIN 		"persist.asuslog.main.enable"
#define KEY_ENABLE_KERNEL 		"persist.asuslog.kernel.enable"
#define KEY_ENABLE_RADIO 		"persist.asuslog.radio.enable"
#define KEY_ENABLE_EVENT 		"persist.asuslog.events.enable"
#define KEY_ENABLE_COREDUMP 	"persist.asuslog.modem.enable"
#define KEY_COMBINE_CONFIG 		"persist.asuslog.combine.config"

#define KEY_BOOTMOUNT_STATE		"persist.asuslog.mount.state"
#define DUMP_ENABLE 			"persist.asuslog.dump.enable"
#define MTS_ROTATE_NUM			"persist.service.mts.rotate_num"

#define KEY_DATE_FOLDER	 		"persist.asuslog.dump.date"
#define KEY_ROTATE_TOTAL 		"persist.asuslog.rotate.num"
#define KEY_BUILD_NOAPP 		"persist.asuslog.no_app"
#define KEY_IMAGE_TYPE			 "ro.build.type"
#define LOG_PATH_CHANGE 		"LOG_PATH_CHANGE"
#define FILE_NAME_TCPDUMP 		"tcpdump"
#define FILE_NAME_BPLOG 		"bplog"

#define DIR_NAME_TCPDUMP			"TcpDump"
#define DIR_ROOT 					"/sdcard/Asuslog/"
#define DIR_ROOT_NO_ZYGOTE 			"/data/Asuslog/"
#define DIR_DATA    				"/data"
#define DIR_SDCARD					"/sdcard"
#define DIR_MICRO_SDCRAR	    	"/Removable"
#define MAX_LOG_LEN		(5*1024+1)
#define DIR_MICRO_WAIT_MOUNT_DIR 	"/data/Asuslog/"
#define MAX_LOGFILE_KBSIZE	8000
#define BUF_SIZE 256
#define BUF_TIME_SIZE PROPERTY_VALUE_MAX
#define PATH_SIZE 256
#define DISABLE 		"0"
#define NO_EXIST 		"-2"
#define ENABLE 			"1"
#define ERROR_BOOTSTRAP_UNMOUNT 65280
#define LOG_DEBUG true

#ifdef __cplusplus
extern "C" {
#endif
	void getDate(char* pszBuf,int len);
	//char* appendCmd(char * prefix,char *posfix);
	int execCmd( char * prefix, char *posfix);
	void getLogRootPath(char* pszBuf);
	void setLogRootPath(char * path);
	bool checkEnble(char*value);
	void createLogData(char * storePath,char * dirPath,char* date,char * filename);
	int openfile (const char *pathname);
	long long getFileSize(int fd);
	int rotateLogs(char *filePath,char * fileNamePrevix,int fd,bool bUpdateMtp);
	int closelogfd(int fd,char * dirPath,bool updateMtp);
	int createDir( char* dirpath,bool bchangePermission,bool updateMtp);
	void broadcastMountDir(char* dirpath);
	void broadcastMountPath(char* filepath);
	bool isMtpUpdate(char * path);
	bool isExternelStrorage(char * path);
	bool isMicroStrorage(char * path);
	bool isBootMountEnable();
	void setBootMountEnable(bool enable);
	bool isSaveMicroSD();
	bool isDataStrorage(char * path);
	void onHandelLogPath(bool waitMircoSDBootMount,char * storePath,char * dirPath,char * fileNamePrevix );
	void waitMount(bool saveMicroSD,char* path);
	bool checkMemorySafe(char* path);
	void lockUnMount(int returnCode,char * path);
	void createDirWaitMount(char *path);
	void getDumpDate(char * date);
	long getuptime();
	int getBuildSDK();
	bool isFileExist(char * path);
#ifdef __cplusplus
}
#endif
