package com.asus.testtool.tool;

import com.asus.testtool.utils.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class MonitorTask extends AsyncTask<Void, String, Void> {

    private Monitor mActivity;
	private boolean [] items = {false,false,false};
    public MonitorTask() {

    }

    // Can change activity this task points to.
    // e.g. when activity recreated after orientation change.
    public void setActivity(Monitor activity) {
        mActivity = activity;
    }
    public void setTestItems(boolean [] items){
    	this.items = items;
    	
    }
    // Runs on main thread.
    @Override
    protected void onPreExecute() {
    }

    // Runs on main thread.
    @Override
    protected void onProgressUpdate(String... test) {
        if (mActivity != null) {
            mActivity.updateMessage(test[0].toString());
        }
    }

    // Runs on main thread.
    @Override
    protected void onPostExecute(Void result) {
    }

    // Runs in separate thread.
    @Override
    protected Void doInBackground(Void... params) {
    	Log.d("AsusTestTool", "Monitor : task run" );
        while (!isCancelled()) {
        	

        //old    
//            String temp ="";
//            String temp1 = Utils.parseThermal(Utils.readInfo("/sys/devices/platform/msm_adc/msm_therm"));
//            String temp2 = Utils.parseThermal(Utils.readInfo("/sys/devices/platform/msm_adc/pmic_therm"));
//            String temp3 = Utils.readInfo("/sys/class/power_supply/battery/temp");
//            
//        	if(items[0])

//        	if(items[1])

//        	if(items[2])

        	
          
            //new
            String temp = "";
		/*
            float pmicTemp;
            float batteryTemp;

            try {
                pmicTemp = Float.valueOf(Utils.static_PmicThermal) / 1000;
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                pmicTemp = -1;
            }

            try {
                batteryTemp = Float.valueOf(Utils.static_BatteryThermal) / 10;
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                batteryTemp = -1;
            }
            */
            if (items[0])
                temp += "CPU:" + Utils.static_Cpu0Thermal + " C | ";
            /*if (items[1])

            if (items[2])
*/
            
            
        	publishProgress(temp);
        	
            try {
                Thread.sleep(3000);
                
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        }
    	Log.d("AsusTestTool", "Monitor : task cancel" );
        return null;
    }
  

}
