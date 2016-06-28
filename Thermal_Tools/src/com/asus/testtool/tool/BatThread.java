package com.asus.testtool.tool;

import android.util.Log;

import com.asus.testtool.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//import com.asus.testtool.tab.SystemTab;

public class BatThread extends Thread{

	private boolean onLog = false;
	private File logFile;
	private FileWriter write;
	private BufferedWriter out ;
	
    //private Date date;	
    //private Long startTime ,timeInterval;	
   private SimpleDateFormat formatter ;  
   private SimpleDateFormat formatter2 ;
	
    public BatThread() {
    	formatter2 = new SimpleDateFormat("HHmmss_ddMMyyyy_");
    	logFile = new File("/sdcard/", formatter2.format(new Date())+"bat.csv");
        formatter = new SimpleDateFormat("HH:mm:ss ,dd/MM/yyyy	");  
    	if(logFile.exists()){
    		logFile.delete();
    		try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
	public void run () {
		onLog = true;
		Log.i("AsusTestTool", "Log Start");
		
        try {
	   		
            //date=new Date();            
            //startTime = date.getTime();            
			write = new FileWriter(logFile.getAbsoluteFile());
	        out = new BufferedWriter(write);	
	        out.write("Time," +
	        		"Date," +
	        		"FCC," +
	        		"RM," +
	        		"SOC," +
	        		"SWgauge," +
	        		"VOLT," +
	        		"BMS," +
	        		"Current," +
	        		"BatTemp," +
	        		"Pad_cap," +
	        		"P_gauge," +
	        		"P_voltage," +
	        		"P_current\r\n"); 
	        			        		
//	        out.write("Time,CPU_USAGE,MEMORY_USAGE,SENSOR0,CPU0_THERMAL,CPU1_THERMAL,CPU2_THERMAL,CPU3_THERMAL,PCB_THERMAL,,BATTERY_THERMAL,CPU0_CurFreq,CPU1_CurFreq,CPU2_CurFreq,CPU3_CurFreq,Current,PA_THERMAL\r\n");
//	        int time = 0;
			while (onLog) {
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {

				}
				
//				time += 5;


				
	            //date=new Date();
	            //timeInterval = date.getTime()-startTime;				
				
				/*
				float pmicTemp;
				float batteryTemp;
				
                try {
                    pmicTemp = Float.valueOf(Utils.static_PmicThermal)/1000;
                } catch (NumberFormatException e) {
                    // TODO Auto-generated catch block
                    pmicTemp = -1 ;
                }
				
                try {
                    batteryTemp = Float.valueOf(Utils.static_BatteryThermal)/10;
                } catch (NumberFormatException e) {
                    // TODO Auto-generated catch block
                    batteryTemp = -1 ;
                }
				*/
				out.write(formatter.format(new Date())+
						  ","+
						  Utils.static_FCC+","+
						  Utils.static_RM+","+
						  Utils.static_SOC+","+
						  Utils.static_SWgauge+","+
						  Utils.static_bat_VOLT+","+
						  Utils.static_BMS+","+
						  Utils.static_bat_Current+","+
						  Utils.static_BatteryThermal+","+
						  Utils.static_Pad_Cap+","+
						  Utils.static_P_gauge+","+
						  Utils.static_P_VOLTAGE+","+
						  Utils.static_P_CURRENT+","+
						  "\r\n");
	            write.flush();
	            out.flush(); 
			}
			
			write.close();
			out.close();
	        
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			
		}

		

		
		Log.i("AsusTestTool", "Log End");
	}
	public void setOnLog(boolean onLog) {
		this.onLog = onLog;
	}
	public boolean isOnLog() {
		return onLog;
	}
	
	
	
}
