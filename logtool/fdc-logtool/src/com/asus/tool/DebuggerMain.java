package com.asus.tool;

import java.io.BufferedReader;

import java.io.File;

import java.io.FileInputStream;

import java.io.IOException;

import java.io.InputStreamReader;

import java.io.PrintWriter;

import java.util.ArrayList;

import java.util.TreeMap;

import com.asus.log.QPSTDownloadMode;

import com.asus.log.AudioLog;

import com.asus.log.BaseLog;

import com.asus.log.GeneralLog;

import com.asus.log.MemoryDiskCpu;

import com.asus.log.ModemLog;

import com.asus.log.NetWorkLog;

import com.asus.log.OtherLog;

import com.asus.log.PowerLog;

import com.asus.log.UploadeLog;

import com.asus.fdclogtool.R;

import android.media.MediaScannerConnection;

import android.media.MediaScannerConnection.MediaScannerConnectionClient;

import android.net.LocalSocket;

import android.net.LocalSocketAddress;

import android.net.Uri;

import android.net.wifi.WifiManager.WifiLock;

import android.os.AsyncTask;

import android.os.Build;

import android.os.Bundle;

import android.os.Environment;

import android.os.Handler;

import android.os.IBinder;

import android.os.Message;

import android.os.RemoteException;

import android.R.bool;
import android.R.integer;

import android.app.Activity;
import android.app.ActivityManager;

import android.app.AlertDialog;

import android.app.AlertDialog.Builder;

import android.app.ProgressDialog;

import android.content.BroadcastReceiver;

import android.content.ComponentName;

import android.content.ContentResolver;

import android.content.Context;

import android.content.ContextWrapper;

import android.content.DialogInterface;

import android.content.Intent;

import android.content.IntentFilter;

import android.content.ServiceConnection;

import android.content.DialogInterface.OnClickListener;

import android.content.pm.PackageParser.NewPermissionInfo;

import android.content.res.AssetManager;

import android.util.Log;

import android.view.Menu;

import android.view.MenuItem;

import android.view.View;

import android.view.WindowManager;

import android.widget.EditText;

import android.widget.LinearLayout;

import android.widget.ProgressBar;

import android.widget.Toast;

import android.os.SystemProperties;

import android.provider.VoicemailContract;

