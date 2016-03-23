package com.asus.tool;

public class DumpSyslog 

{
	 static {
		    System.loadLibrary("dumpsys_logtool");
	 }
		   
	 public static native int dumpsys(String cmd);
}
