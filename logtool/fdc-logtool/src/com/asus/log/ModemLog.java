package com.asus.log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.content.Intent;
import android.content.IntentFilter;
import java.lang.String;

import android.hardware.SerialManager;
import android.hardware.SerialPort;
import android.net.LocalSocket;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.asus.tool.ATCommandActivity;
import com.asus.tool.DebuggerMain;
import com.asus.tool.DumpService;
import com.asus.tool.DumpSyslog;
import com.asus.tool.SerialPortControl;
import com.asus.tool.Settings;
import com.asus.tool.UpdateMedia;
import com.asus.tool.Util;
import com.asus.fdclogtool.R;
import android.app.AlertDialog.Builder;

public class ModemLog extends BaseLog implements OnCheckedChangeListener,
		Handler.Callback {

	private static final String TAG = "ModemLog";
	private static final String EXPERT_PROPERTY = "persist.service.amtl.expert";
	public static final String KEY_MODEM_OFFLINE_LOGGING = "persist.asuslog.modem.enable";
	public static final String KEY_MODEM_SIZE = "persist.asuslog.modem.size";
	public static final String KEY_MODEM_COUNT = "persist.asuslog.modem.count";
	public static final String KEY_SYSTEM_PROXY = "persist.system.at-proxy.mode";
	public static final String KEY_SYSTEM_CFG = "persist.asuslog.modem.diacfg";
	public static final String MODEM_RAMDUMP = "persist.asuslog.modem.ramdumps";

	private Switch mModemOfflineSwitch;
	private Switch mModemCrashSwitch;
	static final String AT_END = "\r\n";

	//
	// public static final String
	// AT_SET_XSYSTRACE_LEVEL_BB_3G="at+xsystrace=0,\"bb_sw=1;3g_sw=1;digrfx=0\",,\"oct=4\"";
	public static final String AT_SET_XSYSTRACE_LEVEL_BB = "at+xsystrace=0,\"bb_sw=1;3g_sw=0;digrfx=0\",\"bb_sw=sdl:Si,tr,pr,st,db,lt,li,gt\",\"oct=4\"";
	public static final String AT_SET_XSYSTRACE_LEVEL_BB_3G = "at+xsystrace=0,\"bb_sw=1;3g_sw=1;digrfx=0\",\"bb_sw=sdl:Si,tr,pr,st,db,lt,li,gt\",\"oct=4\"";
	public static final String AT_SET_XSYSTRACE_LEVEL_BB_3G_DIGRF = "at+xsystrace=0,\"bb_sw=1;3g_sw=1;digrfx=1;3g_dsp=1\",\"bb_sw=sdl:Si,tr,pr,st,db,lt,li,gt;digrfx=0x03\",\"oct=4\"";
	// \r\n
	// public static final String
	// AT_SET_XSYSTRACE_LEVEL_BB_3G_DIGRF="at+xsystrace=0,\"digrfx=1;bb_sw=1;3g_sw=1\",\"digrfx=0x03\",\"oct=4\"";
	public static final String AT_SET_XSYSTRACE_RATE = "at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=0\"";// decode
																															// normal
																															// keypoint

	public static final String AT_TRACE_CHECK1_SUCCESS = "AT+TRACE?";
	public static final String AT_TRACE_CHECK2_SUCCESS = "at+xsystrace=10";

	public static final String AT_TRACE_CHECK1_RETURN = "115200";
	public static final String AT_TRACE_CHECK2_RETURN = "Oct";

	// public static final String CLOSE_MODEM_CMD0="AT+TRACE=0";
	// public static final String CLOSE_MODEM_CMD1="AT+XSYSTRACE=0";
	// public static final String CLOSE_MODEM_CMD2="at@xl1:dsptrace_stop(1)";//
	// public static final String CLOSE_MODEM_CMD3="at@xl1:sc_config_3g(0)";
	// //DISABLE 3G Speech logging: SUCCESS OK
	// public static final String[] CLOSE_CMD_RETURN=new
	// String[]{"OK","OK","OK","OK"};
	public static final String AT_BASIC_SUCCESS = "OK";

	public static String[] I2S_CMD_PCM = new String[] {
			"at@xl1:dsptrace_stop(1)", "at@xl1:sc_config_3g(1)",
			"at@xl1:xllt_set_template(0,{AUD_HW_PROBE_I2S1_RX})",
			"at@xl1:xllt_set_template(0,{AUD_HW_PROBE_I2S1_TX})",
			"at@xl1:xllt_set_template(1,{AUD_HW_PROBE_I2S1_RX})",
			"at@xl1:xllt_set_template(1,{AUD_HW_PROBE_I2S1_TX})",
			"at@xl1:sc_start(shared_mem)", };

	public static String[] I2S_CMD = new String[] {
			"at@xl1:xllt_set_template(0,{AUD_HW_PROBE_I2S1_RX})",
			"at@xl1:xllt_set_template(0,{AUD_HW_PROBE_I2S1_TX})",
			"at@xl1:xllt_set_template(1,{AUD_HW_PROBE_I2S1_RX})",
			"at@xl1:xllt_set_template(1,{AUD_HW_PROBE_I2S1_TX})", };

	public static String[] DELAYCHECK = new String[] {
			"at@xl1:sc_config_3g(1)", "at@xl1:sc_config_3g(0)",
			"at@xl1:dsptrace_stop(1)", "at@xl1:sc_start(shared_mem)",

	};

	public static String[] CLOSE_MODEM_CMD = new String[] { "AT+TRACE=0",
			"AT+XSYSTRACE=0", };

	public static String[] CLOSE_MODEM_PCM_CMD = new String[] {
			"at@xl1:xllt_set_template(0,{basic})", "AT+TRACE=0",
			"AT+XSYSTRACE=0", "at@xl1:dsptrace_stop(1)",
			"at@xl1:sc_config_3g(0)",

	};

	public static final String PCM_CMD0 = "at@nvm:fix_cps.stack_masks.uas_masks.umac_mask=0x21";
	public static final String PCM_CMD1 = "at@nvm:fix_cps.u8IsSpeechCoeffLoggingEnabled=1";
	public static final String PCM_CMD2 = "at+xl1set=\"L9\"";
	public static final String PCM_CMD3 = "at@xl1:sc_config_3g(1)";// ENABLE 3G
																	// Speech
																	// logging:SUCCESS
																	// OK
	public static final String PCM_CMD4 = "at@xl1:sc_start(shared_mem)";
	public static final String PCM_CMD5 = "at@xl1:xllt_set_template(1,{basic})";

	public static final String[] PCM_CMD = new String[] { PCM_CMD0, PCM_CMD1,
			PCM_CMD2, PCM_CMD3, PCM_CMD4, PCM_CMD5 };
	public static final String[] PCM_SUCCESS_RETURN = new String[] { "OK",
			"OK", "OK", "OK", "OK", "OK" };

	public static final String START_MODEM_CMD = "AT+TRACE=1";
	public static final String START_MODEM_CMD_RETURN = "OK";
	ProgressDialog mProgressDialog;
	private static final int MSG_CLOSE_BAR = 1;
	private static final int MSG_MODEM_CONNECT_FAIL = 2;
	private static final int MSG_MODEM_SWITCH_FAIL = 3;
	private static final int MSG_MODEM_PCM_FAIL = 4;
	private static final int MSG_MODEM_PCM_SUCESS = 5;
	private static final int MSG_MODEM_RESET_MODEM = 6;
	private Handler mMainHandler = new Handler(this);
	public static final int MSG_CLOSE_DIALOG = 7;
	public static final int MSG_CLOSE_SWITCH_MODEM = 8;
	public static final int MSG_CLOSE_HANDLE = 9;
	public static int mConnectCount = 0;
	private AlertDialog mAlertDialog;
	public static final boolean MODEM_EXTERNEL_FIX_PATH = true;
	Thread mThread;
	boolean mPause;
	public boolean mResetModem = false;
	private SerialPortControl mSerialPortControl;
	private ModemLogTrigger mTrigger;
	public static String MODEM_CHECK_READY = "at+xsio?";
	public static final String INPUT_PROP = "/dev/mdmTrace";
	private static final String OUTPUT_TYPE = "f";
	public static final int START_MODEM_MODE = 1;

	public static final int PCM_ONLY_MODE = 2;
	private boolean TEST = false;
	public static final int LOG_LEVEL_MODE = 3;
	public static final int CLOSE_MODEM_MODE = 4;
	public static final int I2CS_PHONE_MODE = 5;
	public static final int CHECK_RESTART_MODEM_MODE = 6;
	public static final String MODEM_OPEN_PORT = "system.at-proxy.mode";// "system.at-proxy.mode";

	public ModemLog(Activity activity, View view) {
		super(activity, view);
		mModemOfflineSwitch = (Switch) view
				.findViewById(R.id.switch_logging_over_HSI_id);
		view.findViewById(R.id.modem_size_layout).setOnClickListener(this);
		view.findViewById(R.id.modem_setting_layout).setOnClickListener(this);
		mModemOfflineSwitch.setChecked(isModemLogEnable());
		mModemOfflineSwitch.setOnCheckedChangeListener(this);
		view.findViewById(R.id.modem_Offline_logging_layout)
				.setOnClickListener(this);
		mModemCrashSwitch = (Switch) view.findViewById(R.id.main_switch_id);
		mModemCrashSwitch.setChecked(getPropCheck(MODEM_RAMDUMP));
		mModemCrashSwitch.setOnCheckedChangeListener(this);
		view.findViewById(R.id.at_command_layout).setOnClickListener(this);
		view.findViewById(R.id.RelativeLayout01).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.modem_Offline_logging_layout:
			boolean state = mModemOfflineSwitch.isChecked();
			mModemOfflineSwitch.setChecked(state = !state);
			break;
		case R.id.modem_setting_layout:
			if (isModemLogEnable()) {
				Toast.makeText(mActivity, "please close modem log",
						Toast.LENGTH_SHORT).show();
				return;
			}
			onModemSettingSelect();
			break;

		case R.id.modem_size_layout:
			if (isModemLogEnable()) {
				Toast.makeText(mActivity, "please close modem log",
						Toast.LENGTH_SHORT).show();
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle("Modem Size");

			View view = mActivity.getLayoutInflater().inflate(
					R.layout.modem_sizes, null);
			final EditText editExternelSize = (EditText) view
					.findViewById(R.id.editText_externel_size);
			final EditText editExternelCount = (EditText) view
					.findViewById(R.id.editfile_externel_count);
			final String stringExternelSize = SystemProperties.get(
					"persist.asuslog.modem.size", "200");
			final String stringExternelCount = SystemProperties.get(
					"persist.asuslog.modem.count", "3");
			editExternelSize.setHint(stringExternelSize);
			editExternelCount.setHint(stringExternelCount);

			builder.setView(view);
			builder.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (editExternelSize.getText().equals(stringExternelSize) == false
							&& editExternelSize.length() > 0) {
						int value = Integer.valueOf(editExternelSize.getText()
								.toString());
						String sizeVal = editExternelSize.getText().toString();
						if (value == 0) {
							Toast.makeText(mActivity, "parameter need > 0",
									Toast.LENGTH_SHORT).show();
							SystemProperties.set("persist.asuslog.modem.size",
									"200");
						} else {
							SystemProperties.set("persist.asuslog.modem.size",
									sizeVal);
						}
					}
					if (editExternelCount.getText().equals(stringExternelCount) == false
							&& editExternelCount.length() > 0) {
						int value = Integer.valueOf(editExternelCount.getText()
								.toString());
						String countVal = editExternelCount.getText()
								.toString();
						if (value == 0) {
							Toast.makeText(mActivity, "parameter need > 0",
									Toast.LENGTH_SHORT).show();
							SystemProperties.set("persist.asuslog.modem.count",
									"3");
						} else {
							SystemProperties.set("persist.asuslog.modem.count",
									countVal);
						}
					}

					if (isModemLogEnable()) {
						stopService();
						startService(mActivity);
					}
				}
			});
			builder.setNegativeButton("Cancel", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			builder.show();
			break;
		case R.id.at_command_layout:
			boolean modemcrash_state = mModemCrashSwitch.isChecked();
			mModemCrashSwitch.setChecked(modemcrash_state = !modemcrash_state);
			break;
		case R.id.RelativeLayout01:
			AlertDialog.Builder modemcrash_builder = new AlertDialog.Builder(
					mActivity);
			modemcrash_builder.setTitle("set command to make modem crash");

			View modemcrash_view = mActivity.getLayoutInflater().inflate(
					R.layout.modem_crash, null);
			final EditText modemcrash_cmd = (EditText) modemcrash_view
					.findViewById(R.id.modem_crash_cmdsetting);
			final String stringcmd = SystemProperties.get(
					"persist.asuslog.crash.count", "");
			modemcrash_cmd.setHint(stringcmd);

			modemcrash_builder.setView(modemcrash_view);
			modemcrash_builder.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String modemcrash_command = modemcrash_cmd.getText()
							.toString();
					if (!modemcrash_command.equals("")) {
						SystemProperties.set("persist.asuslog.crash.count",
								modemcrash_command);
						Util.setCmd("\"/system/bin/raw_sender "
								+ modemcrash_command + "\"");
					} else {
						Util.setCmd("\"/system/bin/raw_sender "
								+ SystemProperties.get(
										"persist.asuslog.crash.count",
										"75 37 3 0 2") + "\"");
					}
				}

			});
			modemcrash_builder.setNegativeButton("Cancel",
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			modemcrash_builder.show();
			break;
		default:
			break;
		}

	}

	private void onModemSettingSelect() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.title_modem_setting);
		View view = mActivity.getLayoutInflater().inflate(
				R.layout.modem_setting, null);
		final RadioButton radioButtonLevelbb = (RadioButton) view
				.findViewById(R.id.radioButtonLevelbb);
		final RadioButton radioButtonLevel3g = (RadioButton) view
				.findViewById(R.id.radioButtonLevel3g);
		final RadioButton radioButtonLeveldigrf = (RadioButton) view
				.findViewById(R.id.radioButtonLeveldigrf);
		final RadioButton radioButtonLeveldigrf1 = (RadioButton) view
				.findViewById(R.id.radioButtonLeveldigrf1);
		final RadioButton radioButtonCompact = (RadioButton) view
				.findViewById(R.id.radioButtonCompact);
		// final CheckBox checkboxSystemProxy=(CheckBox)
		// view.findViewById(R.id.check_system_at_proxy);
		// final String
		// systemProxy=SystemProperties.get("persist.asuslog.modem.ramdumps");
		// if(systemProxy==null){
		// checkboxSystemProxy.setVisibility(View.GONE);
		// }else if(systemProxy.equals("1")){
		// checkboxSystemProxy.setChecked(true);
		// }else if(systemProxy.equals("0")){
		// checkboxSystemProxy.setChecked(false);
		// }

		if (SystemProperties.get(KEY_SYSTEM_CFG, "/system/etc/Diag.cfg")
				.equals("/system/etc/Diag.cfg")) {
			radioButtonLevelbb.setChecked(true);
		} else if (SystemProperties.get(KEY_SYSTEM_CFG, "/system/etc/Diag.cfg")
				.equals("/system/etc/modem_and_audio.cfg")) {
			radioButtonLevel3g.setChecked(true);
		} else if (SystemProperties.get(KEY_SYSTEM_CFG, "/system/etc/Diag.cfg")
				.equals("/system/etc/gps.cfg")) {
			radioButtonLeveldigrf.setChecked(true);
		} else if (SystemProperties.get(KEY_SYSTEM_CFG, "/system/etc/Diag.cfg")
				.equals("/system/etc/audio.cfg")) {
			radioButtonLeveldigrf1.setChecked(true);
		} else if (SystemProperties.get(KEY_SYSTEM_CFG, "/system/etc/Diag.cfg")
				.equals("/system/etc/Compact_mode.cfg")) {
			radioButtonCompact.setChecked(true);
		} else {
			if (Util.isUserBuild())
				radioButtonCompact.setChecked(true);
			else
				radioButtonLevelbb.setChecked(true);
		}

		builder.setView(view);
		builder.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if (radioButtonLevelbb.isChecked()) {
					SystemProperties
							.set(KEY_SYSTEM_CFG, "/system/etc/Diag.cfg");
				} else if (radioButtonLevel3g.isChecked()) {
					SystemProperties.set(KEY_SYSTEM_CFG,
							"/system/etc/modem_and_audio.cfg");
				} else if (radioButtonLeveldigrf.isChecked()) {
					SystemProperties.set(KEY_SYSTEM_CFG, "/system/etc/gps.cfg");
				} else if (radioButtonLeveldigrf1.isChecked()) {
					SystemProperties.set(KEY_SYSTEM_CFG,
							"/system/etc/audio.cfg");
				} else if (radioButtonCompact.isChecked()) {
					SystemProperties.set(KEY_SYSTEM_CFG,
							"/system/etc/Compact_mode.cfg");
				}

				// if(checkboxSystemProxy.getVisibility()==View.VISIBLE){
				// String value="0";
				// if(checkboxSystemProxy.isChecked()){
				// value="1";
				// }
				// if(value.equals(systemProxy)==false){
				// if(value == "1"){
				// Util.setCmd("ssr_setup modem");
				// SystemProperties.set("persist.asuslog.modem.ramdumps", "1");
				// }else{
				// Util.setCmd("ssr_setup");
				// SystemProperties.set("persist.asuslog.modem.ramdumps", "0");
				// }
				// }
				// }
			};
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub

			};
		});
		builder.show();
	}

	public static boolean updateLogConfig(boolean isSaveMicroSD) {
		boolean result = false;
		if (isSaveMicroSD) {
			result = needUpdate(Settings.getModemMicroSdcardSize(),
					Settings.getModemMicroSdcardCount());
		} else {
			result = needUpdate(Settings.getModemExternelSdcardSize(),
					Settings.getModemExternelSdcardCount());
		}
		return result;
	}

	public static int getModemSizebySetting(int settingsize) {
		return settingsize * 1000;
	}

	private static boolean needUpdate(int settingSize, int settingCount) {
		boolean change = false;

		int size = getModemSize();
		int count = getRotateNum();
		if (getModemSizebySetting(settingSize) != size) {
			SystemProperties.set(KEY_MODEM_SIZE,
					String.valueOf(getModemSizebySetting(settingSize)));
			change = true;
		}
		if (settingCount != count) {
			SystemProperties.set(KEY_MODEM_COUNT, String.valueOf(settingCount));
			change = true;
		}

		return change;
	}

	@Override
	public void onSelectAll() {

		super.onSelectAll();
		mModemOfflineSwitch.setChecked(true);
		mModemCrashSwitch.setChecked(true);
	}

	@Override
	public void onCancelAll() {

		super.onCancelAll();
		mModemOfflineSwitch.setChecked(false);
		mModemCrashSwitch.setChecked(false);
	}

	public static void log(String message) {
		Log.v(TAG, message);
	}

	@Override
	public void onDestroy() {

	}

	@Override
	public void onResume() {
		mPause = false;
	}

	private boolean openDevice() {
		if (mSerialPortControl == null) {
			mSerialPortControl = new SerialPortControl(mActivity, mMainHandler);

			boolean serial = mSerialPortControl.openSericalPort();
			if (serial == false) {
				mModemOfflineSwitch.setEnabled(false);
				close();
				return false;
			}
			mTrigger = new ModemLogTrigger(mActivity, mSerialPortControl, true,
					mMainHandler);
		}

		return true;

	}

	public void close() {
		if (mSerialPortControl != null) {
			mSerialPortControl.close();
			mSerialPortControl = null;
		}
		mTrigger = null;
	}

	@Override
	public void onPause() {
		mPause = true;
		close();
	}

	public static void showFailMsg(Context context, String exceptReason) {
		Toast.makeText(context, "Modem connection failed  msg:" + exceptReason,
				Toast.LENGTH_SHORT).show();
	}

	public void forceStopModemLog() {
		switchModemLogNoTrigerChange(false);
		forceStop();
	}

	public static void forceStop() {
		SystemProperties.set("persist.asuslog.qxdmlog.enable", "0");
	}

	public static void disableProp() {
		setPropCheck(KEY_MODEM_OFFLINE_LOGGING, false);
	}

	public static boolean isModemLogEnable() {
		String value = SystemProperties.get("persist.asuslog.qxdmlog.enable",
				"0");
		if (value.equals("1"))
			return true;
		else
			return false;
	}

	private void closeDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	public void switchModemLogNoTrigerChange(boolean checked) {
		mModemOfflineSwitch.setOnCheckedChangeListener(null);
		mModemOfflineSwitch.setChecked(checked);
		mModemOfflineSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == mModemOfflineSwitch) {
			if (isChecked) {
				String dir = "/sdcard/Asuslog/Modem";
				File file = new File(dir);
				if (file.exists() == false) {
					boolean result = file.mkdirs();
					if (result == false) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
					}
				}
				SystemProperties.set("persist.asuslog.qxdmlog.enable", "1");
			} else {
				SystemProperties.set("persist.asuslog.qxdmlog.enable", "0");
			}

		} else {
			if (isChecked) {
				Util.setCmd("ssr_setup modem");
				SystemProperties.set(MODEM_RAMDUMP, "1");
			} else {
				Util.setCmd("ssr_setup");
				SystemProperties.set(MODEM_RAMDUMP, "0");
			}
		}

	}

	public static void createFolder(ContentResolver cr, String dir) {
		File file = new File(dir);
		if (file.exists() == false) {
			if (file.mkdir()) {
				UpdateMedia.addfolder(cr, dir);
			}
			;
		}
	}

	public static String getModemPath(Context context) {
		String dir = DumpService.getLogRootpath();
		ContentResolver cr = context.getContentResolver();
		createFolder(cr, dir);
		String modempath = dir + DebuggerMain.DIR_NAME_MODEM;
		createFolder(cr, modempath);
		if (MODEM_EXTERNEL_FIX_PATH) {
			return modempath + "/bplog";
		} else {
			String modemdatepath = modempath + "/" + Util.getDate();
			createFolder(cr, modemdatepath);
			return modemdatepath + "/bplog";
		}

	}

	public static void stopService() {
		// SystemProperties.set(MtsProperties.MTS_SERVICE, "0");
		SystemProperties.set("persist.asuslog.qxdmlog.enable", "0");
	}

	public static int getRotateNum() {
		return SystemProperties.getInt(KEY_MODEM_COUNT,
				Settings.getModemDefaultCount());
	}

	public static int getModemSize() {
		return SystemProperties.getInt(KEY_MODEM_SIZE,
				getModemSizebySetting(Settings.getModemDefaultSize()));
	}

	public static void startService(Context context) {
		SystemProperties.set("persist.asuslog.qxdmlog.enable", "1");
	}

	public static void setNewPath(Context context) {
		String path = createModemPath(context);
	}

	private static String createModemPath(Context context) {
		boolean isNeedNewFolder = false;
		String logpath = null;
		String rootpath = DumpService.getLogRootpath();
		if (DumpService.isSaveMicroSD(context) && isNeedNewFolder) {
			String modemdatepath = rootpath + DebuggerMain.DIR_NAME_MODEM + "/"
					+ Util.getDate();
			DumpSyslog.dumpsys("mkdir -p " + modemdatepath);
			logpath = modemdatepath + "/bplog";
		} else {
			logpath = getModemPath(context);
		}
		return logpath;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_CLOSE_BAR:
			closeDialog();
			break;
		case MSG_MODEM_SWITCH_FAIL:
			closeDialog();
			// openLogDialog("Modem Warning","Modem 忙碌中...,如有插上SIM卡,建議請先拔掉SIM卡,再重新進行測試;或者先拔掉SIM卡,重新開機,重新測試");
			break;
		case MSG_MODEM_CONNECT_FAIL:

			break;
		case MSG_MODEM_PCM_FAIL:
			mThread = null;
			Toast.makeText(mActivity, "Audio PCM for Modem Log open fail!!",
					Toast.LENGTH_SHORT).show();
			break;
		case MSG_MODEM_PCM_SUCESS:
			mThread = null;
			Toast.makeText(mActivity, "Audio PCM for Modem Log open Success!!",
					Toast.LENGTH_SHORT).show();
			break;
		case MSG_MODEM_RESET_MODEM:
			mResetModem = true;
			break;
		case SerialPortControl.EVENT_RESULT_SUCCESS:
			log("EVENT_RESULT_SUCCESS receiver");
			if (mTrigger != null) {
				mTrigger.onHandleCmdCallBack(msg);
			}
			// onHandleCmdCallBack(msg);
			break;
		case SerialPortControl.EVENT_RESULT_FAIL:
			log("EVENT_RESULT_FAIL receiver");
			if (mTrigger != null) {
				mTrigger.onHandleCmdCallBack(msg);
			}
			// onHandleCmdCallBack(msg);
			break;
		case SerialPortControl.EVENT_SERIAL_NULL:
			Toast.makeText(mActivity, "Seraial NULL!!", Toast.LENGTH_SHORT)
					.show();
			break;
		case SerialPortControl.EVENT_TIME_OUT_NO_RESPONSE:
			closeDialog();
			AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
			dialog.setTitle("Warning");
			dialog.setMessage("Modem Response Time Out");
			dialog.show();
			break;

		case MSG_CLOSE_DIALOG:
			closeDialog();
			break;
		case MSG_CLOSE_SWITCH_MODEM:
			switchModemLogNoTrigerChange(false);
			closeDialog();
			break;
		default:
			break;
		}
		return false;
	}
}
