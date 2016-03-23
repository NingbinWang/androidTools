package com.asus.tool;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;

public class ModemCrash extends Activity{

	public static String tips = ""; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new AlertDialog.Builder(this)	  
		.setTitle("Modem Crash")   
		.setMessage(tips)    
		.setPositiveButton("ok", null)  
		.show();  	
	}
}

