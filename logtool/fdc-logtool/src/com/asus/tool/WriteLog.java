package com.asus.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class WriteLog {

	private static String TAG=DumpService.TAG;
	
	public static void write(String path,String text){
		File logFile = new File(path);
		 if (!logFile.exists())
		   {
		    
			   try
		      {
		         logFile.createNewFile();
		      } 
		      catch (IOException e)
		      {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		         Log.e(TAG, "logFile not exist="+ e.getMessage());
		         return;
		      }
		   }
		   
		   try
		   {
		      //BufferedWriter for performance, true to set append to file flag
		      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		      buf.append(text);
		      buf.newLine();
		      buf.close();
		   }
		   catch (IOException e)
		   {
		      log("io fail="+ e.getMessage());
		      
		   }
	}
	
	
	
	public static void appendLog(Context context,String text)
	{       
		String Path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/aaa.txt";
		
		Log.v(TAG, "appendLog="+Path);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String currentDateandTime = sdf.format(new Date());	
		text=currentDateandTime+"------------>"+text;
		File logFile = new File(Path);
	   if (!logFile.exists())
	   {
	      Log.v(TAG, "logFile not exist");
		   try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	         //Log.v(TAG, "logFile not exist="+ e.getMessage());
	         Toast.makeText(context,"create file result="+e.getMessage(), Toast.LENGTH_LONG).show();
	      }
	   }
	   
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	      Log.v(TAG, "io fail="+ e.getMessage());
	   }
	}
	
	private static void log(String msg) {
		// TODO Auto-generated method stub
		Log.e(TAG, msg);
	}
}
