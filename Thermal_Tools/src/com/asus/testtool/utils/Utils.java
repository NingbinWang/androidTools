package com.asus.testtool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asus.testtool.Main;


import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

public class Utils {	

	public static String static_CpuUsage ;
	public static String static_CpuType ;
	public static String static_MemUsage ;
	public static String static_Sensor0Thermal;
	public static String static_Sensor1Thermal;
	public static String static_Sensor2Thermal;
	public static String static_Sensor3Thermal;
	public static String static_Sensor4Thermal;
    public static String static_Sensor5Thermal;
    public static String static_Sensor6Thermal;
    public static String static_Sensor7Thermal;
	public static String static_Cpu0Thermal;
	public static String static_Cpu1Thermal;
	public static String static_Cpu2Thermal;
	public static String static_Cpu3Thermal;
	public static String static_Cpu4Thermal;
	public static String static_Cpu5Thermal;
	public static String static_Cpu6Thermal;
	public static String static_Cpu7Thermal;
	public static String static_Sensor9Thermal;
	public static String static_Sensor10Thermal;
	public static String static_PCBThermal ;
	public static String static_BatteryThermal ;
	public static String static_ModemThermal ;
	public static String static_GPUThermal ;
	public static String static_CameraThermal ;
	public static String static_PAThermal ;
	public static String static_PMICThermal;
	public static String static_CaseThermal;
	public static String static_XOThermal;

	public static String static_CHG_THERM_L;
	public static String static_ADC_PIN5;
	public static String static_ADC_PIN6 ;
	public static String static_CASE_THERM;
	public static String static_QUIET_THERM;
	public static String static_PA_THERM0;
	public static String static_REAR_TEMP;

	public static String static_Cpu0CurrentFreq;
	public static String static_Cpu1CurrentFreq;
	public static String static_Cpu2CurrentFreq;
	public static String static_Cpu3CurrentFreq;
	public static String static_Cpu4CurrentFreq;
	public static String static_Cpu5CurrentFreq;
	public static String static_Cpu6CurrentFreq;
	public static String static_Cpu7CurrentFreq;
	public static String static_GpuCurrentFreq;	// --- Shawn add gpuclk to log.csv
	public static String static_bat_Current;
	//public static String static_PAThermal;
	public static String static_FCC;
	public static String static_RM;
	public static String static_SOC;
	public static String static_bat_VOLT;
	public static String static_BMS;
	public static String static_SWgauge;
	// +++ Shawn add for Eason's Pad log	
	public static String static_Pad_Cap;
	public static String static_P_gauge;
	public static String static_P_VOLTAGE;
	public static String static_P_CURRENT;
	// --- Shawn add for Eason's Pad log	
	
	public static String static_RSOC;
	public static String static_USOC;
	public static String static_PVOLT;
	public static String static_AI;
	public static String static_TEMP;
	public static String static_GAUGE_MODE;
	
	//public static String static_UnitTestState;

	public static String readInfo(String filePath) {
		String totalStr = "";
		String str;
		try {
			
			if(!new File(filePath).exists()){
				return "";
			}
			
			FileReader fr = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(fr, 8000);

			while ((str = reader.readLine()) != null) {
				totalStr += (str + "\n");
			}

			//Log.d("AsusTestTool", totalStr );

			reader.close();
			fr.close();
			return totalStr.trim();

		} catch (IOException e) {
//			Log.d("AsusTestTool","error:"+e.getMessage());
			e.printStackTrace();
			return "";
		}
	}
	
	public static String parseThermal (String logContent) {
		Pattern pattern = Pattern.compile("Result.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
		}
		return "";

/*
		Pattern pattern = Pattern.compile("Result: (\\d+) Raw:.*");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return "";
*/
	}
	
	public static String parseFCC (String logContent) {
		Pattern pattern = Pattern.compile("FCC.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseRM (String logContent) {
		Pattern pattern = Pattern.compile("RM.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}

	public static String parseSOC (String logContent) {
		Pattern pattern = Pattern.compile("SOC.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseVOLT (String logContent) {
		Pattern pattern = Pattern.compile("VOLT.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseBMS (String logContent) {
		Pattern pattern = Pattern.compile("BMS.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseSWgauge (String logContent) {
		Pattern pattern = Pattern.compile("SWgauge.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}

	// +++ Shawn add for Eason's Pad log	
	public static String parsePad_Cap (String logContent) {
		Pattern pattern = Pattern.compile("Pad_Cap.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseP_gauge (String logContent) {
		Pattern pattern = Pattern.compile("P_gauge.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseP_VOLTAGE (String logContent) {
		Pattern pattern = Pattern.compile("P_voltage.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseP_CURRENT (String logContent) {
		Pattern pat;
		pat =  Pattern.compile("[ ]");
		Pattern pattern = Pattern.compile("P_current.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			String[] result = pat.split(matcher.group());
			return result[1];
			//pattern = Pattern.compile("\t.*\\d+");
			//matcher = pattern.matcher(matcher.group());
			//if (matcher.find()) {
				//return matcher.group();
			//}
			//return "20";
		}
		return "40";
	}
	// --- Shawn add for Eason's Pad log
	
	public static String parseRSOC (String logContent) {
		Pattern pattern = Pattern.compile("RSOC.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseUSOC (String logContent) {
		Pattern pattern = Pattern.compile("USOC.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parsePVOLT (String logContent) {
		Pattern pattern = Pattern.compile("VOLT.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseAI (String logContent) {
		Pattern pattern = Pattern.compile("AI.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseTEMP (String logContent) {
		Pattern pattern = Pattern.compile("TEMP.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseGAUGE_MODE (String logContent) {
		Pattern pattern = Pattern.compile("Gauge Mode.*\\d+");
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			pattern = Pattern.compile("\\d+");
			matcher = pattern.matcher(matcher.group());
			if (matcher.find()) {
				return matcher.group();
			}
			//return "20";
		}
		return "40";
	}
	
	public static String parseMem (String logContent , String tag) {
		Pattern pattern = Pattern.compile(".*"+tag+":(.*)kB.*"); 
		Matcher matcher = pattern.matcher(logContent);
		if (matcher.find()) {
			return matcher.group(1).trim(); 
		}
		return "";
	}
	
	public static int[] readCpuUsage() {
		String totalStr = "";
		String str;
		int[] returnParameter = new int[] { 0, 0, 0 };

		try {
			FileReader fr = new FileReader("/proc/stat");
			BufferedReader reader = new BufferedReader(fr, 8000);

			while ((str = reader.readLine()) != null) {
				totalStr += (str + "\n");
			}

			reader.close();
			fr.close();
			fr=null;

		} catch (IOException e) {
			return returnParameter;
		}
		// split cat output
		String[] token = totalStr.split(" ");

		// calculate parameter
		int total = 0;
		total = Integer.parseInt(token[2]) + Integer.parseInt(token[3])
				+ Integer.parseInt(token[4]) + Integer.parseInt(token[5]);

		returnParameter[0] = total;
		returnParameter[1] = Integer.parseInt(token[2]); // user
		returnParameter[2] = Integer.parseInt(token[4]); // system
		return returnParameter;
	}   	
	

    public static boolean sdCardTest() {
        // TODO Auto-generated method stub
        
        try {
            Process process = Runtime.getRuntime().exec("su -c mount");
            String temp = getOutput(process);
            if (temp.contains(Main.sdCardFolder)) {
                return true;    
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return false;
    
    }	
    
    public static String getOutput (Process p) {
        InputStream is = p.getInputStream();
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                    sb.append(line);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sb.toString();
    }      
	
}
