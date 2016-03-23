package com.asus.log;

import com.asus.tool.DebuggerMain;
import com.asus.tool.Settings;
import com.asus.tool.Util;
import com.asus.fdclogtool.R;

import android.app.Activity;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class OtherLog extends BaseLog implements OnCheckedChangeListener{
	
	private static final String  CAMERA_DEBUD_PROP="camera.hal.debug";
	private Switch mActivitySwitch;
	private Switch mWindowSwitch;
	//private Switch mCameraDumpSwitch;
	public OtherLog(Activity activity, View view) {
		super(activity, view);
		//mCameraDumpSwitch=(Switch) view.findViewById(R.id.camera_switch_id);
		mActivitySwitch=(Switch) view.findViewById(R.id.activity_switch_id);
		mWindowSwitch=(Switch) view.findViewById(R.id.window_switch_id);
		
		
		mActivitySwitch.setChecked(Settings.isActivityEnable());
		mWindowSwitch.setChecked(Settings.isWindowEnable());
		//mCameraDumpSwitch.setChecked(isCameraDebug());
		
		view.findViewById(R.id.activity_layout).setOnClickListener(this);
		view.findViewById(R.id.window_layout).setOnClickListener(this);
		//view.findViewById(R.id.camera_layout).setOnClickListener(this);
		//mCameraDumpSwitch.setOnCheckedChangeListener(this);
		mActivitySwitch.setOnCheckedChangeListener(this);
		mWindowSwitch.setOnCheckedChangeListener(this);
		
	}

	public static void forceStop(){
		
		Settings.setWindowEnable(false);

		Settings.setActivityEnable(false);
	}
	
	public boolean isCameraDebug(){
		String value=SystemProperties.get(CAMERA_DEBUD_PROP);
		if(value==null || value.length()==0){
			return false;
		}
		
		if(value.equals("3")){
			return true;
		}
		return false;
	}
	
	public void setCameraDebug(boolean enable){
		if(enable){
			Util.setCmd("setprop "+CAMERA_DEBUD_PROP+" 3");
		}else{
			Util.setCmd("setprop "+CAMERA_DEBUD_PROP+" 0");
		}
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) 
		{
	
		case R.id.activity_layout:
			
			boolean state=mActivitySwitch.isChecked();
			mActivitySwitch.setChecked(state=!state);
		
			break;
		case R.id.window_layout:
			
			state=mWindowSwitch.isChecked();
			mWindowSwitch.setChecked(state=!state);
		
			break;
		//case R.id.camera_layout:
			
		//	state=mCameraDumpSwitch.isChecked();
		//	mCameraDumpSwitch.setChecked(state=!state);
		
		//	break;
		default:
			break;
		}
	}
	
	
	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		super.onSelectAll();
		//mCameraDumpSwitch.setChecked(true);
		mActivitySwitch.setChecked(true);
		mWindowSwitch.setChecked(true);
		
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
		//mCameraDumpSwitch.setChecked(false);
		mActivitySwitch.setChecked(false);
		mWindowSwitch.setChecked(false);
		
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked)
		{
			if(isDiskValidShowToast()==false){
				buttonView.setOnCheckedChangeListener(null);
				buttonView.setChecked(false);
				buttonView.setOnCheckedChangeListener(this);
				return;
			}
		}
		
		if(buttonView==mActivitySwitch){
			Settings.setActivityEnable(isChecked);
			try {
				mServiceLog.activityLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		
		if(buttonView==mWindowSwitch){
			Settings.setWindowEnable(isChecked);
			try {
				mServiceLog.windowLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		
		//if(buttonView==mCameraDumpSwitch){
		//	setCameraDebug(isChecked);
		//}
	}

}
