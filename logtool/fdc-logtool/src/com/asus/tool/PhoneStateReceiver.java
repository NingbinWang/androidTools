package com.asus.tool;

import java.io.File;

import com.asus.log.BaseLog;
import com.asus.log.ModemLogService;
import com.asus.log.ModemLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneStateReceiver extends BroadcastReceiver{

	private static final boolean DEBUG = true;
	private static final String TAG = "SerialPort:PhoneStateReceiver";
	public static final String ACTION_PHONE_I2CS="asus.intent.action.phone.i2cs";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){//out-going
			log("-----OutGoing Phone");
			openLog( context);
			return;
		}
		
		//1.step call in 1EXTRA_STATE_RINGING incomingNumber 09100xxxx
		//				2.EXTRA_STATE_OFFHOOK incomingNumber null
		//2.step call out 1.EXTRA_STATE_OFFHOOK incomingNumber null
		
		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		
	    // If an incoming call arrives
	    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)  )//in-incoming
	    {
	    	String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
	    	if(incomingNumber!=null){
	    		log("-----InComing Phone");
	    		openLog(context);
	    	}
	    	
	    }
	    
	   
	}
	
	public void openLog(Context context)
	{
    	SharedPreferences pref = context.getSharedPreferences(DebuggerMain.NAME_SHARE_PREF,Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
		Settings.init(pref);
		
		if(Settings.isAutoI2S() && BaseLog.getPropCheck(ModemLog.KEY_MODEM_OFFLINE_LOGGING))
    	{
    		Intent i2sService = new Intent(context, ModemLogService.class);
    		i2sService.setAction(ACTION_PHONE_I2CS);
		    context.startService(i2sService);
		}
	}
	
	
	private void log(String msg){
		if(DEBUG){
			Log.v(TAG, msg);
		}
	}
}
