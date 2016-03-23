package com.asus.tool;



import java.io.File;

import com.asus.log.BaseLog;
import com.asus.log.ModemLogService;
import com.asus.log.ModemLog;
import com.asus.log.PowerLog;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;


public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = "BootReceiver";
    public static final String ACTION_MODEM_STARTLOG="asus.intent.action.phone.startlog";
    public static final String LAST_JELLY_BEAN_MR2_KERNEL_PATH="/proc/last_kmsg";
    public static final String LAST_KITKAT_KERNEL_PATH="/pstore/console-ramoops";
    public static final String ACTION_MODEM_CHECK_RESTART="asus.intent.action.phone.check.restartlog";
    public static final String ENABLE="1";
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      String mount=Util.getVolumeState( context,Util.getMicroSDPath( context));
	 if(mount.equals(Environment.MEDIA_REMOVED)){
		if(DumpService.isSaveMicroSD(context)){
			ModemLog.updateLogConfig(false);
			DebuggerMain.setSaveDefaultPath();
		}
	 }
	
	 String result=SystemProperties.get("persist.asuslog.dump.enable","0");
	 //if(result.equals(ENABLE)==false){
	 	//Intent intenInit=new Intent(context,LogInitService.class);
	  	//context.startService(intenInit);
	 //}
	 
      if (Intent.ACTION_BOOT_COMPLETED.equals(action))
	   {
    	  Intent dockIntent = new Intent(context, DumpService.class);
	      context.startService(dockIntent);
	   }
      
      if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
    	  SharedPreferences pref = context.getSharedPreferences(DebuggerMain.NAME_SHARE_PREF, Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
    	  Settings.init(pref);
    	  Log.v(TAG, "FirstSystemEnable()="+Settings.isFirstSystemEnable()+"logging enable="+BaseLog.getPropCheck(ModemLog.KEY_MODEM_OFFLINE_LOGGING));
    	  if(BaseLog.getPropCheck(ModemLog.KEY_MODEM_OFFLINE_LOGGING)==false && Settings.isFirstSystemEnable() && Util.isUserBuild()==false){
    		  DumpService.startModemLog(context);
    	  }else if(BaseLog.getPropCheck(ModemLog.KEY_MODEM_OFFLINE_LOGGING)==true){
    		  Intent i2sService = new Intent(context, ModemLogService.class);
      		  i2sService.setAction(ACTION_MODEM_CHECK_RESTART);
      		  context.startService(i2sService);
    	  }
    	  //Settings.setFirstSystemOpen(false);
      }
      
      if (Intent.ACTION_BOOT_COMPLETED.equals(action))
	   {
	   	  Intent crashIntent = new Intent(context, CrashLogServie.class);
	   	  context.startService(crashIntent);
	   }
      
      if (Intent.ACTION_BOOT_COMPLETED.equals(action))
	  {
    	  if(PowerLog.isGauageLeavelSupport()){
    		  PowerLog.sendGauageLevelCmd(context);
    	  }
	  }
      if (Intent.ACTION_BOOT_COMPLETED.equals(action)){
	      //copy last kmsg
    	  String prevBoot=DebuggerMain.getPrevBootRootPath();
    	  if(prevBoot==null){
    		  DebuggerMain.setPrevBootRootPath(DumpService.getCurrentLogPath());
    	  }else{
    		String lastPath=LAST_KITKAT_KERNEL_PATH;
    		if(Build.VERSION.SDK_INT==Build.VERSION_CODES.JELLY_BEAN_MR2){
    			lastPath=LAST_JELLY_BEAN_MR2_KERNEL_PATH;
    		}
    		
    		File fileLastKmsg=new File(lastPath);
    		File prevBbootRootFile=new File(prevBoot);
  			if(fileLastKmsg.exists()==true && prevBbootRootFile.exists()){
  				Log.v(TAG, "lastkernel exist="+fileLastKmsg.exists());
  				try {
  					DumpSyslog.dumpsys("cp "+lastPath+" "+prevBoot);
  				} catch (Exception e) {
  					// TODO: handle exception
  				}
  			}
  			DebuggerMain.setPrevBootRootPath(DumpService.getCurrentLogPath());
    	  }
	      
      }
 
    }
}
