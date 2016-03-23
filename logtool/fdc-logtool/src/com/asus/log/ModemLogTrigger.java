package com.asus.log;

import java.io.File;

import android.R.integer;
import android.content.Context;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

import com.asus.tool.SerialPortControl;
import com.asus.tool.Settings;

public class ModemLogTrigger {


	
	public static final String START_MODEM_CMD="AT+TRACE=1";
	public static final String START_MODEM_CMD_RETURN="OK";
	
	private static final String TAG = "ModemLogTrigger";
	private Context mContext;
	private SerialPortControl mSerialPortControl;
	private boolean mHasUI=false;
	private Handler mMainHandler;
	
	private String mCommand;
	private String mSuccesskey;
	private int mMode ;
	private int mIndex;
	private int mCloseIndex;
	public ModemLogTrigger(Context context,SerialPortControl control,boolean hasUi,Handler handler){
		mContext=context;
		mSerialPortControl=control;
		mHasUI=hasUi;
		mMainHandler=handler;
	}
	
	public void showToast(String msg){
		if(mHasUI){
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void closeDialog() {
		// TODO Auto-generated method stub
		if(mHasUI){
			mMainHandler.sendEmptyMessage(ModemLog.MSG_CLOSE_DIALOG);
		}else{
			mMainHandler.sendEmptyMessage(ModemLog.MSG_CLOSE_HANDLE);
		}
	}
	
	private void sendCloseHandle()
	{
		mMainHandler.sendEmptyMessage(ModemLog.MSG_CLOSE_HANDLE);;
	}
	
	public static boolean isDeviceExist()
	{
		File file =new File("/dev/gsmtty19");
		if(file.exists()==false){
			return false;
		}
		return true;
	}
	
	public void checkLogAndRestart(){
		
		sendCommand(ModemLog.AT_TRACE_CHECK2_SUCCESS,"Oct",ModemLog.CHECK_RESTART_MODEM_MODE,true,true);
	}
	
	public void openLog(){
		//SystemProperties.set(MtsProperties.MTS_INPUT_PROP, ModemLog.INPUT_PROP);
		sendCommand("at+xsio?","3, *3",ModemLog.START_MODEM_MODE,true,true);
	}
	
	public void openI2C()
	{
		mIndex=0;
		sendI2cCmd(0);
		
	}
	
	public void closeLog(){
		mCloseIndex=0;
		sendCloseCmd(0,null);
	}
	
	
	public static boolean isNoNeedDelay(String cmd){
		for(int i=0;i<ModemLog.DELAYCHECK.length;i++){
			if(ModemLog.DELAYCHECK[i].equals(cmd)){
				return false;
			}
		}
		return true;
		
	}
	
	public void sendI2cCmd(int idx){
		
		boolean nodelay=true;
		if(Settings.isAutoPcm())
		{
			if(idx==ModemLog.I2S_CMD_PCM.length){
				closeDialog();
				return;
			}
			nodelay=isNoNeedDelay(ModemLog.I2S_CMD_PCM[idx]);
			sendCommand(ModemLog.I2S_CMD_PCM[idx], "OK", ModemLog.I2CS_PHONE_MODE,nodelay,nodelay);
		}else{
			if(idx==ModemLog.I2S_CMD.length){
				closeDialog();
				return;
			}
			nodelay=isNoNeedDelay(ModemLog.I2S_CMD[idx]);
			sendCommand(ModemLog.I2S_CMD[idx], "OK" , ModemLog.I2CS_PHONE_MODE,nodelay,nodelay);
		}
		mIndex++;
	}
	
	public void sendCloseCmd(int idx,Message msg){
		
		boolean nodelay=true;
		if(Settings.isAutoPcm())
		{
			if(idx==ModemLog.CLOSE_MODEM_PCM_CMD.length){
				closeLogfinish( msg);
				return;
			}
			nodelay=isNoNeedDelay(ModemLog.CLOSE_MODEM_PCM_CMD[idx]);
			sendCommand(ModemLog.CLOSE_MODEM_PCM_CMD[idx], "OK", ModemLog.CLOSE_MODEM_MODE,nodelay,nodelay);
		}else{
			if(idx==ModemLog.CLOSE_MODEM_CMD.length){
				closeLogfinish( msg);
				return;
			}
			nodelay=isNoNeedDelay(ModemLog.CLOSE_MODEM_CMD[idx]);
			sendCommand(ModemLog.CLOSE_MODEM_CMD[idx], "OK" , ModemLog.CLOSE_MODEM_MODE,nodelay,nodelay);
		}
		mCloseIndex++;
	}
	
	private void sendCommand(String command,String successkey,int mode,boolean repeat,boolean check)
	{
		if(isDeviceExist()==false){
			snedSwitchUIClose();
			showToast("device modem gsmtty19 no exist");
			sendCloseHandle();
			return;
		}
		mCommand=command;
		mSuccesskey=successkey;
		mMode=mode;
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if(mSerialPortControl!=null){
			boolean result=mSerialPortControl.sendCommand(mMainHandler,mCommand,mSuccesskey,mMode,repeat,check);
			if(result==false){
				sendCloseHandle();
			}
		}else{
			sendCloseHandle();
			showToast("serial Control empty notify");
		}

		
		
	}
	
	 private void closeLogfinish(Message msg){
		 if(msg!=null && msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				showToast( "close modem Log success!!!");
			}else{
				showToast( "close modem Log fail!!!");
			}
			closeDialog();
	 }
	
	private void snedSwitchUIClose(){
		if(mHasUI){
			mMainHandler.sendEmptyMessage(ModemLog.MSG_CLOSE_DIALOG);
		}else{
			mMainHandler.sendEmptyMessage(ModemLog.MSG_CLOSE_HANDLE);
		}
	}
	
	
	
	public  void onHandleCmdCallBack(Message msg)
	{
		Bundle bundle=msg.getData();
		if(bundle==null){
			return;
		}
		String cmd=bundle.getString(SerialPortControl.COMMAND);
		if(cmd==null){
			log("onHandleCmdCallBack null");
			return;
		}
		log("cmd="+cmd);
		int mode=msg.arg1;
		if(mode==ModemLog.I2CS_PHONE_MODE){
			sendI2cCmd(mIndex);
			return;
		}
		
		if(mode==ModemLog.CLOSE_MODEM_MODE){
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				sendCloseCmd(mCloseIndex,msg);
			}else{
				closeDialog();
				showToast("close modem Log fail!!!");
			}
			return;
		}
		/////////////////////////////////////
		if(cmd.equals(ModemLog.MODEM_CHECK_READY))
		{
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				if(mode==ModemLog.START_MODEM_MODE){
					sendCommand(ModemLog.AT_SET_XSYSTRACE_RATE, "OK",mode,true,true);
				}
			}else{
				//switchModemLogNoTrigerChange(false);
				ModemLog.forceStop();
				snedSwitchUIClose();
				showToast("modem start log fail!!!");
				
			}
		}else if(cmd.equals(ModemLog.AT_SET_XSYSTRACE_RATE))
		{
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				if(Settings.getModemLogLevel()==Settings.MODEM_BB){
					sendCommand(ModemLog.AT_SET_XSYSTRACE_LEVEL_BB, "OK",mode,true,true);
				}else if(Settings.getModemLogLevel()==Settings.MODEM_3G){
					sendCommand(ModemLog.AT_SET_XSYSTRACE_LEVEL_BB_3G, "OK",mode,true,true);
				}else if(Settings.getModemLogLevel()==Settings.MODEM_DIGRF){
					sendCommand(ModemLog.AT_SET_XSYSTRACE_LEVEL_BB_3G_DIGRF, "OK",mode,true,true);
				}
			}
			else{
				ModemLog.forceStop();
				snedSwitchUIClose();
				showToast("modem start log fail!!!");	
			}
		}else if(cmd.equals(ModemLog.PCM_CMD0))
		{
			sendCommand(ModemLog.PCM_CMD1, ModemLog.PCM_SUCCESS_RETURN[1],mode,false,true);
		}else if(cmd.equals(ModemLog.PCM_CMD1))
		{
			sendCommand(ModemLog.PCM_CMD2, ModemLog.PCM_SUCCESS_RETURN[2],mode,false,true);
		}else if(cmd.equals(ModemLog.PCM_CMD2))
		{
			sendCommand(ModemLog.PCM_CMD3, ModemLog.PCM_SUCCESS_RETURN[3],mode,false,false);

		}else if(cmd.equals(ModemLog.PCM_CMD3))
		{
			
			sendCommand(ModemLog.PCM_CMD4, ModemLog.PCM_SUCCESS_RETURN[4],mode,false,false);
			
		}
		else if(cmd.equals(ModemLog.PCM_CMD4))
		{
			sendCommand(ModemLog.PCM_CMD5, ModemLog.PCM_SUCCESS_RETURN[5],mode,false,false);
			
		}else if(cmd.equals(ModemLog.PCM_CMD5)){
			
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				showToast("modem PCM Log success!!!");
			}else
			{
				if(mode==ModemLog.START_MODEM_MODE){
					showToast("modem PCM Log fail!!!  pcm log");
				}else{
					
					showToast("modem PCM Log fail!!!");
				}
				
			}
			if(mode==ModemLog.START_MODEM_MODE || mode==ModemLog.CHECK_RESTART_MODEM_MODE || mode==ModemLog.I2CS_PHONE_MODE){
				closeDialog();
			}
		}
		/*
		else if(cmd.equals(ModemLog.CLOSE_MODEM_CMD0))
		{
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				sendCommand(ModemLog.CLOSE_MODEM_CMD1, ModemLog.CLOSE_CMD_RETURN[1],mode,true,true);
			}else{
				closeDialog();
				showToast("close modem Log fail!!!");
			}
		}else if(cmd.equals(ModemLog.CLOSE_MODEM_CMD1))
		{
			if(Settings.isAutoPcm()){
				if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
					sendCommand(ModemLog.CLOSE_MODEM_CMD2, ModemLog.CLOSE_CMD_RETURN[2],mode,false,false);
				}else{
					closeDialog();
					showToast("close modem Log fail!!!");
				}
			}else{
				closeLogfinish( msg);
			}
			
			
		}else if(cmd.equals(ModemLog.CLOSE_MODEM_CMD2))
		{
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				sendCommand(ModemLog.CLOSE_MODEM_CMD3, ModemLog.CLOSE_CMD_RETURN[3],mode,false,false);
			}else{
				closeDialog();
				showToast("close modem Log fail!!!");
			}
		}else if(cmd.equals(ModemLog.CLOSE_MODEM_CMD3))
		{
			closeLogfinish(msg);
		}*/
		else if(cmd.equals(ModemLog.AT_SET_XSYSTRACE_LEVEL_BB)|| cmd.equals(ModemLog.AT_SET_XSYSTRACE_LEVEL_BB_3G) || cmd.equals(ModemLog.AT_SET_XSYSTRACE_LEVEL_BB_3G_DIGRF)){
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS)
			{
				
				if(mode==ModemLog.START_MODEM_MODE)
				{
					sendCommand(START_MODEM_CMD, START_MODEM_CMD_RETURN,mode,true,true);
				}else if(mode==ModemLog.LOG_LEVEL_MODE){
					showToast("modem level Complete !!!");
				}
			
			}else{
				closeDialog();
				showToast(  "modem level log fail!!!");
				
			}
			
		}else if(cmd.equals(START_MODEM_CMD))
		{
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				sendCommand(ModemLog.AT_TRACE_CHECK2_SUCCESS, ModemLog.AT_TRACE_CHECK2_RETURN,mode,true,true);
			}else{
				closeDialog();
				showToast("AT+TRACE? fail!!!");
			}

		}else if(cmd.equals(ModemLog.AT_TRACE_CHECK1_SUCCESS)){
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				sendCommand(ModemLog.AT_TRACE_CHECK2_SUCCESS, ModemLog.AT_TRACE_CHECK2_RETURN,mode,true,true);
			}else{
				closeDialog();
				showToast(  "AT+TRACE? fail!!!");
			}
			
		}else if(cmd.equals(ModemLog.AT_TRACE_CHECK2_SUCCESS) && msg.arg1==ModemLog.START_MODEM_MODE){
			log("enter AT_TRACE_CHECK2_SUCCESS START_MODEM_MODE");
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				
				log("modem log on success");
				ModemLog.startService(mContext);
				BaseLog.setPropCheck(ModemLog.KEY_MODEM_OFFLINE_LOGGING, true);
				if(Settings.isAutoPcm()){
					showToast( "modem start log Complete!!!,waiting pcm log");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sendCommand(ModemLog.PCM_CMD0, ModemLog.PCM_SUCCESS_RETURN[0],mode,false,true);
				}else{
					showToast( "modem start log Complete!!!");
					closeDialog();
				}
			}else{
				closeDialog();
				showToast(  "at+xsystrace=10 fail!!!");
			}
		}
		else if(cmd.equals(ModemLog.AT_TRACE_CHECK2_SUCCESS) && msg.arg1==ModemLog.CHECK_RESTART_MODEM_MODE){
			log("enter bootcomplete CHECK_RESTART_MODEM_MODE");
			if(msg.what==SerialPortControl.EVENT_RESULT_SUCCESS){
				log("modem bootcomplete log check state Success");
				if(Settings.isAutoPcm()){
					sendCommand(ModemLog.PCM_CMD0, ModemLog.PCM_SUCCESS_RETURN[0],ModemLog.CHECK_RESTART_MODEM_MODE,false,true);
				}else{
					closeDialog();
				}
				
			}else{
				log("modem bootcomplete log check state fail");
				ModemLog.forceStop();
				openLog();
			}
		}

	}
	
	

	public static void log(String message) {
		Log.v(TAG, message);
	}
	
}
