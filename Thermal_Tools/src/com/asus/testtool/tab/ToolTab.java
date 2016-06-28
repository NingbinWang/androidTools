package com.asus.testtool.tab;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.asus.testtool.R;
import com.asus.testtool.tool.BatThread;
import com.asus.testtool.tool.PadBatThread;
import com.asus.testtool.tool.LogThread;
import com.asus.testtool.tool.Monitor;
import com.asus.testtool.tool.MonitorService;
import com.asus.testtool.tool.StayonService;
import com.asus.testtool.tool.Timer;
import com.nea.nehe.lesson08.Lesson08;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Debug;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ToolTab extends Activity implements OnCheckedChangeListener, OnClickListener{

	/** Called when the activity is first created. */
	private GLSurfaceView glSurfaceView;
	public static String FILE_NAME = "timer_log";
	
	ToggleButton timerButton, logButton, stayonButton, batButton, pad_batButton;
	TextView timerCurrentOutput, timerHistoryOutput;
	Button monitorButton;
	
	Timer mTimer ;
	LogThread mLogThread;
	BatThread mBatThread;
	PadBatThread mPadBatThread;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tool_tab);
		
		
		timerButton = (ToggleButton) findViewById(R.id.button_timer);
		timerButton.setOnCheckedChangeListener(this);
		
		
		
		stayonButton = (ToggleButton) findViewById(R.id.button_stayon);
		stayonButton.setOnClickListener(this);
		
		if(serviceIsRunning("com.asus.testtool.tool.StayonService"))
			stayonButton.setChecked(restorePrefs("STAY_ON"));
		else
			storePrefs("STAY_ON", false);
		
		timerCurrentOutput = (TextView) findViewById(R.id.text_timer_current_output);
		timerHistoryOutput = (TextView) findViewById(R.id.text_timer_history_output);
//		loadTimeLog();
		
		logButton = (ToggleButton) findViewById(R.id.button_log);
		logButton.setOnCheckedChangeListener(this);
		
		batButton = (ToggleButton) findViewById(R.id.button_bat);
		batButton.setOnCheckedChangeListener(this);
		
		pad_batButton = (ToggleButton) findViewById(R.id.button_pad_bat);
		pad_batButton.setOnCheckedChangeListener(this);		
		
		restoreTimePrefs();
		
		monitorButton = (Button) findViewById(R.id.button_monitor);
		monitorButton.setOnClickListener(this);
		
		batButton = (ToggleButton) findViewById(R.id.button_bat);
		batButton.setOnCheckedChangeListener(this);
	}
	
    @Override
	protected void onStop() {
		super.onStop();	
//		if(mTimer != null && !mTimer.isCancelled())
//			mTimer.cancel(true);
//    	
//		mTimer = null ;
    }	
	//@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch(buttonView.getId()){
		case R.id.button_timer:
			
			if(isChecked){
				mTimer = new Timer(this , FILE_NAME);
				if(!mTimer.initial())
					Toast.makeText(ToolTab.this,this.getString(R.string.text_error),Toast.LENGTH_LONG).show();
				mTimer.execute();
			}else{
				restoreTimePrefs();
				mTimer.cancel(true);
				mTimer = null ;
			}
			break;
		case R.id.button_log:
//			Log.d("TEST", (TextView) findViewById(R.id.text_cpu_usage).te);
			if(isChecked){
				
				mLogThread = new LogThread();
				mLogThread.start();
				Toast.makeText(ToolTab.this,this.getString(R.string.text_log_start),Toast.LENGTH_LONG).show();
			}
			else{
				mLogThread.setOnLog(false);
				
				
			}
			break;
		case R.id.button_bat:
			if(isChecked){
				
				mBatThread = new BatThread();
				mBatThread.start();
				Toast.makeText(ToolTab.this,this.getString(R.string.text_bat_start),Toast.LENGTH_LONG).show();
			}
			else{
				mBatThread.setOnLog(false);
				
				
			}
			break;
		
		case R.id.button_pad_bat:
			if(isChecked){
			
				mPadBatThread = new PadBatThread();
				mPadBatThread.start();
				Toast.makeText(ToolTab.this,this.getString(R.string.text_pad_bat_start),Toast.LENGTH_LONG).show();
			}
			else{
				mPadBatThread.setOnLog(false);
			
			
			}
			break;
		}
		
	}
	
	public void updateTimer(String arg){
				
		timerCurrentOutput.setText(arg);
		storeTimePrefs(arg) ;
	}
	

	private boolean restorePrefs(String issue) {
		SharedPreferences settings = getSharedPreferences("PREF_DATA", 0);
		return settings.getBoolean( issue , false);
	}
	private void storePrefs(String issue , boolean checked) {
		SharedPreferences settings = getSharedPreferences("PREF_DATA", 0);
		settings.edit()
			.putBoolean(issue, checked)
			.commit();
	}
	
	private void restoreTimePrefs() {
		SharedPreferences settings = getSharedPreferences("PREF_DATA", 0);
		String timeLog = settings.getString("TIME_LOG", "");
		timerHistoryOutput.setText(timeLog);
	}
	private void storeTimePrefs(String time) {
		SharedPreferences settings = getSharedPreferences("PREF_DATA", 0);
		settings.edit()
			.putString("TIME_LOG", time)
			.commit();
	}

	//@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.button_monitor: 
			Intent monitorIntent = new Intent();
			monitorIntent.setClass(ToolTab.this, Monitor.class);
			startActivity(monitorIntent);	
			break;
		case R.id.button_stayon:
			if(stayonButton.isChecked()){
				storePrefs("STAY_ON",true);
				Intent intent = new Intent(this, StayonService.class);
		        startService(intent);
			}
			else{ 
				storePrefs("STAY_ON",false);
				Intent intent = new Intent(this, StayonService.class);	
				stopService(intent);
				
			}
				
		
		}
	}
	
	
    public boolean serviceIsRunning(String service){
    	
        ActivityManager mActivityManager =       
            (ActivityManager)getSystemService(ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> mServiceList = mActivityManager.getRunningServices(50);             
        final String serviceClassName = service ;      
                    
 
        for(int i = 0; i < mServiceList.size(); i ++){      
            if(serviceClassName.equals(mServiceList.get(i).service.getClassName())){      
                return true;      
            }      
        }      
        return false;    
    }

}
