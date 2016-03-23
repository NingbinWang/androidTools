package com.asus.log;

import java.io.File;

import com.asus.tool.BootReceiver;
import com.asus.tool.CmdReceiver;
import com.asus.tool.DebuggerMain;
import com.asus.tool.Define;
import com.asus.tool.PhoneStateReceiver;
import com.asus.tool.SerialPortControl;
import com.asus.tool.Settings;
import com.asus.tool.Util;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ModemLogService extends Service  implements Handler.Callback{

	
	
	private static final String TAG = "ModemLogService";
	private static final boolean DEBUG = true;
	private Handler mMainHandler=new Handler(this);
	private SerialPortControl mSerialPortControl;
	private ModemLogTrigger mTrigger;
	private int mFlag=0;
	private String mAction="";
	@Override
	public void onCreate() {
		
		super.onCreate();
		
	}

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void onDestroy() {
		if(mSerialPortControl!=null){
			log("serial port normal close!!!");
			mSerialPortControl.close();
			mSerialPortControl=null;
		}
		super.onDestroy();
		log("onDestroy");
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log("onStartCommand action="+intent.getAction());
		if(ModemLogTrigger.isDeviceExist()==false)
		{
			log("gsmtty19 died");
			stopSelf();
			return START_NOT_STICKY;
		}
		if(intent.getAction()==null){
			stopSelf();
			return START_NOT_STICKY;
		}
		log("packagename="+getApplicationContext().getPackageName());
		SharedPreferences pref = getApplicationContext().getSharedPreferences(DebuggerMain.NAME_SHARE_PREF, Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS );
		Settings.init(pref);
		log("isAutoPcm="+Settings.isAutoPcm()+"mSerialPortControl="+mSerialPortControl);
		
		if(mSerialPortControl==null){
			mSerialPortControl=new SerialPortControl(this, mMainHandler);
			
			boolean serial=mSerialPortControl.openSericalPort();
			log("serial="+serial);
			if(serial==false){
				mSerialPortControl.close();
				mSerialPortControl=null;
				stopSelf();
				return START_NOT_STICKY;
			}else{
				mTrigger=new ModemLogTrigger(this, mSerialPortControl, false, mMainHandler);
			}
			if(intent.getAction().equals(BootReceiver.ACTION_MODEM_STARTLOG)){
				log("openLog service");
				mTrigger.openLog();
			}else if(intent.getAction().equals(PhoneStateReceiver.ACTION_PHONE_I2CS)){
				log("openI2C service");
				mTrigger.openI2C();
			}else if(intent.getAction().equals(BootReceiver.ACTION_MODEM_CHECK_RESTART)){
				log("checkLogAndRestart");
				mTrigger.checkLogAndRestart();
			}
			else {
				stopSelf();
			}
		}else{
			if(mSerialPortControl!=null){
				mSerialPortControl.close();
				mSerialPortControl=null;
			}
			
			stopSelf();
		}
		
		return START_NOT_STICKY;
	}

	private void log(String msg){
		if(DEBUG)
			Log.v(TAG, msg);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case SerialPortControl.EVENT_RESULT_SUCCESS:
			log("DumpServer EVENT_RESULT_SUCCESS receiver");
			if(mTrigger!=null){
				mTrigger.onHandleCmdCallBack(msg);
			}
			
			break;
		case SerialPortControl.EVENT_RESULT_FAIL:
			log("DumpServer EVENT_RESULT_FAIL receiver");
			if(mTrigger!=null){
				mTrigger.onHandleCmdCallBack(msg);
			}
			break;
		case SerialPortControl.EVENT_SERIAL_NULL:
		case ModemLog.MSG_CLOSE_HANDLE:
			close();
			break;
		case SerialPortControl.EVENT_TIME_OUT_NO_RESPONSE:
			Intent intent=new Intent(CmdReceiver.ACTION_MODEM_NO_RESPONSE_TIME_OUT);
			ModemLogService.this.sendBroadcast(intent);
			close();
			break;
		default:
			break;
		}
		
		return false;
	}
	
	private void close(){
		if(mSerialPortControl!=null){
			log("serial port normal close!!!");
			mSerialPortControl.close();
			mSerialPortControl=null;
		}
		mTrigger=null;
		mMainHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopSelf();
			}
		}, 100);
		
	}
	
}
