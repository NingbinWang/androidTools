package com.asus.testtool.tool;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class MonitorService extends Service {

    public MonitorTask mCountTask;
    public Monitor mActivity;
//	private Handler serviceHandler = null;  
    
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
    
    @Override
    public void onCreate() {
    	Log.d("AsusTestTool", "Monitor : service onCreate" );
    	super.onCreate();
    	
    	
//		try {
//			mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//			/* acquire WakeLock */
//			mWakeLock = mPowerManager.newWakeLock(
//					PowerManager.FULL_WAKE_LOCK, "Main");
//			// wakeLock();
//			mWakeLock.acquire();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}	
    	
        mCountTask = new MonitorTask();
        mCountTask.execute();

    }
  
    public class LocalBinder extends Binder {
        MonitorService getService() {
            return MonitorService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    public void setActivity(Monitor activity) {
        if (mCountTask != null) {
            mCountTask.setActivity(activity);
        }
    }
    
    public void setTestItems(boolean [] items) {
        if (mCountTask != null) {
            mCountTask.setTestItems(items);
        }
    }
    
    
    
    @Override
    public void onDestroy(){
    	Log.d("AsusTestTool", "Monitor : service onDestroy" );
    	super.onDestroy();
    	mCountTask.cancel(true);
    	
//		try {
//			mWakeLock.release();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}	
    }
	
}
