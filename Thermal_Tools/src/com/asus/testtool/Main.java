package com.asus.testtool;

import com.asus.testtool.tab.InfoTab;
import com.asus.testtool.tab.ToolTab;
import com.asus.testtool.tab.UnittestTab;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.TabHost;

public class Main extends TabActivity {
    /** Called when the activity is first created. */
	
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;
	private static TabHost tabHost ;
	public static String sdCardFolder = "/Removable/MicroSD";
	public static String emmcFolder = "/data/data/com.asus.testtool";
//	public static String emmcFolder = "/mtn/sdcard";
    @Override
    public void onCreate(Bundle savedInstanceState) {
//    	Log.d("=================================", "onCreate");
        super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);        
//        setContentView(R.layout.main);
        initialTab();
//        Debug.startMethodTracing("aaa");
        initialPower();
//        Debug.stopMethodTracing(); 
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();	
		try {
			mWakeLock.release();

		} catch (Exception e) {
			e.printStackTrace();
		}	
    	
    	
    }
	public void initialTab(){
	    Resources res = getResources(); // Resource object to get Drawables
	    tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, InfoTab.class);
	    
	    spec = tabHost.newTabSpec("info").setIndicator("Info",
                res.getDrawable(R.drawable.tab_icon))
            .setContent(intent);
	    tabHost.addTab(spec);
/*		MASK SYSTEMTAB	    
	    intent = new Intent().setClass(this, SystemTab.class);
	    spec = tabHost.newTabSpec("system").setIndicator("System",
                res.getDrawable(R.drawable.tab_icon))
            .setContent(intent);
	    tabHost.addTab(spec);	   
*/
/*	    
	    intent = new Intent().setClass(this, SystemTab.class);
	    spec = tabHost.newTabSpec("devices").setIndicator("Devices",
                res.getDrawable(R.drawable.tab_icon))
            .setContent(intent);
	    tabHost.addTab(spec);
*/	    
/*		MASK GPUTAB
	    intent = new Intent().setClass(this, GpuTab.class);
	    spec = tabHost.newTabSpec("gpu").setIndicator("GPU",
                res.getDrawable(R.drawable.tab_icon))
            .setContent(intent);    
	    tabHost.addTab(spec);	 	    
*/
/*	    MASK CAMERATAB
        intent = new Intent().setClass(this, CameraTab.class);
        spec = tabHost.newTabSpec("camera").setIndicator("Camera",
                res.getDrawable(R.drawable.tab_icon))
            .setContent(intent);
        tabHost.addTab(spec);           
*/	    

//        intent = new Intent().setClass(this, PanelTest.class);
//        spec = tabHost.newTabSpec("panel").setIndicator("Panel",
//                res.getDrawable(R.drawable.tab_icon))
//            .setContent(intent);
//        tabHost.addTab(spec);           
        
	    
	    intent = new Intent().setClass(this, ToolTab.class);
	    spec = tabHost.newTabSpec("tool").setIndicator("Tool",
                res.getDrawable(R.drawable.tab_icon))
            .setContent(intent);
	    tabHost.addTab(spec);	

	    intent = new Intent().setClass(this, UnittestTab.class);
	    spec = tabHost.newTabSpec("unittest").setIndicator("Unittest",
                res.getDrawable(R.drawable.tab_icon))
            .setContent(intent);
	    tabHost.addTab(spec);		    
	    
	    tabHost.setCurrentTab(0);
	    
	    
	} 
	
	
	public void initialPower(){
		
		// power
		/* acquire PowerManager */
		try {
			mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			/* acquire WakeLock */
			mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.asus.testtool");
			//mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.asus.testtool");
			// wakeLock();
			mWakeLock.acquire();

		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		
	}	
    
}