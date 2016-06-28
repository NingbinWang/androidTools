package com.asus.testtool.tool;


import java.lang.reflect.Array;
import java.util.List;

import com.asus.testtool.R;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class Monitor extends Activity implements  OnClickListener {
    /** Called when the activity is first created. */
	
	public boolean [] items = {false,false,false};
	
	private static String CPU_THERMAL = "CPU_THERMAL";
	private static String PMIC_THERMAL = "PMIC_THERMAL";
	private static String BATTERY_THERMAL = "BATTERY_THERMAL";
	
	private CheckBox cpuThermalCB ;   
	private CheckBox pmicThermalCB ;
	private CheckBox batteryThermalCB ;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor);
        
        cpuThermalCB = (CheckBox) findViewById(R.id.cpu_thermal_cb);
        pmicThermalCB = (CheckBox) findViewById(R.id.pmic_thermal_cb);
        batteryThermalCB = (CheckBox) findViewById(R.id.battery_thermal_cb);
        if(serviceIsRunning()){
	        cpuThermalCB.setChecked(restorePrefs(CPU_THERMAL));
	        pmicThermalCB.setChecked(restorePrefs(PMIC_THERMAL));
	        batteryThermalCB.setChecked(restorePrefs(BATTERY_THERMAL));
	        
	        items[0]=restorePrefs(CPU_THERMAL);
	        items[1]=restorePrefs(PMIC_THERMAL);
	        items[2]=restorePrefs(BATTERY_THERMAL);
        }
        else{
        	storePrefs(CPU_THERMAL,false);
        	storePrefs(PMIC_THERMAL,false);
        	storePrefs(BATTERY_THERMAL,false); 
        	
        	
        }
        
        cpuThermalCB.setOnClickListener(this);
        pmicThermalCB.setOnClickListener(this);
        batteryThermalCB.setOnClickListener(this);
        
    }
    
    public void updateMessage(String info){
    	CharSequence aa;

        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);                

        Intent notifyIntent = new Intent(this,Monitor.class); 
        notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
        
        PendingIntent appIntent=PendingIntent.getActivity(this,0,
                                                          notifyIntent,0);
        Notification notification = new Notification();

        notification.icon=R.drawable.icon;

        notification.tickerText = info;

        notification.defaults=Notification.DEFAULT_LIGHTS;

        notification.setLatestEventInfo(this,"Title","content",appIntent);

        notificationManager.notify(0,notification);    
    	
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder mBinder) {
        	Log.d("AsusTestTool", "Monitor : onServiceConnected");
        	MonitorService mThreadzService = ((MonitorService.LocalBinder) mBinder).getService();
        	mThreadzService.setActivity(Monitor.this);     
        	mThreadzService.setTestItems(items);
        }

        public void onServiceDisconnected(ComponentName name) {
        	Log.d("AsusTestTool", "Monitor : onServiceDisconnected");
        }
    };

	
    public void setupService(){
    	if(serviceIsRunning()){
			Intent intent = new Intent(this, MonitorService.class);
	        startService(intent);
	        try {
				unbindService(mConnection);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	        Log.d("AsusTestTool", "Monitor : Setup monitor service " ); 
    	}
    	else{
			Intent intent = new Intent(this, MonitorService.class);
	        startService(intent);
	        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	        Log.d("AsusTestTool", "Monitor : Setup monitor service " );    		
    		
    	}
    }    
    
    public void endService(){
		Intent intent = new Intent(this, MonitorService.class);	
        try {
			unbindService(mConnection);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopService(intent);
		Log.d("AsusTestTool", "Monitor : end monitor service " ); 
    }
	
    public boolean serviceIsRunning(){
    	
        ActivityManager mActivityManager =       
            (ActivityManager)getSystemService(ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> mServiceList = mActivityManager.getRunningServices(50);             
        final String serviceClassName = "com.asus.testtool.tool.MonitorService";      
                    
 
        for(int i = 0; i < mServiceList.size(); i ++){      
            if(serviceClassName.equals(mServiceList.get(i).service.getClassName())){      
                return true;      
            }      
        }      
        return false;    
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

	//@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.cpu_thermal_cb:
			storePrefs(CPU_THERMAL,cpuThermalCB.isChecked());
			items[0]=cpuThermalCB.isChecked();
			resetService();
			break;
		case R.id.pmic_thermal_cb:
			storePrefs(PMIC_THERMAL,pmicThermalCB.isChecked());
			items[1]=pmicThermalCB.isChecked();
			resetService();
	
			break;
		case R.id.battery_thermal_cb:
			storePrefs(BATTERY_THERMAL,batteryThermalCB.isChecked());
			items[2]=batteryThermalCB.isChecked();
			resetService();	
			break;			
		
		}
	}



	private void resetService() {
		// TODO Auto-generated method stub
		Log.d("AsusTestTool", "Monitor : reset service " );
		if(testIsRunning())
			setupService();
		else
			endService();
			
	}

	private boolean testIsRunning(){
		
		if(restorePrefs(CPU_THERMAL)||restorePrefs(PMIC_THERMAL)||restorePrefs(BATTERY_THERMAL))
			return true;
		return false;
	}
    
    
}