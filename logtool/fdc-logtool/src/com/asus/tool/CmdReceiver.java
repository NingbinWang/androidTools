package com.asus.tool;

import java.io.File;

import com.asus.log.ModemLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;

public class CmdReceiver extends BroadcastReceiver{

	private static final String TAG = "CmdReceiver";
	private static final String ACTION_APP_LOCK="asus.intent.action.monkeytest_lock";
	private static final String ACTION_APP_UNLOCK="asus.intent.action.monkeytest_unlock";
	 
	private static final String ACTION_APP_STARTSERVICE="asus.intent.action.startService";
	private static final String ACTION_LOG_UPLOAD_FINISH="com.asus.packlogs.completed";
	public static final String ACTION_DELETE_START="com.asus.deleteLog.start";
	public static final String ACTION_DELETE_END="com.asus.deleteLog.completed";
	public static final String ACTION_INIT_LOG_CMD="asus.intent.action.fdcloginit";
	public static final String ACTION_MODEM_NO_RESPONSE_TIME_OUT="asus.intent.action.modem_time_out";
	public static final String ACTION_TEST_CMD="com.asus.test.cmd";
	@Override
	public void onReceive(Context context, Intent intent) {
		String action=intent.getAction();
		SharedPreferences pref = context.getSharedPreferences(DebuggerMain.NAME_SHARE_PREF,  Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
		Settings.init(pref);
		
		if(action.equals(ACTION_APP_LOCK)){
			Settings.setAllowAppOpen(false);
			Toast.makeText(context, "No Allow App  Open", Toast.LENGTH_SHORT).show();
		}else if(action.equals(ACTION_APP_UNLOCK)){
			Toast.makeText(context, "Allow App Open", Toast.LENGTH_SHORT).show();
			Settings.setAllowAppOpen(true);
		}
		
		if(action.equals(ACTION_APP_STARTSERVICE)){
			Intent dockIntent = new Intent(context, DumpService.class);
		    context.startService(dockIntent);
		}
		
		if(action.equals(ACTION_DELETE_START)){
			Toast.makeText(context, "Delete log start....", Toast.LENGTH_SHORT).show();
		}
		
		if(action.equals(ACTION_DELETE_END)){
			Toast.makeText(context, "Delete log end....", Toast.LENGTH_SHORT).show();
		}
		
		if(action.equals(ACTION_INIT_LOG_CMD)){
			Log.v(TAG, "ACTION_INIT_LOG_CMD");
			DebuggerMain.startInitServer(context); 
		}
		
		if(action.equals(ACTION_MODEM_NO_RESPONSE_TIME_OUT)){
			Toast.makeText(context, "log tool->Modem No Response Time Out", Toast.LENGTH_LONG).show();
		}
		
		if(action.equals(ACTION_TEST_CMD)){
			
		}
		
	}

}
