package com.android.detectappstate;

import java.io.File;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.os.Build;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

public class LogAppStateReceiver extends BroadcastReceiver {
	private static String TAG = "LogAppStateReceiver";
	private static final String LOG_PACKAGEMANE = "com.asus.qcomlogtool";
	private static final String LOG_COMMAND = "fdclogcommand";
	
	// modem log
	public static final String MTS_INPUT_PROP = "persist.service.mts.input";
	public static final String MTS_OUTPUT_PROP = "persist.service.mts.output";
	public static final String MTS_OUTPUT_TYPE_PROP = "persist.service.mts.output_type";
	public static final String MTS_ROTATE_NUM_PROP = "persist.service.mts.rotate_num";
	public static final String MTS_ROTATE_SIZE_PROP = "persist.service.mts.rotate_size";
	public static final String MTS_SERVICE = "persist.asuslog.fdcmts.enable";
	//private static final String ASUS_LOGTOOL_SET_RADIO_LEVEL = "asus.intent.action.set.radio.level";
	private static final String ASUS_LOGTOOL_LOGTOOL_REMOVE = "asus.intent.action.logtool.remove";
	private Handler mHandler = new Handler();
	public static final String KEY_APP_REPLACE="persist.asuslog.replace";
	

	public boolean isPackageExisted(Context context, String targetPackage) {
		List<ApplicationInfo> packages;
		PackageManager pm;
		pm = context.getPackageManager();
		packages = pm.getInstalledApplications(0);
		for (ApplicationInfo packageInfo : packages) {
			if (packageInfo.packageName.equals(targetPackage))
				return true;
		}
		return false;
	}

	public void closeTool(Context context) {
		if (context == null) {
			return;
		}
		if (isPackageExisted(context, LOG_PACKAGEMANE) == false) {
			
			String result = SystemProperties.get("persist.asuslog.dump.enable","0");
			if (result.equals("1") == true) {
				log("stop log");
				File file=new File("/data/data/"+LOG_PACKAGEMANE);
				if(file.exists()){
					delDir(file);
				}
				stopLog(context);
			}
		}
	}

	private void delDir(File file)
	{
		if(file.isDirectory())
		{
			File[] filesFile=file.listFiles();
			for(File child:filesFile){
				delDir(child);
			}
		}
		file.delete();
	}
	
