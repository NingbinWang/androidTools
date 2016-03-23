package com.asus.log;
import java.util.zip.Inflater;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.asus.tool.Settings;
import com.asus.fdclogtool.R;


public class GeneralLog extends BaseLog implements OnCheckedChangeListener{

	
	private static final String TAG = "GeneralLog";
	public static final String KEY_KERNEL="persist.asuslog.kernel.enable";
	public static final String KEY_MAIN="persist.asuslog.main.enable";
	public static final String KEY_EVENT="persist.asuslog.events.enable";
	public static final String KEY_RADIO="persist.asuslog.radio.enable";
	public static final String KEY_COMBINE_ENABLE="persist.asuslog.combine.enable";
	public static final String KEY_COMBINE_CONFIG="persist.asuslog.combine.config";
	public static final String KEY_RIL_LEVEL="persist.asuslog.ril.level";
	private static final String ASUS_LOGTOOL_SET_RADIO_LEVEL = "asus.intent.action.set.radio.level";
	public static final String LOGCAT = "persist.asuslog.logcat.enable";
	public static final String LOGCAT_RADIO = "persist.asuslog.logcatr.enable";
	public static final String LOGCAT_EVENTS = "persist.asuslog.logcate.enable";
	public Switch mKernalSwitch;
	public Switch mMainSwitch;
	public Switch mEventSwitch;
	public Switch mRadioSwitch;
	public Switch mCombineSwitch;
	public static final int LOG_MAIN 		=1;
	public static final int LOG_SYSTEM 	=2;
	public static final int LOG_KERNEL 	=4;
	public static final int LOG_RADIO		=8;
	public static final int LOG_EVENTS 	=16;
	
