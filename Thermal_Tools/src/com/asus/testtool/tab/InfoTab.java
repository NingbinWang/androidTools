package com.asus.testtool.tab;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asus.testtool.R;
import com.asus.testtool.info.InfoUpdate;
import com.asus.testtool.utils.Utils;

import android.app.Activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Debug;

import android.util.Log;
import android.widget.TextView;

public class InfoTab extends Activity {

	/** Called when the activity is first created. */
	public static String TAG = "AsusTestTool";
	public TextView cpuUsage, memoryUsage, cpu0Thermal,cpu1Thermal,cpu2Thermal,cpu3Thermal,cpu4Thermal,cpu5Thermal,cpu6Thermal,cpu7Thermal, pcbThermal, batteryThermal;
    public TextView modemThermal,volt,current,gpuThermal,cameraThermal;
	public TextView cpu0CurFreq,cpu1CurFreq,cpu2CurFreq,cpu3CurFreq,cpu4CurFreq,cpu5CurFreq,cpu6CurFreq,cpu7CurFreq;

	public TextView PAThermal,PMICThermal,CaseThermal,XOThermal,CHG_THERM_L,ADC_PIN5,ADC_PIN6,CASE_THERM,QUIET_THERM,PA_THERM0,REAR_TEMP;
	
	
	public static InfoUpdate infoUpdate = null;
	
