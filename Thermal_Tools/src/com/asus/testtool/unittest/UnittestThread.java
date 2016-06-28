package com.asus.testtool.unittest;

import android.util.Log;

import com.asus.testtool.tab.UnittestTab;

import java.io.BufferedReader;
import java.io.InputStreamReader;



//import org.omg.CORBA.portable.InputStream;


public class UnittestThread extends Thread{
	public boolean onTest = false;
	String state, error;
	UnittestTab mUnittestTab;
	
    public UnittestThread(UnittestTab unittestTab) {
    	mUnittestTab = unittestTab;
    	
    }
    
    
    @Override
	public void run () {
    	onTest = true;
    	//while(onTest){
    		try {
    			Process proc = Runtime.getRuntime().exec("/data/data/unit_test/cpu/cpu_test.sh"); //Whatever you want to execute
    			Log.e("cliff","start++++");
    			Log.e("lamda","vvvv");
    			BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    			try {
    				
    				//state = SystemProperties.get("sys.test.status.cpu_test", "default");
    				//error = SystemProperties.get("sys.test.errmsg.cpu_test", "default");
    				//state = "GOGO";
    				//error = "FFFF";
    				//mUnittestTab.UpdateStateMsg(state);
    				proc.waitFor();
    				Log.e("XXXXX","YYYYY");
    			} catch (InterruptedException e) {
    				//System.out.println(e.getMessage());
    				System.setProperty("sys.test.status.cpu_test", "fail");
    				//mUnittestTab.UpdateStateMsg();
    				proc.destroy();
    			}
				Log.e("aaaaa","bbbbb");
				System.setProperty("sys.test.status.cpu_test", "pass");
				//mUnittestTab.enableUI("pass");
				mUnittestTab.CheckState();
				//mUnittestTab.UpdateErrMsg();
    			//state = "GOGO";
				//error = "FFFF";
				//mUnittestTab.UpdateStateMsg(state);
    			//SystemProperties.set("sys.test.status.cpu_test", "pass");
				//mUnittestTab.UpdateStateMsg();
				//mUnittestTab.UpdateErrMsg(error);
    			/*
    			while (onTest) {
	            	Log.e("cliff","testing");
	            	try {
	    	            Thread.sleep(1000);
	    	              
	    	        } catch (InterruptedException e) {
	    	        }  
	            } */
    			//Log.e("cliff","destroy+++");
    			//proc.destroy();
    			//Log.e("cliff","destroy---");
	        } catch (Exception e) {
	        	System.out.println(e.getMessage());
	        }
    	
    	//}
	}
  
	public void setTesting(boolean onTest) {
		this.onTest = onTest;
	}
    
}
