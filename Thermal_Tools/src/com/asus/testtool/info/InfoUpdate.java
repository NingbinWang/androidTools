package com.asus.testtool.info;

import android.os.AsyncTask;
import android.util.Log;

import com.asus.testtool.tab.InfoTab;
import com.asus.testtool.utils.Utils;

import java.io.File;
import java.math.BigDecimal;

public class InfoUpdate extends AsyncTask<Void, String , Void> {

    private InfoTab mActivity;
    private static int A80_DEVICE=0;
    private static int A86_DEVICE=1;	
    private int mDeviceInfo;
	
    public InfoUpdate(InfoTab activity) {
        mActivity = activity;
        firstTime = true ;		
        String strDeviceInfo = System.getProperty("ro.product.device","A80");

		if(strDeviceInfo.equals("ASUS-A80")){

			mDeviceInfo = A80_DEVICE;
			
		}else{
			
			mDeviceInfo = A86_DEVICE;

		}

    }
    
    
    public void setActivity(InfoTab activity){
    	
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
	String cpu0Thermal = "" ;
	String cpu1Thermal = "" ;
	String cpu2Thermal = "" ;
	String cpu3Thermal = "" ;
	String cpu4Thermal = "" ;
	String cpu5Thermal = "" ;
	String cpu6Thermal = "" ;
	String cpu7Thermal = "" ;
	String modemthermal = "" ;
	String camerathermal = "" ;
	String gputhermal = "" ;
	String pcbThermal ;
	String sensor0Thermal ;
	String sensor1Thermal ;
	String sensor2Thermal ;
	String sensor3Thermal ;
	String sensor4Thermal ;
	String PAThermal="" ;
	String PMICThermal="";
	String CaseThermal="";
	String XOThermal="";

	String CHG_THERM_L="";
	String ADC_PIN5 = "";
	String ADC_PIN6 = "";
	String CASE_THERM = "";
	String QUIET_THERM = "";
	String PA_THERM0="";
	String REAR_TEMP="";

    String sensor5Thermal ;
    String sensor6Thermal ;
    String sensor7Thermal ;
	String sensor9Thermal ;
	String sensor10Thermal ;
	String paThermal ;
	String fcc;
	String rm;
	String soc;
	String bat_volt;
	String bms;
	String swgauge;
	// +++ Shawn add for Eason's Pad log	
	String pad_cap;
	String p_gauge;
	String p_voltage;
	String p_current;
	// --- Shawn add for Eason's Pad log
	
	String rsoc;
	String usoc;
	String ai;
	String pvolt;
	String temp;
	String gauge_mode;
	
	String unitteststate;
	String cputype = Utils.static_CpuType;
	File filepath;
	Log.d("thermal_tool","[jevian log]CPU Type is " + cputype);
	if(cputype.equals("MSM8939") || cputype.equals("MSM8929")) {
		for(int i=0;i<50;i++)
		{
			filepath = new File("/sys/class/thermal/thermal_zone" + i +"/type");
			if(filepath.exists()) {
				String thermal_type = Utils.readInfo("/sys/class/thermal/thermal_zone" + i +"/type");
				if(thermal_type.equals("tsens_tz_sensor5")) {
					cpu0Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
					cpu4Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor6")) {
					cpu1Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
					cpu5Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor7")) {
					cpu2Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
					cpu6Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor8")) {
					cpu3Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
					cpu7Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor2")) {
					modemthermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor1")) {
					camerathermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor3")) {
					gputhermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				}
			} else {
				break;
			}
		}
	} else if(cputype.equals("MSM8916")) {
		for(int i=0;i<50;i++)
		{
			filepath = new File("/sys/class/thermal/thermal_zone" + i +"/type");
			if(filepath.exists()) {
				String thermal_type = Utils.readInfo("/sys/class/thermal/thermal_zone" + i +"/type");
				if(thermal_type.equals("tsens_tz_sensor5")) {
					cpu0Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
					cpu1Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor4")) {
					cpu2Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
					cpu3Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor0")) {
					modemthermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor1")) {
					camerathermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				} else if(thermal_type.equals("tsens_tz_sensor2")) {
					gputhermal = Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp");
				}
			} else {
				break;
			}
		}
	} else if(cputype.equals("MSM8996")) {
		for(int i=0;i<50;i++)
		{
			filepath = new File("/sys/class/thermal/thermal_zone" + i +"/type");
			if(filepath.exists()) {
				String thermal_type = Utils.readInfo("/sys/class/thermal/thermal_zone" + i +"/type");
				if(thermal_type.equals("tsens_tz_sensor4")) {
					cpu0Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor6")) {
					cpu1Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor8")) {
					cpu2Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor10")) {
					cpu3Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor3")) {
					cpu4Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor5")) {
					cpu5Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor9")) {
					cpu6Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor11")) {
					cpu7Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor20")) {
					modemthermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor13")) {
					camerathermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor14")) {
					gputhermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				}
			} else {
				break;
			}
		}
	} else if(cputype.equals("MSM8953")) {
		for(int i=0;i<50;i++)
		{
			filepath = new File("/sys/class/thermal/thermal_zone" + i +"/type");
			if(filepath.exists()) {
				String thermal_type = Utils.readInfo("/sys/class/thermal/thermal_zone" + i +"/type");
				if(thermal_type.equals("tsens_tz_sensor5")) {
					cpu0Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor4")) {
					cpu1Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor12")) {
					cpu2Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor11")) {
					cpu3Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor7")) {
					cpu4Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor6")) {
					cpu5Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor9")) {
					cpu6Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor10")) {
					cpu7Thermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor1")) {
					modemthermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor3")) {
					camerathermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				} else if(thermal_type.equals("tsens_tz_sensor15")) {
					gputhermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone" + i + "/temp")) / 10.0);
				}
			} else {
				break;
			}
		}
	}

	if(cputype.equals("MSM8953")){
		pcbThermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone2/temp")) / 1.0);
		PAThermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone22/temp")) / 1.0) ;
		PMICThermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone5/temp")) / 1000.0);
		CaseThermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone4/temp")) / 1.0);
		XOThermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone2/temp")) / 1.0);
		CHG_THERM_L = "error";
		ADC_PIN5 = String.valueOf(Double.parseDouble(Utils.readInfo("/proc/PRT8301_temp")) / 1.0);
		ADC_PIN6 = String.valueOf(Double.parseDouble(Utils.readInfo("/proc/PRT8302_temp")) / 1.0);
		CASE_THERM = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone4/temp")) / 1.0);
		QUIET_THERM = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone3/temp")) / 1.0);
		PA_THERM0 = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/thermal/thermal_zone22/temp")) / 1.0);
		REAR_TEMP = Utils.readInfo("/proc/driver/camera_temp");
	}
	else{
		pcbThermal = Utils.parseThermal(Utils.readInfo("/sys/class/hwmon/hwmon0/device/xo_therm"));
	}

	if(cputype.equals("MSM8953"))
		paThermal = Utils.parseThermal(Utils.readInfo("/sys/class/thermal/thermal_zone22/temp"));
	else
		paThermal = Utils.parseThermal(Utils.readInfo("/sys/class/hwmon/hwmon0/device/pa_therm0"));
    	bat_volt = Utils.readInfo("/sys/class/power_supply/battery/voltage_now");
	String bat_current = Utils.readInfo("/sys/class/power_supply/battery/current_now");
	String batteryThermal;
	if(cputype.equals("MSM8916") || cputype.equals("MSM8929") || cputype.equals("MSM8939"))
		batteryThermal = Utils.parseThermal(Utils.readInfo("/sys/class/hwmon/hwmon0/device/batt_therm"));
	else if(cputype.equals("MSM8996") || cputype.equals("MSM8953"))
		batteryThermal = String.valueOf(Double.parseDouble(Utils.readInfo("/sys/class/power_supply/battery/temp")) / 10.0);
	else
		batteryThermal = new String("Unkown");
 
    //for bat log
    fcc = Utils.parseFCC(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    rm = Utils.parseRM(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    soc = Utils.parseSOC(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    bms = Utils.parseBMS(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    swgauge = Utils.parseSWgauge(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
	// +++ Shawn add for Eason's Pad log    
    pad_cap = Utils.parsePad_Cap(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    p_gauge = Utils.parseP_gauge(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    p_voltage = Utils.parseP_VOLTAGE(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
    p_current = Utils.parseP_CURRENT(Utils.readInfo("/proc/driver/bq27520_test_info_dump"));
	// --- Shawn add for Eason's Pad log
    
    //for pad bat log
    
    rsoc = Utils.parseRSOC(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    usoc = Utils.parseUSOC(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    pvolt = Utils.parsePVOLT(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    ai = Utils.parseAI(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    temp = Utils.parseTEMP(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    gauge_mode = Utils.parseGAUGE_MODE(Utils.readInfo("/proc/driver/bq27520_pad_test_info_dump"));
    
    unitteststate = System.getProperty("sys.test.status.cpu_test", "default");
    
    
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
    sensor5Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone5/temp");
    sensor6Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone6/temp");
    sensor7Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone7/temp");
    sensor9Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone9/temp");
    sensor10Thermal = Utils.readInfo("/sys/class/thermal/thermal_zone10/temp");
	String cpu0CurrentFreq = Utils.readInfo("/sys/bus/cpu/devices/cpu0/cpufreq/scaling_cur_freq");
	String cpu1CurrentFreq = Utils.readInfo("/sys/bus/cpu/devices/cpu1/cpufreq/scaling_cur_freq");
	String cpu2CurrentFreq = Utils.readInfo("/sys/bus/cpu/devices/cpu2/cpufreq/scaling_cur_freq");
	String cpu3CurrentFreq = Utils.readInfo("/sys/bus/cpu/devices/cpu3/cpufreq/scaling_cur_freq");
	String cpu4CurrentFreq = Utils.readInfo("/sys/bus/cpu/devices/cpu4/cpufreq/scaling_cur_freq");
	String cpu5CurrentFreq = Utils.readInfo("/sys/bus/cpu/devices/cpu5/cpufreq/scaling_cur_freq");
	String cpu6CurrentFreq = Utils.readInfo("/sys/bus/cpu/devices/cpu6/cpufreq/scaling_cur_freq");
	String cpu7CurrentFreq = Utils.readInfo("/sys/bus/cpu/devices/cpu7/cpufreq/scaling_cur_freq");
	String gpuCurrentFreq = Utils.readInfo("/sys/class/kgsl/kgsl-3d0/gpuclk");	// --- Shawn add gpuclk to log.csv
       
    Utils.static_CpuUsage = cpuUsage ;
    Utils.static_MemUsage = memUsage ;
    Utils.static_Sensor0Thermal = sensor0Thermal ;
    Utils.static_Sensor1Thermal = sensor1Thermal ;
    Utils.static_Sensor2Thermal = sensor2Thermal ;
    Utils.static_Sensor3Thermal = sensor3Thermal ;
    Utils.static_Sensor4Thermal = sensor4Thermal ;
    Utils.static_Sensor5Thermal = sensor5Thermal ;
    Utils.static_Sensor6Thermal = sensor6Thermal ;
    Utils.static_Sensor7Thermal = sensor7Thermal ;
    Utils.static_Cpu0Thermal = cpu0Thermal ;
	Utils.static_Cpu1Thermal = cpu1Thermal ;
	Utils.static_Cpu2Thermal = cpu2Thermal ;
	Utils.static_Cpu3Thermal = cpu3Thermal ;
    Utils.static_Cpu4Thermal = cpu4Thermal ;
	Utils.static_Cpu5Thermal = cpu5Thermal ;
	Utils.static_Cpu6Thermal = cpu6Thermal ;
	Utils.static_Cpu7Thermal = cpu7Thermal ;
	Utils.static_Sensor9Thermal = sensor9Thermal ;
	Utils.static_Sensor10Thermal = sensor10Thermal ;
    Utils.static_PCBThermal = pcbThermal ;
    Utils.static_BatteryThermal = batteryThermal ;
    Utils.static_ModemThermal = modemthermal ;
    Utils.static_GPUThermal = gputhermal ;
    Utils.static_CameraThermal = camerathermal ;
    
	Utils.static_Cpu0CurrentFreq = cpu0CurrentFreq;
	Utils.static_Cpu1CurrentFreq = cpu1CurrentFreq ;
	Utils.static_Cpu2CurrentFreq = cpu2CurrentFreq ;
	Utils.static_Cpu3CurrentFreq = cpu3CurrentFreq ;
	Utils.static_Cpu4CurrentFreq = cpu4CurrentFreq;
	Utils.static_Cpu5CurrentFreq = cpu5CurrentFreq ;
	Utils.static_Cpu6CurrentFreq = cpu6CurrentFreq ;
	Utils.static_Cpu7CurrentFreq = cpu7CurrentFreq ;
	Utils.static_GpuCurrentFreq = gpuCurrentFreq ;	// --- Shawn add gpuclk to log.csv
	Utils.static_bat_Current = bat_current ;
	Utils.static_PAThermal = PAThermal;
	Utils.static_FCC = fcc;
	Utils.static_RM = rm;
	Utils.static_SOC = soc;
	Utils.static_bat_VOLT = bat_volt;
	Utils.static_BMS = bms;
	Utils.static_SWgauge = swgauge;
	// +++ Shawn add for Eason's Pad log
	Utils.static_Pad_Cap = pad_cap;
	Utils.static_P_gauge = p_gauge;
	Utils.static_P_VOLTAGE = p_voltage;
	Utils.static_P_CURRENT = p_current;
	// --- Shawn add for Eason's Pad log
	
	Utils.static_RSOC = rsoc;
	Utils.static_USOC = usoc;
	Utils.static_PVOLT = pvolt;
	Utils.static_AI = ai;
	Utils.static_TEMP = temp;
	Utils.static_GAUGE_MODE = gauge_mode;

	//Utils.static_PAThermal ;
	Utils.static_PMICThermal = PMICThermal;
	Utils.static_CaseThermal =CaseThermal;
	Utils.static_XOThermal = XOThermal;

	Utils.static_CHG_THERM_L = CHG_THERM_L ;
	Utils.static_ADC_PIN5 = ADC_PIN5 ;
	Utils.static_ADC_PIN6  = ADC_PIN6 ;
	Utils.static_CASE_THERM = CASE_THERM ;
	Utils.static_QUIET_THERM = QUIET_THERM ;
	Utils.static_PA_THERM0 = PA_THERM0 ;
	Utils.static_REAR_TEMP = REAR_TEMP ;

	//Utils.static_UnitTestState = unitteststate;
	
        	publishProgress(cpuUsage,
				memUsage,
				cpu0Thermal,
				cpu1Thermal,
				cpu2Thermal,
				cpu3Thermal,
				cpu4Thermal,
				cpu5Thermal,
				cpu6Thermal,
				cpu7Thermal,
				pcbThermal,
				batteryThermal,
				cpu0CurrentFreq,
				cpu1CurrentFreq,
				cpu2CurrentFreq,
				cpu3CurrentFreq,
				cpu4CurrentFreq,
				cpu5CurrentFreq,
				cpu6CurrentFreq,
				cpu7CurrentFreq,
				modemthermal,
				bat_volt,
				bat_current,
				gputhermal,
				camerathermal,
				PAThermal,
				PMICThermal,
				CaseThermal,
				XOThermal,
				CHG_THERM_L,
				ADC_PIN5,
				ADC_PIN6,
				CASE_THERM,
				QUIET_THERM,
				PA_THERM0,
				REAR_TEMP
				);
	        try {
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
