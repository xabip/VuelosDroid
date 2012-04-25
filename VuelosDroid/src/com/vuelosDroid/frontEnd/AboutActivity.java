/*
 * Copyright (C) 2011 Wglxy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vuelosDroid.frontEnd;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.app.AlertDialog; 
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vuelosDroid.R;


/**
 * This is the About activity in the dashboard application.
 * It displays some text and provides a way to get back to the home activity.
 *
 */

public class AboutActivity extends AbstractActivity{
	AlertDialog.Builder builder;
	AlertDialog alertDialog;
	int valor = 0;
	int valorIni = 0;
	/**
	 * onCreate
	 *
	 * Called when the activity is first created. 
	 * This is where you should do all of your normal static set up: create views, bind data to lists, etc. 
	 * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
	 * 
	 * Always followed by onStart().
	 *
	 * @param savedInstanceState Bundle
	 */
	private GregorianCalendar cal;

    private String[] horariosNum = {"-1", "10", "11", "12", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
     WheelView hourss;

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView (R.layout.activity_about);
        cal = new GregorianCalendar();
		/**Capture the AutoCompleteTextView widget*/
      /* // AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_origen);
        *//**Get the list of the months*//*
        String[] aeropuertos = getResources().getStringArray(R.array.aeropuertos_array);
        *//**Create a new ArrayAdapter and bind list_item.xml to each list item*//*
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, aeropuertos);
        *//**Associate the adapter with textView*//*
        textView.setAdapter(adapter);*/
       // titulos = getResources().getStringArray(R.array.titulos);
	    String[] horarios = getResources().getStringArray(R.array.horario_array);
	    for (int i = 0; i < horarios.length; i++) {
			Log.e(TAG, horarios[i]);
		}
		Log.d(TAG, "1");
        hourss = (WheelView) findViewById(R.id.wheel_horario);
		Log.d(TAG, "2");
		String[] ah = { "6:00 – 8:00", "8:00 – 10:00", "10:00 – 12:00",	"12:00 – 14:00",
				"14:00 – 16:00", "16:00 – 18:00", "18:00 – 20:00", "20:00 – 22:00",
				"22:00 – 00:00", "00:00 – 2:00", "2:00 – 4:00", "4:00 – 6:00"};
        
		final TextView text = (TextView) findViewById(R.id.text_wheel);
        ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<String>(this, ah);
       // NumericWheelAdapter hourAdapter = new NumericWheelAdapter(this, 0, 23);
		Log.d(TAG, "3");
		ampmAdapter.setItemResource(R.layout.wheel_text_item);
		Log.d(TAG, "4");
		ampmAdapter.setItemTextResource(R.id.text_wheel);
		Log.d(TAG, "5");
        hourss.setViewAdapter(ampmAdapter);
		Log.d(TAG, "6");
		OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				text.setText(newValue);
			}
		};
		


	}
	
	public void onClickBoton(View v){
		Log.w(TAG, valor + "");
		Log.e(TAG, cal.toString());
		Log.e(TAG, cal.get(Calendar.DAY_OF_MONTH)+"");
		Log.e(TAG, cal.get(Calendar.MONTH) + "");
		Log.e(TAG, cal.get(Calendar.YEAR)+"");
		
		

		//alertDialog.show();

		final Dialog ventana=new Dialog(this);
		
		ventana.setContentView(R.layout.activity_about);
		 final WheelView hours =  (WheelView) ventana.findViewById(R.id.wheel_horario);
			Log.d(TAG, "2");
			String[] ah = { "6:00 – 8:00", "8:00 – 10:00", "10:00 – 12:00",	"12:00 – 14:00",
					"14:00 – 16:00", "16:00 – 18:00", "18:00 – 20:00", "20:00 – 22:00",
					"22:00 – 00:00", "00:00 – 2:00", "2:00 – 4:00", "4:00 – 6:00"};
	        
			//final TextView text = (TextView) findViewById(R.id.text_wheel);
	        ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<String>(this, ah);
			ampmAdapter.setItemResource(R.layout.wheel_text_item);
			Log.d(TAG, "4");
			ampmAdapter.setItemTextResource(R.id.text_wheel);
			Log.d(TAG, "5");
	        hours.setViewAdapter(ampmAdapter);
			Log.d(TAG, "6");
			OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					Log.e(TAG, "Ruleta Cambiada: " + oldValue+ " "+ newValue);
					valor = newValue;
					hourss.setCurrentItem(valor);
					
				}
				
			};
			hours.addChangingListener(wheelListener);
			hours.setCurrentItem(valor);
			hours.setVisibleItems(4);
			Button boton = (Button) ventana.findViewById(R.id.boton_ok_rueda);
			boton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Log.w(TAG, valor+"");
					ventana.dismiss();
					
				}
			});
			ventana.setTitle("Seleccione el horario");
			
			ventana.show(); 
		
	}
	
	  protected Dialog onCreateDialog(int id) {
  	    Dialog dialogo = null;
  	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
  	    
  	    builder.setTitle("Informacion");
  	    builder.setMessage("Esto es un mensaje de alerta.");
  	 
  	    
  	 
  	    return builder.create();

  	}
	
	

} 