 package com.asus.log;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.asus.tool.Settings;
import com.asus.fdclogtool.R;
import com.asus.tool.DumpSyslog;
import com.asus.tool.Util;

import android.content.Intent;
import android.content.IntentFilter;

public class QPSTDownloadMode extends BaseLog implements OnCheckedChangeListener{

	
	private static final String TAG = "QPSTDownloadMode";
	public Switch mQPSTSwitch;
	public QPSTDownloadMode(Activity activity, View view) {
		super(activity, view);
		mQPSTSwitch=(Switch) view.findViewById(R.id.switch_QPSTDownloadMode_id);
		mQPSTSwitch.setChecked(isQPSTDownloadModeEnable());
		mQPSTSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	public void onSelectAll() {
		super.onSelectAll();
		log("onSelectAll");
		mQPSTSwitch.setChecked(true);
	}

	@Override
	public void onCancelAll() {
		super.onCancelAll();
		mQPSTSwitch.setChecked(false);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	        if (buttonView == mQPSTSwitch) {
			if(isChecked){
				SystemProperties.set("persist.asuslog.qpst.enable","1");
				if(Util.isUserBuild())
					mActivity.sendBroadcast(new Intent("com.asus.QPST.fw"));
					//Util.setCmd("am broadcast -a \"com.asus.QPST.fw\"");
			}
			else{
  				SystemProperties.set("persist.asuslog.qpst.enable","0");
				if(Util.isUserBuild())
					mActivity.sendBroadcast(new Intent("com.asus.QPST.fw"));
					//Util.setCmd("am broadcast -a \"com.asus.QPST.fw\"");
			}
		}		
	}
	
	public static boolean isQPSTDownloadModeEnable(){
		String value = null;
		if(Util.isUserBuild())
			value = SystemProperties.get("persist.asuslog.qpst.enable", "0");
		else 
                        value = SystemProperties.get("persist.asuslog.qpst.enable", "1");
		if(value.equals("1")) return true;
		else return false;
	  }
}
