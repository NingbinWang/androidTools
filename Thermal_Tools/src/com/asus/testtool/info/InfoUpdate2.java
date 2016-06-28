package com.asus.testtool.info;

import android.os.AsyncTask;

import com.asus.testtool.tab.UnittestTab;
import com.asus.testtool.utils.Utils;

import java.math.BigDecimal;

public class InfoUpdate2 extends AsyncTask<Void, String , Void> {

    private UnittestTab mActivity;
    private static int A80_DEVICE=0;
    private static int A86_DEVICE=1;	
    private int mDeviceInfo;
	
    public InfoUpdate2(UnittestTab activity) {
        mActivity = activity;
        firstTime = true ;		
        String strDeviceInfo = System.getProperty("ro.product.device","A86");

		if(strDeviceInfo.equals("ASUS-A86")){

			mDeviceInfo = A86_DEVICE;
			
		}else{
			
			mDeviceInfo = A80_DEVICE;

		}

    }
    
    
    public void setActivity(UnittestTab activity){
    	
    	mActivity = activity;
    }
    // Runs on main thread.
    @Override
    protected void onPreExecute() {
    }

    // Runs on main thread.
    @Override
    protected void onProgressUpdate(String... arg) {
        if (mActivity != null) {
        	mActivity.updateInfo(arg);
        }
    }

    // Runs on main thread.
    @Override
    protected void onPostExecute(Void result) {
    }