	public static final int CPU_USAGE = 0;
	public static final int MEMORY_USAGE = 1;
	public static final int CPU_THERMAL = 2;
	public static final int PMIC_THERMAL = 3;
	public static final int BATTERY_THERMAL = 4;	
	@Override
	public void onCreate(Bundle savedInstanceState) {
//		Log.d("============================================", "oncreate");	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_tab);
		
        
		TextView text_CPUType = (TextView) findViewById(R.id.text_cpu_type);
		if(Utils.readInfo("/proc/cpuinfo").contains("MSM8939")) {
			Utils.static_CpuType = new String("MSM8939");
		} else if(Utils.readInfo("/proc/cpuinfo").contains("MSM8916")){
			Utils.static_CpuType = new String("MSM8916");
		} else if(Utils.readInfo("/proc/cpuinfo").contains("MSM8929")){
			Utils.static_CpuType = new String("MSM8929");
		} else if(android.os.Build.MODEL.contains("MSM8996")){
			Utils.static_CpuType = new String("MSM8996");
		} else if(Utils.readInfo("/proc/cpuinfo").contains("MSM8953")){
			Utils.static_CpuType = new String("MSM8953");
		} else {
			Utils.static_CpuType = new String("Unkown");
		}
		text_CPUType.setText(Utils.static_CpuType);
		TextView text_OS = (TextView) findViewById(R.id.text_os);
		text_OS.setText(Build.VERSION.RELEASE);
		
		TextView text_Model = (TextView) findViewById(R.id.text_Model);
		text_Model.setText(android.os.Build.MODEL);
		
		TextView text_Build = (TextView) findViewById(R.id.text_build);
		text_Build.setText(Build.DISPLAY);
		
		TextView text_Kernel = (TextView) findViewById(R.id.text_kernel);
		text_Kernel.setText(getFormattedKernelVersion());
		
		cpuUsage = (TextView) findViewById(R.id.text_cpu_usage);
		memoryUsage = (TextView) findViewById(R.id.text_memory_usage);
		modemThermal = (TextView) findViewById(R.id.text_modem_thermal);
		gpuThermal = (TextView) findViewById(R.id.text_gpu_thermal);
		cameraThermal = (TextView) findViewById(R.id.text_camera_thermal);
		cpu0Thermal = (TextView) findViewById(R.id.text_cpu0_thermal);
		cpu1Thermal = (TextView) findViewById(R.id.text_cpu1_thermal);
		cpu2Thermal = (TextView) findViewById(R.id.text_cpu2_thermal);
		cpu3Thermal = (TextView) findViewById(R.id.text_cpu3_thermal);
		cpu4Thermal = (TextView) findViewById(R.id.text_cpu4_thermal);
		cpu5Thermal = (TextView) findViewById(R.id.text_cpu5_thermal);
		cpu6Thermal = (TextView) findViewById(R.id.text_cpu6_thermal);
		cpu7Thermal = (TextView) findViewById(R.id.text_cpu7_thermal);
		pcbThermal = (TextView) findViewById(R.id.text_pcb_thermal);
		batteryThermal = (TextView) findViewById(R.id.text_battery_thermal);
		PAThermal = (TextView) findViewById(R.id.pa_temp_val);
		PMICThermal = (TextView) findViewById(R.id.pmic_temp_val);
		CaseThermal = (TextView) findViewById(R.id.case_temp_val);
		XOThermal = (TextView) findViewById(R.id.xo_temp_val);

		CHG_THERM_L = (TextView) findViewById(R.id.chg_therm_l);
		ADC_PIN5 = (TextView) findViewById(R.id.adc_pin5);
		ADC_PIN6 = (TextView) findViewById(R.id.adc_pin6);
		CASE_THERM = (TextView) findViewById(R.id.case_thermal);
		QUIET_THERM = (TextView) findViewById(R.id.quiet_therm);
		PA_THERM0 = (TextView) findViewById(R.id.pa_therm0);
		REAR_TEMP = (TextView) findViewById(R.id.rear_temp);

		cpu0CurFreq = (TextView) findViewById(R.id.text_cpu0_cur_freq);
		cpu1CurFreq = (TextView) findViewById(R.id.text_cpu1_cur_freq);
		cpu2CurFreq = (TextView) findViewById(R.id.text_cpu2_cur_freq);
		cpu3CurFreq = (TextView) findViewById(R.id.text_cpu3_cur_freq);
		cpu4CurFreq = (TextView) findViewById(R.id.text_cpu4_cur_freq);
		cpu5CurFreq = (TextView) findViewById(R.id.text_cpu5_cur_freq);
		cpu6CurFreq = (TextView) findViewById(R.id.text_cpu6_cur_freq);
		cpu7CurFreq = (TextView) findViewById(R.id.text_cpu7_cur_freq);
		volt = (TextView) findViewById(R.id.text_volt);
		current = (TextView) findViewById(R.id.text_current);
		
        infoUpdate = new InfoUpdate(this);
        infoUpdate.execute();   			     
        
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
    @Override
	protected void onPause() {
		super.onPause();
	
    }	
    @Override
	protected void onStop() {	
		super.onStop();
    }
    @Override
	protected void onDestroy() {
		super.onDestroy();	    	
		infoUpdate.cancel(true);
        
    }   
    
	
	// Parser
	private String parser(String source, String target, char end) {

		String result = "";
		char array[] = source.toCharArray();
		int start = source.indexOf(target);
		if (start == -1)
			return "ERROR!";
		boolean Tag = false;
		for (int i = start; i < source.length(); i++) {
			if ((array[i] == ':') || (array[i] == '=')) { // separate char (":"
															// or "=")

				i++;
				while (i < source.length() && array[i] != end) { // end char
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
	
    public static String getFormattedKernelVersion() {
        String procVersionStr;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
            try {
                procVersionStr = reader.readLine();
            } finally {
                reader.close();
            }

            final String PROC_VERSION_REGEX =
                "\\w+\\s+" + /* ignore: Linux */
                "\\w+\\s+" + /* ignore: version */
                "([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
                "\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /* group 2: (xxxxxx@xxxxx.constant) */
                "\\([^)]+\\)\\s+" + /* ignore: (gcc ..) */
                "([^\\s]+)\\s+" + /* group 3: #26 */
                "(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
                "(.+)"; /* group 4: date */

            Pattern p = Pattern.compile(PROC_VERSION_REGEX);
            Matcher m = p.matcher(procVersionStr);

            if (!m.matches()) {
                Log.e(TAG, "Regex did not match on /proc/version: " + procVersionStr);
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                Log.e(TAG, "Regex match on /proc/version only returned " + m.groupCount()
                        + " groups");
                return "Unavailable";
            } else {
                return (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString();
            }
        } catch (IOException e) {  
            Log.e(TAG, "IO Exception when getting kernel version for Device Info screen", e);
            return "Unavailable";
        }
    }
    
    
    public void updateInfo(String [] arg){
    	cpuUsage.setText( arg[0].equals("wait")? "wait" : arg[0] +" %");
    	memoryUsage.setText(arg[1] +" %");
    	cpu0Thermal.setText( arg[2].equals("")? "error" : arg[2] +" C");
	cpu1Thermal.setText( arg[3].equals("")? "error" : arg[3] +" C");
	cpu2Thermal.setText( arg[4].equals("")? "error" : arg[4] +" C");
	cpu3Thermal.setText( arg[5].equals("")? "error" : arg[5] +" C");
		cpu4Thermal.setText( arg[6].equals("")? "error" : arg[6] +" C");
	cpu5Thermal.setText( arg[7].equals("")? "error" : arg[7] +" C");
	cpu6Thermal.setText( arg[8].equals("")? "error" : arg[8] +" C");
	cpu7Thermal.setText( arg[9].equals("")? "error" : arg[9] +" C");
	pcbThermal.setText( arg[10].equals("")? "error" : arg[10] +" C");
	batteryThermal.setText( arg[11].equals("")? "error" : arg[11] +" C");

	cpu0CurFreq.setText( arg[12].equals("")? "error" : arg[12] +" hz");
	cpu1CurFreq.setText( arg[13].equals("")? "offline" : arg[13] +" hz");
	cpu2CurFreq.setText( arg[14].equals("")? "offline" : arg[14] +" hz");
	cpu3CurFreq.setText( arg[15].equals("")? "offline" : arg[15] +" hz");
	cpu4CurFreq.setText( arg[16].equals("")? "error" : arg[16] +" hz");
	cpu5CurFreq.setText( arg[17].equals("")? "offline" : arg[17] +" hz");
	cpu6CurFreq.setText( arg[18].equals("")? "offline" : arg[18] +" hz");
	cpu7CurFreq.setText( arg[19].equals("")? "offline" : arg[19] +" hz");
	modemThermal.setText( arg[20].equals("")? "error" : arg[20] +" C");

	volt.setText(arg[21].equals("")? "error" : arg[21] +" uV");
	current.setText( arg[22].equals("")? "error" : arg[22] +" uA");
	gpuThermal.setText( arg[23].equals("")? "error" : arg[23] +" C");
	cameraThermal.setText( arg[24].equals("")? "error" : arg[24] +" C");

	PAThermal.setText( arg[25].equals("")? "error" : arg[25] +" C");
	PMICThermal.setText( arg[26].equals("")? "error" : arg[26] +" C");
	CaseThermal.setText( arg[27].equals("")? "error" : arg[27] +" C");
	XOThermal.setText( arg[28].equals("")? "error" : arg[28] +" C");

	CHG_THERM_L.setText( arg[29].equals("")? "error" : arg[29]);
	ADC_PIN5.setText( arg[30].equals("")? "error" : arg[30] +" C");
	ADC_PIN6.setText( arg[31].equals("")? "error" : arg[31] +" C");
	CASE_THERM.setText( arg[32].equals("")? "error" : arg[32] +" C");
	QUIET_THERM.setText( arg[33].equals("")? "error" : arg[33] +" C");
	PA_THERM0.setText( arg[34].equals("")? "error" : arg[34] +" C");
	REAR_TEMP.setText( arg[35].equals("")? "error" : arg[35] +" C");
/*	
    	try {
            pcbThermal.setText( arg[6].equals("")? "error" : Float.valueOf(arg[6])/1000 +" C");
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            Log.d("AsusTestTool", "Read pmic thermal error !");
        }


    	try {
            batteryThermal.setText( arg[4].equals("")? "error" : Float.valueOf(arg[4])/10 +" C");
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            Log.d("AsusTestTool", "Read battery thermal error !");
        }*/
    }

}
