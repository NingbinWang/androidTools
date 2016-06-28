package com.asus.testtool.unittest;

import android.util.Log;

import com.asus.testtool.tab.UnittestTab;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CpuUnittestThread extends Thread{
	public boolean onTest = false;
	//public boolean flag = false;
	String state, error;
	UnittestTab mUnittestTab;
	
    public CpuUnittestThread(UnittestTab unittestTab) {
    	mUnittestTab = unittestTab;	
    }
    
    @Override
	public void run () {
    	onTest = true;
    		try {
    			Process proc = Runtime.getRuntime().exec("/data/data/unit_test/cpu_test.sh"); //Whatever you want to execute
    			Log.e("cliff","start++++");
    			BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    			try {
    				proc.waitFor();
    				//Log.e("XXXXX","YYYYY");
    				
    			} catch (InterruptedException e) {
    				System.setProperty("sys.test.status.cpu_test", "fail");
    				System.setProperty("sys.test.errmsg.cpu_test", "Test be interrupted");
    				proc.destroy();
    			}
    			mUnittestTab.flag = true;
    			//flag = true;
    			//mUnittestTab.CheckState();
				Log.e("jim","jim0++");
				mUnittestTab.enableUI("pass");
				Log.e("jim","jim0--");
				//SystemProperties.set("sys.test.status.cpu_test", "pass");
	        } catch (Exception e) {
	        	System.out.println(e.getMessage());
	        }
	}
}
