package com.asus.tool;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileReader;
import java.lang.StringBuffer;
import java.io.FileWriter;
import java.io.BufferedWriter; 

import com.asus.fdclogtool.R;
import com.asus.log.AudioLog;
import com.asus.log.BaseLog;
import com.asus.log.GeneralLog;
import com.asus.log.MemoryDiskCpu;
import com.asus.log.ModemLog;
import com.asus.log.ModemLogService;
import com.asus.log.ModemLogTrigger;
import com.asus.log.NetWorkLog;
import com.asus.log.OtherLog;
import com.asus.log.PowerLog;



import android.R.integer;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;

public class DumpService extends Service {

	public static final String TAG = "DumpService";
	private Thread mAudioThread;
	private Thread mWifiThread;
	private Thread mPowerThread;
	private Thread mBatteryThread;
	private Thread mMeminfoThread;
	private Thread mDiskThread;
	private Thread mActivityThread;
	private Thread mWindowThread;
	private Thread mProcaneThread;
	private Thread mCpuThread;
	private Thread mCpuLoadingThread;
	private Thread mTopThread;
	private LogRunnable mAudioRunnable;
	private LogRunnable mWifiRunnable;
	private LogRunnable mPowerRunnable;
	private LogRunnable mBatteryRunnable;
	private LogRunnable mMeminfoRunnable;
	private LogRunnable mDiskRunnable;
	private LogRunnable mActivityRunnable;
	private LogRunnable mWindowRunnable;
	private LogRunnable mProcaneRunnable;
	private LogRunnable mCpuRunnable;
	private LogRunnable mCpuLoadingRunnable;
	private LogRunnable mTopRunnable;
	public static final String DEFAULT_ROOT_PATH="/sdcard/Asuslog/";
	public static final String KEY_SAVE_DIR =	"persist.asuslog.savedir";
	public static final String KEY_DATE_DIR =	"persist.asuslog.dump.date";
	public static final String KEY_DUMP_ENABLE ="persist.asuslog.dump.enable";
	public static final String DIR_NAME_AUDIO="AudioLog";
	public static final String DIR_NAME_WIFI="WifiLog";
	public static final String DIR_NAME_POWER="PowerLog";
	public static final String DIR_NAME_BATTERY="BatteryLog";
	public static final String DIR_NAME_DISK="DiskLog";
	public static final String DIR_NAME_WINDOW="WindowLog";
	public static final String DIR_NAME_ACTIVITY="ActivityLog";
	public static final String DIR_NAME_MEMINFO="Meminfo";
	public static final String DIR_NAME_CPU="CpuInfo";
	public static final String DIR_NAME_PROCANE="ProcaneLog";
	private boolean mbSystemReady=true;
	public static final String ACTION_PATH_UNMOUNT="asus.intent.action.unmount";
	public static final String ACTION_MAIN_DISKLOW_CLOSE="asus.intent.action.disklow.close";
	public static final String ASUS_SKU="ro.build.asus.sku";
	public static final String ACTION_UPLOAD_LOG="com.asus.packlogs.completed" ;
	public static final String ACTION_INIT_LOG_COMPLETE="com.asus.init.log.completed" ;
	public static final String SOCKET_NAME="tool_connect";
	public static final String FILE_NAME_VERSION="version.txt";
	
	private static final boolean DEBUG = true;
	private Handler mHandler=new Handler();
	
