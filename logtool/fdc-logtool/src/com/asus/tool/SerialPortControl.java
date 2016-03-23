package com.asus.tool;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.asus.log.ModemLogTrigger;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.hardware.SerialManager;
import android.hardware.SerialPort;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.widget.Toast;

public class SerialPortControl implements Runnable{
	private SerialManager mSerialManager;
	private ByteBuffer mInputBuffer;
	private ByteBuffer mOutputBuffer;
	
	private SerialPort mSerialPort;
	private static final int SERIAL_BUFFER=1024;
	private static final String TAG = "SerialPort";
	
	private Context mContext;
	/* EVENT */
	static final String AT_END="\r\n";
	public static final int EVENT_GET_AT_RESULT = 10;
	public static final int EVENT_SERIAL_NULL= 11;
	public static final int EVENT_RESULT_FAIL= 12;
	public static final int EVENT_RESULT_SUCCESS= 13;
	public static final int EVENT_RESULT_NO_CHECK= 14;
	public static final int EVENT_TIME_OUT_NO_RESPONSE= 15;
	public static final String COMMAND= "COMMAND";
	private Handler mMainHandler;
	private String mCommand;
	private static final int REPEAT_DEFAULT=5;
	private int mMaxTime=REPEAT_DEFAULT;
	private String mSuccessKey="";
	private int mCurrentSendTimes=0;
	private int mMode=0;
	private boolean mClose=false;
	private Thread mThread;
	private boolean mRepeate=true;
	private boolean mCheckAlreadyDelay=true;
	private String mMessage="";
	private Handler mTimeOutHandler=new Handler();
	public SerialPortControl(Context context,Handler handler){
		mContext=context;
		mSerialManager = (SerialManager) mContext.getSystemService(ATCommandActivity.SERIAL_SERVICE);
		mInputBuffer  =  ByteBuffer.allocate(SERIAL_BUFFER);
		mOutputBuffer =  ByteBuffer.allocate(SERIAL_BUFFER);
		mMainHandler=handler;
	}
	
	public void setRepeatMaxTimes(int time){
		mMaxTime=time;
	}
	
	 
	
	public synchronized boolean sendCommand(Handler handler,String command,String successKey,int mode,boolean repeat,boolean check){
		if(ModemLogTrigger.isDeviceExist()==false){
			log("sendCommand gsmtty19 died");
			return false;
		}
		log("sendCommand "+command);
		mRepeate=repeat;
		mCheckAlreadyDelay=check;
		mCommand=command;
		mSuccessKey= successKey;
		mCurrentSendTimes=0;
		mMode=mode;
		mMainHandler=handler;
		try {
			String cmd =mCommand+AT_END;
			byte[] buffer = cmd.getBytes();
			
			mInputBuffer.clear();
			mInputBuffer.put(buffer);
			if(mSerialPort!=null){
				mSerialPort.write(mInputBuffer, buffer.length);
			}
			mTimeOutHandler.postDelayed(mTimeOutRunnable, 2000);
		} catch (IOException e) {
			Log.e(TAG, "Write Failed", e);
			Toast.makeText(mContext, "fail please retry", Toast.LENGTH_SHORT).show();
			return false;
		}
		mCurrentSendTimes=1;
		return true;
	}
	
