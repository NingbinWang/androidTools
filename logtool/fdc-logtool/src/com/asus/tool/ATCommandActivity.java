package com.asus.tool;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.io.IOException;

import com.asus.log.ModemLog;
import com.asus.log.ModemLogTrigger;
import com.asus.fdclogtool.R;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageParser.NewPermissionInfo;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import android.hardware.SerialManager;
import android.hardware.SerialPort;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.view.View.OnClickListener;

import android.util.Log;
import android.text.method.ScrollingMovementMethod;

public class ATCommandActivity extends Activity implements Runnable {

	private static final String TAG = "ATCommandActivity";

	/* UI */
	private TextView mATResult;
	private AutoCompleteTextView mInputCommand;
	private Button mSendBtn;
	private Button mCancelBtn;
	private Button mClearBtn;
	private Button mAudioBtn;
	private Button mMoMtBtn;
	private Button mLogStateBtn;
	private ByteBuffer mInputBuffer;
	private ByteBuffer mOutputBuffer;

	private SerialManager mSerialManager;
	private SerialPort mSerialPort;
	 
	/* EVENT */
	private static final int EVENT_GET_AT_RESULT = 99;
	private static final int EVENT_GET_FAIl = 100;
	public static final String SERIAL_SERVICE = "serial";
	private ArrayList<String> mCommandList=new ArrayList<String>();
	private boolean mSendContinueCommand=false;
	static final String AT_END="\r\n";
	private boolean mClose=false;
	
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mSerialManager = (SerialManager) getSystemService(SERIAL_SERVICE);
		setContentView(R.layout.activity_atcommand);