    // Runs in separate thread.
    @Override
    protected Void doInBackground(Void... params) {
    	
        while(!isCancelled()){
        	
   	String cpuUsage = cpuInfo();
    String memUsage = memInfo();
	String cpu0Thermal ;
	String cpu1Thermal ;
	String cpu2Thermal ;
	String cpu3Thermal ;
	String pcbThermal ;
	String sensor0Thermal ;
	String sensor1Thermal ;
	String sensor2Thermal ;
	String sensor3Thermal ;
	String sensor4Thermal ;
	String sensor9Thermal ;
	String sensor10Thermal ;
	String paThermal ;
	String fcc;
	String rm;
	String soc;
	String volt;
	String bms;
	
	String rsoc;
	String usoc;
	String ai;
	String pvolt;
	String temp;
	String gauge_mode;
	
	String cpuState;
	String gpuState;
	String batState;
	String cpuErrorMsg;
	String gpuErrorMsg;
	String batErrorMsg;
	String unitteststate;
	String ErrorMsg;
	
	if(A80_DEVICE == mDeviceInfo){
		cpu0Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone7/temp");
		cpu1Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone8/temp");
		cpu2Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone9/temp");
		cpu3Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone10/temp");
		pcbThermal = Utils.parseThermal(Utils.readInfo("/sys/devices/platform/msm_ssbi.0/pm8921-core/pm8xxx-adc/pa_therm0"));
		paThermal = Utils.parseThermal(Utils.readInfo("/sys/devices/qpnp-vadc-ee125800/pa_therm"));
	}else{
		cpu0Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone5/temp");
		cpu1Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone6/temp");
		cpu2Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone7/temp");
		cpu3Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone8/temp");
		pcbThermal = Utils.parseThermal(Utils.readInfo("/dev/therm/vadc/msm_therm"));
		paThermal = Utils.parseThermal(Utils.readInfo("/dev/therm/vadc/pa_therm0"));
	}
	//for bat
    String batteryThermal = Utils.readInfo("/proc/driver/BatTemp");
    fcc = Utils.parseFCC(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    rm = Utils.parseRM(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    soc = Utils.parseSOC(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    volt = Utils.parseVOLT(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    bms = Utils.parseBMS(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    
    //for pad bat
    
    rsoc = Utils.parseRSOC(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    usoc = Utils.parseUSOC(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    pvolt = Utils.parsePVOLT(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    ai = Utils.parseAI(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    temp = Utils.parseTEMP(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    gauge_mode = Utils.parseGAUGE_MODE(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    
    cpuState = System.getProperty("sys.test.status.cpu_test", "default");
    gpuState = System.getProperty("sys.test.status.gpu_test", "default");
    batState = System.getProperty("sys.test.status.bat_test", "default");
    cpuErrorMsg = System.getProperty("sys.test.errmsg.cpu_test", "default");
    gpuErrorMsg = System.getProperty("sys.test.errmsg.gpu_test", "default");
    batErrorMsg = System.getProperty("sys.test.errmsg.bat_test", "default");
    unitteststate = System.getProperty("sys.test.status.cpu_test", "default");
    ErrorMsg = System.getProperty("sys.test.errmsg.cpu_test", "default");
    
    /*
    rsoc = Utils.parseRSOC(Utils.readInfo("/data/data/bq27520_pad_test_info_dump"));
    usoc = Utils.parseUSOC(Utils.readInfo("/data/data/bq27520_pad_test_info_dump"));
    pvolt = Utils.parsePVOLT(Utils.readInfo("/data/data/bq27520_pad_test_info_dump"));
    ai = Utils.parseAI(Utils.readInfo("/data/data/bq27520_pad_test_info_dump"));
    temp = Utils.parseTEMP(Utils.readInfo("/data/data/bq27520_pad_test_info_dump"));
    gauge_mode = Utils.parseGAUGE_MODE(Utils.readInfo("/data/data/bq27520_pad_test_info_dump"));
    */
//        	String cpuThermal = Utils.readInfo("/sys/devices/virtual/thermal/thermal_zone0/temp");   //TF700K
//            String pmicThermal = Utils.readInfo("/sys/devices/virtual/thermal/thermal_zone5/temp");  //TF700K             
//            String batteryThermal = Utils.readInfo("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");  //TF 700K
    sensor0Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone0/temp");
    sensor1Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone1/temp");
    sensor2Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone2/temp");
    sensor3Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone3/temp");
    sensor4Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone4/temp");
    sensor9Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone9/temp");
    sensor10Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone10/temp");			
	String cpu0CurrentFreq = Utils.readInfo("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
	String cpu1CurrentFreq = Utils.readInfo("/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq");
	String cpu2CurrentFreq = Utils.readInfo("/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq");
	String cpu3CurrentFreq = Utils.readInfo("/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq");
	String current = Utils.readInfo("/proc/driver/MPdecisionCurrent");

       
    Utils.static_CpuUsage = cpuUsage ;
    Utils.static_MemUsage = memUsage ;
    Utils.static_Sensor0Thermal = sensor0Thermal ;
    Utils.static_Sensor1Thermal = sensor1Thermal ;
    Utils.static_Sensor2Thermal = sensor2Thermal ;
    Utils.static_Sensor3Thermal = sensor3Thermal ;
    Utils.static_Sensor4Thermal = sensor4Thermal ;
    Utils.static_Cpu0Thermal = cpu0Thermal ;
	Utils.static_Cpu1Thermal = cpu1Thermal ;
	Utils.static_Cpu2Thermal = cpu2Thermal ;
	Utils.static_Cpu3Thermal = cpu3Thermal ;
	Utils.static_Sensor9Thermal = sensor9Thermal ;
	Utils.static_Sensor10Thermal = sensor10Thermal ;
    Utils.static_PCBThermal = pcbThermal ;
    Utils.static_BatteryThermal = batteryThermal ;

	Utils.static_Cpu0CurrentFreq = cpu0CurrentFreq;
	Utils.static_Cpu1CurrentFreq = cpu1CurrentFreq ;
	Utils.static_Cpu2CurrentFreq = cpu2CurrentFreq ;
	Utils.static_Cpu3CurrentFreq = cpu3CurrentFreq ;
	Utils.static_bat_Current = current ;
	Utils.static_PAThermal = paThermal;
	Utils.static_FCC = fcc;
	Utils.static_RM = rm;
	Utils.static_SOC = soc;
	Utils.static_bat_VOLT = volt;
	Utils.static_BMS = bms;
	
	Utils.static_RSOC = rsoc;
	Utils.static_USOC = usoc;
	Utils.static_PVOLT = pvolt;
	Utils.static_AI = ai;
	Utils.static_TEMP = temp;
	Utils.static_GAUGE_MODE = gauge_mode;

	//Utils.static_UnitTestState = unitteststate;
	
        	publishProgress(
        			cpuState,
        			cpuErrorMsg,
        			gpuState,
        			gpuErrorMsg,
        			batState,
        			batErrorMsg,
        			unitteststate,
        			ErrorMsg
				);
	        try {
	        	//Log.e(unitteststate,"CCCCCCC");
	            Thread.sleep(1000);
	              
	        } catch (InterruptedException e) {
	        }        
		
        }

        return null;
    }
    
    
    private boolean firstTime;
	private int[] cpuUsageOld = new int[3];
	private int[] cpuUsageNew = new int[3];    
    private String cpuInfo() {
    	if(firstTime){
    		cpuUsageNew = Utils.readCpuUsage();
    		firstTime = false;
    		
    		return "wait";
    	}
    	else{
    		double userCpuUsage, systemCpuUsage, totalCpuUsage;
    		cpuUsageOld[0] = cpuUsageNew[0];
			cpuUsageOld[1] = cpuUsageNew[1];
			cpuUsageOld[2] = cpuUsageNew[2];

			cpuUsageNew = Utils.readCpuUsage();

			userCpuUsage = 100.0 * (cpuUsageNew[1] - cpuUsageOld[1])
					/ (cpuUsageNew[0] - cpuUsageOld[0]);
			systemCpuUsage = 100.0 * (cpuUsageNew[2] - cpuUsageOld[2])
					/ (cpuUsageNew[0] - cpuUsageOld[0]);
			totalCpuUsage = userCpuUsage + systemCpuUsage;
			return ""+new BigDecimal(totalCpuUsage).setScale(2, 1);
    		
    		
    	}
	}

    private String memTotalInfo, memFreeInfo;
    private double memTotal, memFree, memUsage;
	private String memInfo(){
		memTotalInfo = Utils.parseMem(Utils.readInfo("/proc/meminfo"),"MemTotal");
		memFreeInfo = Utils.parseMem(Utils.readInfo("/proc/meminfo"),"MemFree");
    	
		memTotal = Integer.parseInt(memTotalInfo);
		memFree = Integer.parseInt(memFreeInfo);
		memUsage = 100.0 * (memTotal - memFree) / memTotal;
		
    	return ""+new BigDecimal(memUsage).setScale(2, 1);
    	
    }



	
}