	private  Runnable mTimeOutRunnable=new Runnable() {
		@Override
		public void run() {
			log("ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
			log("Modem Response Time Out");
			log("ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
			log("ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
			sendTimeOut();
		}
	};
	
	public synchronized void sendRepeatCommand(){
		log("sendRepeatCommand mCommand"+mCommand);
		if(ModemLogTrigger.isDeviceExist()==false){
			log("sendRepeatCommand gsmtty19 died");
			return ;
		}
		try {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String cmd =mCommand+AT_END;
			byte[] buffer = cmd.getBytes();
			
			mInputBuffer.clear();
			mInputBuffer.put(buffer);
			if(mSerialPort!=null){
				mSerialPort.write(mInputBuffer, buffer.length);
			}
			mTimeOutHandler.postDelayed(mTimeOutRunnable, 2000);
		} catch (IOException e) {
			Log.e(TAG, "Write Failed", e);
			Toast.makeText(mContext, "fail please retry", Toast.LENGTH_SHORT).show();
			
		}
		
	}
	
	public boolean openSericalPort()
	{
		if(mSerialManager==null){
			Toast.makeText(mContext, "SerialManager Empty", Toast.LENGTH_LONG).show();
			return false;
		}
		String[] ports = mSerialManager.getSerialPorts();
		log("ports="+ports.length);
		if (ports != null && ports.length > 0) {
			try {
				mSerialPort = mSerialManager.openSerialPort(ports[0], 115200);
				if (mSerialPort != null) {
					mThread=new Thread(this);
					mThread.start();
				}
				log("mSerialPort="+mSerialPort);
			} catch (IOException e) {
				Toast.makeText(mContext, "Open Port Failed "+e.getMessage(), Toast.LENGTH_LONG).show();
				return false;
			}
		}
		if(ports==null || ports.length==0){
			Toast.makeText(mContext, "modem don't any port", Toast.LENGTH_LONG).show();
			mMainHandler.sendEmptyMessage(EVENT_SERIAL_NULL);
			return false;
		}
		return true;
	}
	
	public void sendLeave(){
    	String command="AT";
    	if(ModemLogTrigger.isDeviceExist()==false){
			Toast.makeText(mContext, "serial port null ,please reboot", Toast.LENGTH_SHORT).show();
    		return ;
		}

    	Log.d (TAG, "AT Command : " + command);
    	command = command + "\r\n";
    	
    	byte[] buffer = command.getBytes();
    	
    	mInputBuffer.clear();
    	mInputBuffer.put(buffer);
    	try {
    		mSerialPort.write (mInputBuffer, buffer.length);
    	} catch (IOException e) {
    		Log.e (TAG, "Write Failed", e);
    	}
    }
	
	public void close(){
		mClose=true;
		mMainHandler=null;
		if (mSerialPort != null) {
			sendLeave();
			try {
				mSerialPort.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSerialPort = null;
		}
		
	}
	
	public void run() {
		Log.d(TAG, "thread run");
		int ret = 0;
		byte[] buffer = new byte[1024];

		while (ret >= 0 ) {
			if(mClose){
				return;
			}
			try {
				mOutputBuffer.clear();
				if (mSerialPort == null) {
					//onHandleSerialEmpty();
					mHandler.sendEmptyMessage(EVENT_SERIAL_NULL);
					if(mClose){
						return;
					}
					
					break;
				}
				ret = mSerialPort.read(mOutputBuffer);
				if(mClose){
					return;
				}
				if(mMainHandler==null){
					return;
				}
				mOutputBuffer.get(buffer, 0, ret);
			} catch (IOException e) {
				Log.e(TAG, "Read Port Failed", e);
				break;
			}

			if (ret > 0) {
				Message m = Message.obtain(mHandler, EVENT_GET_AT_RESULT);
				String text = new String(buffer, 0, ret);
				if(text.equals("\r\n")==false){
					m.obj = text;
					mHandler.sendMessage(m);
				}
				
			}
		}
		Log.d(TAG, "thread out");
	}

	private void log(String msg){
		Log.v(TAG, msg);
	}
	
	private void sendFail(Message msg){
		Message msgMessage =new Message();
		msgMessage.what=EVENT_RESULT_FAIL;
		msgMessage.obj=msg.obj;
		msgMessage.arg1=mMode;
		Bundle bundle=new Bundle();
		bundle.putString(COMMAND, mCommand);
		log("send fail");
		msgMessage.setData(bundle);
		mMainHandler.sendMessage(msgMessage);
	}
	
	
	private void sendTimeOut(){
		
		mMainHandler.sendEmptyMessage(EVENT_TIME_OUT_NO_RESPONSE);
	}
	
	
	
	private void sendSuccess(Message msg){
		Message msgMessage =new Message();
		msgMessage.what=EVENT_RESULT_SUCCESS;
		msgMessage.obj=msg.obj;
		msgMessage.arg1=mMode;
		Bundle bundle=new Bundle();
		bundle.putString(COMMAND, mCommand);
		msgMessage.setData(bundle);
		log("send success");
		if(mMainHandler!=null){
			mMainHandler.sendMessage(msgMessage);
		}else{
			log("AndroidRuntime mMainHandler NULL");
		}
		
	}
	
	Runnable mRunnable =new Runnable() {
		
		@Override
		public void run() {
			Message msg =new Message();
			msg.obj=mMessage;
			sendNext( mMessage, msg);
		}
	};
	
	public void sendNext(String result,Message msg){
		if(result.contains(mSuccessKey)){
			log("-------------sendSuccess");
			sendSuccess(msg);
			
		}else if(mCurrentSendTimes==mMaxTime)
		{
			sendFail( msg);
		}else{
			//sendSuccess(msg);
			if(mRepeate){
				log("-------------fail repeat send cmd");
				mCurrentSendTimes++;
				if(mSerialPort!=null){
					sendRepeatCommand();
				}
			}else{
				log("-------------no repeat sendfail");
				sendFail( msg);	
			}	    				
		}
	}
	
	 Handler mHandler = new Handler () {
	    	@Override
	    	public void handleMessage (Message msg) {
	    		if(mMainHandler==null){
	    			return;
	    			
	    		}
	    		mTimeOutHandler.removeCallbacks(mTimeOutRunnable);
	    		switch (msg.what) {
	    		case EVENT_GET_AT_RESULT:
	    			//mATResult.setText(mATResult.getText() + (String)msg.obj);
	    			String result=(String) msg.obj;
	    			
	    			log(mCommand+ ",AT Response={"+result+"},target success cmd="+mSuccessKey);
	    			if(mCheckAlreadyDelay==false){
	    				mHandler.removeCallbacks(mRunnable);
	    				mMessage+=result;
	    				mHandler.postDelayed(mRunnable, 300);
	    				break;
	    			}
	    			sendNext( result, msg);
	    			
	    			break;
	    		case EVENT_SERIAL_NULL:
//	    			Toast.makeText(mContext, "Serial Port NULL FAIL!!",
//							Toast.LENGTH_SHORT).show();
	    			log("EVENT_SERIAL_NULL");
	    			mMainHandler.sendEmptyMessage(EVENT_SERIAL_NULL);
	    			break;
	    		}
	    	}
	    };
	
	
}
