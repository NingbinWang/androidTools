package com.asus.log;
import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.asus.fdclogtool.R;
public class RilLog extends BaseLog{
	private static final String TAG = "RilLog";
	
	public RilLog(Activity activity, View view) {
		super(activity, view);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void onSelectAll() {
		// TODO Auto-generated method stub
		super.onSelectAll();
	}

	@Override
	public void onCancelAll() {
		// TODO Auto-generated method stub
		super.onCancelAll();
	}
	
	public static void log(String message){
		Log.v(TAG, message);
	}
}