	private Handler mDiskHandler=new Handler();
	private static int mTrigerMtpUpdateTime=120;//sencond
	private static int mTrigerDetectDiskTime=60*1;
	private static final int THREE_DAY=60*24*60*3;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		SystemProperties.set("persist.asuslog.logtool.clear", "0");
		SystemProperties.set("persist.asuslog.fac.init", "1");
		if(Util.isUserBuild()){
			if(SystemProperties.get("persist.asuslog.logcat.enable", "null").equals("null"))
				SystemProperties.set("persist.asuslog.logcat.enable", "1");
			if(SystemProperties.get("persist.asuslog.logcatr.enable", "null").equals("null"))
				SystemProperties.set("persist.asuslog.logcatr.enable", "1");
			if(SystemProperties.get("persist.asuslog.logcate.enable", "null").equals("null"))
				SystemProperties.set("persist.asuslog.logcate.enable", "1");
		}
		SharedPreferences pref = getSharedPreferences(DebuggerMain.NAME_SHARE_PREF,  Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
		Settings.init(pref);
		
		
		if(Settings.isAutoUploadFirst()){
			BaseLog.setPropCheck(DebuggerMain.KEY_AUTO_UPLOAD, true);
			Settings.setAutoUploadFirst(false);
		}
		
		String dirPath=getLogRootpath();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(ACTION_PATH_UNMOUNT);
		intentFilter.addAction(ACTION_UPLOAD_LOG);
		intentFilter.addAction(ACTION_INIT_LOG_COMPLETE);
		registerReceiver(mReceiver, intentFilter);

                //register modem crash receiver 
		IntentFilter ModemintentFilter = new IntentFilter();
            	ModemintentFilter.addAction("com.asus.modem.crash");
                registerReceiver(ModemCrashReceiver, ModemintentFilter);

		File file=new File(dirPath);
		if(file.exists()==false){
			file.mkdirs();
		}
		
		if(Settings.isFirstLogLevelSet())
		{
			SystemProperties.set(GeneralLog.KEY_RIL_LEVEL, String.valueOf(GeneralLog.RIL_LOG_INFO));
			GeneralLog.sendBrocastForRilLog(this, GeneralLog.RIL_LOG_INFO);
			Settings.setFirstRilLog(false);
		}
		// system uid no access microsdcard
		//createVersionInfo();
		trigerUpdateMtp();
		storagelisten();
		try{
			Thread.sleep(100);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		initLog();
	}
	
	public void initLog(){
		if(Settings.isAudioEnable()){
			startAudio();
		}
		if(Settings.isWifiEnable()){
			startWifi();
		}
		if(Settings.isPowerEnable()){
			startPower();
		}
		if(Settings.isBatteryEnable()){
			startBattery();
		}
		if(Settings.isMemoinfoEnable()){
			startMeminfo();
		}
		if(Settings.isDiskLogEnable()){
			startDisk();
		}
		if(Settings.isActivityEnable()){
			startActivityLog();
		}
		if(Settings.isWindowEnable()){
			startWindow();
		}
		if(Settings.isCpuLogEnable()){
			startCpu();
		}
		
		if(Settings.isCpuLoadingEnable()){
			startCpuLoading();
		}
		
		if(Settings.isProcaneLogEnable()){
			startProcane();
		}
		
		if(Settings.isTopEnable()){
			startTop();
		}
	}
	
	public static boolean isSaveMicroSD(Context context){
		String path= Util.getMicroSDPath(context);
		String savepath=SystemProperties.get(DumpService.KEY_SAVE_DIR);
		if(savepath.equals(path+"/Asuslog/")){
			return true;
		}
		return false;
	}
	
	public static String getCurrentLogPath()
	{
		return getLogRootpath()+SystemProperties.get(KEY_DATE_DIR,Util.getDate());
	}
	
	public static boolean isEnableLogTool(){
		 return BaseLog.getPropCheck(KEY_DUMP_ENABLE);
	}
	
	public static void setCurrentLogNewDate()
	{
		SystemProperties.set(KEY_DATE_DIR,Util.getDate());
	}
	
	public String getSaveDate() {
		return SystemProperties.get(KEY_DATE_DIR);
	}
	
	public static String getLogRootpath(){
		return SystemProperties.get(KEY_SAVE_DIR,DEFAULT_ROOT_PATH);
	}
	
	public void  storagelisten()
	{
		Thread thread=new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				SharedPreferences pref = getSharedPreferences(DebuggerMain.NAME_SHARE_PREF, Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
				Settings.init(pref);
				File file=new File(getLogRootpath());
				if(file.exists()==false){
					mDiskHandler.postDelayed(this, mTrigerDetectDiskTime*1000);
					return;
				}
				int space =Util.getDiskSize(getLogRootpath());

				if(space<Util.getReminderLowClose(getLogRootpath()))
				{
					if(Util.isUserBuild() && Settings.isAlreadyReminderDiskLowClear()==false)
					{
						Log.e(TAG, "clear old data build run out of disk space clear data ");
						clearOldData();
						//clear data
						space =Util.getDiskSize(getLogRootpath());
						Settings.setAlreadyReminderDiskLowClear(true);
						if(space>Util.getReminderLowClose(getLogRootpath())){
							return;
						}
					}
					if(Settings.isAlreadyReminderDiskLowClose()==false){
						
						setALLLogEnable( false);
						getBaseContext().sendBroadcast(new Intent(ACTION_MAIN_DISKLOW_CLOSE));
						GeneralLog.forceState(false);
						MemoryDiskCpu.forceStop();
						ModemLog.forceStop();
						NetWorkLog.forceStop();
						PowerLog.forceStop();
						AudioLog.forceStop();
						OtherLog.forceStop();
						if(Settings.isReminderDiskLow()){
							
							Intent intent=new Intent(DialogLogReceiver.ACTION_CLOSE_LOG);
							getBaseContext().sendBroadcast(intent);
							Settings.setAlreadyReminderDiskLowClose(true);
						}
						Util.dumplogtool("logtool is already close");
						Util.dumplogtool("folder date"+getSaveDate());
					}
				}else if(space<Util.getReminderLowSpace(getLogRootpath()))
				{
					if(Util.isUserBuild() && Settings.isAlreadyReminderDiskLowClear()==false)
					{
						Log.e(TAG, "clear old data build run out of disk space clear data ");
						clearOldData();
						//clear data
						space =Util.getDiskSize(getLogRootpath());
						Settings.setAlreadyReminderDiskLowClear(true);
						if(space>Util.getReminderLowSpace(getLogRootpath())){
							return;
						}
						
						
					}
					if(Settings.isAlreadyReminderDiskLow()==false){
						if(Settings.isReminderDiskLow()){
							Intent intent=new Intent(DialogLogReceiver.ACTION_DISK_LOW);
							getBaseContext().sendBroadcast(intent);
							Settings.setAlreadyReminderDiskLow(true);
						}
					}
				}else{
					
					Settings.setAlreadyReminderDiskLow(false);
					Settings.setAlreadyReminderDiskLowClose(false);
					Settings.setAlreadyReminderDiskLowClear(false);
				}
				mDiskHandler.postDelayed(this, mTrigerDetectDiskTime*1000);
			}
		});
		thread.start();
		
	}
	
	
	
	public void clearOldData()
	{//no modem log,for user mode
		boolean enable[]=DeleteTask.closeRemoteRunningLog(getBaseContext());
		File logDir=new File(DumpService.getLogRootpath());
		long currentTime=Util.toSecond(Util.getDate());
		if(logDir.exists()==false){
			Log.e(TAG, "clearOldData:folder not exist "+logDir);
			return;
		}
		ContentResolver cr=getBaseContext().getContentResolver();
		for(int i=0;i<DebuggerMain.ALL_CATEGORY.length;i++){
			recursiveDeleteOldFile(cr,currentTime, new File( logDir+"/"+DebuggerMain.ALL_CATEGORY[i]));
		}
		File files[]=logDir.listFiles();
		if(files!=null && files.length>0){
			for(File childFile : files){
				if(childFile.isDirectory()){
					boolean result=Util.isDigitIgnoreDash( childFile.getName());
					if(result){
						recursiveDeleteOldFile(cr,currentTime, new File( logDir+"/"+childFile.getName()));
						
					}
					
				}
				
			}
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DeleteTask.restartRemoteRunningLog(enable);
	}

	public void recursiveDeleteOldFile(ContentResolver cr,long current,File file){//針對資料夾
		
		if (file.isDirectory())
		{
			if(isOldPath( current,file))
			{
				deleteAllFolder( file);
				UpdateMedia.deleteRecusiveFiles( cr,file.getAbsolutePath(),true,false);
			}else{
				for(File childfile:file.listFiles()){
					recursiveDeleteOldFile(cr, current, childfile);
				}
			}
		}else{//一般文件
			
			if(isOldPath(current, file))
			{
				delFile( cr, file);
			}
		}
	}
	
	public static boolean isOldPath(long now,File file){
		String datepath="yyyy_MMdd_HHmmss";
		if(file.getName().length()>=datepath.length()){
			String dateName=file.getName().substring(0,datepath.length());
			if(Util.isDigitIgnoreDash(dateName)){
				long pastTime=Util.toSecond(dateName);
				if(pastTime+THREE_DAY<now){
					return true;
				}
			};
		}
		return false;
	}
	
	
	private void delFile(ContentResolver cr, File file) {
		// TODO Auto-generated method stub
		if(file.delete()){
			UpdateMedia.deleteRecusiveFiles( cr,file.getAbsolutePath(),false,false);
		}
	}

	public void deleteAllFolder(File file){
		if(file==null){
			return;
		}
		File[] childs=file.listFiles();
		for( File child:childs){
			if(child.isDirectory()){
				deleteAllFolder(child);
			}else{
				child.delete();
			}
		}
		file.delete();
	}
	
	public static boolean isNeedUpdateMtp(){
		String savepath=SystemProperties.get(DumpService.KEY_SAVE_DIR);
		if(savepath.startsWith("/data")){
			return false;
		}
		return true;
	}
	
	private Runnable mUpdateMtpRunnable=new Runnable() {
		
		@Override
		public void run() {
			
			if(isNeedUpdateMtp() && Settings.isAutoUpdateMtp())
			{
				Thread thread=new Thread(new Runnable()
				{
					@Override
					public void run() 
					{
						log("updateMtpByFolder");
						Util.updateMtpByFolder(getBaseContext(), getLogRootpath());
					}
				});
				thread.start();
				mHandler.postDelayed(mUpdateMtpRunnable, mTrigerMtpUpdateTime*1000);
			}
			
		}
	};
	
	private void stopUpdateMtp()
	{
		mHandler.removeCallbacks(mUpdateMtpRunnable);
	}
	
	private void trigerUpdateMtp()//micro sdcard
	{
		//update mtp every 5 minute
		if(isNeedUpdateMtp() && Settings.isAutoUpdateMtp())
		{
			mHandler.removeCallbacks(mUpdateMtpRunnable);
			mHandler.postDelayed(mUpdateMtpRunnable, 0);
		}
	}
	
	private  void createVersionInfo(){
		//create version info
		String sku=SystemProperties.get(ASUS_SKU, "");
		String line = "";
		String prevDisplay="Build.DISPLAY:";
		String prevlogtoolVerson="LogTool_Version:";
		String rootpath=DumpService.getLogRootpath();
		DumpSyslog.dumpsys("mkdir "+rootpath);
		String path=rootpath+ FILE_NAME_VERSION;
		
		File file=new File(path);

		if(file.exists()==false){
			DumpSyslog.dumpsys("echo "+prevDisplay+Build.DISPLAY+" > "+path);
			if(sku.length()!=0){
				DumpSyslog.dumpsys("echo Build.SKU:"+sku+" >> "+path);
			}
			DumpSyslog.dumpsys("echo "+prevlogtoolVerson+getString(R.string.version_number)+" >> " +path);
		}else{//check fota
			String display="";
			String logtoolVersion="";
			FileInputStream instream=null;
			try {
				 instream=new FileInputStream(file);
				 if (instream != null)
			        {
					 InputStreamReader inputreader = new InputStreamReader(instream); 
			         BufferedReader buffreader = new BufferedReader(inputreader); 
		            	while ((line = buffreader.readLine()) != null){
		            		if(line.startsWith(prevDisplay)){//find last line version info
		            			display=line.substring(prevDisplay.length());
		            		}
		            		if(line.startsWith(prevlogtoolVerson)){
		            			logtoolVersion=line.substring(prevlogtoolVerson.length());
		            		}
		            	}
		            	
		            	if(display.equals(Build.DISPLAY)==false){
		            		DumpSyslog.dumpsys("echo ====fota==============time:"+Util.getDate()+" >> "+path);
		            		DumpSyslog.dumpsys("echo "+prevDisplay+Build.DISPLAY+" >> "+path);
		            		if(sku.length()!=0){
		        				DumpSyslog.dumpsys("echo Build.SKU:"+sku+" >> "+path);
		        			}else{
		        				DumpSyslog.dumpsys("echo Build.SKU:"+"none"+" >> "+path);
		        			}
		            	}
		            	if(logtoolVersion.equals(getString(R.string.version_number))==false){
		            		DumpSyslog.dumpsys("echo ====logtool change version==============time:"+Util.getDate()+" >> "+path);
		            		//log(prevlogtoolVerson+getString(R.string.version_number));
		            		DumpSyslog.dumpsys("echo "+prevlogtoolVerson+getString(R.string.version_number)+" >> " +path);
		            	}
			        
			        }
				 
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(instream!=null){
					try {
						instream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}
		
	}
	
	
	
	BroadcastReceiver mReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action=intent.getAction();
			if(action.equals(ACTION_PATH_UNMOUNT)){
				handleLogPathChange();
			}
			if(action.equals(ACTION_UPLOAD_LOG)){
				DeleteTask deleteTask=new DeleteTask(getBaseContext(), false, null, null, stub);
				deleteTask.execute("");
			}
			if(action.equals(ACTION_INIT_LOG_COMPLETE)){
				initLog();
				startModemLog(DumpService.this);
			}
		}

		
	};

	 BroadcastReceiver ModemCrashReceiver = new BroadcastReceiver() {
	    @Override
    	    public void onReceive(Context context, Intent intent) {
                String tempString = "";
		String[] strings = null;
		DumpSyslog.dumpsys("echo ----------"+Util.getDate()+"-------- >> "+ "/sdcard/modemcrash.txt");
			DumpSyslog.dumpsys("dmesg | busybox grep \"subsystem\" >> /sdcard/modemcrash.txt");
		try{
		   Thread.sleep(3000);
 		}catch(Exception e){
		}
                File file = new File("/sdcard/modemcrash.txt");
                BufferedReader reader = null;
                try {
                 reader = new BufferedReader(new FileReader(file));
                 while ((tempString = reader.readLine()) != null) {
			if(tempString.contains("modem subsystem failure reason"))
			strings = tempString.split("]");
		 } 
                 reader.close();
               } catch (IOException e) {
                    e.printStackTrace();
               } finally {
                    if (reader != null) {
                        try {
                              reader.close();
                        } catch (IOException e1) {
                        }
                    }
               }
	     if(strings != null && strings.length > 1){
		ModemCrash.tips = strings[2];
		Toast.makeText(context, strings[2], Toast.LENGTH_SHORT).show();
		DialogLogReceiver.showDialog(context, strings[2]);
	     }
	     //else{
		//Toast.makeText(context, "modem crash !!!", Toast.LENGTH_SHORT).show();
		//DialogLogReceiver.showDialog(context, "modem crash !!!");
	     //}
            }
         };
	
	public static void startModemLog(Context context) {
		if(LightVersionMain.mBuildLight==false)
		{
			if(Settings.isFirstSystemEnable() && BaseLog.getPropCheck(ModemLog.KEY_MODEM_OFFLINE_LOGGING)==false && Util.isUserBuild()==false){
			 	Intent i2sService = new Intent(context, ModemLogService.class);
	    		i2sService.setAction(BootReceiver.ACTION_MODEM_STARTLOG);
			    context.startService(i2sService);
			}
		}

		 Settings.setFirstSystemOpen(false);
	}
	
	
	private LogRunnable closeRunnable(LogRunnable runnable){
		if(runnable!=null){
			runnable.close();
			runnable=null;
		} 
		return null;
	}
	
	private Thread closeThread(Thread thread){
		
		if(thread!=null){
			thread.interrupt();
			thread=null;
		}
		return thread;
	}
	
	public void setALLLogEnable(boolean enable){
		handleAudioLogEnable( enable);
		handleWifiLogEnable( enable);
		handlePowerLogEnable( enable);
		handleBatteryLogEnable(enable);
		handleMeminfoLogEnable(enable);
		handleDiskLogEnable( enable);
		handleActivityLogEnable( enable);
		handleWindowLogEnable( enable);
		handleCpuLogEnable(enable);
		handleCpuLoadingEnable(enable);
		handleTopLogEnable(enable);
		handleProcaneLogEnable(enable);
	}

	IServiceLog.Stub stub = new IServiceLog.Stub() {

		@Override
		public void audioLogEnable(boolean enable) throws RemoteException {
			handleAudioLogEnable( enable);
		}

		@Override
		public void wifiLogEnable(boolean enable) throws RemoteException {
			handleWifiLogEnable( enable);
			
		}

		@Override
		public void powerLogEnable(boolean enable) throws RemoteException {
			handlePowerLogEnable( enable);
		}

		@Override
		public void batteryLogEnable(boolean enable) throws RemoteException {
			handleBatteryLogEnable(enable);
		}

		@Override
		public void meminfoLogEnable(boolean enable) throws RemoteException {
			handleMeminfoLogEnable(enable);
		}

		@Override
		public void diskLogEnable(boolean enable) throws RemoteException {
			handleDiskLogEnable( enable);
		}

		@Override
		public void activityLogEnable(boolean enable) throws RemoteException {
			 handleActivityLogEnable( enable);
		}

		@Override
		public void windowLogEnable(boolean enable) throws RemoteException {
			handleWindowLogEnable( enable);
		}

		@Override
		public void onLogPathChange() throws RemoteException {
			handleLogPathChange();
		}

		@Override
		public boolean isSystemReady() throws RemoteException {
			
			return mbSystemReady;
		}

		@Override
		public void onAutoUpdateMtp(boolean enable) throws RemoteException {
			if(enable){
				trigerUpdateMtp();
			}else{
				stopUpdateMtp();
			}
		}

		@Override
		public void onRefreshMtp() throws RemoteException {
			Util.updateMtpByFolder(getBaseContext(), getLogRootpath());
		}

		@Override
		public void procaneLogEnable(boolean enable) throws RemoteException {
			// TODO Auto-generated method stub
			handleProcaneLogEnable( enable);
		}

		@Override
		public void cpuLogEnable(boolean enable) throws RemoteException {
			// TODO Auto-generated method stub
			handleCpuLogEnable( enable);
		}
		
		@Override
		public void cpuLoadingEnable(boolean enable) throws RemoteException {
			handleCpuLoadingEnable( enable);
			
		}
		
		@Override
		public void cpuLoadingChangeTime() throws RemoteException {
			handleCpuLoadingEnable( false);
			handleCpuLoadingEnable( true);
		}

		@Override
		public void onReStartLog() throws RemoteException {
			onRestartLog();
		}

		@Override
		public void topLogEnable(boolean enable) throws RemoteException {
			handleTopLogEnable(enable);
		}

		@Override
		public void setLogAllEnable(boolean enable) throws RemoteException {
			// TODO Auto-generated method stub
			setALLLogEnable( enable);
		}

		

	};
	
	
	
	private void handleAudioLogEnable(boolean enable){
		if(enable ){
			startAudio();
		}else{
			mAudioThread=closeThread(mAudioThread);
			mAudioRunnable=closeRunnable(mAudioRunnable);
		}
	}
	
	private void handleWifiLogEnable(boolean enable){
		if(enable){
			startWifi();
		}else{
			mWifiThread=closeThread(mWifiThread);
			mWifiRunnable=closeRunnable(mWifiRunnable);
		}
	}
	
	private void handlePowerLogEnable(boolean enable){
		if(enable){
			startPower();
		}else{
			mPowerThread=closeThread(mPowerThread);
			mPowerRunnable=closeRunnable(mPowerRunnable);
		}
	}
	
	private void handleBatteryLogEnable(boolean enable){
		if(enable){
			startBattery();
		}else{
			mBatteryThread=closeThread(mBatteryThread);
			mBatteryRunnable=closeRunnable(mBatteryRunnable);
		}
	}
	
	private void handleMeminfoLogEnable(boolean enable){
		if(enable){
			startMeminfo();
		}else{
			mMeminfoThread=closeThread(mMeminfoThread);
			mMeminfoRunnable=closeRunnable(mMeminfoRunnable);
		}
	}
	
	private void handleDiskLogEnable(boolean enable){
		if(enable){
			startDisk();
		}else{
			mDiskThread=closeThread(mDiskThread);
			mDiskRunnable=closeRunnable(mDiskRunnable);
		}
	}
	
	private void handleActivityLogEnable(boolean enable){
		if(enable){
			startActivityLog();
		}else{
			mActivityThread=closeThread(mActivityThread);
			mActivityRunnable=closeRunnable(mActivityRunnable);
		}
	}
	
	private void handleWindowLogEnable(boolean enable){
		if(enable){
			startWindow();
		}else{
			mWindowThread=closeThread(mWindowThread);
			mWindowRunnable=closeRunnable(mWindowRunnable);
		}
	}

	private void handleCpuLogEnable(boolean enable){
		if(enable){
			startCpu();
		}else{
			mCpuThread=closeThread(mCpuThread);
			mCpuRunnable=closeRunnable(mCpuRunnable);
		}
	}
	
	private void handleCpuLoadingEnable(boolean enable){
		if(enable){
			startCpuLoading();
		}else{
			mCpuLoadingThread=closeThread(mCpuLoadingThread);
			mCpuLoadingRunnable=closeRunnable(mCpuLoadingRunnable);
		}
	}
	
	private void handleProcaneLogEnable(boolean enable){
		if(enable){
			startProcane();
		}else{
			mProcaneThread=closeThread(mProcaneThread);
			mProcaneRunnable=closeRunnable(mProcaneRunnable);
		}
	}
	
	private void handleTopLogEnable(boolean enable){
		if(enable){
			startTop();
		}else{
			mTopThread=closeThread(mTopThread);
			mTopRunnable=closeRunnable(mTopRunnable);
		}
	}
	
	private void onRestartLog(){
		if(mAudioThread!=null){
			handleAudioLogEnable(false);
			handleAudioLogEnable(true);
		}
		if(mWifiThread!=null){
			handleWifiLogEnable(false);
			handleWifiLogEnable(true);
		}
		if(mPowerThread!=null){
			handlePowerLogEnable(false);
			handlePowerLogEnable(true);
		}
		if(mBatteryThread!=null){
			handleBatteryLogEnable(false);
			handleBatteryLogEnable(true);
		}
		if(mMeminfoThread!=null){
			handleMeminfoLogEnable(false);
			handleMeminfoLogEnable(true);
		}
		if(mDiskThread!=null){
			handleDiskLogEnable(false);
			handleDiskLogEnable(true);
		}
		if(mActivityThread!=null){
			handleActivityLogEnable(false);
			handleActivityLogEnable(true);
		}
		if(mWindowThread!=null){
			handleWindowLogEnable(false);
			handleWindowLogEnable(true);
		}
		
		if(mProcaneThread!=null){
			handleProcaneLogEnable(false);
			handleProcaneLogEnable(true);
		}
		
		if(mCpuThread!=null){
			handleCpuLogEnable(false);
			handleCpuLogEnable(true);
		}
		
		if(mCpuLoadingThread!=null){
			handleCpuLoadingEnable(false);
			handleCpuLoadingEnable(true);
		}
		
		if(mTopThread!=null){
			handleTopLogEnable(false);
			handleTopLogEnable(true);
		}
	}
	
	private void handleLogPathChange(){
		mbSystemReady=false;
		log("handleLogPathChange");
		createVersionInfo();
		onRestartLog();
		
		mbSystemReady=true;
		trigerUpdateMtp();
	}
	private void startAudio(){
		if(mAudioRunnable!=null){
			mAudioRunnable.close();
		}
		mAudioRunnable=new LogRunnable(getCurrentLogPath()+"/", "audio", "dumpsys audio",false);
		mAudioThread=new Thread(mAudioRunnable);
		mAudioThread.start();
	}
	
	private void startWifi(){
		mWifiRunnable=new LogRunnable(getCurrentLogPath()+"/", "wifi", "dumpsys wifi",false);
		mWifiThread=new Thread(mWifiRunnable);
		mWifiThread.start();
	}
	
	private void startPower(){
		mPowerRunnable=new LogRunnable(getCurrentLogPath()+"/", "power", "dumpsys power",false);
		mPowerThread=new Thread(mPowerRunnable);
		mPowerThread.start();
	}
	
	private void startBattery(){
		if(Build.VERSION.SDK_INT<=DebuggerMain.JELLY_BEAN_MR2){
			mBatteryRunnable=new LogRunnable(getCurrentLogPath()+"/", "battery", "batteryinfo","dumpsys battery","dumpsys batteryinfo");
		}else{
			mBatteryRunnable=new LogRunnable(getCurrentLogPath()+"/", "battery", "batterystats","dumpsys battery","dumpsys batterystats");
		}
		
		mBatteryThread=new Thread(mBatteryRunnable);
		mBatteryThread.start();
	}
	
	private void startMeminfo(){
		mMeminfoRunnable=new LogRunnable(getCurrentLogPath()+"/", "meminfo", "dumpsys meminfo",false);
		mMeminfoThread=new Thread(mMeminfoRunnable);
		mMeminfoThread.start();
	}
	
	private void startDisk(){
		mDiskRunnable=new LogRunnable(getCurrentLogPath()+"/", "disk", "dumpsys diskstats",false);
		mDiskThread=new Thread(mDiskRunnable);
		mDiskThread.start();
	}
	
	private void startActivityLog(){
		mActivityRunnable=new LogRunnable(getCurrentLogPath()+"/", "activity", "dumpsys activity",false);
		mActivityThread=new Thread(mActivityRunnable);
		mActivityThread.start();
	}
	
	private void startWindow(){
		mWindowRunnable=new LogRunnable(getCurrentLogPath()+"/", "window", "dumpsys window",false);
		mWindowThread=new Thread(mWindowRunnable);
		mWindowThread.start();
	}
	
	private void startProcane(){
		mProcaneRunnable=new LogRunnable(getCurrentLogPath()+"/", "procrank", "procrank",true);
		mProcaneThread=new Thread(mProcaneRunnable);
		mProcaneThread.start();
	}
	
	private void startTop(){
		mTopRunnable=new LogRunnable(getCurrentLogPath()+"/", "top", "top -n 1 -t",true);
		mTopThread=new Thread(mTopRunnable);
		mTopThread.start();
	}
	
	private void startCpu(){
		mCpuRunnable=new LogRunnable(getCurrentLogPath()+"/", "cpuinfo", "dumpsys cpuinfo",false);
		mCpuThread=new Thread(mCpuRunnable);
		mCpuThread.start();
	}
	
	private void startCpuLoading()
	{
		mCpuLoadingRunnable=new LogRunnable(getCurrentLogPath()+"/", "cpuloading", "cat /proc/stat",Settings.getCpuLoadIntervelTime(),false);
		mCpuLoadingThread=new Thread(mCpuLoadingRunnable);
		mCpuLoadingThread.start();
	}
	
	public static void createFolderCmd(ContentResolver  cr,PrintWriter out,BufferedReader in,String folderPath ){
		DumpService.sendCMD( out, in,"mkdir "+folderPath );
		int id=UpdateMedia.getFileID(cr,folderPath);
		if(id==0)
		{
			UpdateMedia.addfolder(cr, folderPath);
		}
	}
	
	
	public static void sendCMD(PrintWriter out,BufferedReader in,String cmd ){
		
		out.println(cmd);
		out.flush();
		char output[]=new char[255];
		try {
			log("before cmd"+cmd);
			int idx= in.read(output);
			log("after result"+String.valueOf(output, 0, idx));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class LogRunnable implements Runnable{

		String mLogDir="";
		String mSuffix="";
		String mCmd="";
		String mCmd2=null;
		String mSuffix2=null;
		String tempdir="";
		boolean mClose=false; 
		int mDelayTime=60*60;
		boolean mbRoot;
		LogRunnable(String dir,String suffix,String cmd,boolean root){
			mLogDir=dir;
			mSuffix=suffix;
			mCmd=cmd;
			mbRoot=root;
		}
		
		LogRunnable(String dir,String suffix,String cmd,int time,boolean root){
			mLogDir=dir;
			mSuffix=suffix;
			mCmd=cmd;
			mDelayTime=time;
			mbRoot=root;
		}
		
		LogRunnable(String dir,String suffix,String suffix2,String cmd,String cmd2){
			
			mLogDir=dir;
			mSuffix=suffix;
			mCmd=cmd;
			mCmd2=cmd2;
			mSuffix2=suffix2;
		}
		
		public void close(){
			mClose=true;
		}
		
		@Override
		public void run()
		{
			while(true)
			{
				if(Thread.currentThread().isInterrupted() || mClose==true){
					return;
				}
				//mLogDir=DumpService.getCurrentLogPath()+"/";
				mLogDir = "/data/Asuslog/" + SystemProperties.get(KEY_DATE_DIR,Util.getDate()) + "/";
				tempdir="/data/Asuslog/";
				try {
					
					String dir=mLogDir;
					File file=new File(dir);
					File tempfile = new File(tempdir);
					if(file.exists()==false)
					{
						boolean result=file.mkdirs();
						if(result==false){
							Thread.sleep(1000);
							continue;
						}
					}
					if(tempfile.exists()==false)
					{
						boolean result=tempfile.mkdirs();
						if(result==false){
							Thread.sleep(1000);
							continue;
						}
					}
					String path=dir+mSuffix+".log";
					String temPath = tempdir + mSuffix + ".log";
					String path2="";
					String temPath2 = "";
					String tempOutput="";
					if(mSuffix2!=null)
					{
						path2=dir+mSuffix2+".log";
						temPath2 = tempdir + mSuffix2 + ".log";
					}
					
					String dump=mCmd +" >> "+path;
					String tempDump = mCmd + " > " + temPath;
					//String dump = mCmd +">> /sdcard/1.txt 2>&1 & ";
					DumpSyslog.dumpsys("echo ----------"+Util.getDate()+"-------- >> "+path);
					if(mbRoot){
						Util.setCmd(dump);
					}else{
						if(DumpSyslog.dumpsys(dump)!=0){
							Thread.sleep(1000);
							continue;
						};
						//readFile(temPath, path);
					}
					
					if(mCmd2!=null){
						 DumpSyslog.dumpsys("echo ----------"+Util.getDate()+"-------- >> "+path2);
						 tempDump = mCmd2 + " > " + temPath2;
						 dump = mCmd2 + " >> " + path2;
						 DumpSyslog.dumpsys(dump);
						 //readFile(temPath2, path2);
					}
					if(mClose==false){
						Thread.sleep(mDelayTime*1000);
					}
				} catch (InterruptedException e) {
					mClose=true;
					return;
				}
			}
		}


		public void readFile(String filePath, String desPath){
	         File file = new File(filePath);
             BufferedReader reader = null;
			 String outputStirng = "";
             try {
                 reader = new BufferedReader(new FileReader(file));
                 String tempString = null;
                 int line = 1;
                 while ((tempString = reader.readLine()) != null) {
				 	 outputStirng += tempString;
					 outputStirng += "\n";
                     line++;
                 }
                 reader.close();
              } catch (IOException e) {
                    e.printStackTrace();
              } finally {
                    if (reader != null) {
                        try {
                              reader.close();
                        } catch (IOException e1) {
                        }
                    }
               }
			  writeFile(outputStirng, desPath);
     	}

		 public void writeFile(String str, String desPath){
		     boolean append =false;
			 FileWriter fw = null;
             BufferedWriter bf = null;
     		 File file = new File(desPath);
     		 try{
                 if(file.exists()) append =true;
                 fw = new FileWriter(desPath,append);
                 bf = new BufferedWriter(fw);
                 bf.append(str);
                 bf.flush();
                 bf.close();  
             }catch (IOException e){
                   e.printStackTrace();
             }
       }
	}
	
	

	@Override
	public IBinder onBind(Intent intent) {
		log("onBind");
		return stub;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log("onStartCommand");
		return START_STICKY;
	}

	private static void log(String msg) {
		// TODO Auto-generated method stub
		if(DEBUG)
			Log.v(TAG, msg);
	}

	
}
