/*
 Copyright 2012 Xabier Pena & Urko Guinea

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.vuelosDroid.frontEnd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.behind.AlarmaService;
import com.vuelosDroid.backEnd.behind.AlarmasSqlAux;

/**
 * 
 * @author Xabi
 *
 */
public class PreferenciasActivity extends AbstractActivity  {

	/**
	 * Constantes de estado
	 */
	public static final int SI = 1;
	public static final int NO = 0;

	/**
	 * Variables de los elementos de la interfaz
	 */
	CheckBox checkBoxSonido;
	CheckBox checkBoxDespegar;
	CheckBox checkBoxAterrizar;
	CheckBox checkBoxCambios;
	CheckBox checkBoxAlarma;
	private SeekBar seek;
	private TextView textSeek;
	private int id;

	/**
	 * Variables de estado
	 */
	private int sonido;
	private int cambios;
	private int aterrizar;
	private int despegar;
	private int minutos;
	private int alarma;
	private Bundle bundle;
	private Bundle bun;

	protected void onCreate(Bundle savedInstanceState) {
		bun = savedInstanceState;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferencias_alarma);
		bundle = this.getIntent().getExtras();
		id = bundle.getInt("id");
		Log.d(TAG, "PreferenciasActivity - OnCreate + id: " + id);
		getAlarmasId();
		setLayout();
		setOnClicks();

	}

	public void getAlarmasId() {
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - getAlarmasId - Funciona la llamada");
		String[] args = new String[] { AlarmasSqlAux.ID,
				AlarmasSqlAux.ATERRIZAR, AlarmasSqlAux.CAMBIOS,
				AlarmasSqlAux.DESPEGAR, AlarmasSqlAux.MINUTOS,
				AlarmasSqlAux.SONIDO, AlarmasSqlAux.ALARMAVERDAD };
		String[] args2 = { id + "" };
		Cursor c = db.query("alarmas_aux", args, "id=?", args2, null, null,
				null);
		// Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {

			Log.d(TAG,
					"PreferenciasActivity - getAlarmasId - id:" + c.getInt(0));
			Log.d(TAG,
					"PreferenciasActivity - getAlarmasId - ATERRIZAR:"
							+ c.getInt(1));
			Log.d(TAG,
					"PreferenciasActivity - getAlarmasId - CAMBIOS:"
							+ c.getInt(2));
			Log.d(TAG,
					"PreferenciasActivity - getAlarmasId - DESPEGAR:"
							+ c.getInt(3));
			Log.d(TAG,
					"PreferenciasActivity - getAlarmasId - MINUTOS:"
							+ c.getInt(4));
			Log.d(TAG,
					"PreferenciasActivity - getAlarmasId - SONIDO:"
							+ c.getInt(5));

			aterrizar = c.getInt(1);
			cambios = c.getInt(2);
			despegar = c.getInt(3);
			minutos = c.getInt(4);
			sonido = c.getInt(5);
			alarma = c.getInt(6);
		}
		db.close();
	}

	/**
	 * Update the bd and recharges the view
	 */

	private void guardarCambios() {
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "PreferenciasActivity - guardarCambios - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ATERRIZAR, aterrizar);
		editor.put(AlarmasSqlAux.CAMBIOS, cambios);
		editor.put(AlarmasSqlAux.DESPEGAR, despegar);
		editor.put(AlarmasSqlAux.MINUTOS, minutos);
		editor.put(AlarmasSqlAux.SONIDO, sonido);
		editor.put(AlarmasSqlAux.ALARMAVERDAD, alarma);
		//editor.put(AlarmasSqlAux.ALARMAVERDAD, alarma);
		String[] args2 = { id + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
		Intent intent = new Intent(this, AlarmaService.class);
		intent.putExtra("id", id);
		startService(intent);
		getAlarmasId();
		setLayout();
	}

	/**
	 * Prepare and set the layout
	 */
	private void setLayout() {
		checkBoxSonido = (CheckBox) findViewById(R.id.checkbox_preferencias_alarma_sonido);
		checkBoxDespegar = (CheckBox) findViewById(R.id.checkbox_preferencias_alarmas_cambios_despege);
		checkBoxAterrizar = (CheckBox) findViewById(R.id.checkbox_preferencias_alarma_cambios_aterrizaje);
		checkBoxCambios = (CheckBox) findViewById(R.id.checkbox_preferencias_alarma_cambios);
		checkBoxAlarma = (CheckBox) findViewById(R.id.checkbox_preferencias_alarmas_alarma);
		seek = (SeekBar) findViewById(R.id.seekBar_preferencias_alarma_minutos);
		textSeek = (TextView) findViewById(R.id.text_preferencias_alarma_minutos_numero);

		if (sonido == SI) {
			checkBoxSonido.setChecked(true);
		}
		if (despegar == SI) {
			checkBoxDespegar.setChecked(true);
		}
		if (cambios == SI) {
			checkBoxCambios.setChecked(true);
		}
		if (aterrizar == SI) {
			checkBoxAterrizar.setChecked(true);
		}
		if (sonido == SI){
			checkBoxAterrizar.setChecked(true);
		}
		if (alarma == SI){
			checkBoxAlarma.setChecked(true);
		}
		seek.setProgress(minutos);
		textSeek.setText(getMin(minutos));

	}

	public void setOnClicks() {
		seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.i(TAG,
						"PreferenciasActivity - setOnclicks - onProgressChanged");
				minutos = seekBar.getProgress();
				guardarCambios();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				Log.i(TAG,
						"PreferenciasActivity - setOnclicks - onProgressChanged");
				textSeek.setText("" + getMin(progress));
			}
		});

		checkBoxSonido
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.i(TAG,
						"PreferenciasActivity - setListeners - checkBoxSonido - onCheckedChanged");
				if (isChecked) {
					sonido = SI;
				} else {
					sonido = NO;
				}
				guardarCambios();
			}
		});

		checkBoxDespegar
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.i(TAG,
						"PreferenciasActivity - setListeners - checkBoxDespegar - onCheckedChanged");
				if (isChecked) {
					despegar = SI;
				} else {
					despegar = NO;
				}
				guardarCambios();
			}
		});

		checkBoxAterrizar
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.i(TAG,
						"PreferenciasActivity - setListeners - checkBoxAterrizar - onCheckedChanged");
				if (isChecked) {
					aterrizar = SI;
				} else {
					aterrizar = NO;
				}
				guardarCambios();
			}
		});

		checkBoxCambios
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.i(TAG,
						"PreferenciasActivity - setListeners - checkBoxCambios - onCheckedChanged");
				if (isChecked) {
					cambios = SI;
				} else {
					cambios = NO;
				}
				guardarCambios();
			}
		});

		checkBoxAlarma
		.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.i(TAG,
						"PreferenciasActivity - setListeners - checkBoxAlarma - onCheckedChanged");
				if (isChecked) {
					alarma = SI;
				} else {
					alarma = NO;
				}
				guardarCambios();
			}
		});
	}

	public void onClickActualizar(View v) {
	}

	public String getMin (int pMin){
		if (pMin == 0){
			return "0 mins";
		} else {
			if (pMin / 60 == 0){
				if (pMin % 60 == 1){
					return "1 min";
				} else {
					return (pMin % 60 + " mins");
				}
			}
			else if (pMin / 60 == 1){
				if (pMin % 60 == 1){
					return "1 h 1 min";
				} else {
					return ("1 h " + pMin % 60 + " mins");
				}
			} else {
				if (pMin % 60 == 1){
					return (pMin/60 + " h " + " 1 min");
				} else {
					return (pMin/60 + " h " + pMin % 60 + " mins");
				}
			}
		}
	}

	public void onClickSearch(View v) {
		startActivity(new Intent(getApplicationContext(),
				BusquedaActivity.class));
	}
	
	protected void onResume(){
		onCreate(bun);
	}
	
	public void onClickGuardar(View v){
		finish();
	}
	
	
}

