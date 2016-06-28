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

public class LogThread extends Thread{

	private boolean onLog = false;
	private File logFile;
	private FileWriter write;
	private BufferedWriter out ;
	
    //private Date date;	
    //private Long startTime ,timeInterval;	
   private SimpleDateFormat formatter ;  

	
    public LogThread() {
    	logFile = new File("/sdcard/","log.csv");
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
	        		"CPU_USAGE," +
	        		"MEMORY_USAGE," +
	        		"CPU0_THERMAL," +
	        		"CPU1_THERMAL," +
	        		"CPU2_THERMAL," +
	        		"CPU3_THERMAL," +
	        		"CPU4_THERMAL," +
	        		"CPU5_THERMAL," +
	        		"CPU6_THERMAL," +
	        		"CPU7_THERMAL," +
	        		"CPU0_CurFreq," +
	        		"CPU1_CurFreq," +
	        		"CPU2_CurFreq," +
	        		"CPU3_CurFreq," +
	        		"CPU4_CurFreq," +
	        		"CPU5_CurFreq," +
	        		"CPU6_CurFreq," +
	        		"CPU7_CurFreq," +
	        		"Modem_THERMAL," +
	        		"GPU_THERMAL," +
	        		"Camera_THERMAL," +
	        		"BAT_CURRENT," +
	        		"BAT_VOL," +
	        		"BAT_THERMAL," +
	        		"GPU_CurFreq," +	// --- Shawn add gpuclk to log.csv
	        		"PCB_THERMAL," +
				"PAThermal," +
				"PMICThermal," +
				"CaseThermal," + 
				"XOThermal," + 
				"CHG_THERM_L," + 
				"ADC_PIN5," + 
				"ADC_PIN6," + 
				"CASE_THERM," + 
				"QUIET_THERM," + 
				"PA_THERM0," + 
				"REAR_TEMP\r\n");
//	        out.write("Time,CPU_USAGE,MEMORY_USAGE,SENSOR0,CPU0_THERMAL,CPU1_THERMAL,CPU2_THERMAL,CPU3_THERMAL,PCB_THERMAL,,BATTERY_THERMAL,CPU0_CurFreq,CPU1_CurFreq,CPU2_CurFreq,CPU3_CurFreq,Current,PA_THERMAL\r\n");
//	        int time = 0;
			while (onLog) {
				
				try {
					Thread.sleep(1000);
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
						  Utils.static_CpuUsage+","+
						  Utils.static_MemUsage+","+
						  Utils.static_Cpu0Thermal+","+
						  Utils.static_Cpu1Thermal+","+
						  Utils.static_Cpu2Thermal+","+
						  Utils.static_Cpu3Thermal+","+
						  Utils.static_Cpu4Thermal+","+
						  Utils.static_Cpu5Thermal+","+
						  Utils.static_Cpu6Thermal+","+
						  Utils.static_Cpu7Thermal+","+
						  Utils.static_Cpu0CurrentFreq+","+
						  Utils.static_Cpu1CurrentFreq+","+
                          Utils.static_Cpu2CurrentFreq+","+
						  Utils.static_Cpu3CurrentFreq+","+
						  Utils.static_Cpu4CurrentFreq+","+
						  Utils.static_Cpu5CurrentFreq+","+
                          Utils.static_Cpu6CurrentFreq+","+
						  Utils.static_Cpu7CurrentFreq+","+
						  Utils.static_ModemThermal+","+
						  Utils.static_GPUThermal+","+
  						  Utils.static_CameraThermal+","+
						  Utils.static_bat_Current+","+
  						  Utils.static_bat_VOLT+","+
  						  Utils.static_BatteryThermal+","+
						  Utils.static_GpuCurrentFreq+","+	// --- Shawn add gpuclk to log.csv
						  Utils.static_PCBThermal+","+
						  Utils.static_PAThermal+","+
						  Utils.static_PMICThermal+","+
						  Utils.static_CaseThermal+","+
						  Utils.static_XOThermal+","+
						  Utils.static_CHG_THERM_L+","+
						  Utils.static_ADC_PIN5+","+
						  Utils.static_ADC_PIN6+","+
						  Utils.static_CASE_THERM+","+
						  Utils.static_QUIET_THERM+","+
						  Utils.static_PA_THERM0+","+
						  Utils.static_REAR_TEMP+
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
