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
	
	public static final String TAG = "OtherLog";
	private static final String  CAMERA_DEBUD_PROP="camera.hal.debug";
	private Switch mActivitySwitch;
	private Switch mWindowSwitch;
	private Switch mUartSwitch;
	private Switch mUsbdiagSwitch;
	
	public static final String AUDBG = "persist.asus.audbg";
	public static final String USB_Diag = "persist.sys.usb.config";
	//private Switch mCameraDumpSwitch;
	public OtherLog(Activity activity, View view) {
		super(activity, view);
		//mCameraDumpSwitch=(Switch) view.findViewById(R.id.camera_switch_id);
		mActivitySwitch=(Switch) view.findViewById(R.id.activity_switch_id);
		mWindowSwitch=(Switch) view.findViewById(R.id.window_switch_id);
		mUartSwitch=(Switch) view.findViewById(R.id.uartlog_switch_id);
		mUsbdiagSwitch=(Switch) view.findViewById(R.id.usbdiag_switch_id);
		
		mActivitySwitch.setChecked(Settings.isActivityEnable());
		mWindowSwitch.setChecked(Settings.isWindowEnable());
		//mCameraDumpSwitch.setChecked(isCameraDebug());
		mUartSwitch.setChecked(getPropCheck(AUDBG));
		String value = SystemProperties.get(USB_Diag);
		int a = value.indexOf("diag"); 
		if(a != -1)
			mUsbdiagSwitch.setChecked(true);
		else
			mUsbdiagSwitch.setChecked(false);
		
		view.findViewById(R.id.activity_layout).setOnClickListener(this);
		view.findViewById(R.id.window_layout).setOnClickListener(this);
		view.findViewById(R.id.uartlog_layout).setOnClickListener(this);
		view.findViewById(R.id.usbdiag_layout).setOnClickListener(this);
		//view.findViewById(R.id.camera_layout).setOnClickListener(this);
		//mCameraDumpSwitch.setOnCheckedChangeListener(this);
		mActivitySwitch.setOnCheckedChangeListener(this);
		mWindowSwitch.setOnCheckedChangeListener(this);
		mUartSwitch.setOnCheckedChangeListener(this);
		mUsbdiagSwitch.setOnCheckedChangeListener(this);
	}

	public static void forceStop(){
		
		Settings.setWindowEnable(false);

		Settings.setActivityEnable(false);
		
		setPropCheck(AUDBG, false);
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
	
	public String Stringinsert(String a,String b,int t){ 
		return a.substring(0,t)+b+a.substring(t,a.length());
	}
	
	/*indexOf,not find string ,return -1; mtp,diag,adb*/	
	public void SetUsbDiag(boolean enable){
		
		String value=SystemProperties.get(USB_Diag);
		
		if(enable){
			Log.v(TAG, "enable usbdiag " + value.toString());
			value = "diag,serial_smd,rmnet_ipa,adb";
			/*
			int a = value.indexOf("diag"); 
			if(a != -1){
				Log.v(TAG,"diag exist already " + value.toString());
				return; 
			}
			
			a = value.indexOf("mtp"); 
			if(a != -1){
				value = Stringinsert(value,",diag",3);
			}else{
				int e = value.indexOf("adb");
				if(e != -1)
					value = "diag," + value;
				else
					value = "diag";
			}*/
			//Log.v(TAG, Integer.toHexString(a));			
		}else{		
			Log.v(TAG, "disable usbdiag " + value.toString());
			value = "mtp,adb";
			/*
			int b = value.indexOf("diag"); 
			if(b == -1){
				Log.v(TAG,"diag not exist already " + value.toString());
				return; 
			}
			
			b = value.indexOf("mtp"); 
			if(b != -1){				
				int c = value.indexOf("adb"); 
				if(c != -1)
					value = value.substring(0,3)+value.substring(8,value.length());
				else
					value = value.substring(0,3);
			}else{
				int d = value.indexOf("adb"); 
				if(d != -1)
					value = "adb";
				else
					value = "";
			}*/			
		}
		SystemProperties.set(USB_Diag, value);
		Log.v(TAG, value.toString());
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
		case R.id.uartlog_layout:
			
			state = mUartSwitch.isChecked();
			mUartSwitch.setChecked(state=!state);
			
			break;
		case R.id.usbdiag_layout:
			
			state = mUsbdiagSwitch.isChecked();
			mUsbdiagSwitch.setChecked(state=!state);
			SetUsbDiag(state);
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
		mUartSwitch.setChecked(true);
		mUsbdiagSwitch.setChecked(true);
		
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
		//mCameraDumpSwitch.setChecked(false);
		mActivitySwitch.setChecked(false);
		mWindowSwitch.setChecked(false);
		mUartSwitch.setChecked(false);
		mUsbdiagSwitch.setChecked(false);
		
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
		
		if(buttonView==mUartSwitch){
			setPropCheck(AUDBG, isChecked);
		}
		
		if(buttonView==mUsbdiagSwitch){
			SetUsbDiag(isChecked);
		}

		//if(buttonView==mCameraDumpSwitch){
		//	setCameraDebug(isChecked);
		//}
	}

}
