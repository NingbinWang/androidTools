package com.asus.testtool.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Memtester{
	private Process memtesterPID;
	private boolean onTest;
	private String size ;
	public Memtester(String size){	
		memtesterPID = null;
		onTest = false;
		this.size = size ;
	}
	
	public boolean start(){
		
		File memtesterFile = new File("/system/xbin/memtester");
		if(!memtesterFile.exists())
			return false;
		
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/xbin/su", "-",
					"/system/xbin/memtester", size };
			cmd = new ProcessBuilder(args);
			
			memtesterPID = cmd.start();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}		
	
		onTest = true;
		return true;
	}
	
	public boolean isOnTest(){
		return onTest;
	}
	
	public boolean stop(){
		
		if (memtesterPID == null)
			return false;
		if (!onTest)
			return false;
		
		ProcessBuilder cmd;
		
		try {
			String[] args = { "/system/xbin/su", "-", "kill",
					parser(memtesterPID.toString(), "id", ']') };
			cmd = new ProcessBuilder(args);
			cmd.start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		onTest = false;
		return true;
		
	}
	// Parser
	private String parser(String source, String target, char end) {

		String result = "";
		char array[] = source.toCharArray();
		int start = source.indexOf(target);
		if (start == -1)
			return "-1";
		boolean Tag = false;
		for (int i = start; i < source.length(); i++) {
			if ((array[i] == ':') || (array[i] == '=')) { // separate char (":"
															// or "=")

				i++;
				while (array[i] != end) { // end char
					if (Tag == false && array[i] != ' ')
						Tag = true;
					if (Tag)
						result += array[i];
					i++;
				}

				break;

			}
		}
		return result;

	}
	
}