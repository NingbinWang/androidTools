package com.asus.log;

import com.asus.tool.DumpService;
import com.asus.tool.IServiceLog;
import com.asus.tool.Util;

import android.app.Activity;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BaseLog implements OnClickListener{
	
	public static final String TAG = "BaseLog";
	protected Activity mActivity;
	protected View mView;
	protected IServiceLog mServiceLog;
	
	public static void log(String message){
		Log.v(TAG, message);
	}
	public void onSelectAll(){
		
	}
	
	public void onCancelAll(){
		
	}
	
	 public void onDestroy(){
		 
	 }
	
	 public void onResume() {
		 
	 }
	 
	 public boolean isDiskValidShowToast(){
		 if(Util.isDiskAllowOpen( DumpService.getLogRootpath())==false){
			 Toast.makeText(mActivity, "Run out of disk sapce", Toast.LENGTH_SHORT).show();
			 return false;
		 }
		return true;
	 }
	
	 public boolean isDiskSpaceAllow(){
		
		return Util.isDiskAllowOpen( DumpService.getLogRootpath());
	 }
	 
	 public void onPause(){
		 
	 }
	public BaseLog(Activity activity,View view)
	{
		mActivity=activity;
		mView=view;
	}
	
	public void setServiceLog(IServiceLog servicelog){
		mServiceLog=servicelog;
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
	}
	
	public static boolean getPropCheck(String key){
		if(key.equals("persist.asuslog.dump.enable"))
			Log.e("==enableLogtool==1","====0=====");
		String value=SystemProperties.get(key, "0");
		if(value.equals("1")){
			if(key.equals("persist.asuslog.dump.enable"))
			Log.e("==enableLogtool==2","====1=====");
			return true;
		}
		if(key.equals("persist.asuslog.dump.enable"))
			Log.e("==enableLogtool==2","====0=====");
		return false;
	}
	
	public static void setPropCheck(String key,boolean check){
		if(check==true){
			SystemProperties.set(key, "1");
		}else{
			SystemProperties.set(key, "0");
		}
	}
	
}
