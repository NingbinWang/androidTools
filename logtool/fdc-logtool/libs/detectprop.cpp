#include "detectprop.h"
#include <cutils/properties.h>
#include <cutils/log.h>
#include <sys/atomics.h>
#include "logdump.h"
#include "logtool.h"
#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>
#include <pthread.h>
#include  <unistd.h>
#pragma GCC diagnostic ignored "-Wwrite-strings"
#define LOG_TAG "LogTool"
//extern prop_area *__system_property_area__;

typedef struct pwatch pwatch;

struct pwatch
{
    const prop_info *pi;
    unsigned serial;
};
#define KEY_ENABLE_MAIN 	"persist.asuslog.main.enable"
#define KEY_ENABLE_KERNEL 	"persist.asuslog.kernel.enable"
#define KEY_ENABLE_RADIO 	"persist.asuslog.radio.enable"
#define KEY_ENABLE_EVENT 	"persist.asuslog.events.enable"

static pwatch watchlist[1024];
int pResult[4]={-1};
pthread_mutex_t mutex;


bool isKernelSupport(){
	char szValue[256]={0};
	property_get(KEY_ENABLE_KERNEL,szValue,"");
	if(strcmp(szValue,ENABLE)==0){
		return true;
	}
	return false;
}



bool isMainSupport(){
	char szValue[256]={0};
	property_get(KEY_ENABLE_MAIN,szValue,"");
	if(strcmp(szValue,ENABLE)==0){
		return true;
	}
	return false;
}

bool isRadioSupport(){
	char szValue[256]={0};
	property_get(KEY_ENABLE_RADIO,szValue,"");
	if(strcmp(szValue,ENABLE)==0){
		return true;
	}
	return false;
}

bool isEventSupport(){
	char szValue[256]={0};
	property_get(KEY_ENABLE_EVENT,szValue,"");
	if(strcmp(szValue,ENABLE)==0){
		return true;
	}
	return false;
}




int detectProp()
 {
	bool bKernel=::isKernelSupport();
	bool bEvent=::isEventSupport();
	bool bMain=::isMainSupport();
	bool bRadio=::isRadioSupport();
	char szlogRoot[PATH_SIZE]={0};
	getLogRootPath(szlogRoot);
	bool bDynamicKernel;
	bool bDynamicEvent;
	bool bDynamicMain;
	bool bDynamicRadio;
	char szDynamiclogRoot[PATH_SIZE]={0};
	getLogRootPath(szDynamiclogRoot);
	while(true){

		if(( bDynamicKernel=isKernelSupport())!=bKernel){
			if(bDynamicKernel==true){
				callback(KEY_ENABLE_KERNEL,"1");
			}else{
				callback(KEY_ENABLE_KERNEL,"0");
			}

			bKernel=bDynamicKernel;
		}
		if(( bDynamicMain=isMainSupport())!=bMain){
					if(bDynamicMain==true){
						callback(KEY_ENABLE_MAIN,"1");
					}else{
						callback(KEY_ENABLE_MAIN,"0");
					}

					bMain=bDynamicMain;
				}
		if(( bDynamicEvent=isEventSupport())!=bEvent){
					if(bDynamicEvent==true){
						callback(KEY_ENABLE_EVENT,"1");
					}else{
						callback(KEY_ENABLE_EVENT,"0");
					}

					bEvent=bDynamicEvent;
				}
		if(( bDynamicRadio=isRadioSupport())!=bRadio){
					if(bDynamicRadio==true){
						callback(KEY_ENABLE_RADIO,"1");
					}else{
						callback(KEY_ENABLE_RADIO,"0");
					}

					bRadio=bDynamicRadio;
		}
		//listen log path change
		getLogRootPath(szDynamiclogRoot);
		if(strcmp(szDynamiclogRoot,szlogRoot)!=0){
			callback(LOG_PATH_CHANGE,szDynamiclogRoot);
			sprintf(szlogRoot,"%s",szDynamiclogRoot);
		}

		sleep(1);//vold
	}
    /* watchprop
	prop_area *pa = __system_property_area__;
     unsigned serial = pa->serial;
     unsigned count = pa->count;
     unsigned n;

     if(count >= 1024) exit(1);

     for(n = 0; n < count; n++) {
         watchlist[n].pi = __system_property_find_nth(n);
         watchlist[n].serial = watchlist[n].pi->serial;
     }

     for(;;) {

         do {
             __futex_wait(&pa->serial, serial, 0);
         } while(pa->serial == serial);

         while(count < pa->count){
             watchlist[count].pi = __system_property_find_nth(count);
             watchlist[count].serial = watchlist[n].pi->serial;
             SLOGE("announce 1 ");
             announce(watchlist[count].pi);
             count++;
             if(count == 1024) exit(1);
         }
         pResult[0]=-1;
         pResult[1]=-1;
         pResult[2]=-1;
         pResult[3]=-1;
         int i=0;
         for(n = 0; n < count; n++){
             unsigned tmp = watchlist[n].pi->serial;
             if(watchlist[n].serial != tmp) {


            	 char name[PROP_NAME_MAX];
            	 char value[PROP_VALUE_MAX];

            	 __system_property_read(watchlist[n].pi, name, value);
            	 if(strcmp(name,KEY_ENABLE_MAIN)==0){
            		 pResult[0]=n;

            	 }else if(strcmp(name,KEY_ENABLE_KERNEL)==0){
            		 pResult[1]=n;
            	 }else if(strcmp(name,KEY_ENABLE_RADIO)==0){
            		 pResult[2]=n;
            	 }else if(strcmp(name,KEY_ENABLE_EVENT)==0){
            		 pResult[3]=n;
            	 }

            	// announce(watchlist[n].pi);
                 watchlist[n].serial = tmp;

             }
             if(n==count-1)
             {
            	 for(int i=0;i<4;i++){
            		 if(pResult[i]!=-1){
            			 SLOGE("announce i %d",i);
            			 announce(watchlist[pResult[i]].pi);
            		 }
            	 }
             }
         }
     }*/
     return 0;
 }
