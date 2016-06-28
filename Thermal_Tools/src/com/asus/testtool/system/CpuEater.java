package com.asus.testtool.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CpuEater{
	
	private String cpuEaterPID;
	private boolean onTest;
	public CpuEater(){	
		cpuEaterPID = null;
		onTest = false;
	}
	
	public boolean start(){
		
		File cpueaterFile = new File("/system/xbin/cpueater");
		if(!cpueaterFile.exists())
			return false;
		
		ProcessBuilder cmd;
		String result = "";
		try {
			String[] args = { "/system/xbin/cpueater" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			while (in.read(re) != -1) {
				System.out.println(new String(re));
				result = result + new String(re);
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		cpuEaterPID = result;
		onTest = true;
		return true;
	}
	
	public boolean isOnTest(){
		return onTest;
	}
	
	public boolean stop(){
		
		if (cpuEaterPID == null)
			return false;
		if (!onTest)
			return false;
		
		ProcessBuilder cmd;
		try {
			String[] args = { "kill", cpuEaterPID };
			cmd = new ProcessBuilder(args);
			cmd.start();
		} catch (IOException ex) {
			ex.printStackTrace();
			
			return false;
		}
		onTest = false;
		return true;
		
	}
}