public class DebuggerMain extends Activity implements OnClickListener,
		Handler.Callback {

	IServiceLog mIServiceLog;

	private ArrayList<BaseLog> mBaseLogList = new ArrayList<BaseLog>();

	public static final String NAME_SHARE_PREF = "AsusLog";

	public static final String TAG = "DebuggerMain";

	public static final int KITKAT = 19;
	public static final int JELLY_BEAN_MR2 = 18;
	public static int recovery_tag = 0;

	// private static final String SRC_ASUSLOG="/data/Asuslog/";

	private static final String DIR_NAME_MAIN = "MainLog";
	private static final String DIR_NAME_KERNEL = "KernelLog";
	private static final String DIR_NAME_EVENT = "EventLog";
	private static final String DIR_NAME_RADIO = "RadioLog";
	private static final String DIR_NAME_TCPDUMP = "TcpDump";
	public static final String DIR_NAME_MODEM = "Modem";
	public static final int MSG_UPDATE_DELETE = 2;
	public static final int MSG_SHOW_PROGRESSBAR = 3;
	public static final int MSG_CLOSE_PROGRESSBAR = 4;
	public static final String KEY_TITLE = "KEY_TITLE";
	public static final String KEY_MSG = "KEY_MSG";
	public static final String KEY_AUTO_UPLOAD = "persist.asus.autoupload.enable";
	public static final String KEY_PREV_FOLDER = "persist.asuslog.prevrootpath";
	public String dirPath = SystemProperties.get("persist.asuslog.savedir",
			"/sdcard/Asuslog/");
	ProgressDialog mProgressDialog;
	private CopyTask mCopyTask;
	private DeleteTask mDeleteTask;
	private Handler mMainHandler = new Handler(this);

	// public static final String ALL_CATEGORY[]=new
	// String[]{DIR_NAME_MAIN,DIR_NAME_KERNEL,DIR_NAME_EVENT,DIR_NAME_RADIO,DIR_NAME_TCPDUMP,DIR_NAME_MODEM,DumpService.DIR_NAME_AUDIO,

	// DumpService.DIR_NAME_ACTIVITY,DumpService.DIR_NAME_BATTERY,
	// DumpService.DIR_NAME_DISK,DumpService.DIR_NAME_MEMINFO,

	// DumpService.DIR_NAME_POWER,DumpService.DIR_NAME_WIFI,DumpService.DIR_NAME_WINDOW,};

	public static final String ALL_CATEGORY[] = new String[] { DIR_NAME_MODEM,
			DIR_NAME_TCPDUMP };
	public static final String REMOTE_KEY[] = new String[] {
			GeneralLog.KEY_MAIN, GeneralLog.KEY_KERNEL, GeneralLog.KEY_EVENT,
			GeneralLog.KEY_RADIO, NetWorkLog.KEY_TCPDUMP,
			GeneralLog.KEY_COMBINE_ENABLE

	};

	public static synchronized void startInitServer(Context context) {
		log("startInitServer");
		Intent initLogIntent = new Intent(context, LogInitService.class);
		context.startService(initLogIntent);
	}

	public static synchronized void startInitConfigService(Context context) {
		if (Util.isUserBuild()) {
			if (recovery_tag == 0) {
				startInitServer(context);
				recovery_tag = 1;
			}
		}
	}

	public static void initAsusConfig(Context context) {
		Settings.init(context.getSharedPreferences(NAME_SHARE_PREF,
				Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS));
		SystemProperties.set("persist.asus.mupload.enable", "1");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (Util.isUserBuild()) {
			if (SystemProperties.get("persist.asuslog.logcat.enable", "null")
					.equals("null"))
				SystemProperties.set("persist.asuslog.logcat.enable", "1");
			if (SystemProperties.get("persist.asuslog.logcatr.enable", "null")
					.equals("null"))
				SystemProperties.set("persist.asuslog.logcatr.enable", "1");
			if (SystemProperties.get("persist.asuslog.logcate.enable", "null")
					.equals("null"))
				SystemProperties.set("persist.asuslog.logcate.enable", "1");
			if (SystemProperties.get("persist.asus.autoupload.enable", "null")
					.equals("null"))
				SystemProperties.set("persist.asus.autoupload.enable", "1");
			if (SystemProperties.get("persist.asuslog.fw.update", "null")
					.equals("null"))
				SystemProperties.set("persist.asuslog.fw.update", "0");
		}
		setContentView(R.layout.log_main);
		if (LightVersionMain.mBuildLight) {
			Toast.makeText(this, "build light version error",
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// startInitConfigService(this);
		initAsusConfig(this);
		if (Settings.isAllowAppOpen() == false) {
			Toast.makeText(this, "monkey test...no allow open",
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		GeneralLog general = new GeneralLog(this,
				findViewById(R.id.general_log_id));
		NetWorkLog network = new NetWorkLog(this, findViewById(R.id.network_id));
		AudioLog audio = new AudioLog(this, findViewById(R.id.audio_id));
		PowerLog powerLog = new PowerLog(this, findViewById(R.id.power_id));
		OtherLog otherLog = new OtherLog(this, findViewById(R.id.other_id));
		MemoryDiskCpu memoryDiskCpu = new MemoryDiskCpu(this,
				findViewById(R.id.memory_disk));
		ModemLog modem = new ModemLog(this, findViewById(R.id.modem_id));
		QPSTDownloadMode qpstmode = new QPSTDownloadMode(this,
				findViewById(R.id.QPSTDownloadMode_id));
		mBaseLogList.add(modem);
		mBaseLogList.add(general);
		mBaseLogList.add(network);
		mBaseLogList.add(audio);
		mBaseLogList.add(powerLog);
		mBaseLogList.add(memoryDiskCpu);
		mBaseLogList.add(otherLog);
		mBaseLogList.add(qpstmode);

		ContextWrapper c = new ContextWrapper(this);
		String datadir = c.getFilesDir().getAbsolutePath();
		File filebusybox = new File(datadir + "/" + "busybox");
		File fileRaw_sender = new File(datadir + "/" + "raw_sender");
		File fileQMESA_64 = new File(datadir + "/" + "QMESA_64");
		if (!filebusybox.exists() || !fileRaw_sender.exists()
				|| !fileQMESA_64.exists()) {
			if (c.getFilesDir().exists() == false) {
				c.getFilesDir().mkdirs();
			}
			Util.copyAssetFile(this.getAssets(), this, "busybox", datadir + "/"
					+ "busybox");
			Util.copyAssetFile(this.getAssets(), this, "raw_sender", datadir
					+ "/" + "raw_sender");
			Util.copyAssetFile(this.getAssets(), this, "QMESA_64", datadir
					+ "/" + "QMESA_64");
		}

		initConfigServie(this, mReceiver, mConnection);
		// register modem crash receiver
		IntentFilter ModemintentFilter = new IntentFilter();
		ModemintentFilter.addAction("com.asus.modem.crashed");
		this.registerReceiver(ModemCrashReceiver, ModemintentFilter);

		IntentFilter QPSTintentFilter = new IntentFilter();
		QPSTintentFilter.addAction("com.asus.QPST.fw");
		this.registerReceiver(QPSTReceiver, QPSTintentFilter);
	}

	public static void initConfigServie(Context context,
			BroadcastReceiver receiver, ServiceConnection connection) {
		Intent dumpIntent = new Intent(context, DumpService.class);
		context.startService(dumpIntent);

		Intent crashIntent = new Intent(context, CrashLogServie.class);
		context.startService(crashIntent);
		context.bindService(new Intent(context, DumpService.class), connection,
				Context.BIND_AUTO_CREATE);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DumpService.ACTION_PATH_UNMOUNT);
		intentFilter.addAction(DumpService.ACTION_MAIN_DISKLOW_CLOSE);
		intentFilter.addAction(CmdReceiver.ACTION_DELETE_START);
		intentFilter.addAction(CmdReceiver.ACTION_DELETE_END);
		intentFilter.addAction(DumpService.ACTION_INIT_LOG_COMPLETE);
		context.registerReceiver(receiver, intentFilter);
	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(DumpService.ACTION_PATH_UNMOUNT)) {
				invalidateOptionsMenu();
			} else if (action.equals(DumpService.ACTION_MAIN_DISKLOW_CLOSE)) {
				for (BaseLog base : mBaseLogList) {
					if (base instanceof ModemLog) {
						ModemLog modemLog = (ModemLog) base;
						modemLog.forceStopModemLog();
					} else {
						base.onCancelAll();
					}
				}
			} else if (action.equals(CmdReceiver.ACTION_DELETE_START)) {
				showProgress("clear log", "waiting...");
			} else if (action.equals(CmdReceiver.ACTION_DELETE_END)) {
				closeDialog();
			} else if (action.equals(DumpService.ACTION_INIT_LOG_COMPLETE)) {
				for (BaseLog base : mBaseLogList) {
					if (base instanceof GeneralLog) {
						GeneralLog generalLog = (GeneralLog) base;
						generalLog.initInstallAppUpdate();
					}
				}
			}
		}
	};

	BroadcastReceiver ModemCrashReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "Modem Crash", Toast.LENGTH_SHORT).show();
		}
	};

	BroadcastReceiver QPSTReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent intenInit = new Intent(context, LogInitService.class);
			context.startService(intenInit);
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mIServiceLog = IServiceLog.Stub.asInterface(service);
			for (BaseLog base : mBaseLogList) {
				base.setServiceLog(mIServiceLog);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
		};

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		String savepath = SystemProperties.get(DumpService.KEY_SAVE_DIR);
		String path = Util.getMicroSDPath(this) + "/Asuslog/";

		if (Settings.isCrashReminderEnable()) {
			setMenuCheck(menu, R.id.action_crash_reminder, true);
		}

		if (Settings.isOutputAndClear()) {
			setMenuCheck(menu, R.id.action_output_clear, true);
		}

		if (BaseLog.getPropCheck(KEY_AUTO_UPLOAD)) {
			setMenuCheck(menu, R.id.action_auto_upload, true);
		}

		if (Settings.isAutoUpdateMtp() == false) {
			setMenuCheck(menu, R.id.action_auto_refresh_mtp, false);
		}

		if (Settings.isReminderDiskLow() == false) {
			setMenuCheck(menu, R.id.action_disk_low_reminder, false);
		}

		if (savepath.equals(path)) {
			MenuItem item = menu
					.findItem(R.id.action_write_to_removable_sdcard);
			if (item != null) {
				item.setIcon(R.drawable.sdcard_on);
			}
		}

		MenuItem item = menu.findItem(R.id.action_version);
		item.setTitle("Version : " + getString(R.string.version_number));
		return true;
	}

	public void setMenuCheck(Menu menu, int menuid, boolean check) {

		MenuItem item = menu.findItem(menuid);

		if (item != null) {

			item.setChecked(check);

		}

	}

	@Override
	protected void onDestroy() {
		if (Settings.isAllowAppOpen() == false) {
			super.onDestroy();
			return;
		}
		for (BaseLog base : mBaseLogList) {
			base.onDestroy();
		}
		super.onDestroy();
		unbindService(mConnection);
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onPause() {

		for (BaseLog base : mBaseLogList) {
			base.onPause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		for (BaseLog base : mBaseLogList) {
			base.onResume();
		}
		File file = new File(dirPath);
		if (file.exists() == false) {
			file.mkdirs();
		}
		super.onResume();
		Settings.init(getSharedPreferences(NAME_SHARE_PREF,
				Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS));
	}

	private void crashLogReminder(boolean check) {
		Settings.setCrashReminderEnable(check);
		if (check) {
			sendBroadcast(new Intent(CrashLogServie.CRASH_REMINDER_ENABLE));
		} else {
			sendBroadcast(new Intent(CrashLogServie.CRASH_REMINDER_DISABLE));
		}
	}

	private void autoloadLog(boolean check) {
		if (check) {
			BaseLog.setPropCheck(KEY_AUTO_UPLOAD, true);
		} else {
			BaseLog.setPropCheck(KEY_AUTO_UPLOAD, false);
		}
	}

	public static void setSaveDefaultPath() {
		SystemProperties.set(DumpService.KEY_SAVE_DIR,
				DumpService.DEFAULT_ROOT_PATH);
	}

	public boolean createDirWait(String path) {
		File file = new File(path);// micro sd mkdir may be fail
		if (file.exists() == false) {
			file.mkdirs();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (file.exists() == false) {
			return false;
		}
		return true;
	}

	private void clearLogCheck() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Warning");
		builder.setMessage("check log delete");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				clearlog(null);
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	public static String getPrevBootRootPath() {
		return SystemProperties.get(KEY_PREV_FOLDER, null);
	}

	public static void setPrevBootRootPath(String path) {
		SystemProperties.set(KEY_PREV_FOLDER, path);
	}

	private void openLogDialog(String title, String Message) {

		Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(title);
		dialog.setMessage(Message);

		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}

		});

		dialog.show();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_selectall:
			if (Util.isDiskAllowOpen(DumpService.getLogRootpath())) {
				setCheckAll();
			} else {
				Toast.makeText(this, "Run Out Of Disk Space!!",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.action_deselectall:
			closeAll();
			break;
		case R.id.action_output_log:
			outputFile();
			break;
		case R.id.action_clear:
			clearLogCheck();
			break;
		case R.id.action_output_upload:
			upLoadLog();
			break;
		case R.id.action_auto_upload:
			item.setChecked(!item.isChecked());
			autoloadLog(item.isChecked());
			break;
		case R.id.action_output_clear:
			item.setChecked(!item.isChecked());
			Settings.setOutputAndClear(item.isChecked());
			break;
		case R.id.action_crash_reminder:
			item.setChecked(!item.isChecked());
			crashLogReminder(item.isChecked());
			break;
		case R.id.action_auto_refresh_mtp:
			item.setChecked(!item.isChecked());
			Settings.setAutoUpdateMtpEnable(item.isChecked());
			try {
				mIServiceLog.onAutoUpdateMtp(item.isChecked());
			} catch (RemoteException e2) {
				e2.printStackTrace();
			}
			break;
		case R.id.action_disk_low_reminder:
			item.setChecked(!item.isChecked());
			Settings.setDiskLowReminderEnable(item.isChecked());
			break;
		case R.id.action_default_log:
			defaultLog();
			break;
		case R.id.action_write_to_removable_sdcard:
			try {
				boolean ready = mIServiceLog.isSystemReady();
				if (ready == false) {
					Toast.makeText(this, "please insert Micro SD",
							Toast.LENGTH_SHORT).show();
					break;
				}
			} catch (RemoteException e1) {
				e1.printStackTrace();
				break;
			}
			String path = Util.getMicroSDPath(this);
			if (path == null) {
				Toast.makeText(this, "no support Micro SD", Toast.LENGTH_SHORT)
						.show();
				break;
			}
			;
			boolean mount = Util.isMicroSDMount(this);
			if (mount == false) {
				Toast.makeText(this, "please insert Micro SD",
						Toast.LENGTH_SHORT).show();
				break;
			}

			String savepath = DumpService.getLogRootpath();

			boolean enableLog[] = null;
			boolean ModemResult = ModemLog.isModemLogEnable();
			if (savepath.equals(path + "/Asuslog/")) {// switch default data
				if (createDirWait(DumpService.DEFAULT_ROOT_PATH) == false) {
					openLogDialog("Error", "storage mount error");
					break;
				}
				SystemProperties.set("persist.asuslog.modem.path",
						"/sdcard/Asuslog/Modem");
				if (ModemResult == true) {
					ModemLog.stopService();
					SystemProperties.set("persist.asuslog.qxdmlog.enable", "1");
				}
				enableLog = DeleteTask.closeRemoteRunningLog(this);
				DumpService.setCurrentLogNewDate();
				// ModemLog.updateLogConfig(false);
				setSaveDefaultPath();
				savepath = DumpService.DEFAULT_ROOT_PATH;
				item.setIcon(R.drawable.sdcard_off);
			} else {// switch Micro Sdcard
				if (createDirWait(path + "/Asuslog/") == false) {
					openLogDialog("Error", "sdcard mount error");
					break;
				}
				;
				enableLog = DeleteTask.closeRemoteRunningLog(this);
				SystemProperties.set("persist.asuslog.modem.path", path
						+ "/Asuslog/Modem");
				String modemdir = path + "/Asuslog/Modem";
				File modemfile = new File(modemdir);
				if (modemfile.exists() == false) {
					boolean result = modemfile.mkdirs();
					if (result == false) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				}
				if (ModemResult == true) {
					ModemLog.stopService();
					SystemProperties.set("persist.asuslog.qxdmlog.enable", "1");
				}
				DumpService.setCurrentLogNewDate();
				// ModemLog.updateLogConfig(true);
				SystemProperties.set(DumpService.KEY_SAVE_DIR, path
						+ "/Asuslog/");
				item.setIcon(R.drawable.sdcard_on);
				savepath = path + "/Asuslog/";
			}
			setPrevBootRootPath(DumpService.getCurrentLogPath());
			Toast.makeText(this, "write log path to " + savepath,
					Toast.LENGTH_SHORT).show();
			try {
				DeleteTask.restartRemoteRunningLog(enableLog);
				if (ModemResult == true) {
					// ModemLog.setNewPath(this);
					ModemLog.startService(this);
				}
				mIServiceLog.onLogPathChange();
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.e(TAG, e.getMessage());
			}
			break;
		case R.id.action_add_tag:
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					DebuggerMain.this);
			final EditText editText = new EditText(DebuggerMain.this);
			dialog.setView(editText);
			dialog.setTitle("add Tag Comment");
			dialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (editText.getText().toString().length() == 0) {
								return;
							}
							String content = "";
							for (int i = 0; i < 5; i++) {
								content += "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
										+ "\n";
							}
							content += editText.getText().toString();
							for (int i = 0; i < 5; i++) {
								content += "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
										+ "\n";
							}
							Log.e("logtool", content);
						}
					});
			dialog.setNegativeButton("Cancel", null);
			dialog.show();
			break;
		case R.id.action_rotate_num:
			closeAll();
			SystemProperties.set("persist.asuslog.logtool.clear", "1");
			break;
		/**	
		case R.id.memory_test:
			startQMESA();
			break;
		**/
		default:
			break;
		}
		return super.onOptionsItemSelected(item);

	}

	public void setCheckAll() {
		for (BaseLog base : mBaseLogList) {
			base.onSelectAll();
		}
	}

	public void defaultLog() {
		for (BaseLog base : mBaseLogList) {
			if (base instanceof NetWorkLog) {
				if (!Util.isUserBuild()) {
					NetWorkLog netWorkLog = (NetWorkLog) base;
					netWorkLog.onSetDefault();
				}
			} else if (base instanceof ModemLog) {
				ModemLog modem = (ModemLog) base;
				// if(Util.isUserBuild()){
				modem.onCancelAll();
				// }else{
				// modem.onSelectAll();
				// }
			} else {
				if (Util.isUserBuild()) {
					if (base instanceof GeneralLog)
						base.onSelectAll();
				} else
					base.onSelectAll();
			}
		}
	}

	public void clearlog(String path) {
		DeleteTask deleteTask = new DeleteTask(this, true, mMainHandler, path,
				mIServiceLog);
		deleteTask.execute("");
	}

	public void startUploadService() {
		try {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.asus.loguploader",
					"com.asus.loguploader.LogUploaderService"));
			startService(intent);
		} catch (Exception e) {
			Log.e(TAG, "Can not find com.asus.loguploader package!!!");
		}
	}

	public void upLoadLog() {
		try {
			startUploadService();
			Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.asus.loguploader",
					"com.asus.loguploader.MyTabActivity"));
			startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Can not find com.asus.loguploader package!!!");
		}
	}
	/**
	public void startQMESA(){
		Intent intent = new Intent(this, QmesaActivity.class);
		startActivity(intent);
	}
	**/
	private void showProgress(String title, String msg) {
		mProgressDialog = new ProgressDialog(this,
				AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		mProgressDialog.setTitle(title);
		mProgressDialog.setMessage(msg);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	public void outputFile() {
		File srcDir = new File(DumpService.getLogRootpath());
		if (srcDir.exists() == false) {
			Toast.makeText(this, "no log souce file", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (mProgressDialog != null || mCopyTask != null) {
			Toast.makeText(this, "copy already execute", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		showProgress("Output Log File to SDCARD", "");
		mCopyTask = new CopyTask(this, srcDir);
		mCopyTask.execute("");
	}

	public void clearDir(AsyncTask task, String DirPath, boolean clearNotWrite) {
		File file = new File(DirPath);
		if (file.exists() == false) {
			return;
		}
		File[] files = file.listFiles();

		for (File delFile : files) {
			if (task.isCancelled()) {
				return;
			}
			if (delFile.isDirectory()) {
				recursiveDeleteFile(delFile, clearNotWrite);
			} else {// 涓�埇鏂囦欢
				if (clearNotWrite) {
					delFile.delete();//
				} else {
					if (delFile.canWrite()) {
						delFile.delete();
					}
				}
			}
		}
	}

	public boolean recursiveDeleteFile(File file, boolean clearNotWrite) {// 閲濆皪璩囨枡澶�
		File[] files = file.listFiles();
		boolean dirAllowDel = true;

		for (File delFile : files) {
			if (delFile.isDirectory()) {
				boolean result = recursiveDeleteFile(delFile, clearNotWrite);
				if (result == false) {
					dirAllowDel = false;
				}
			} else {// 涓�埇鏂囦欢
					// 鐣欎笅涓嶅彲瀵殑妾旀
				if (clearNotWrite) {
					delFile.delete();//
				} else {
					if (delFile.canWrite()) {
						delFile.delete();
					} else {
						dirAllowDel = false;
					}
				}
			}
		}

		if (dirAllowDel) {
			file.delete();
			return true;
		}
		return false;
	}

	public class CopyTask extends AsyncTask<String, Integer, Integer> {

		private Context mContext;
		private File mSrcFile;
		private String mOutput = "";
		private String mOutputPath = "";
		private String Output_date = "";
		private int MSG_STROAGE_LOW = -1;
		private int MSG_LOG_CMD_UIEXIST = -2;

		public CopyTask(Context context, File srcFile) {
			mContext = context;
			mSrcFile = srcFile;

		}

		@Override
		protected Integer doInBackground(String... arg0) {
			// Util.setCmd("source /system/etc/savelogmtp.sh");
			String asuslogPath = DumpService.getLogRootpath();
			String intelCrashlogPath = "/sdcard/logs/";
			File asusFile = new File(asuslogPath);
			File intelCrashFile = new File(intelCrashlogPath);
			long asussize = Util.getFolderSize(asusFile);
			long intelCrashsize = Util.getFolderSize(intelCrashFile);
			long intelsize = 0;

			String intelInfoSrcPath = "/data/logcat_log/";
			String gpsLogPathString = "/data/gps/log";
			String btLogPathString = "/sdcard/btsnoop_hci.log";
			String modemCrashlog = "/data/ramdump/";
			String Nativecrashlog = "/data/tombstones/";
			String mdcrash = "/sdcard/modemcrash.txt";
			String dumpsyslog = "/data/Asuslog";

			File intelInfofile = new File(intelInfoSrcPath);
			File fileGps = new File(gpsLogPathString);
			File fileBT = new File(btLogPathString);
			File fileModemCrash = new File(modemCrashlog);
			File fileNativeCrash = new File(Nativecrashlog);
			File filemdcrash = new File(mdcrash);
			File fileDumpsys = new File(dumpsyslog);

			if (intelInfofile.exists() || fileGps.exists()) {
				boolean result = Util.setCmd("chmod -R 777 " + intelInfoSrcPath
						+ " | chmod -R 777 " + gpsLogPathString
						+ " | chmod -R 777 " + modemCrashlog);
				if (result == false) {
					return new Integer(MSG_LOG_CMD_UIEXIST);
				}
				intelsize = Util.getFolderSize(intelCrashFile);
			}

			long totalsize = (asussize + intelCrashsize + intelsize) / 1024 / 1024;

			if (totalsize >= Util.getDiskSize(asuslogPath) + 10) {// storage is
																	// not
																	// enough
				return new Integer(MSG_STROAGE_LOW);
			}
			String output = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/AsusOutputLog/";
			mOutputPath = "/sdcard/AsusOutputLog/";
			if (DumpService.isSaveMicroSD(mContext)) {
				output = Util.getMicroSDPath(mContext) + "/AsusOutputLog/";
				mOutputPath = "/Removable/MicroSD/AsusOutputLog/";
			}

			File file = new File(output);
			if (file.exists() == false) {
				file.mkdirs();
				UpdateMedia.addfolder(mContext, file);
			}
			;
			Output_date = Util.getDate();
			File outputDir = new File(output + Output_date);
			mOutput = outputDir.getAbsolutePath();
			mOutputPath = mOutputPath + Output_date;
			if (asusFile.exists()) {
				Util.copyFile(this, mContext, mMainHandler, asusFile, outputDir);
			}

			if (intelCrashFile.exists()) {
				Util.copyFile(this, mContext, mMainHandler, intelCrashFile,
						outputDir);
			}
			if (intelInfofile.exists()) {
				Util.copyFile(this, mContext, mMainHandler, intelInfofile,
						new File(outputDir, "logcat_log"));
			}

			File fileDataAnrFile = new File("/data/anr");

			log("anr:" + fileDataAnrFile.exists());

			if (fileDataAnrFile.exists() == true) {

				Util.copyFile(this, mContext, mMainHandler, fileDataAnrFile,
						new File(outputDir, "anr"));

			}

			if (fileGps.exists() == true) {
				Util.copyFile(this, mContext, mMainHandler, fileGps, new File(
						outputDir, "gps"));
			}

			if (fileBT.exists() == true) {
				//Util.copyFile(this, mContext, mMainHandler, fileBT, new File(
				//		outputDir, "BT"));
				Util.setCmd("cp -rf /sdcard/bt* " + mOutputPath);//ward_du20150909 copy all bt_snoop log
			}

			if (fileNativeCrash.exists() == true) {
				Util.copyFile(this, mContext, mMainHandler, fileNativeCrash,
						new File(outputDir, "tombstones"));
			}

			if (filemdcrash.exists() == true) {
				Util.copyFile(this, mContext, mMainHandler, filemdcrash,
						new File(outputDir, "modemcrash"));
			}

			if (fileModemCrash.exists() == true) {
				Util.copyFile(this, mContext, mMainHandler, fileModemCrash,
						new File(outputDir, "ramdump"));
			}

			if (fileDumpsys.exists() == true) {
				Util.copyFile(this, mContext, mMainHandler, fileDumpsys,
						new File(outputDir, "dumpsys"));
			}

			Util.setCmd("cp -r /asdf " + mOutputPath);
			return new Integer(0);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			log("onPostExecute");
			if (result == MSG_STROAGE_LOW) {
				showCopyPathDialog(mContext, "Error", "stroage is not enough");
				mProgressDialog.dismiss();
				mCopyTask = null;
				mProgressDialog = null;
				return;
			}
			if (result == MSG_LOG_CMD_UIEXIST) {
				showCopyPathDialog(mContext, "Error", "deamon do not exist.");
				mProgressDialog.dismiss();
				mCopyTask = null;
				mProgressDialog = null;
				return;
			}
			if (isCancelled() == true) {
				return;
			}
			mProgressDialog.dismiss();
			mCopyTask = null;
			mProgressDialog = null;
			if (Settings.isOutputAndClear()) {
				Toast.makeText(mContext, "Copy Complete! and clear Data",
						Toast.LENGTH_SHORT).show();
				clearlog(mOutput);
			} else {
				showCopyPathDialog(mContext, "Copy Complete!", mOutputPath);
			}

		}

	}

	public static void showCopyPathDialog(Context context, String title,
			String output) {
		final Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(title);
		// 寤虹珛閬告搰鐨勪簨浠�
		dialog.setMessage(output);
		dialog.setNeutralButton(
				context.getResources().getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
		dialog.show();
	}

	public void updateFile(Context context, File targetFile) {

		if (targetFile.isDirectory()) {
			UpdateMedia.addfolder(context, targetFile);
			File[] childrenFiles = targetFile.listFiles();
			for (File srcfile : childrenFiles) {
				updateFile(context, srcfile);
			}
		} else {
			UpdateMedia.addFile(context, targetFile);
		}
	}

	public void closeAll() {
		for (BaseLog base : mBaseLogList) {
			base.onCancelAll();
		}
	}

	public static void log(String message) {
		Log.v(TAG, message);
	}

	private void closeDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Util.MSG_UPDATE_COPY_FILE:
			if (mProgressDialog != null) {
				mProgressDialog.setMessage((String) msg.obj);
			}
		case MSG_UPDATE_DELETE:
			if (mProgressDialog != null) {
				mProgressDialog.setMessage((String) msg.obj);
			}
			break;
		case MSG_SHOW_PROGRESSBAR:
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				break;
			}
			showProgress("clear log", "waiting...");
			break;
		case MSG_CLOSE_PROGRESSBAR:
			closeDialog();
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		log("cancel");
		if (mCopyTask != null) {
			mCopyTask.cancel(true);
			mCopyTask = null;
		}
		if (mDeleteTask != null) {
			mDeleteTask.cancel(true);
			mDeleteTask = null;
		}

		mProgressDialog = null;

	}

}
