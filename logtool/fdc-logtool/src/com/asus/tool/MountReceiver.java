package com.asus.tool;

import com.asus.log.ModemLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;

public class MountReceiver extends BroadcastReceiver{

	private static final String TAG = "MountReceiver";
	private static String KEY_MOUNT_STATE="persist.asuslog.mount.state";
	@Override
	public void onReceive(Context context, Intent intent) {
		String action=intent.getAction();
		if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
			SystemProperties.set(KEY_MOUNT_STATE, "1");
		}else if(action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)){
			
			String mount =Util.getMicroSDPath(context);
			if(mount!=null && mount.length()>0){
				mount+="/Asuslog/";
				Log.v(TAG, mount);
				String savepath=DumpService.getLogRootpath();
				if(mount.equals(savepath)){
					ModemLog.updateLogConfig(false);
					SystemProperties.set(DumpService.KEY_SAVE_DIR, DumpService.DEFAULT_ROOT_PATH);
					Intent service_intent=new Intent(DumpService.ACTION_PATH_UNMOUNT);
					context.sendBroadcast(service_intent);
				}
				
			}
			
		}
	}

}
