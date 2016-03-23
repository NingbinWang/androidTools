#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>

#include <stdarg.h>
#include <unistd.h>
#include <fcntl.h>
#include <assert.h>
#include <ctype.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <arpa/inet.h>

#define LOG_TAG "logtool"

#include <cutils/properties.h>
#include <cutils/log.h>
#define KEY_LOG_CMD 		"persist.asuslog.logcmd"
int main(int argc, char **argv)
{
        
        int i;
	char szCmd[260]={0};
        char match = '>';
	char match1 = 'f';
	char match2 = ' ';
        char *ptr = NULL;
	char *ptr1 = NULL;
        char cmd1[] = "top -n 1 -t";
        char cmd2[] = "procrank";
	char cmd3[] = "ssr_setup modem";
	char cmd4[] = "ssr_setup";
	char cmd5[] = "am broadcast -a \"com.asus.QPST.fw\"";
	char cmd6[] = "echo --update_package=/cache/update.zip > /cache/recovery/command";
	char cmd7[] = "reboot recovery";
	char cmd8[] = "cp -r /asdf";
	char cmd9[] = "am broadcast -a \"com.asus.wl.ed\"";
	char cmd10[] = "chmod -R 777 /data/logcat_log/ | chmod -R 777 /data/gps/log | chmod -R 777 /data/ramdump/";
    	char cmd11[] = "/data/data/com.asus.fdclogtool/files/raw_sender normal";
    	char cmd12[] = "/data/data/com.asus.fdclogtool/files/raw_sender compact";
	char cmd13[] = "/data/debug/QMESA_64";
	char cmd14[] = "QMESA_64 kill";
	char cmd15[] = "\"/data/debug/raw_sender";
	char cmd16[] = "\"/system/bin/raw_sender";
	property_get(KEY_LOG_CMD,szCmd,"");
	if(0 == strcmp(szCmd, cmd3) || 0 == strcmp(szCmd, cmd4) || 0 == strcmp(szCmd, cmd10) 
	      || 0 == strcmp(szCmd, cmd6) || 0 == strcmp(szCmd, cmd7) || 0 == strcmp(szCmd, cmd5)
	      || 0 == strcmp(szCmd, cmd9) || 0 == strcmp(szCmd, cmd11) || 0 == strcmp(szCmd, cmd12)
	      || 0 == strcmp(szCmd, cmd13) || 0 == strcmp(szCmd, cmd14)){
	      if(0 == strcmp(szCmd, cmd11)){
		  system("/data/debug/raw_sender 0x4b 0x12 0x21 0x0d  0xc8 0x00 0x00 0x00  0x00 0x20 0x00 0x00");
	      }else if(0 == strcmp(szCmd, cmd12)){
		  system("/data/debug/raw_sender 0x4b 0x12 0x21 0x0d  0x20 0xbf 0x02 0x00  0x00 0x00 0x10 0x00");
	      }else if(0 == strcmp(szCmd, cmd13)){
		  char memtest[255]={0};
		  property_get("persist.asuslog.time.set",szCmd,"43200");
		  sprintf(memtest,"/data/debug/QMESA_64 -startSize 4MB -endSize 32MB -secs %s -numThreads 8 -randSeed 0XF83FA3CA > /sdcard/Asuslog/QMESA_64.log",szCmd);
		  SLOGI(memtest);
	          system(memtest);
		  system("/data/debug/busybox grep \"ERROR\" /sdcard/Asuslog/QMESA_64.log > /sdcard/Asuslog/temp.txt");
		  int fd = open("/sdcard/Asuslog/temp.txt", O_RDONLY);
		  static char  cmdline[100];
		  if (fd >= 0) {
                        int  n = read(fd, cmdline, sizeof(cmdline)-1);
                        if (n > 1)
			{
				system("am broadcast -a \"com.asus.QMESA.fail\"");
			}
			else{
				system("am broadcast -a \"com.asus.QMESA.pass\"");
			}
                        close(fd);
                  }
		system("rm -rf /sdcard/Asuslog/temp.txt");
	      }else{
		  system(szCmd);
	      }
	      return 0;
        }
        ptr = strchr(szCmd, match);
        if(ptr){
           int index = ptr - szCmd; 
           char temp[20] = {0};
           for(i = 0; i < index - 1; i++){
              temp[i] = szCmd[i];
           }
           if(0 == strcmp(temp, cmd1) || 0 == strcmp(temp, cmd2)){
              system(szCmd);
	      return 0;
           }
        }
       ptr1 = strchr(szCmd, match1);
       if(ptr1){
           int index = ptr1 - szCmd; 
           char temp[20] = {0};
           for(i = 0; i < index + 1; i++){
              temp[i] = szCmd[i];
           }
           if(0 == strcmp(temp, cmd8)){
              system(szCmd);
	      return 0;
           }
        }
	
	ptr1 = strchr(szCmd, match2);
	if(ptr1){
	   int index = ptr1 - szCmd; 
           char temp[30] = {0};
           for(i = 0; i < index; i++){
              temp[i] = szCmd[i];
           }
           if(0 == strcmp(temp, cmd15) || 0 == strcmp(temp, cmd16)){
	      int length = strlen(szCmd);
	      char szCmd_temp[260] = {0};
	      for(i = 1; i < length - 1; i++){
              	szCmd_temp[i-1] = szCmd[i];
              }
              system(szCmd_temp);
           }
	}
	return 0;
}



