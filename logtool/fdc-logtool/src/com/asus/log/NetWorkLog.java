package com.asus.log;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.asus.fdclogtool.R;
import com.asus.tool.DebuggerMain;
public class NetWorkLog extends BaseLog implements OnCheckedChangeListener{

	public static final String KEY_TCPDUMP="persist.asuslog.tcpdump.enable";
	private static final String TAG = "TcpDumpLog";
	private Switch mTcpLogSwitch;
	private Switch mWifiSwitch;
	private Switch mBTSwitch;
	private final ContentResolver mContet;
	public NetWorkLog(Activity activity, View view) {
		super(activity, view);
		mContet=activity.getContentResolver();
		mTcpLogSwitch=(Switch) view.findViewById(R.id.switch_tcpdump_id);
		mWifiSwitch=(Switch) view.findViewById(R.id.switch_wifi_id);
		mBTSwitch=(Switch) view.findViewById(R.id.switch_BT_id);
		mTcpLogSwitch.setChecked(getPropCheck(KEY_TCPDUMP));
		//mTcpLogSwitch.setEnabled(false);
		BluetoothAdapter adapter =BluetoothAdapter.getDefaultAdapter();
		mTcpLogSwitch.setOnCheckedChangeListener(this);
		
		mWifiSwitch.setChecked(com.asus.tool.Settings.isWifiEnable());
		mWifiSwitch.setOnCheckedChangeListener(this);
		
		mBTSwitch.setChecked(com.asus.tool.Settings.isBTEnable());
		mBTSwitch.setOnCheckedChangeListener(this);
		if(com.asus.tool.Settings.isBTEnable()==true)
		{
			adapter.configHciSnoopLog(true);
			Settings.Secure.putInt(mContet,Settings.Secure.BLUETOOTH_HCI_LOG,1);
		}
		else
		{
			adapter.configHciSnoopLog(false);
			Settings.Secure.putInt(mContet,Settings.Secure.BLUETOOTH_HCI_LOG,0);
		}

		
		view.findViewById(R.id.tcpdump_layout).setOnClickListener(this);
		view.findViewById(R.id.wifi_layout).setOnClickListener(this);
		view.findViewById(R.id.bluetooth_layout).setOnClickListener(this);
	}
	

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.tcpdump_layout:
			log("switch_tcpdump_id=");
			boolean state=mTcpLogSwitch.isChecked();
			mTcpLogSwitch.setChecked(state=!state);
		
			break;
		case R.id.wifi_layout:
			log("switch_wifi_id=");
			state=mWifiSwitch.isChecked();
			mWifiSwitch.setChecked(state=!state);
		
			break;
		
	    case R.id.bluetooth_layout:
			log("switch_bt_id=");
			state=mBTSwitch.isChecked();
			mBTSwitch.setChecked(state=!state);
		
			break;
		default:
			break;
		}
	}

	 public static void forceStop(){
		 setPropCheck(KEY_TCPDUMP, false);
		 com.asus.tool.Settings.setWifiEnable(false);
		 com.asus.tool.Settings.setBTEnable(false);
	 }
	
	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		super.onSelectAll();
		mTcpLogSwitch.setChecked(true);
		mWifiSwitch.setChecked(true);
		mBTSwitch.setChecked(true);
	}

	public void onSetDefault(){
		mTcpLogSwitch.setChecked(false);
		mWifiSwitch.setChecked(true);
		mBTSwitch.setChecked(true);
	}
	
	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
		mTcpLogSwitch.setChecked(false);
		mWifiSwitch.setChecked(false);
		mBTSwitch.setChecked(false);
	}
	
	public static void log(String message){
		Log.v(TAG, message);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked)
		{
			if(isDiskValidShowToast()==false){
				buttonView.setOnCheckedChangeListener(null);
				buttonView.setChecked(false);
				buttonView.setOnCheckedChangeListener(this);
				return;
			}
		}
		if(buttonView==mTcpLogSwitch){
			setPropCheck(KEY_TCPDUMP, isChecked);
		}
		if(buttonView==mWifiSwitch){
			com.asus.tool.Settings.setWifiEnable(isChecked);
			try {
				mServiceLog.wifiLogEnable(isChecked);
			} catch (RemoteException e) {
				
				Log.e(DebuggerMain.TAG, "error="+e.getMessage());
			}
		}
		if(buttonView==mBTSwitch){
			com.asus.tool.Settings.setBTEnable(isChecked);
			BluetoothAdapter adapter =BluetoothAdapter.getDefaultAdapter();
			if(isChecked==true)
			{
				adapter.configHciSnoopLog(true);
				Settings.Secure.putInt(mContet,Settings.Secure.BLUETOOTH_HCI_LOG,1);
			}
			else
			{
				adapter.configHciSnoopLog(false);
				Settings.Secure.putInt(mContet,Settings.Secure.BLUETOOTH_HCI_LOG,0);
			}

		}
	}
}
