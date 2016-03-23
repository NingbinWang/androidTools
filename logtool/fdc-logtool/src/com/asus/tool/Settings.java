package com.asus.tool;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Settings {
	
	private static final boolean DEBUG = false;
	private static final String TAG = "Setting";
	private static SharedPreferences mSharedPref = null;
	private static final String KEY_AUDIO_LOG	="KEY_AUDIO_LOG";
	private static final String KEY_POWER_LOG	="KEY_POWER_LOG";
	private static final String KEY_WIFI_LOG	="KEY_WIFI_LOG";
	private static final String KEY_BT_LOG	="KEY_BT_LOG";
	private static final String KEY_BATTERY_LOG	="KEY_BATTERY_LOG";
	private static final String KEY_MEMEINFO_LOG	="KEY_MEMEINFO_LOG";
	private static final String KEY_DISK_LOG	="KEY_DISK_LOG";
	private static final String KEY_PROCANE_LOG	="KEY_PROCANE_LOG";
	private static final String KEY_CPU_LOG	="KEY_CPU_LOG";
	private static final String KEY_CPULOADING_LOG	="KEY_CPULOADING_LOG";
	private static final String KEY_CPULOADING_TIME	="KEY_CPULOADING_TIME";
	private static final String KEY_TOP	="KEY_TOP";
	
	private static final String KEY_ACTIVITY_LOG	="KEY_ACTIVITY_LOG";
	private static final String KEY_WINDOW_LOG	="KEY_WINDOW__LOG";
	private static final String KEY_UPATE_MTP	="KEY_UPATE_MTP";
	private static final String KEY_AUTO_UPLOAD_FIRST	="KEY_AUTO_UPLOAD_FIRST";
	private static final String KEY_FIRST_START_SYSTEM	="KEY_FIRST_START_SYSTEM";
	private static final String KEY_CRASH_REMINDER_LOG	="KEY_CRASH_REMINDER_LOG";
	private static final String KEY_MODEM_MICRO_SDCARD_SIZE	="KEY_MODEM_MICRO_SDCARD_SIZE";
	private static final String KEY_MODEM_EXTERNEL_SDCARD_SIZE	="KEY_MODEM_EXTERNEL_SDCARD_SIZE";
	private static final String KEY_MODEM_MICRO_SDCARD_COUNT	="KEY_MODEM_MICRO_SDCARD_COUNT";
	private static final String KEY_MODEM_EXTERNEL_SDCARD_COUNT	="KEY_MODEM_EXTERNEL_SDCARD_COUNT";
	public static final int DEFAULT_EXTERNAL_SIZE=200; 
	public static final int DEFAULT_EXTERNAL_COUNT=3; 
	public static final int DEFAULT_MICRO_SIZE=200; 
	public static final int DEFAULT_MICRO_COUNT=6;
	public static final int DEFAULT_CPULOAD_INTERVEL_SECOND=60*60;
	
	private static final String KEY_ALLOW_APP_OPEN = "KEY_ALLOW_APP_OPEN"; 
	private static final String KEY_DISK_LOW_REMINDER = "KEY_DISK_LOW_REMINDER"; 
	private static final String KEY_DISK_REMINDER_ALREADY="KEY_DISK_REMINDER_ALREADY";
	private static final String KEY_DISK_REMINDER_ALREADY_CLOSE="KEY_DISK_REMINDER_ALREADY_CLOSE";
	private static final String KEY_DISK_REMINDER_ALREADY_CLEAR="KEY_DISK_REMINDER_ALREADY_CLEAR";
	private static final String KEY_OUTOUT_CLEAR="KEY_OUTOUT_CLEAR";
	private static final String KEY_AUTO_PCM="KEY_AUTO_PCM";
	private static final String KEY_AUTO_I2S="KEY_AUTO_I2S";
	private static final String KEY_PHONE_PCM="KEY_PHONE_PCM";
	private static final String KEY_MODEM_LOG_LEVEL="KEY_MODEM_LOG_LEVEL";
	private static final String KEY_RIL_FIRST_LOG_SET="KEY_RIL_FIRST_LOG_SET";
	public static final int MODEM_BB=0;
	public static final int MODEM_3G=1;
	public static final int MODEM_DIGRF=2;
	public static void init( SharedPreferences pref)
	{
        if(DEBUG)
        {
        	Log.d(TAG, "Settings");
        }
        mSharedPref = pref;
	}
	
	
	public static void setAudioEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_AUDIO_LOG , enable);
		editor.apply();
	}
	
	
	public static void setPowerEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_POWER_LOG , enable);
		editor.apply();
	}
	
	
	
	public static void setBatteryEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_BATTERY_LOG , enable);
		editor.apply();
	}
	
	public static void setAllowAppOpen(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_ALLOW_APP_OPEN , enable);
		editor.apply();
	}
	
	public static void setWifiEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_WIFI_LOG , enable);
		editor.apply();
	}
	
	
	public static void setBTEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_BT_LOG , enable);
		editor.apply();
	}
	
	public static void setProcaneEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_PROCANE_LOG , enable);
		editor.apply();
	}
	
	public static void setCpuEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_CPU_LOG , enable);
		editor.apply();
	}
	
	public static void setCpuLoadingEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_CPULOADING_LOG , enable);
		editor.apply();
	}
	
	public static void setMeminfoEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_MEMEINFO_LOG , enable);
		editor.apply();
	}
	
	public static void setDiskEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_DISK_LOG , enable);
		editor.apply();
	}
	
	public static void setActivityEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_ACTIVITY_LOG , enable);
		editor.apply();
	}
	
	public static void setWindowEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_WINDOW_LOG , enable);
		editor.apply();
	}
	
	
	
	public static void setCrashReminderEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_CRASH_REMINDER_LOG , enable);
		editor.apply();
	}

	public static void setAutoPcmEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_AUTO_PCM , enable);
		editor.apply();
	}
	
	public static void setAutoI2SEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_AUTO_I2S , enable);
		editor.apply();
	}
	
	public static void setPhoneAppendPcmEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_PHONE_PCM , enable);
		editor.apply();
	}
	
	public static void setModemMicroSdcardSize(int size){
		Editor editor=mSharedPref.edit();
		editor.putInt(KEY_MODEM_MICRO_SDCARD_SIZE, size);
		editor.apply();
	}
	
	public static int getModemMicroSdcardSize(){
		return mSharedPref.getInt(KEY_MODEM_MICRO_SDCARD_SIZE, DEFAULT_MICRO_SIZE);
	}
	
	public static void setModemLevelValue(int value){
		Editor editor=mSharedPref.edit();
		editor.putInt(KEY_MODEM_LOG_LEVEL , value);
		editor.apply();
	}
	
	public static void setModemExternelSdcardSize(int size){
		Editor editor=mSharedPref.edit();
		editor.putInt(KEY_MODEM_EXTERNEL_SDCARD_SIZE, size);
		editor.apply();
	}
	
	public static void setCpuLoadIntervelTime(int second){//by second
		Editor editor=mSharedPref.edit();
		editor.putInt(KEY_CPULOADING_TIME, second);
		editor.apply();
	}
	
	public static void setTop(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_TOP, enable);
		editor.apply();
	}
	
	public static int getCpuLoadIntervelTime(){
		return mSharedPref.getInt(KEY_CPULOADING_TIME,DEFAULT_CPULOAD_INTERVEL_SECOND);
	}
	
	public static int getModemDefaultSize(){
		return DEFAULT_EXTERNAL_SIZE;
	}
	
	public static int getModemLogLevel(){
		return mSharedPref.getInt(KEY_MODEM_LOG_LEVEL,MODEM_3G);
	}
	
	public static int getModemDefaultCount(){
		return DEFAULT_EXTERNAL_COUNT;
	}
	
	
	public static int getModemExternelSdcardSize(){
		return mSharedPref.getInt(KEY_MODEM_EXTERNEL_SDCARD_SIZE,DEFAULT_EXTERNAL_SIZE);
	}
	
	
	
	public static void setModemMicroSdcardCount(int count){
		Editor editor=mSharedPref.edit();
		editor.putInt(KEY_MODEM_MICRO_SDCARD_COUNT, count);
		editor.apply();
	}
	
	public static int getModemMicroSdcardCount(){
		return mSharedPref.getInt(KEY_MODEM_MICRO_SDCARD_COUNT, DEFAULT_MICRO_COUNT);
	}
	
	public static void setModemExternelSdcardCount(int count){
		Editor editor=mSharedPref.edit();
		editor.putInt(KEY_MODEM_EXTERNEL_SDCARD_COUNT, count);
		editor.apply();
	}
	
	public static void setAutoUpdateMtpEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_UPATE_MTP, enable);
		editor.apply();
	}
	
	public static void setDiskLowReminderEnable(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_DISK_LOW_REMINDER, enable);
		editor.apply();
	}
	
	public static void setAlreadyReminderDiskLow(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_DISK_REMINDER_ALREADY, enable);
		editor.apply();
	}
	
	public static void setFirstRilLog(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_RIL_FIRST_LOG_SET, enable);
		editor.apply();
	}
	
	
	public static void setFirstSystemOpen(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_FIRST_START_SYSTEM, enable);
		editor.apply();
	}
	
	public static void setOutputAndClear(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_OUTOUT_CLEAR, enable);
		editor.apply();
	}
	
	public static void setAlreadyReminderDiskLowClose(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_DISK_REMINDER_ALREADY_CLOSE, enable);
		editor.apply();
	}
	
	public static void setAlreadyReminderDiskLowClear(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_DISK_REMINDER_ALREADY_CLEAR, enable);
		editor.apply();
	}
	
	public static void setAutoUploadFirst(boolean enable){
		Editor editor=mSharedPref.edit();
		editor.putBoolean(KEY_AUTO_UPLOAD_FIRST, enable);
		editor.apply();
	}
	
	
	public static int getModemExternelSdcardCount(){
		return mSharedPref.getInt(KEY_MODEM_EXTERNEL_SDCARD_COUNT,DEFAULT_EXTERNAL_COUNT);
	}
	
	public static boolean isCrashReminderEnable(){
		return mSharedPref.getBoolean(KEY_CRASH_REMINDER_LOG,false);
	}
	
	public static boolean isFirstSystemEnable(){
		return mSharedPref.getBoolean(KEY_FIRST_START_SYSTEM,true);
	}
	
	public static boolean isFirstLogLevelSet(){
		return mSharedPref.getBoolean(KEY_RIL_FIRST_LOG_SET,true);
	}
	
	public static boolean isAudioEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_AUDIO_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_AUDIO_LOG,true);
	}
	
	public static boolean isPowerEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_POWER_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_POWER_LOG,true);
	}
	
	public static boolean isWifiEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_WIFI_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_WIFI_LOG,true);
	}
	
	public static boolean isBTEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_BT_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_BT_LOG,true);
	}
	
	public static boolean isMemoinfoEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_MEMEINFO_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_MEMEINFO_LOG,true);
	}
	
	public static boolean isTopEnable(){
		return mSharedPref.getBoolean(KEY_TOP,false);
	}
	
	public static boolean isDiskLogEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_DISK_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_DISK_LOG,true);
	}
	
	public static boolean isProcaneLogEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_PROCANE_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_PROCANE_LOG,true);
	}
	
	public static boolean isCpuLogEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_CPU_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_CPU_LOG,true);
	}
	
	public static boolean isCpuLoadingEnable(){
		return mSharedPref.getBoolean(KEY_CPULOADING_LOG,false);
	}
	
	public static boolean isActivityEnable(){
		return mSharedPref.getBoolean(KEY_ACTIVITY_LOG,false);// dump is too large ,system lock
	}
	
	public static boolean isWindowEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_WINDOW_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_WINDOW_LOG,true);
	}
	
	public static boolean isBatteryEnable(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_BATTERY_LOG,false);
		}
		return mSharedPref.getBoolean(KEY_BATTERY_LOG,true);
	}
	
	public static boolean isAutoUpdateMtp(){
		return mSharedPref.getBoolean(KEY_UPATE_MTP,false);
	}
	
	public static boolean isAutoUploadFirst(){
		if(Util.isUserBuild()){
			return mSharedPref.getBoolean(KEY_AUTO_UPLOAD_FIRST,false);
		}else{
			return mSharedPref.getBoolean(KEY_AUTO_UPLOAD_FIRST,false);
		}
		
	}
	
	public static boolean isAllowAppOpen(){// monkey test
		return mSharedPref.getBoolean(KEY_ALLOW_APP_OPEN,true);
	}
	
	public static boolean isReminderDiskLow(){
		return mSharedPref.getBoolean(KEY_DISK_LOW_REMINDER,true);
	}
	
	public static boolean isAlreadyReminderDiskLow(){
		return mSharedPref.getBoolean(KEY_DISK_REMINDER_ALREADY,true);
	}
	
	public static boolean isAlreadyReminderDiskLowClose(){
		return mSharedPref.getBoolean(KEY_DISK_REMINDER_ALREADY_CLOSE,true);
	}
	
	public static boolean isAlreadyReminderDiskLowClear(){
		return mSharedPref.getBoolean(KEY_DISK_REMINDER_ALREADY_CLEAR,true);
	}
	
	public static boolean isAutoPcm()
	{
		return mSharedPref.getBoolean(KEY_AUTO_PCM,true);
	}
	
	public static boolean isAutoI2S()
	{
		return mSharedPref.getBoolean(KEY_AUTO_I2S,true);
	}
	
	public static boolean isPhonePcm()
	{
		return mSharedPref.getBoolean(KEY_PHONE_PCM,false);
	}
	
	public static boolean isOutputAndClear()
	{
		return mSharedPref.getBoolean(KEY_OUTOUT_CLEAR,false);
	}
}
