package com.asus.tool;

import com.asus.fdclogtool.R;

import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.Log;
import android.view.WindowManager;



public class DialogLogReceiver extends BroadcastReceiver {
    public static final String TAG = "AsusBootReceiver";
    public static final String ACTION_CRASH="asus.intent.action.crashlog";
    public static final String ACTION_DISK_LOW="asus.intent.action.diskspace_low";
    public static final String ACTION_CLOSE_LOG="asus.intent.action.diskspace_closelog";
    
    public static final String ACTION_INIT_LOG_CMD="asus.intent.action.fdcloginit";
	public static final String ACTION_MODEM_LOG_INIT_FAIL="com.asus.modemlog.initfail";
	public static final String ACTION_MODEM_LOG_PORT_NULL="com.asus.modemlog.portfail";
	
    private static boolean DEBUG=true;
    static AlertDialog malert;
    private static boolean mShowError=false;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
      if(DEBUG)
         Log.v(TAG, "onReceive action="+action);
  
      if(action.equals(ACTION_CRASH)){
    	  showDialog( context,"New Crash Log Path: "+ intent.getStringExtra("path"));
      }
      if(action.equals(ACTION_DISK_LOW)){
    	  String result=context.getResources().getString(R.string.message_run_out, Util.getReminderLowClose(DumpService.getLogRootpath()));
    	  showDialog( context,result);
      }
      if(action.equals(ACTION_CLOSE_LOG)){
    	  showDialog( context,"All running log is already closed.");
      }
      if(action.equals(ACTION_MODEM_LOG_INIT_FAIL) && mShowError){
    	  showDialog( context,"modem log is Init fail!!");
      }
      if(action.equals(ACTION_MODEM_LOG_PORT_NULL) && mShowError ){
    	  showDialog( context,"modem log serial port is empty,don`t get log");
      }
    }
    
    public static void showDialog(Context context,String message)
    {
    	if(malert!=null){
    		malert.dismiss();
    	}
    	
    	AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
    	mBuilder.setTitle("Warning");
    	Drawable dr = context.getResources().getDrawable(R.drawable.debug);
    	Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
    	// Scale it to 50 x 50
    	Drawable d = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, 64, 64, true));
    	mBuilder.setIcon(d);
    	mBuilder.setMessage(message);
   
    	mBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    	dialog.dismiss();
    	
    	}
    	});

    	
    	AlertDialog alert = mBuilder.create();
    	malert=alert;
    	alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//
    	alert.show();
    }

    public static void showDialog_next(Context context,String message)
    {
    	if(malert!=null){
    		malert.dismiss();
    	}
    	
    	AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
    	mBuilder.setTitle("prompt");
    	Drawable dr = context.getResources().getDrawable(R.drawable.debug);
    	Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
    
    	Drawable d = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, 64, 64, true));
    	mBuilder.setIcon(d);
    	mBuilder.setMessage(message);
   
    	mBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			dialog.dismiss();
			SystemProperties.set("persist.asuslog.fw.update", "1");
    			Util.setCmd("reboot recovery");
    		}
    	});

	mBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			dialog.dismiss();
			SystemProperties.set("persist.asuslog.fw.update", "2");
    		}
    	});

    	AlertDialog alert = mBuilder.create();
    	malert=alert;
    	alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//
    	alert.show();
    }
    
}
