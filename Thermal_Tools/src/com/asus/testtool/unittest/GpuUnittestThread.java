package com.asus.testtool.unittest;

import com.asus.testtool.tab.UnittestTab;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GpuUnittestThread extends Thread{
	public boolean onTest = false;
	String state, error;
	UnittestTab mUnittestTab;
	
    public GpuUnittestThread(UnittestTab unittestTab) {
    	mUnittestTab = unittestTab;
    	
    }
    
    @Override
	public void run () {
    	onTest = true;
    		try {
    			Process proc = Runtime.getRuntime().exec("/data/data/unit_test/gpu_test.sh"); //Whatever you want to execute
    			//Log.e("cliff","start++++");
    			BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    			try {
    				proc.waitFor();
    				//Log.e("XXXXX","YYYYY");
    			} catch (InterruptedException e) {
    				System.setProperty("sys.test.status.gpu_test", "fail");
    				System.setProperty("sys.test.errmsg.gpu_test", "Test be interrupted");
    				proc.destroy();
    			}
				//Log.e("aaaaa","bbbbb");
				//SystemProperties.set("sys.test.status.gpu_test", "pass");
	        } catch (Exception e) {
	        	System.out.println(e.getMessage());
	        }
	}
}
