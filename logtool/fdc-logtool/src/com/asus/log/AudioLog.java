package com.asus.log;
import android.app.Activity;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.asus.tool.DebuggerMain;
import com.asus.tool.IServiceLog;
import com.asus.tool.Settings;
import com.asus.tool.Util;
import com.asus.fdclogtool.R;
public class AudioLog extends BaseLog implements OnCheckedChangeListener{

	private static final String TAG = "AudioLog";
	private Switch mAudioSwitch;
	
	public AudioLog(Activity activity, View view) {
		super(activity, view);
		
		mAudioSwitch=(Switch) view.findViewById(R.id.audio_switch_id);
		mAudioSwitch.setChecked(Settings.isAudioEnable());
		
		view.findViewById(R.id.audio_layout).setOnClickListener(this);
		mAudioSwitch.setOnCheckedChangeListener(this);
	}

	public static void forceStop(){
		Settings.setAudioEnable(false);
	}
	
	@Override
	public void onClick(View v) {
		boolean state=mAudioSwitch.isChecked();
		mAudioSwitch.setChecked(state=!state);
		
	}
	
	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		super.onSelectAll();
		mAudioSwitch.setChecked(true);
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
		mAudioSwitch.setChecked(false);
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
		if(buttonView==mAudioSwitch){
			Settings.setAudioEnable(isChecked);
			try {
				mServiceLog.audioLogEnable(isChecked);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		
	}

}
