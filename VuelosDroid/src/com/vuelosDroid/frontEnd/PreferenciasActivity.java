package com.vuelosDroid.frontEnd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.vuelosDroid.R;


public class PreferenciasActivity extends AbstractActivity {
	
	SharedPreferences prefer;
	private int tamano;
	private SeekBar seek;
	private TextView textSeek;
	SharedPreferences.Editor editor;
	RadioGroup radioPrefs;
	RadioButton rad;
	

	protected void onCreate(Bundle savedInstanceState){
	    super.onCreate(savedInstanceState);
	    setContentView (R.layout.activity_preferencias);

		prefer = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
	    editor = prefer.edit();

		tamano = prefer.getInt("tamano", 10);
		Log.e(TAG, ""+ tamano);
		seek = (SeekBar)findViewById(R.id.seekBar1);
		seek.setProgress(tamano);
		textSeek = (TextView)findViewById(R.id.text_seek);
		textSeek.setText(""+tamano);
	    radioPrefs = (RadioGroup)findViewById(R.id.grupo_modo);
	    if(prefer.getInt("modo", 0) == 0){
		   rad = (RadioButton)findViewById(R.id.radio_prefs0);
	    }else{
	    	rad = (RadioButton)findViewById(R.id.radio_prefs1);
	    }
	    radioPrefs.check(rad.getId());
		setOnClicks();

	}
	
	public void setOnClicks(){
		seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				editor.commit();
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				editor.putInt("tamano", progress);
				textSeek.setText(""+progress);
			}
		});
		
		radioPrefs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.e(TAG, ""+checkedId);
				rad = (RadioButton) findViewById(checkedId);
				if (rad.getText().equals("Manual")){
					editor.putInt("modo", 1);

				}else{
					editor.putInt("modo", 0);
				}
				editor.commit();
			}
		});
	}
	
	public void onClickActualizar(View v){
	}
	
	public void onClickSearch(View v){
		startActivity (new Intent(getApplicationContext(), BusquedaActivity.class));
	}
}

