package com.asus.testtool.tab;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.asus.testtool.R;
import com.asus.testtool.info.InfoUpdate2;
import com.asus.testtool.unittest.AsusTest;
import com.asus.testtool.unittest.BatUnittestThread;
import com.asus.testtool.unittest.CpuUnittestThread;
import com.asus.testtool.unittest.GpuUnittestThread;
import com.asus.testtool.unittest.UnittestThread;

import java.util.ArrayList;
import java.util.List;




public class UnittestTab extends Activity implements OnCheckedChangeListener{

	Thread countToTen;
	
	ToggleButton thermal_cpuButton, thermal_gpuButton, thermal_batButton, testButton;
	TextView test_state_msg, txtCount, CpuTest, GpuTest, BatTest;
	TextView ErrorMsg, CpuErrorMsg, GpuErrorMsg, BatErrorMsg; 
	String ErrorMsgTest="Tina is the most beautiful woman in the world!";
	UnittestThread mUnittestThread;
	CpuUnittestThread mCpuUnittestThread;
	GpuUnittestThread mGpuUnittestThread;
	BatUnittestThread mBatUnittestThread;
	
	Spinner mUI_testitem_spnr;
	Button mUI_go_btn;
	ArrayAdapter<CharSequence> mAdapter;
	static AsusTest s_AsusTest;
	static boolean s_IsTestCanceled = false;
	private static boolean bTestResult=true;
	
	public boolean flag = false; 
	
	public static InfoUpdate2 infoUpdate = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		 
			
			
		setContentView(R.layout.unittest_tab);
		
		s_AsusTest = new AsusTest();
		mUI_testitem_spnr = (Spinner) findViewById(R.id.selectionCTSItems);
		mUI_go_btn = (Button) findViewById(R.id.goBtn);
	
        // Setup Spinner items
        List<CharSequence> planets = new ArrayList<CharSequence>();
        mAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, planets);
		mUI_testitem_spnr.setAdapter(mAdapter);
		
		// Add Test items. Default selec No. 10 stressBurstSnapshot 
		for (int i=0;i<s_AsusTest.m_CTSItems.length;i++) 
			mAdapter.add(s_AsusTest.m_CTSItems[i][1]); // CTSItems[i][0]+"#"+CTSItems[i][1]
		mUI_testitem_spnr.setSelection(0);
		
		// Setup Go button click event handler
        mUI_go_btn.setOnClickListener( new View.OnClickListener() {
	    	int iSelectItem=-1;
			@Override
			public void onClick(View v) {
				if ( mUI_testitem_spnr.getSelectedItem() != null ) {
					iSelectItem = mUI_testitem_spnr.getSelectedItemPosition();
				}
				//setProgressBarIndeterminateVisibility(true);
				
				mUI_testitem_spnr.setEnabled(false);
				mUI_go_btn.setEnabled(false);
				
				new Thread(new Runnable() {
					public void run() {
						s_IsTestCanceled = false;
						if(iSelectItem==0){
							
						}
						
						//bTestResult=s_AsusTest.runtestitem(iSelectItem);
				        
		                //Message msg = new Message();
		                //mCloseHandler.sendMessage(msg);
					}
				}).start();      
	
			}
        }); // mUI_go_btn setOnClickListener
		
		
		
		
		
		registerReceiver(mBroadcast, new IntentFilter("show.info.on"));
		registerReceiver(mBroadcast, new IntentFilter("show.info.off"));
		
		System.setProperty("sys.test.status.cpu_test", "none");
		System.setProperty("sys.test.status.gpu_test", "none");
		System.setProperty("sys.test.status.bat_test", "none");
		System.setProperty("sys.test.errmsg.cpu_test", "Error Msg:");
		System.setProperty("sys.test.errmsg.gpu_test", "Error Msg:");
		System.setProperty("sys.test.errmsg.bat_test", "Error Msg:");
		
		thermal_cpuButton = (ToggleButton) findViewById(R.id.button_thermal_cpu);
		thermal_cpuButton.setOnCheckedChangeListener(this);
		
		thermal_gpuButton = (ToggleButton) findViewById(R.id.button_thermal_gpu);
		thermal_gpuButton.setOnCheckedChangeListener(this);
		
		thermal_batButton = (ToggleButton) findViewById(R.id.button_thermal_bat);
		thermal_batButton.setOnCheckedChangeListener(this);
		
		testButton = (ToggleButton) findViewById(R.id.button_test);
		testButton.setOnCheckedChangeListener(this);
		
		CpuTest = (TextView) findViewById(R.id.text_cpu_state_msg);
		CpuErrorMsg = (TextView) findViewById(R.id.text_cpu_error_msg);
		
		GpuTest = (TextView) findViewById(R.id.text_gpu_state_msg);
		GpuErrorMsg = (TextView) findViewById(R.id.text_gpu_error_msg);
		
		BatTest = (TextView) findViewById(R.id.text_bat_state_msg);
		BatErrorMsg = (TextView) findViewById(R.id.text_bat_error_msg);
		
		ErrorMsg = (TextView) findViewById(R.id.text_error_msg);
		test_state_msg = (TextView) findViewById(R.id.text_test_state_msg);
		
		infoUpdate = new InfoUpdate2(this);
        infoUpdate.execute();
        
       
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		//String value;
		switch(buttonView.getId()){

		case R.id.button_thermal_cpu:
			if(isChecked){	
				mCpuUnittestThread = new CpuUnittestThread(this);
				mCpuUnittestThread.start();
				Toast.makeText(UnittestTab.this,this.getString(R.string.text_show_test),Toast.LENGTH_LONG).show();
				System.setProperty("sys.test.status.cpu_test", "ongoing");
				System.setProperty("sys.test.errmsg.cpu_test", "Error Msg:");
				Log.e("lamda","llll");
				//thermal_cpuButton.setChecked(false);
			}
			else{
				//value="FFFF";
				mCpuUnittestThread.interrupt();
			}
			break;
			
		case R.id.button_thermal_gpu:
			if(isChecked){
				//Log.e("SSSS","MMMMM");
				mGpuUnittestThread = new GpuUnittestThread(this);
				mGpuUnittestThread.start();
				Toast.makeText(UnittestTab.this,this.getString(R.string.text_show_test),Toast.LENGTH_LONG).show();
				System.setProperty("sys.test.status.gpu_test", "ongoing");
				System.setProperty("sys.test.errmsg.gpu_test", "Error Msg:");
			}
			else{
				//value="FFFF";
				mGpuUnittestThread.interrupt();
			}
			break;
		
		case R.id.button_thermal_bat:
			if(isChecked){	
				mBatUnittestThread = new BatUnittestThread(this);
				mBatUnittestThread.start();
				Toast.makeText(UnittestTab.this,this.getString(R.string.text_show_test),Toast.LENGTH_LONG).show();
				System.setProperty("sys.test.status.bat_test", "ongoing");
				System.setProperty("sys.test.errmsg.bat_test", "Error Msg:");
				CheckState();
			}
			else{
				//value="FFFF";
				mBatUnittestThread.interrupt();
			}
			break;
			
		case R.id.button_test:
			
			if(isChecked){	
				mUnittestThread = new UnittestThread(this);
				mUnittestThread.start();
				Toast.makeText(UnittestTab.this,this.getString(R.string.text_show_test),Toast.LENGTH_LONG).show();
				System.setProperty("sys.test.status.cpu_test", "ongoing");
				//value = SystemProperties.get("sys.test.status.cpu_test", "default");
				//UpdateStateMsg();
				//UpdateErrMsg();
			}
			else{
				//value = SystemProperties.get("sys.test.errmsg.cpu_test", "default");
				//value="FFFF";
				//UpdateErrMsg();
				//mUnittestThread.setTesting(false);
				//mUnittestThread.onTest = false;
				mUnittestThread.interrupt();
			}
			
			break;

		}
			
	}
		
	private BroadcastReceiver mBroadcast = new BroadcastReceiver(){
		AlertDialog alert;
		
		@Override
		public void onReceive(Context mContext, Intent mIntent){
			String value;
			value = System.getProperty("sys.test.unit.msg", "default");
			if(alert == null){
				AlertDialog.Builder builder = new AlertDialog.Builder(UnittestTab.this);
				alert = builder.create();
			}
			
			if(mIntent.getAction().equals("show.info.on")){
				alert.setTitle("Warning");
				alert.setMessage(value);
				alert.show();
			}
			if(mIntent.getAction().equals("show.info.off")){
				alert.cancel();
			}
		}
	};
