package com.asus.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.asus.log.BaseLog;
import com.asus.log.ModemLog;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

 class DeleteTask  extends AsyncTask<String, Integer, String>{

	private static final String TAG = "DeleteTask";
	private Handler mHandler;
	private String mCopyPath=null;
	private Context mContext;
	private IServiceLog mIServiceLog;

	private static final String SDCARD=Environment.getExternalStorageDirectory().getAbsolutePath();
	private boolean mHasUI=false;
	
	public DeleteTask(Context context,boolean hasUI,Handler handler,String copypath,IServiceLog iServiceLog){
		mHandler=handler;
		mCopyPath=copypath;
		mContext=context;
		mIServiceLog=iServiceLog;
		mHasUI=hasUI;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		if(mHasUI){
			Message msg=new Message();
			msg.what=DebuggerMain.MSG_SHOW_PROGRESSBAR;
			Bundle bundle=new Bundle();
			bundle.putString(DebuggerMain.KEY_TITLE, "clear log");
			bundle.putString(DebuggerMain.KEY_MSG,"waiting...");
			msg.setData(bundle);
			mHandler.sendMessage(msg);
			
		}
		
		if(mHasUI==false)
		{
			Intent intent=new Intent();
			intent.setAction(CmdReceiver.ACTION_DELETE_START);
			mContext.sendBroadcast(intent);
		}
	}

	@Override
	protected String doInBackground(String... params) {
		
		return clearLog(mIServiceLog,mContext,this,true);
	}
	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		log("onPostExecute");
		if(isCancelled()==true){
			return;
		}
		if(mHasUI ){
			mHandler.sendEmptyMessage(DebuggerMain.MSG_CLOSE_PROGRESSBAR);
			//mProgressDialog.dismiss();
			if(result!=null){
				Toast.makeText(mContext, "Delete Fail "+result, Toast.LENGTH_SHORT).show();
				DebuggerMain.showCopyPathDialog(mContext,"Delete Fail!!",result);
			}else{
				Toast.makeText(mContext, "Delete Complete", Toast.LENGTH_SHORT).show();
				//mProgressDialog=null;
				if(mCopyPath!=null){
					DebuggerMain.showCopyPathDialog(mContext,"Copy Complete!",mCopyPath);
				}
			}
		}
		if(mHasUI==false){
			Intent intent=new Intent();
			intent.setAction(CmdReceiver.ACTION_DELETE_END);
			mContext.sendBroadcast(intent);
		}
	}

	private void sendDeleteMessage(String content){
		if(mHasUI){
			Message msg=new Message();
			msg.obj=content;
			msg.what=DebuggerMain.MSG_UPDATE_DELETE;
			//clear Main log
			mHandler.sendMessage(msg);
		}
	}
	
	public static boolean[] closeRemoteRunningLog(Context context)
	{
		//close running dump
		boolean enable[]=new boolean[DebuggerMain.REMOTE_KEY.length];
		for(int i=0;i<DebuggerMain.REMOTE_KEY.length;i++){
			enable[i]=BaseLog.getPropCheck(DebuggerMain.REMOTE_KEY[i]);
			if(enable[i]){
				log(DebuggerMain.REMOTE_KEY[i]+"result"+enable[i]);
				BaseLog.setPropCheck(DebuggerMain.REMOTE_KEY[i], false);
			}
		}
		
		return enable;
	}
	
	public synchronized String  clearLog(final IServiceLog iServiceLog,Context context,DeleteTask task,boolean hasUI)
	{
		
		boolean bMicroSD=DumpService.isSaveMicroSD(context);
		File logDir=new File(DumpService.getLogRootpath());
		if(bMicroSD==false){
			if(logDir.exists()==false){
				Log.e(TAG, "clearLog:folder not exist "+logDir);
				return "folder not exist "+logDir;
			}
		}
		
		
		boolean enable[]=closeRemoteRunningLog(context);
		boolean ModemResult=ModemLog.isModemLogEnable();
		if(ModemResult==true){
			ModemLog.stopService();
		}
		try {
			iServiceLog.onAutoUpdateMtp(false);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Util.setCmd("logcat  -b main -b system -b kernel -b events -b radio -c");//clear logcat buffer
		
		ContentResolver cr=context.getContentResolver();
		task.sendDeleteMessage("start delete...");
		 for(int i=0;i<DebuggerMain.ALL_CATEGORY.length;i++){
			task.sendDeleteMessage("delete "+DebuggerMain.ALL_CATEGORY[i]);
			DumpSyslog.dumpsys("rm -rf "+logDir+"/"+DebuggerMain.ALL_CATEGORY[i]+"/*");
			
			if(DumpService.isNeedUpdateMtp()){
				UpdateMedia.deleteRecusiveFiles( cr,logDir+"/"+DebuggerMain.ALL_CATEGORY[i], true,true);
			}
		}
		File files[]=logDir.listFiles();
		if(files!=null && files.length>0){
			for(File childFile : files){
				if(childFile.isDirectory()){
					boolean result=Util.isDigitIgnoreDash( childFile.getName());
					log(" childFile.getName()="+ childFile.getName()+",digit="+result);
					if(result){
						task.sendDeleteMessage("delete floder:"+ childFile.getName());
						DumpSyslog.dumpsys("rm -r "+logDir+"/"+childFile.getName()+"/");
						if(DumpService.isNeedUpdateMtp()){
							UpdateMedia.deleteRecusiveFiles( cr,logDir+"/"+childFile.getName(), true,false);
						}
					}
					
				}
				else 
					DumpSyslog.dumpsys("rm -r "+logDir+"/"+childFile.getName());
			}
		}
		//delete gps log
		//Util.setCmd("rm -rf /data/gps/log/*");
		//delete crashlog 
		task.sendDeleteMessage("delete Crashlog");
		DumpSyslog.dumpsys("rm -rf "+CrashLogServie.PATH_CRASH_LOG+"*");
		//delete /sdcard/logs
		task.sendDeleteMessage("delete /sdcard/logs");
		DumpSyslog.dumpsys("rm -rf "+SDCARD+"/logs/*");
		//delete /data/randump
		DumpSyslog.dumpsys("rm -f /data/ramdump/*");
		DumpSyslog.dumpsys("rm -f /sdcard/modemcrash.txt");

		DumpSyslog.dumpsys("rm -rf /data/Asuslog/*");
		//restart running dump
		try {
			DumpService.setCurrentLogNewDate();
			File file =new File("/data/Asuslog/" + SystemProperties.get("persist.asuslog.dump.date",Util.getDate()));
			log("new path="+DumpService.getCurrentLogPath());
			if(file.exists()==false){
				file.mkdirs();
				UpdateMedia.addfolder(cr, DumpService.getCurrentLogPath());
			}
			
			DebuggerMain.setPrevBootRootPath( DumpService.getCurrentLogPath());
			
			task.sendDeleteMessage("restart log");
			Thread.sleep(1000);// important update remote log > 1000,else log do not restart
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		
		restartRemoteRunningLog(enable);
		try {
			mIServiceLog.onReStartLog();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(ModemResult==true){
			//ModemLog.setNewPath(context);
			ModemLog.startService(context);
		}
	
		Thread thread =new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					if(Settings.isAutoUpdateMtp()){
						iServiceLog.onAutoUpdateMtp(true);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		thread.start();
		return null;
	}
	
	public static void restartRemoteRunningLog(boolean enable[]){
		for(int i=0;i<enable.length;i++){
			if(enable[i]){
				log("update="+DebuggerMain.REMOTE_KEY[i]);
				BaseLog.setPropCheck(DebuggerMain.REMOTE_KEY[i], true);
			}
		}
	}
	
	public static void execCmd(PrintWriter out,BufferedReader in,String cmd){
		char output[]=new char[255];
		out.println(cmd);
		out.flush();
		try {
			log("before cmd"+cmd);
			int idx= in.read(output);
			log("after result"+String.valueOf(output, 0, idx));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void log(String msg) {
		Log.v(TAG, msg);
		
	}
	
	
}
