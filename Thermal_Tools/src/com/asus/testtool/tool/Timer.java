package com.asus.testtool.tool;

import android.os.AsyncTask;

import com.asus.testtool.tab.ToolTab;

import java.io.FileOutputStream;
import java.util.Date;

//import com.asus.testtool.tab.SystemTab;

public class Timer extends AsyncTask<Void, Void , Void> {

    private ToolTab mActivity;
    //private UnittestTab mActivity2;
    private FileOutputStream fos;
    
    private Long startTime ,timeInterval;
    private Date date;
	private String temp;
    private String fileName;
    public Timer(ToolTab activity , String file) {
        mActivity = activity;
        fileName = file;
    }
    
   /* public Timer(UnittestTab activity , String file) {
        mActivity2 = activity;
        fileName = file;
    }
    */
    public boolean initial(){
    	
//        try {
//			fos = mActivity.openFileOutput(fileName,Context.MODE_WORLD_READABLE);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}   	
    	return true;
    }
    
    // Runs on main thread.
    @Override
    protected void onPreExecute() {
    }

    // Runs on main thread.
    @Override
    protected void onProgressUpdate(Void... arg) {
 
    	
    	
        if (mActivity != null) {
        	date=new Date();
        	timeInterval = date.getTime()-startTime;
        	temp = timeInterval/3600000+":"+timeInterval/60000%60+":"+timeInterval/1000%60+"\n";
//    	    try {
//    	    	fos.flush();
//    			fos.write(temp.getBytes());
//    		} catch (IOException e1) {
//    			// TODO Auto-generated catch block
//    			e1.printStackTrace();
//    		}	        	
        	mActivity.updateTimer(temp);
        	
        }
    }

    // Runs on main thread.
    @Override
    protected void onPostExecute(Void result) {
    }

    // Runs in separate thread.
    @Override
    protected Void doInBackground(Void... params) {
    	date=new Date();
    	startTime = date.getTime();
        while(!isCancelled()){
        	publishProgress();
            try {
                Thread.sleep(1000);
            } 
            catch (InterruptedException e) {
            }		
            
        }
        
        
//        try {
//			fos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        
        return null;
    }
}