/*
	public void UpdateStateMsg() {
		String value;
		value = SystemProperties.get("sys.test.status.cpu_test", "default");
		test_state_msg.setText(value);
	}
	
	public void UpdateErrMsg() {
		String value;
		value = SystemProperties.get("sys.test.errmsg.cpu_test", "default");
		ErrorMsg.setText(value);
	}
*/	
	public void CheckState() {
		String value;
		value = System.getProperty("sys.test.status.cpu_test", "default");
		Log.e(value,"aaaa");
		Log.e("jim","jim3");
		if((value.compareTo("pass") == 0)){
			Log.e("CheckState","bbbb");
			thermal_cpuButton.setChecked(false);
			Log.e("CheckState","bbbb");
		}else if((value.compareTo("fail") != 0)){
			Log.e("XXXX","cccc");
		}
		else{
			Log.e("YYYY","dddd");
		}
	}
	
	public void updateInfo(String [] arg){
		CpuTest.setText(arg[0]);
		CpuErrorMsg.setText(arg[1]);
		GpuTest.setText(arg[2]);
		GpuErrorMsg.setText(arg[3]);
		BatTest.setText(arg[4]);
		BatErrorMsg.setText(arg[5]);
		test_state_msg.setText(arg[0]);
		ErrorMsg.setText(arg[1]);
	}
	
	//ASUS_BSP+++ jim3_lin "Handler for enable button"
	Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	String str = (String)msg.obj;
        	Log.e("jim","jim1");
        	if (str!=null && str.compareTo("pass")==0){ 
        		Log.e("jim","jim2++");
        		CheckState();
        		Log.e("jim","jim2--");
        	}
        	//Log.d(TAG, "Displayed new tittle.");
        }
    }; 
    
    public void enableUI(String msg2) {
    	//Log.d(TAG, msg);
        Message msg = new Message();
        
        msg.obj = msg2;
        mHandler.sendMessage(msg);    	
    } // void enableUI(String msg)
    //ASUS_BSP--- jim3_lin "Handler for enable button"
	
}