	public static final int RIL_LOG_EMPTY=-1;
	public static final int RIL_LOG_NONE=0;
	public static final int RIL_LOG_VERBOSE=1;
	public static final int RIL_LOG_INFO=2;
	public static final int RIL_LOG_WARN=4;
	public static final int RIL_LOG_CRITICAL=8;
	public static final int LOG_ALL 	=LOG_MAIN|LOG_SYSTEM|LOG_KERNEL|LOG_RADIO|LOG_EVENTS;
	//public Switch mLogCatSwitch;
	public GeneralLog(Activity activity, View view) {
		super(activity, view);
		mKernalSwitch=(Switch) view.findViewById(R.id.kernal_switch_id);
		mEventSwitch=(Switch) view.findViewById(R.id.event_switch_id);
		mRadioSwitch=(Switch) view.findViewById(R.id.radio_switch_id);
		mKernalSwitch.setChecked(getPropCheck(LOGCAT));
		mEventSwitch.setChecked(getPropCheck(LOGCAT_EVENTS));
		mRadioSwitch.setChecked(getPropCheck(LOGCAT_RADIO));
		mKernalSwitch.setOnCheckedChangeListener(this);
		mEventSwitch.setOnCheckedChangeListener(this);
		mRadioSwitch.setOnCheckedChangeListener(this);
		view.findViewById(R.id.kernal_layout).setOnClickListener(this);
		view.findViewById(R.id.radio_layout).setOnClickListener(this);
		view.findViewById(R.id.event_layout).setOnClickListener(this);
	}

	

	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		super.onSelectAll();
		log("onSelectAll");
		mKernalSwitch.setChecked(true);
		mEventSwitch.setChecked(true);
		mRadioSwitch.setChecked(true);
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
		mKernalSwitch.setChecked(false);
		mEventSwitch.setChecked(false);
		mRadioSwitch.setChecked(false);
	}
	
	public String getCombineConfig(){
		String value=SystemProperties.get(KEY_COMBINE_CONFIG);
		if(value==null || value.equals("0") || value.length()==0){
			//set default
			SystemProperties.set(KEY_COMBINE_CONFIG, String.valueOf(LOG_ALL));
			return String.valueOf(LOG_ALL);
		}
		return value;
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.kernal_layout:
			boolean state=mKernalSwitch.isChecked();
			mKernalSwitch.setChecked(state=!state);
			break;
		case R.id.event_layout:
			state=mEventSwitch.isChecked();
			mEventSwitch.setChecked(state=!state);
			break;
		case R.id.radio_layout:
			state=mRadioSwitch.isChecked();
			mRadioSwitch.setChecked(state=!state);
			break;
		/**
		case R.id.combine_general_layout:
			AlertDialog.Builder dialog=new AlertDialog.Builder(mActivity);
			dialog.setTitle("Custom General Log");
			final View view=mActivity.getLayoutInflater().inflate(R.layout.combine_general_item, null);
			dialog.setView(view);
			final CheckBox mainBox=(CheckBox) view.findViewById(R.id.check_combine_main);
			final CheckBox systemBox=(CheckBox) view.findViewById(R.id.check_combine_system);
			final CheckBox kernelBox=(CheckBox) view.findViewById(R.id.check_combine_kernel);
			final CheckBox radioBox=(CheckBox) view.findViewById(R.id.check_combine_radio);
			final CheckBox eventsBox=(CheckBox) view.findViewById(R.id.check_combine_events);
		
			String value=getCombineConfig();
			
			int digit=LOG_ALL;
			log("digit="+digit);
			try {
				digit=Integer.valueOf(value);
			} catch (Exception e) {
				SystemProperties.set(KEY_COMBINE_CONFIG, String.valueOf(LOG_ALL));
				log(e.getMessage());;
			}
			
			mainBox.setChecked(((digit&LOG_MAIN)==LOG_MAIN)?true:false);
			systemBox.setChecked(((digit&LOG_SYSTEM)==LOG_SYSTEM)?true:false);
			kernelBox.setChecked(((digit&LOG_KERNEL)==LOG_KERNEL)?true:false);
			radioBox.setChecked(((digit&LOG_RADIO)==LOG_RADIO)?true:false);
			eventsBox.setChecked(((digit&LOG_EVENTS)==LOG_EVENTS)?true:false);
			
			dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				
					int value=0;
					value += (mainBox.isChecked()? LOG_MAIN :0);
					value += (systemBox.isChecked()? LOG_SYSTEM :0);
					value += (kernelBox.isChecked()? LOG_KERNEL :0);
					value += (radioBox.isChecked()? LOG_RADIO :0);
					value += (eventsBox.isChecked()? LOG_EVENTS :0);
					if(value==0){
						Toast.makeText(mActivity, "choice one...", Toast.LENGTH_SHORT).show();
						return;
					}
					SystemProperties.set(KEY_COMBINE_CONFIG,String.valueOf(value));
					if(mCombineSwitch.isChecked()){
						Toast.makeText(mActivity, "restart service...", Toast.LENGTH_SHORT).show();
						BaseLog.setPropCheck(KEY_COMBINE_ENABLE, false);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						BaseLog.setPropCheck(KEY_COMBINE_ENABLE, true);
					}
				}
			});
			
			dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					
				}
			});
			dialog.show();
			break;
		**/
		default:
			break;
		}
		
		
	}
	
	public void selectRadioLogLevel(){
		AlertDialog.Builder dialog=new AlertDialog.Builder(mActivity);
		dialog.setTitle("Ril Log Level");
		View view =mActivity.getLayoutInflater().inflate(R.layout.log_level, null);
		dialog.setView(view);
		final RadioButton radioDefault=(RadioButton) view.findViewById(R.id.btnLevelDefault);
		final RadioButton radioVerbose=(RadioButton) view.findViewById(R.id.btnLevelVerbose);
		final RadioButton radioInfo=(RadioButton) view.findViewById(R.id.btnLevelInfo);
		final RadioButton radioWarn=(RadioButton) view.findViewById(R.id.btnLevelWarn);
		final RadioButton radioCritical=(RadioButton) view.findViewById(R.id.btnLevelCritical);
		String prop=SystemProperties.get(KEY_RIL_LEVEL);
		if(prop!=null && prop.length()>0){
			int level=Integer.valueOf(prop);
			switch (level) {
			case RIL_LOG_NONE:
				radioDefault.setChecked(true);
				break;
			case RIL_LOG_VERBOSE:
				radioVerbose.setChecked(true);
				break;
			case RIL_LOG_INFO:
				radioInfo.setChecked(true);
				break;
			case RIL_LOG_WARN:
				radioWarn.setChecked(true);
				break;
			case RIL_LOG_CRITICAL:
				radioCritical.setChecked(true);
				break;
			default:
				break;
			}
		}
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				int oldlevel=RIL_LOG_EMPTY;
				try {
					String prop=SystemProperties.get(KEY_RIL_LEVEL);
					if(prop!=null && prop.length()>0){
						oldlevel=Integer.valueOf(prop);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

				int level=0;
				if(radioVerbose.isChecked()){
					level=1;
				}else if(radioInfo.isChecked()){
					level=2;
				}else if(radioWarn.isChecked()){
					level=4;
				}else if(radioCritical.isChecked()){
					level=8;
				}
				
				if(oldlevel!=level){
					SystemProperties.set(KEY_RIL_LEVEL, String.valueOf(level));
					sendBrocastForRilLog( mActivity, level);
				}
				
			}
		});
		dialog.setNegativeButton(android.R.string.cancel, null);
		dialog.show();
	}
	
	public static void sendBrocastForRilLog(Context context,int level) {
		Intent intent =new Intent(ASUS_LOGTOOL_SET_RADIO_LEVEL);
		intent.putExtra("level",  level);
		context.sendBroadcast(intent);
	}
	
	public void setCheckStateNoTrigger(Switch switch1,boolean enable){
		switch1.setOnCheckedChangeListener(null);
		switch1.setChecked(enable);
		switch1.setOnCheckedChangeListener(this);
	}
	
	public void initInstallAppUpdate()
	{
		setCheckStateNoTrigger(mKernalSwitch,getPropCheck(KEY_KERNEL));
		setCheckStateNoTrigger(mMainSwitch	,getPropCheck(KEY_MAIN));
		setCheckStateNoTrigger(mEventSwitch	,getPropCheck(KEY_EVENT));
		setCheckStateNoTrigger(mRadioSwitch	,getPropCheck(KEY_RADIO));
		setCheckStateNoTrigger(mCombineSwitch	,getPropCheck(KEY_COMBINE_ENABLE));
	}
	
	public static void forceState(boolean enable){
		setPropCheck(KEY_KERNEL, enable);
		setPropCheck(KEY_MAIN, 	enable);
		setPropCheck(KEY_EVENT, enable);
		setPropCheck(KEY_RADIO, enable);
		setPropCheck(KEY_COMBINE_ENABLE, enable);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			if(isDiskValidShowToast()==false){
				buttonView.setOnCheckedChangeListener(null);
				buttonView.setChecked(false);
				buttonView.setOnCheckedChangeListener(this);
				return;
			}
		}
		log("isChecked="+isChecked);
		if(buttonView==mKernalSwitch){
			setPropCheck(LOGCAT, isChecked);
		}
		if(buttonView==mEventSwitch){
			setPropCheck(LOGCAT_EVENTS, isChecked);
		}
		if(buttonView==mRadioSwitch){
			setPropCheck(LOGCAT_RADIO, isChecked);
		}
	}
	
	public static void log(String message){
		Log.v(TAG, message);
	}
	

}
