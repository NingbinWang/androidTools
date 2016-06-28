package com.asus.testtool.unittest;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import android.util.Log;

public class AsusTest {
	final static String TAG = "ASUSCameraTest";
    final static CharSequence CTSItemsPhone[][] = {				
    	{"UnitTest",  		"ThermalCpuTest"},
    	{"UnitTest", 		"ThermalGpuTest"},
    	{"UnitTest", 		"ThermalBatTest"}
    }; // static CharSequence CTSItemsPhone[][]
      
    public CharSequence m_CTSItems[][];
    
    int mRandomTestCnt;
    boolean[] mRandomCheckedItems = new boolean[] { true,true,true,true,true,true,true,true,true };
    int[] mRandomPassCnt = new int[] {0,0,0,0,0,0,0,0,0};
    
    public AsusTest( ) {
    	m_CTSItems = CTSItemsPhone;
        
    }
    
    


}
