package com.asus.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.view.View;
import android.widget.Toast;


public class SaveLog extends BaseLog{
	private static final String TAG = "SaveLog";
	public SaveLog(Activity activity, View view) {
		super(activity, view);
		// TODO Auto-generated constructor stub
	}




	private Handler mHandler=new Handler();

//	public LogSave(Activity activity){
//		mActivity=activity;
//		
//		File file =new File(Environment.getExternalStorageDirectory()+"/logs");
//		if(file.exists()==false){
//			file.mkdirs();
//		}
//	}
	


	Runnable mBrocastRunnable=new Runnable() {
		
		@Override
		public void run() {
			
			mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			Toast.makeText(mActivity, "copy finish", Toast.LENGTH_SHORT).show();
		}
	};
	
	
 
	
	public void onClick() {
	
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyyMMddHHmm");
		String currentDateandTime = sdf.format(new Date());
		
		File file =new File(Environment.getExternalStorageDirectory()+"/logs/"+currentDateandTime+"/");
		file.mkdir();

		SystemClock.sleep(200L);
		
		
		SystemProperties.set("root_run.cmd", "cp -r /logs/* /sdcard/logs/"+currentDateandTime+"/");
		
		
		SystemProperties.set("ctl.start","app_runcmd");
		SystemClock.sleep(100L);
		
		
		String cmdString= SystemProperties.get("root_run.ret");
		 
			 
		SystemClock.sleep(500L);
		
		
		
		mHandler.postDelayed(mBrocastRunnable, 2000);
	}




	@Override
	public void onSelectAll() {
		
		super.onSelectAll();
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
}
