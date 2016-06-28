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

public class StayonService extends Service {

	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
    
    @Override
    public void onCreate() {
    	Log.d("AsusTestTool", "Stayon : service onCreate" );
    	super.onCreate();
    	
    	  
		try {
			mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			/* acquire WakeLock */
			mWakeLock = mPowerManager.newWakeLock(
					PowerManager.FULL_WAKE_LOCK, "Main");
			// wakeLock();
			mWakeLock.acquire();

		} catch (Exception e) {
			e.printStackTrace();
		}	
   

    }
  

    
    
    
    @Override
    public void onDestroy(){
    	Log.d("AsusTestTool", "Stayon : service onDestroy" );
    	super.onDestroy();
    	
		try {
			mWakeLock.release();

		} catch (Exception e) {
			e.printStackTrace();
		}	
    }





	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
