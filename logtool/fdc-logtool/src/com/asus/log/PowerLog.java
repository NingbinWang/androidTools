package com.asus.log;

import java.io.File;

import com.asus.tool.DebuggerMain;
import com.asus.tool.IServiceLog;
import com.asus.tool.Settings;
import com.asus.tool.Util;
import com.asus.fdclogtool.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class PowerLog extends BaseLog implements OnCheckedChangeListener{

	private static final String TAG = "PowerLog";
	private Switch mPowerSwitch;
	private Switch mBatterySwitch;
	private Switch mGuageSwitch;
	private static final String  KEY_GAUAGE_LEVEL="persist.asuslog.guage_level";
	private static final String  KEY_GAUAGE_LEVEL_ENABLE="persist.asuslog.gauage_enable";
	private static final String GAUAGE_LEVEL_DEFAULT="0";
	public PowerLog(Activity activity, View view) {
		super(activity, view);
		mPowerSwitch	=(Switch) view.findViewById(R.id.power_switch_id);
		mBatterySwitch	=(Switch) view.findViewById(R.id.battery_switch_id);
		mGuageSwitch	=(Switch) view.findViewById(R.id.switch_battery_gauge);
		mPowerSwitch.setChecked(Settings.isPowerEnable());
		mBatterySwitch.setChecked(Settings.isBatteryEnable());
		mGuageSwitch.setChecked(isGauageLeavelSupport());
		view.findViewById(R.id.power_layout).setOnClickListener(this);
		view.findViewById(R.id.battery_layout).setOnClickListener(this);
		view.findViewById(R.id.battery_gauge_level_layout).setOnClickListener(this);
		
		mGuageSwitch.setOnCheckedChangeListener(this);
		mPowerSwitch.setOnCheckedChangeListener(this);
		mBatterySwitch.setOnCheckedChangeListener(this);
	}
	
	public static void forceStop(){
		Settings.setPowerEnable(false);
		Settings.setBatteryEnable(false);
		setPropCheck(KEY_GAUAGE_LEVEL_ENABLE,false);
	}
	
	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		super.onSelectAll();
		mPowerSwitch.setChecked(true);
		mBatterySwitch.setChecked(true);
		mGuageSwitch.setChecked(true);
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
		mPowerSwitch.setChecked(false);
		mBatterySwitch.setChecked(false);
		mGuageSwitch.setChecked(false);
	}
	
	public static boolean isGauageLeavelSupport()
	{//ACTION_BOOT_COMPLETED exec
		return getPropCheck(KEY_GAUAGE_LEVEL_ENABLE);
	}
	
	private static String getGuageSLevel(){
		return SystemProperties.get(KEY_GAUAGE_LEVEL, GAUAGE_LEVEL_DEFAULT);
	}
	
	private static void setGuageSLevel(String level){
		SystemProperties.set(KEY_GAUAGE_LEVEL, level);
	}
	
	public static void sendGauageLevelCmd(Context context)
	{//ACTION_BOOT_COMPLETED exec
		
		boolean result=Util.setCmd("echo "+getGuageSLevel()+" > sys/module/ug31xx_battery/parameters/op_options");
		if(result==false){
			Toast.makeText(context, "exec echo fail!!!", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void onGauageLevelSelect()
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.gauage_log_level);
		View view=mActivity.getLayoutInflater().inflate(R.layout.gauage_level, null);
		final RadioButton radioButtonLevel0=(RadioButton) view.findViewById(R.id.radioButtonLevel0);
		final RadioButton radioButtonLevel4=(RadioButton) view.findViewById(R.id.radioButtonLevel4);
		final RadioButton radioButtonLevel8=(RadioButton) view.findViewById(R.id.radioButtonLevel8);
		final RadioButton radioButtonLevel12=(RadioButton) view.findViewById(R.id.radioButtonLevel12);
		
		
		
		if(getGuageSLevel().equals("4")){
			radioButtonLevel4.setChecked(true);
		}else if(getGuageSLevel().equals("8")){
			radioButtonLevel8.setChecked(true);
		}else if(getGuageSLevel().equals("12")){
			radioButtonLevel12.setChecked(true);
		}else if(getGuageSLevel().equals("0")){
			radioButtonLevel0.setChecked(true);
		}
		
		
		builder.setView(view);
		builder.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String resultString="0";
				if(radioButtonLevel8.isChecked()){
					resultString="8";
				}else if(radioButtonLevel12.isChecked()){
					resultString="12";
				}else if(radioButtonLevel4.isChecked()){
					resultString="4";
				}
				 setGuageSLevel(resultString);
				 
				if(mGuageSwitch.isChecked()){
					sendGauageLevelCmd(mActivity);
				}
				
			
			};
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			};
		});
		builder.show();
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) 
		{
		case R.id.power_layout:
			log("switch_tcpdump_id=");
			boolean state=mPowerSwitch.isChecked();
			mPowerSwitch.setChecked(state=!state);
		
			break;
		case R.id.battery_layout:
			log("switch_tcpdump_id=");
			state=mBatterySwitch.isChecked();
			mBatterySwitch.setChecked(state=!state);
		
			break;
		case R.id.battery_gauge_level_layout:
			onGauageLevelSelect();
			break;
		default:
			break;
		}
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
		if(buttonView==mPowerSwitch){
			Settings.setPowerEnable(isChecked);
			try {
				mServiceLog.powerLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		if(buttonView==mBatterySwitch){
			Settings.setBatteryEnable(isChecked);
			try {
				mServiceLog.batteryLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		if(buttonView==mGuageSwitch){
			setPropCheck(KEY_GAUAGE_LEVEL_ENABLE, mGuageSwitch.isChecked());
			if(mGuageSwitch.isChecked()){
				sendGauageLevelCmd(mActivity);
			}
			
		}
	}

}
