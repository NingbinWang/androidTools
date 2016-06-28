package com.asus.tool;



import java.io.File;
import java.lang.annotation.Retention;


import android.app.AlertDialog;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.content.Intent;

import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;


public class CrashLogServie extends Service  {

	private static final String TAG = "CrashLogServie";
	Context mContext;
	
	FileObserver mFileObserver;
	public static final String PATH_CRASH_LOG="/mnt/sdcard/logs/";
	
	public static final String CRASH_REMINDER_ENABLE="ACTION_CRASH_LOG_REMINDER_ENABLE";
	public static final String CRASH_REMINDER_DISABLE="ACTION_CRASH_LOG_REMINDER_DISABLE";
	public static final String LOGTOOL_INSTALLED="com.asus.loguploader.action.LOGTOOL_INSTALLED";
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Settings.init(getSharedPreferences(DebuggerMain.NAME_SHARE_PREF, Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS));
		mContext=getApplicationContext();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(CRASH_REMINDER_ENABLE);
		intentFilter.addAction(CRASH_REMINDER_DISABLE);
		intentFilter.addAction(LOGTOOL_INSTALLED);
		registerReceiver(mReceiver, intentFilter);
		
		createFileObserver();
		boolean result=Settings.isCrashReminderEnable();
		if(result){
			onStartWatch();
		}
		
	}
	
	
	public void createFileObserver()
	{
		if(mFileObserver!=null){
			return;
		}
		mFileObserver=new FileObserver(PATH_CRASH_LOG, FileObserver.CREATE)
		{
			
			@Override 
			public void onEvent(int event, String path) {
				if(path.startsWith("crashlog")==false){
					return;
				}
				Intent intent=new Intent(DialogLogReceiver.ACTION_CRASH);
				intent.putExtra("path", PATH_CRASH_LOG+path);
				mContext.sendBroadcast(intent);

			}
		};
	}
	
	BroadcastReceiver mReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(action.equals(CRASH_REMINDER_ENABLE)){
				Log.v(TAG, "CRASH_REMINDER_ENABLE");
				createFileObserver();
				onStartWatch();
			}else if(action.equals(CRASH_REMINDER_DISABLE)){
				Log.v(TAG, "CRASH_REMINDER_DISABLE");
				onStopWatch();
			}else if(action.equals(LOGTOOL_INSTALLED)){
				Log.v(TAG, "LOGTOOL_INSTALLED");
				createFileObserver();
				onStartWatch();
			}
			
		}
		
	};

	public void onStartWatch(){
		if(mFileObserver!=null)
			mFileObserver.startWatching();
	}
	
	public void onStopWatch(){
		if(mFileObserver!=null)
			mFileObserver.stopWatching();
	}
	    
	    private void log(String msg){
	    	Log.v(TAG, msg);
	    }
	
	    @Override
		public int onStartCommand(Intent intent, int flags, int startId) {
		  
		    return START_STICKY;
		}
	    
	    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