	public static boolean setCmd(String cmd){
		File file=new File("/system/bin/"+LOG_COMMAND);
		if(file.exists()==false){
			return false;
		}
		
		SystemProperties.set("persist.asuslog.logcmd", cmd);
		SystemProperties.set("ctl.start",LOG_COMMAND);
		return true;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {


		
		String packageName = intent.getData().getSchemeSpecificPart();
		Log.v(TAG, "action=" + intent.getAction() + ",packageName="
				+ packageName);

		if (packageName == null || packageName.equals(LOG_PACKAGEMANE) == false) {
			return;
		}
		
		
		if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
			boolean dataRemove=intent.getExtras().getBoolean(Intent.EXTRA_DATA_REMOVED, true);
			boolean dateReplace=intent.getExtras().getBoolean(Intent.EXTRA_REPLACING);
			log("dateRemove="+dataRemove+",dateReplace="+dateReplace);
			if(dataRemove==false && dateReplace==false){//adb push /system/app or adb rm /system/app
				
				closeTool(context);
			}
			if(dataRemove==false && dateReplace==true){ //adb install
				String testreplace=SystemProperties.get("persist.asuslog.test.replace", "0");
				if(testreplace!=null && testreplace.equals("0")){
					Toast.makeText(context, "logtool replace old,please wait...", Toast.LENGTH_SHORT).show();
					SystemProperties.set(KEY_APP_REPLACE, "start");
					appReplaceClose(context);
				}
			}
		} else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {

			String result = SystemProperties.get("persist.asuslog.dump.enable","0");
			boolean replace=intent.getBooleanExtra(intent.EXTRA_REPLACING, false);
			String testreplace=SystemProperties.get("persist.asuslog.test.replace", "0");
			if(testreplace!=null && testreplace.equals("1")){
				replace=false;
			}
			log("replace="+replace);
			if (result.equals("1") == false||replace==true) {
				
				Intent intenInit = new Intent();
				log("start service");
				if(replace==false){
					intenInit.putExtra("state", "firstInstall");
				}else{
					intenInit.putExtra("state", "replace");
				}
				
				intenInit.setComponent(new ComponentName(LOG_PACKAGEMANE,"com.asus.tool.LogInitService"));
				context.startService(intenInit);
			}
			
		} else if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent
				.getAction())) {
			stopLog(context);
			
			
		} else if (Intent.ACTION_PACKAGE_DATA_CLEARED
				.equals(intent.getAction())) {
			stopLog(context);
		}

	}
	
	public static boolean isUserBuild(){
		
		String sku=SystemProperties.get("ro.build.type","user");
		
		if(sku.equals("user")){
			return true;
		}
		return false;
	}
	
	public void appReplaceClose(Context context){
		
		SystemProperties.set("ctl.stop", "logMain");
		SystemProperties.set("ctl.stop", "logKernel");
		SystemProperties.set("ctl.stop", "logRadio");
		SystemProperties.set("ctl.stop", "logEvent");
		SystemProperties.set("ctl.stop", "logTcpdump");
		SystemProperties.set("ctl.stop", "logCombine");
		SystemProperties.set("ctl.stop", "fdcmts");
		SystemProperties.set("ctl.stop", "fdcmts2");
		SystemProperties.set("ctl.stop", "fdcmdm1");
		SystemProperties.set("ctl.stop", "fdcmdm2");
		SystemProperties.set("ctl.stop", "toolconnect");
		
		log("appReplaceClose finish");
	}
	
	public void shareClose(){
		SystemProperties.set("ctl.stop", "logMain");
		SystemProperties.set("ctl.stop", "logKernel");
		SystemProperties.set("ctl.stop", "logRadio");
		SystemProperties.set("ctl.stop", "logEvent");
		SystemProperties.set("ctl.stop", "logTcpdump");
		SystemProperties.set("ctl.stop", "fdcmts");
		SystemProperties.set("ctl.stop", "fdcmts2");
		SystemProperties.set("ctl.stop", "fdcmdm1");
		SystemProperties.set("ctl.stop", "fdcmdm2");
		SystemProperties.set("ctl.stop", "toolconnect");
		if(isUserBuild()){
			SystemProperties.set("ctl.stop", "crashlogd");
		}
		
		SystemProperties.set("persist.asuslog.main.enable", "");
		SystemProperties.set("persist.asuslog.kernel.enable", "");
		SystemProperties.set("persist.asuslog.events.enable", "");
		SystemProperties.set("persist.asuslog.radio.enable", "");
		SystemProperties.set("persist.asuslog.tcpdump.enable", "");
		SystemProperties.set("persist.asuslog.fdcmts.enable", "");
		SystemProperties.set("persist.asuslog.combine.enable", "");
		SystemProperties.set("persist.asuslog.combine.config", "");
		SystemProperties.set("persist.asuslog.ril.level", "");
		SystemProperties.set("persist.asuslog.set.date", "");
		SystemProperties.set("persist.asuslog.modem1.enable", "");
		SystemProperties.set("persist.asuslog.modem2.enable", "");
		SystemProperties.set("sys.asuslog.modem1.enable", "");
		SystemProperties.set("sys.asuslog.modem2.enable", "");
		SystemProperties.set("sys.asuslog.date.temp", "");
		SystemProperties.set("persist.asuslog.modem1.pcmi2s", "");
		SystemProperties.set("persist.asuslog.modem2.pcmi2s", "");
		
		
		SystemProperties.set("persist.asuslog.savedir", "");
		SystemProperties.set("persist.asuslog.prevrootpath", "");
		SystemProperties.set("persist.asuslog.rotate.num", "");
		
		SystemProperties.set("persist.asuslog.fdcmdm1.enable", "");
		SystemProperties.set("persist.asuslog.fdcmdm2.enable", "");
	
		SystemProperties.set("persist.asuslog.modem.noreboot", "");
		SystemProperties.set("persist.asuslog.tool.enable", "");
		if(isUserBuild()==false){
			SystemProperties.set("persist.asuslog.fdc_crashlog", "1"); //crash log default enable
		}else{
			SystemProperties.set("persist.asuslog.fdc_crashlog", ""); 
		}
		
		
		SystemProperties.set(MTS_INPUT_PROP, "");
		SystemProperties.set(MTS_OUTPUT_PROP, "");
		SystemProperties.set(MTS_OUTPUT_TYPE_PROP, "");
		SystemProperties.set(MTS_ROTATE_NUM_PROP, "");
		SystemProperties.set(MTS_ROTATE_SIZE_PROP, "");
	}
	
	public void stopLog(Context context) {
		try {
			Intent intent=new Intent();
			intent.setComponent(new ComponentName("com.asus.loguploader", "com.asus.loguploader.LogUploaderService"));
			context.stopService(intent);
		} catch (Exception e) {
			
		}
		shareClose();
		//remove log upload config
		SystemProperties.set("persist.asus.mupload.enable", "0");
		SystemProperties.set("persist.asus.autoupload.enable", "0");
		SystemProperties.set("persist.asuslog.dump.enable", "0");
		SystemProperties.set("persist.asuslog.dump.date", "0");
		setCmd("rm /data/debug/busybox");
		
		if(isUserBuild()){
			setCmd("rm -rf /data/logs/*");
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SystemProperties.set("persist.asuslog.logcmd", "");
		
		Intent intent =new Intent(ASUS_LOGTOOL_LOGTOOL_REMOVE);
		context.sendBroadcast(intent);
		
	}

	private static void log(String msg) {
		Log.v(TAG, msg);
	}
}
