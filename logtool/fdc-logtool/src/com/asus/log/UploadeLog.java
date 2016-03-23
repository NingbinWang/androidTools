package com.asus.log;

import com.asus.fdclogtool.R;

import android.app.Activity;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class UploadeLog extends BaseLog implements OnCheckedChangeListener{

	private static final String TAG = "UploadeLog";
	
	private Switch mUploadSwitch;
	private String KEY_AUTO_UPLOAD="persist.asus.autoupload.enable";
	public UploadeLog(Activity activity, View view) {
		super(activity, view);
		mUploadSwitch=(Switch) view.findViewById(R.id.switch_upload_id);
		view.findViewById(R.id.upload_layout).setOnClickListener(this);
		mUploadSwitch.setChecked(getPropCheck(KEY_AUTO_UPLOAD));
		mUploadSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.upload_layout:
			
			boolean state=mUploadSwitch.isChecked();
			mUploadSwitch.setChecked(state=!state);
		
			break;
	
		default:
			break;
		}
	}

	
	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		super.onSelectAll();
		mUploadSwitch.setChecked(true);
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
		mUploadSwitch.setChecked(false);
	}
	
	public static void log(String message){
		Log.v(TAG, message);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if(buttonView==mUploadSwitch){
			setPropCheck(KEY_AUTO_UPLOAD, isChecked);
		}
	}
}
