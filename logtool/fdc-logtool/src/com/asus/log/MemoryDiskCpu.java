package com.asus.log;

import java.util.zip.Inflater;

import com.asus.tool.DebuggerMain;
import com.asus.tool.Settings;
import com.asus.fdclogtool.R;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MemoryDiskCpu extends BaseLog implements OnCheckedChangeListener{
	//MemoryDiskCpu
	private Switch mMemInfoSwitch;
	private Switch mDiskSwitch;
	private Switch mProCaneSwitch;
	private Switch mCpuSwitch;
	private Switch mCpuLoadingSwitch;
	private Switch mTopSwitch;
	public MemoryDiskCpu(Activity activity, View view) {
		super(activity, view);
		mMemInfoSwitch	=(Switch) view.findViewById(R.id.meminfo_switch_id);
		mDiskSwitch	=(Switch) view.findViewById(R.id.disk_switch_id);
		mProCaneSwitch=(Switch) view.findViewById(R.id.procrank_switch_id);
		mCpuSwitch=(Switch) view.findViewById(R.id.cpu_switch_id);
		mCpuLoadingSwitch=(Switch) view.findViewById(R.id.cpuloading_switch_id);
		mTopSwitch=(Switch) view.findViewById(R.id.top_switch_id);
		mMemInfoSwitch.setChecked(Settings.isMemoinfoEnable());
		mDiskSwitch.setChecked(Settings.isDiskLogEnable());
		mProCaneSwitch.setChecked(Settings.isProcaneLogEnable());
		mCpuSwitch.setChecked(Settings.isCpuLogEnable());
		mCpuLoadingSwitch.setChecked(Settings.isCpuLoadingEnable());
		mTopSwitch.setChecked(Settings.isTopEnable());
		view.findViewById(R.id.meminfo_layout).setOnClickListener(this);
		view.findViewById(R.id.disk_layout).setOnClickListener(this);
		view.findViewById(R.id.procrank_layout).setOnClickListener(this);
		view.findViewById(R.id.cpu_layout).setOnClickListener(this);
		view.findViewById(R.id.cpuloading_layout).setOnClickListener(this);
		view.findViewById(R.id.top_layout).setOnClickListener(this);
		
		mMemInfoSwitch.setOnCheckedChangeListener(this);
		mDiskSwitch.setOnCheckedChangeListener(this);
		mProCaneSwitch.setOnCheckedChangeListener(this);
		mCpuSwitch.setOnCheckedChangeListener(this);
		mCpuLoadingSwitch.setOnCheckedChangeListener(this);
		mTopSwitch.setOnCheckedChangeListener(this);
	}

	public static void forceStop(){
		Settings.setMeminfoEnable(false);
		Settings.setWindowEnable(false);
		Settings.setDiskEnable(false);
		Settings.setActivityEnable(false);
		Settings.setCpuEnable(false);
		Settings.setCpuLoadingEnable(false);
		Settings.setTop(false);
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) 
		{
		case R.id.meminfo_layout:
			boolean state=mMemInfoSwitch.isChecked();
			mMemInfoSwitch.setChecked(state=!state);
			break;
		case R.id.disk_layout:
			state=mDiskSwitch.isChecked();
			mDiskSwitch.setChecked(state=!state);
			break;
		case R.id.procrank_layout:
			state=mProCaneSwitch.isChecked();
			mProCaneSwitch.setChecked(state=!state);
			break;
		case R.id.cpu_layout:
			state=mCpuSwitch.isChecked();
			mCpuSwitch.setChecked(state=!state);
			break;
		case R.id.top_layout:
			state=mTopSwitch.isChecked();
			mTopSwitch.setChecked(state=!state);
			break;
		case R.id.cpuloading_layout:
			AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(mActivity);
			dialogBuilder.setTitle("cpu loading time intervel");
			LayoutInflater inflater=LayoutInflater.from(mActivity);
			View view=inflater.inflate(R.layout.select_time_intervel, null);
			dialogBuilder.setView(view);
			TextView textView=(TextView)view.findViewById(R.id.titletime);
			int second=Settings.getCpuLoadIntervelTime();
			int hour=second/3600;
			String time="";
			if(hour>0){
				second-=hour*3600;
				time=hour+" h ";
				
			}
			int minute=second/60;
			if(minute>0){
				second-=minute*60;
				time+=minute+" m ";
			}
			
			if(second>0){
				time+=second+" s ";
			}
			
			String title=mActivity.getString(R.string.time_intervel_default_title); ;
			
			textView.setText(title+" "+time);
			final EditText editTextHour=(EditText) view.findViewById(R.id.hour_edit);
			final EditText editTextMin=(EditText)view.findViewById(R.id.minute_edit);
			final EditText editTextSec=(EditText)view.findViewById(R.id.second_edit);
			
			dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					int second=0;
					if(editTextSec.getText().toString().length()>0){
						second+=Integer.valueOf(editTextSec.getText().toString());
					}
					if(editTextMin.getText().toString().length()>0){
						second+=Integer.valueOf(editTextMin.getText().toString())*60;
					}
					if(editTextHour.getText().toString().length()>0){
						second+=Integer.valueOf(editTextHour.getText().toString())*60*60;
					}
					if(second!=0){
						Settings.setCpuLoadIntervelTime(second);
						if(Settings.isCpuLoadingEnable()){
							try {
								mServiceLog.cpuLoadingChangeTime();
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
						
					}
					
				}
			});
			dialogBuilder.setNegativeButton("Cancel", null);
			dialogBuilder.show();
//			state=mCpuLoadingSwitch.isChecked();
//			mCpuLoadingSwitch.setChecked(state=!state);
			break;
		default:
			break;
		}
	}
	
	
	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		super.onSelectAll();
		mMemInfoSwitch.setChecked(true);
		mDiskSwitch.setChecked(true);
		mProCaneSwitch.setChecked(true);
		mCpuSwitch.setChecked(true);
		mCpuLoadingSwitch.setChecked(true);
		mTopSwitch.setChecked(true);
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
		mMemInfoSwitch.setChecked(false);
		mDiskSwitch.setChecked(false);
		mProCaneSwitch.setChecked(false);
		mCpuSwitch.setChecked(false);
		mCpuLoadingSwitch.setChecked(false);
		mTopSwitch.setChecked(false);
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
		if(buttonView==mMemInfoSwitch){
			Settings.setMeminfoEnable(isChecked);
			try {
				mServiceLog.meminfoLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		
		if(buttonView==mDiskSwitch){
			Settings.setDiskEnable(isChecked);
			try {
				mServiceLog.diskLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		
		if(buttonView==mTopSwitch){
			Settings.setTop(isChecked);
			try {
				mServiceLog.topLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		
		if(buttonView==mProCaneSwitch){
			Settings.setProcaneEnable(isChecked);
			try {
				mServiceLog.procaneLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		
		if(buttonView==mCpuLoadingSwitch){
			Settings.setCpuLoadingEnable(isChecked);
			try {
				mServiceLog.cpuLoadingEnable(isChecked);
			} catch (RemoteException e) {
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		
		if(buttonView==mCpuSwitch){
			Settings.setCpuEnable(isChecked);
			try {
				mServiceLog.cpuLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
	}

}
