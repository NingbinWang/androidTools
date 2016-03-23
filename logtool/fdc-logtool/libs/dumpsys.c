#include <jni.h>

#define LOG_TAG "logtool"

#include <cutils/properties.h>
#include <cutils/log.h>
#include <stdlib.h>

int  Java_com_asus_tool_DumpSyslog_dumpsys(JNIEnv * env, jobject jobj, jstring cmd)
{
	char * str=(char*)(*env)->GetStringUTFChars(env,cmd, 0);

	int result=system(str);
	SLOGI("content =%s,result=%d",str,result);
	(*env)->ReleaseStringUTFChars(env, cmd , str);
	return result;
}
