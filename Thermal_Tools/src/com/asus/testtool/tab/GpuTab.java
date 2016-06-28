package com.asus.testtool.tab;

import com.asus.testtool.R;
import com.nea.nehe.lesson08.Lesson08;

import android.app.Activity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Debug;

import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class GpuTab extends Activity implements OnCheckedChangeListener{

	/** Called when the activity is first created. */
	private GLSurfaceView glSurfaceView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		/* First Tab Content */

		setContentView(R.layout.gpu_tab);
		
		ToggleButton btn_GpuTest = (ToggleButton) findViewById(R.id.btn_gpuTest);
		
		btn_GpuTest.setOnCheckedChangeListener(this);
		
		glSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        Lesson08 lesson08 = new Lesson08(this);
        lesson08.setSpeedAndTester(1, 1/*, this*/);		
		glSurfaceView.setRenderer(lesson08);
		glSurfaceView.onPause();
		

	}
	
    @Override
	protected void onStop() {
		super.onStop();	
		
    	
    	
    }	
	//@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch(buttonView.getId()){
		case R.id.btn_gpuTest:
			if(isChecked)
				glSurfaceView.onResume();
			else
				glSurfaceView.onPause();
			break;
			
		}
		
	}

}