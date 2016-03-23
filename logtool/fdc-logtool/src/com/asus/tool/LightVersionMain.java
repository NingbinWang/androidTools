package com.asus.tool;


import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.asus.fdclogtool.R;
import com.asus.log.BaseLog;
import com.asus.log.GeneralLog;
import com.asus.tool.DebuggerMain.CopyTask;


public class LightVersionMain extends Activity implements OnClickListener, OnCheckedChangeListener,Handler.Callback{
	Switch mOpenLogSwitch;
	Switch mPugSwitch;
	static final String KEY_FILTER_PROP="persist.asus.logtool.filter";
	static final String KEY_PUG_PROP="persist.asus.logtool.pug";
	static final String KEY_STATE_OPEN_LOG="asus.intent.action.filter.open";
	static final String KEY_STATE_CLOSE_LOG="asus.intent.action.filter.close";
	IServiceLog mIServiceLog;
	public static final boolean mBuildLight=false;
	ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_light_main);
		DebuggerMain.startInitConfigService(this);
		if(LightVersionMain.mBuildLight==false){
			Toast.makeText(this, "build formal version error", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		DebuggerMain.initAsusConfig(this);
		
		if(Settings.isAllowAppOpen()==false){
			Toast.makeText(this, "monkey test...no allow open", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		mOpenLogSwitch=(Switch) findViewById(R.id.light_switch_id);
		mPugSwitch=(Switch) findViewById(R.id.pug_switch_id);
		
		
		if(BaseLog.getPropCheck(GeneralLog.KEY_KERNEL)){
			mOpenLogSwitch.setChecked(true);
			if(BaseLog.getPropCheck(KEY_FILTER_PROP)==false){
				BaseLog.setPropCheck(KEY_FILTER_PROP, true);
			}
		}else{
			if(BaseLog.getPropCheck(KEY_FILTER_PROP)==true){
				BaseLog.setPropCheck(KEY_FILTER_PROP, false);
			}
		}
		
		if(BaseLog.getPropCheck(KEY_PUG_PROP)){
			mPugSwitch.setChecked(true);
		}
		
		mOpenLogSwitch.setOnCheckedChangeListener(this);
		mPugSwitch.setOnCheckedChangeListener(this);
		DebuggerMain.initConfigServie(this,mReceiver,mConnection);
	}
	
	
	private ServiceConnection mConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service) {
			mIServiceLog = IServiceLog.Stub.asInterface(service);

		}

		public void onServiceDisconnected(ComponentName className) {};

	};

	BroadcastReceiver mReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action=intent.getAction();
			if(action.equals(DumpService.ACTION_MAIN_DISKLOW_CLOSE))
			{
				mOpenLogSwitch.setOnCheckedChangeListener(null);
				mPugSwitch.setOnCheckedChangeListener(null);
				mOpenLogSwitch.setChecked(false);
				mPugSwitch.setChecked(false);
				mOpenLogSwitch.setOnCheckedChangeListener(LightVersionMain.this);
				mPugSwitch.setOnCheckedChangeListener(LightVersionMain.this);
			}
			
		}
	
	};
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.light_switch_id:
			boolean state=mOpenLogSwitch.isChecked();
			mOpenLogSwitch.setChecked(state=!state);
			break;
		case R.id.pug_switch_id:
			state=mPugSwitch.isChecked();
			mPugSwitch.setChecked(state=!state);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			if(isDiskValidShowToast()==false){
				buttonView.setOnCheckedChangeListener(null);
				buttonView.setChecked(false);
				buttonView.setOnCheckedChangeListener(this);
				return;
			}
		}
		
		if(buttonView==mOpenLogSwitch)
		{
			GeneralLog.forceState(isChecked);
			if(mIServiceLog!=null)
			{
				try {
					mIServiceLog.setLogAllEnable(isChecked);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Intent intent=null;
			if(isChecked){
				intent=new Intent(KEY_STATE_OPEN_LOG);;
			}else{
				intent=new Intent(KEY_STATE_CLOSE_LOG);;
			}
			BaseLog.setPropCheck(KEY_FILTER_PROP, isChecked);
			sendBroadcast(intent);
			Toast.makeText(this, "broadcast log change", Toast.LENGTH_SHORT).show();
		}
		
		if(buttonView==mPugSwitch){
			BaseLog.setPropCheck(KEY_PUG_PROP, isChecked);
			
		}
		
		
	}
	
	 public boolean isDiskValidShowToast(){
		 if(Util.isDiskAllowOpen( DumpService.getLogRootpath())==false){
			 Toast.makeText(this, "Run out of disk sapce", Toast.LENGTH_SHORT).show();
			 return false;
		 }
		return true;
	 }


	@Override
	public boolean handleMessage(Message arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
