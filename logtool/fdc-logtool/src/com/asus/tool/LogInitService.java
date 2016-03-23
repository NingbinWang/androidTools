package com.asus.tool;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;


public class LogInitService extends Service{

	private static final String TAG = "LogInitService";
     
	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}

	public static synchronized void initFile(Context context){

		String filename = null;
                if(SystemProperties.get("persist.asuslog.qpst.enable","0").equals("1"))
			filename = "update.zip";
		else
                        filename = "update_user.zip";

		//if(SystemProperties.get("persist.asuslog.fw.update", "0").equals("1"))
		//{
		//	return;
		//}

		File file =new File("/cache/update.zip");
		//if(file.exists()==false){
		Util.copyAssetFile(context.getAssets(), context, filename, "/cache/update.zip");
		//}

		File recovery_file = new File("/cache/recovery");
		if(recovery_file.exists() == false)
		{
			Log.e("zxl", "recovery is not exist");
			boolean result = recovery_file.mkdirs();
			if(result == false){
				try{
					Thread.sleep(100);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
		
		Util.setCmd("echo --update_package=/cache/update.zip > /cache/recovery/command");
		DialogLogReceiver.showDialog_next(context, "Are you sure to update sbl1.mbn and tz.mbn");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand");
		initFile(this);
		stopSelf();
		return START_NOT_STICKY;
	}	
}