		mATResult = (TextView) findViewById(R.id.result);
		mATResult.setMovementMethod(ScrollingMovementMethod.getInstance());
		mSendBtn = (Button) findViewById(R.id.send);
		mCancelBtn = (Button) findViewById(R.id.cancel);
		mClearBtn = (Button) findViewById(R.id.clear);
		mLogStateBtn= (Button) findViewById(R.id.modem_log_state);
		mAudioBtn= (Button) findViewById(R.id.audioTest);
		mMoMtBtn= (Button) findViewById(R.id.mo_mt);
		mInputCommand = (AutoCompleteTextView) findViewById(R.id.input_command);
		mInputCommand.setSelection(3);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.at_command_item, getResources().getStringArray(
						R.array.atcommands_array));
		mInputCommand.setAdapter(adapter);
		mInputBuffer = ByteBuffer.allocate(1024);
		mOutputBuffer = ByteBuffer.allocate(1024);

		mATResult.setText("");

		mCancelBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mInputCommand.setText("");
			}

		});

		mClearBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mATResult.setText("");
			}
		});

		mSendBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if(ModemLogTrigger.isDeviceExist()==false){
					Toast.makeText(ATCommandActivity.this, "gsmtty19 died", Toast.LENGTH_SHORT).show();
					return;
				};
				String command = mInputCommand.getText().toString();

				if (command.length() <= 0) {
					return;
				}

				mInputCommand.setText("");

				Log.d(TAG, "AT Command : " + command);
				command = command + "\r\n";

				
				try {
					byte[] buffer = command.getBytes();

					mInputBuffer.clear();
					mInputBuffer.put(buffer);
					mSerialPort.write(mInputBuffer, buffer.length);
				} catch (IOException e) {
					Log.e(TAG, "Write Failed", e);
					Toast.makeText(ATCommandActivity.this, "fail please retry", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		mAudioBtn.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View arg0) {
				
				
				String command1="at@nvm:fix_cps.stack_masks.uas_masks.umac_mask=0x21"+AT_END;;
				String command2="at@nvm:fix_cps.u8IsSpeechCoeffLoggingEnabled=1"+AT_END;;
				String command3="at+xl1set=\"L9\""+AT_END;
				String command4="at@xl1:sc_config_3g(1)"+AT_END;
				String command5="at@xl1:sc_start(shared_mem)"+AT_END;
				String command6="at@xl1:xllt_set_template(1,{basic})"+AT_END;
				
				mCommandList.clear();
				mCommandList.add(command1);
				mCommandList.add(command2);
				mCommandList.add(command3);
				mCommandList.add(command4);
				mCommandList.add(command5);
				mCommandList.add(command6);
				mSendContinueCommand=true;
				sendCommand(command1);
			}
		});
		
		mMoMtBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				mCommandList.clear();
				for(int i=0;i<ModemLog.I2S_CMD.length;i++){
					mCommandList.add(ModemLog.I2S_CMD[i]);
				}
				
				
				mSendContinueCommand=true;
				sendCommand(ModemLog.I2S_CMD[0]);
			}
			
		});
		
		mLogStateBtn.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View arg0) {
				
				
				
				String command1=ModemLog.AT_TRACE_CHECK1_SUCCESS+AT_END;
				String command2=ModemLog.AT_TRACE_CHECK2_SUCCESS+AT_END;
			
				mCommandList.clear();
				mCommandList.add(command1);
				mCommandList.add(command2);
				
			
				mSendContinueCommand=true;
				sendCommand(command1);
			}
		});
		
	}


	private void sendCommand(String command){
		log("send cmd="+command);
		if(ModemLogTrigger.isDeviceExist()==false){
			Toast.makeText(ATCommandActivity.this, "gsmtty19 died", Toast.LENGTH_SHORT).show();
			return;
		};
		try {
			byte[] buffer = command.getBytes();
			mInputCommand.setText(mCommandList.get(0));
			mInputBuffer.clear();
			mInputBuffer.put(buffer);
			mSerialPort.write(mInputBuffer, buffer.length);
		} catch (IOException e) {
			Log.e(TAG, "Write Failed", e);
			Toast.makeText(ATCommandActivity.this, "fail please retry", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mClose=false;
		openSericalPort();
		
	}

	public boolean openSericalPort()
	{
		if(mSerialManager==null){
			Toast.makeText(this, "SerialManager Empty", Toast.LENGTH_LONG).show();
			finish();
			return false;
		}
		String[] ports = mSerialManager.getSerialPorts();
		log("ports="+ports.length);
		if (ports != null && ports.length > 0) {
			try {
				mSerialPort = mSerialManager.openSerialPort(ports[0], 115200);
				if (mSerialPort != null) {
					new Thread(this).start();
				}
				log("mSerialPort="+mSerialPort);
			} catch (IOException e) {
				Toast.makeText(this, "Open Port Failed "+e.getMessage(), Toast.LENGTH_LONG).show();
				return false;
			}
		}
		if(ports==null || ports.length==0){
			Toast.makeText(this, "modem don't any port", Toast.LENGTH_LONG).show();
			
			return false;
		}
		return true;
	}

	
	public void onHandleSerialEmpty(){
		if(mSerialPort==null){
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setTitle("Error");
			builder.setMessage("SerialPort Empty");
			builder.setPositiveButton("Reconnect", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					openSericalPort();
				}

			});
			builder.setNegativeButton("leave", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					
					ATCommandActivity.this.finish();
				}
			});
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mClose=true;
		if (mSerialPort != null) {
			sendLeave();
			try {
				mSerialPort.close();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
			mSerialPort = null;
		}
	}

	 public void sendLeave(){
	    	String command="AT";
	    	
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
	
	@Override
	public void onDestroy() {
		
		super.onDestroy();
	}

	public void run() {
		Log.d(TAG, "thread run");
		int ret = 0;
		byte[] buffer = new byte[1024];

		while (ret >= 0) {
			try {
				if(mClose){
					return;
				}
				mOutputBuffer.clear();
				if (mSerialPort == null) {
					if(mClose){
						return;
					}
					onHandleSerialEmpty();
					//mHandler.sendEmptyMessage(EVENT_GET_FAIl);
					break;
				}
				ret = mSerialPort.read(mOutputBuffer);
				if(mClose){
					return;
				}
				mOutputBuffer.get(buffer, 0, ret);
			} catch (IOException e) {
				Log.e(TAG, "Read Port Failed", e);
				break;
			}

			if (ret > 0) {
				Message msg=new Message();
				msg.what=EVENT_GET_AT_RESULT;
				
				String text = new String(buffer, 0, ret);
				Log.d(TAG, "Modem Response : " + text);
				msg.obj = text;
				mHandler.sendMessage(msg);
			}
		}
		Log.d(TAG, "thread out");
	}
	private void log(String msg){
		Log.v(TAG, msg);
	}
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_GET_AT_RESULT:
				
				mATResult.setText(mATResult.getText() + (String) msg.obj);
				if(mSendContinueCommand==true){
					mCommandList.remove(0);
					if(mCommandList.size()==0){
						mCommandList.clear();						
						Toast.makeText(ATCommandActivity.this, "Complete!!",
								Toast.LENGTH_SHORT).show();
						mInputCommand.setText("AT+");
						mSendContinueCommand=false;
					}else{
						if(mCommandList.size()>0){
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							sendCommand(mCommandList.get(0));
						}
						
					}
					
				}
				break;
			case EVENT_GET_FAIl:
				if(mSendContinueCommand==true)
				{
					if(mCommandList.size()>0){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sendCommand(mCommandList.get(0));
						Toast.makeText(ATCommandActivity.this, "Audio command fail,重新下指令 AT",
								Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(ATCommandActivity.this, "fail,size ==0",
								Toast.LENGTH_SHORT).show();
					}
					
					
				}else{
					Toast.makeText(ATCommandActivity.this, "fail,請重新下指令 AT",
							Toast.LENGTH_SHORT).show();
				}
				
				break;
			}
		}
	};
}
