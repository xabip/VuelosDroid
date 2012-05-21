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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.viewpagerindicator.TitleProvider;
import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.behind.BusquedaRecienteSql;
import com.vuelosDroid.backEnd.scrapper.DatosVuelo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 
 * @author Xabi
 *
 */
public class ViewPagerAdapter extends PagerAdapter implements TitleProvider{

	public String TAG = "VUELOS ANDROID";
	private final Context context;
	AutoCompleteTextView auto;
	ArrayAdapter<String> adapter;
	ArrayList<String> destinosArray = new ArrayList<String>();
	private String[] titulos ;
	private String[] horariosNum = {"-1", "10", "11", "12", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
	ArrayList<DatosVuelo> datos = new ArrayList<DatosVuelo>();
	//private static final int DIALOGO = 1;
	int valor = 0;
	int valor2 = 0;
	ListView miLista;
	ArrayList<String> dias = new ArrayList<String>();
	LinearLayout lCod;
	LinearLayout lSal;
	LinearLayout lLle;

	public ViewPagerAdapter(Context context){
		this.context = context;
		this.titulos = context.getResources().getStringArray(R.array.titulos);
	}

	public String getTitle(int position) {
		return titulos[position];
	}

	public void onClickBorrar(View v){

	}

	public void getBusquedaReciente(LinearLayout v){
		BusquedaRecienteSql alarms =  new BusquedaRecienteSql(context); 
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.w(TAG, "Funciona la lectura de busqueda reciente");
		String[] args = new String[] {BusquedaRecienteSql.CODIGOVUELO, BusquedaRecienteSql.HORAORIGEN,
				BusquedaRecienteSql.AEROPUERTODESTINO, BusquedaRecienteSql.AEROPUERTOORIGEN,
				BusquedaRecienteSql.DIA, BusquedaRecienteSql.URL, BusquedaRecienteSql.FECHAORIGEN};

		Cursor c = db.query("busqueda_reciente", args, null, null, null, null, null);
		//Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {
			//Recorremos el cursor hasta que no haya más registros
			do {
				DatosVuelo dat = new DatosVuelo();

				Log.d(TAG, c.getString(0));
				dat.setNombreVuelo(c.getString(0));
				dat.setHoraOrigen(c.getString(1));
				dat.setAeropuertoDestino(c.getString(2));
				dat.setAeropuertoOrigen(c.getString(3));
				dat.setLinkInfoVuelo(c.getString(5));
				dat.setFechaOrigen(c.getString(6));
				Log.e(TAG, dat.getFechaOrigen());
				dias.add(c.getString(4));

				datos.add(dat);

			} while(c.moveToNext());
		}
		db.close();
		setListaReciente(v);

		/*if (!datos.isEmpty()){
		}else{
			DatosVuelo dats = new DatosVuelo();
			dats.setNombreVuelo("NoHayAntiguas"); 
			datos.add(dats);
			TextView text = (TextView)v.findViewById(R.id.text_columna1_sin_resultados);
			text.setText("No hay busquedas recientes");
		}*/
	}

	public void setListaReciente(LinearLayout v){
		miLista = (ListView) v.findViewById(R.id.lista_resultados_reciente);
		Collections.reverse(datos);
		if(datos.size()>10){
			for(int i = 11; i < datos.size(); i++){
				datos.remove(i);
			}
		}
		Collections.reverse(dias);
		if(dias.size()>10){
			for(int i = 11; i < dias.size(); i++){
				dias.remove(i);
			}
		}
		if(datos.isEmpty()){
			DatosVuelo dats = new DatosVuelo();
			dats.setAeropuertoOrigen("NoHay");
			datos.add(dats);
			dias.add("No");
		}
		miLista.setAdapter(new miAdapter(context, datos, dias));
		final Intent intent = new Intent(context, VueloResultadoActivity.class);

		miLista.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bundle extras = new Bundle();
				Log.w(TAG, datos.get(arg2).getLinkInfoVuelo());
				extras.putString("url", datos.get(arg2).getLinkInfoVuelo());
				Log.w("VuelosAndroid2", "ViewPager - setListaReciente" + datos.get(arg2).getLinkInfoVuelo());

				extras.putString("codigo", "");
				extras.putString("dia", "hoy");
				if(!datos.get(arg2).getHoraOrigen().equals("--")){
					String[] pHora = datos.get(arg2).getHoraOrigen().split(":");
					if(Integer.parseInt(pHora[0]) >= 22){
						if(!datos.get(arg2).getFechaOrigen().equals("--")){
							String[] pDia = datos.get(arg2).getHoraOrigen().split("/");
							GregorianCalendar cal = new GregorianCalendar();
							if (cal.get(Calendar.HOUR_OF_DAY) < 2){
								if(Integer.parseInt(pDia[0]) == (cal.get(Calendar.DATE) - 1)){
									extras.putInt("or", 1);
								}
							}
						}
					}
					else{
						extras.putInt("or", 0);
					}
				}
				intent.putExtras(extras);
				if(!tieneRed()){
					Toast toast1 = Toast.makeText(context, "Necesitas tener red para poder continuar", Toast.LENGTH_SHORT);
					toast1.show();
				}else{
					context.startActivity(intent);
				}	
			}
		});
		//datosVuelos = (List<DatosVuelo>) listaVuelos.getValues();
	}

	public boolean tieneRed() {

		boolean wifi = false;
		boolean mobile = false;

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = cm.getAllNetworkInfo();
		for (NetworkInfo ni : info) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					wifi = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					mobile = true;
		}
		return wifi || mobile;
	}
	@Override
	public int getCount() {
		return titulos.length;
	}

	@Override
	public Object instantiateItem(View pager, int position) {
		LinearLayout v = null;
		if (position == 0) {
			v = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.columna1, null);
			setLayoutCodigo(v);
			lCod = v;
		} else if (position == 1) {
			v = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.columna2, null);
			setLayoutsSalidas(v);
			lSal =  (LinearLayout) LayoutInflater.from(context).inflate(R.layout.columna2, null);
		} else {
			v = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.columna3, null);
			setLayoutLlegadas(v);
			lLle =  (LinearLayout) LayoutInflater.from(context).inflate(R.layout.columna3, null);
		}    
		((ViewPager) pager).addView(v, 0);
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

		return v;
	}

	public void setLayoutCodigo(LinearLayout v){
		final EditText edit = (EditText)v.findViewById(R.id.edittext_codigo);
		TextView text = (TextView)v.findViewById(R.id.text_columna1_codigo);
		text.setPressed(true);
		Button boton = (Button)v.findViewById(R.id.btn_codigo);
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
		getBusquedaReciente(v);
		
		

		//Listeners
		boton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String text = edit.getText().toString();
				if(text.equals("")){  
					Log.w(TAG,text+ " No hay nada");
					Toast toast1 = Toast.makeText(context.getApplicationContext(), "El código introducido es incorrecto", Toast.LENGTH_SHORT);
					toast1.show();
				} else if (!Pattern.matches("\\D{2,4}\\d{3,5}", text)){
					Log.w(TAG,text+ "ViewPagerAdapter - setLayoutCodigo - onClick -  No valido");
					Toast toast1 = Toast.makeText(context.getApplicationContext(), "El código introducido es incorrecto", Toast.LENGTH_SHORT);
					toast1.show();
				}else {
					Log.w(TAG, text);

					Intent intent = new Intent(context, VueloResultadoActivity.class);
					Bundle extras = new Bundle();
					extras.putString("url", " ");
					Log.e(TAG, text);
					extras.putString("codigo", text);
					//RadioButton rad = (RadioButton)(radioDia.findViewById(radioDia.getCheckedRadioButtonId()));
					extras.putString("dia", "hoy");
					extras.putString("tipo", " ");
					intent.putExtras(extras);
					if(!tieneRed()){
						Toast toast1 = Toast.makeText(context.getApplicationContext(), "No hay red. No puedes hacer búsquedas", Toast.LENGTH_SHORT);
						toast1.show();

					}else{
						context.startActivity(intent);
					}	
				}
			}
		});
		
		edit.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				edit.setText("");
				edit.refreshDrawableState();
				return false;
			}
		});
	}

	public void setLayoutLlegadas(LinearLayout v){
		//Seleccion de llegadas.
		//Inicializar los Autocomplete   <--Autocomplete Aeropuerto Origen-->
		final AutoCompleteTextView autoDestinos= (AutoCompleteTextView) v.findViewById(R.id.autocomplete_destino_llegadas);
		String[] aeropuertos = v.getResources().getStringArray(R.array.aeropuertos_array);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.list_item, aeropuertos);	
		autoDestinos.setAdapter(adapter);
		//autoOrigen.setHint("Introduce el origen");
		autoDestinos.setPressed(false);
		autoDestinos.setThreshold(1);
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(autoDestinos.getWindowToken(), 0);


		//<-- Autocomplete Aeropuerto Destino -->
		final AutoCompleteTextView autoOrigen = (AutoCompleteTextView) v.findViewById(R.id.autocomplete_origen_llegadas);
		autoOrigen.setEnabled(false);
		autoOrigen.setThreshold(1);

		String[] horarios = v.getResources().getStringArray(R.array.horario_array);
		for (int i = 0; i < horarios.length; i++) {
			Log.e(TAG, horarios[i]);
		}
		Log.d(TAG, "1");
		WheelView horarioLlegada = (WheelView) v.findViewById(R.id.wheel_horario_llegadas);
		Log.d(TAG, "2");


		ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<String>(context, horarios);
		Log.d(TAG, "3");
		ampmAdapter.setItemResource(R.layout.wheel_text_item);
		Log.d(TAG, "4");
		ampmAdapter.setItemTextResource(R.id.text_wheel);
		Log.d(TAG, "5");
		horarioLlegada.setViewAdapter(ampmAdapter);
		horarioLlegada.setVisibleItems(3);
		horarioLlegada.setMinimumWidth(170);
		horarioLlegada.setBackgroundColor(0xFFFFFF);
		Log.d(TAG, "6");
		OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				valor2 = newValue;
				Log.w(TAG, valor2+"");
			}
		};

		horarioLlegada.addChangingListener(wheelListener);


		//Boton
		final Button botonLlegadas = (Button)v.findViewById(R.id.btn_llegadas);


		//RadioButton
		final RadioGroup radioDia = (RadioGroup)v.findViewById(R.id.grupo_dia);
		radioDia.clearCheck();
		radioDia.check(R.id.radio1);

		//OnClickListeners <--AutoOrigen-->
		autoDestinos.setOnItemClickListener( new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("TAG","onItemClick "+ arg0.getItemAtPosition(arg2));
				setOrigenes(autoOrigen, arg0.getItemAtPosition(arg2).toString());
				autoOrigen.setHint("Cualquiera");
				autoOrigen.setHintTextColor(Color.argb(185,0, 0, 0));

				autoOrigen.setEnabled(true);
				botonLlegadas.setEnabled(true);
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(autoOrigen.getWindowToken(), 0);


			}
		});

		autoDestinos.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				autoDestinos.setText("");
				autoOrigen.setText("");
				autoDestinos.refreshDrawableState();
				autoOrigen.refreshDrawableState();
				return false;
			}
		});
		
		autoOrigen.setOnItemClickListener( new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("TAG","onItemClick "+ arg0.getItemAtPosition(arg2));
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(autoOrigen.getWindowToken(), 0);

			}
		});
		
		autoOrigen.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				autoOrigen.setText("");
				autoOrigen.refreshDrawableState();
				return false;
			}
		});

		//<--Boton-->
		botonLlegadas.setOnClickListener( new OnClickListener(){
			public void onClick(View arg0) {
				if(autoDestinos.getText().toString().equals("")){
					Toast toast1 = Toast.makeText(context.getApplicationContext(), "No has seleccionado ningún destino", Toast.LENGTH_SHORT);
					toast1.show();
				} else{
					Intent intent = new Intent(context, ResultadoActivity.class);
					Bundle extras = new Bundle();
					extras.putString("destino", autoDestinos.getText().toString());
					extras.putString("origen", autoOrigen.getText().toString());
					extras.putString("horario", horariosNum[valor2]);
					RadioButton rad = (RadioButton)(radioDia.findViewById(radioDia.getCheckedRadioButtonId()));
					extras.putString("dia", rad.getText().toString());
					extras.putString("tipo", "Destino:  ");
					intent.putExtras(extras);
					if(!tieneRed()){
						Toast toast1 = Toast.makeText(context.getApplicationContext(), "No hay red. No puedes hacer búsquedas", Toast.LENGTH_SHORT);
						toast1.show();

					}else{
						context.startActivity(intent);
					}	
				}
			} 
		});

		//Menus Contextuales.
		autoDestinos.setOnLongClickListener(null);
	}
	public void setLayoutsSalidas(LinearLayout v){

		//Seleccion de llegadas.
		//Inicializar los Autocomplete   <--Autocomplete Aeropuerto Origen-->
		final AutoCompleteTextView autoOrigen = (AutoCompleteTextView) v.findViewById(R.id.autocomplete_origen);
		String[] aeropuertos = v.getResources().getStringArray(R.array.aeropuertos_array);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.list_item, aeropuertos);	
		autoOrigen.setAdapter(adapter);
		autoOrigen.setHint("Introduce el origen");
		autoOrigen.setThreshold(1);
		autoOrigen.setPressed(false);
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(autoOrigen.getWindowToken(), 0);



		//<-- Autocomplete Aeropuerto Destino -->
		final AutoCompleteTextView autoDestino = (AutoCompleteTextView) v.findViewById(R.id.autocomplete_destino);
		autoDestino.setEnabled(false);
		autoDestino.setHint("Introduce un origen primero");
		autoDestino.setThreshold(1);
		imm.hideSoftInputFromWindow(autoDestino.getWindowToken(), 0);

		//<-- Wheel Horario -->
		String[] horarios = v.getResources().getStringArray(R.array.horario_array);
		for (int i = 0; i < horarios.length; i++) {
			Log.e(TAG, horarios[i]);
		}
		Log.d(TAG, "1");
		WheelView horarioLlegada = (WheelView) v.findViewById(R.id.wheel_horario_salidas);
		Log.d(TAG, "2");
		String[] ah = { "Cualquiera", "6:00 – 8:00", "8:00 – 10:00", "10:00 – 12:00",	"12:00 – 14:00",
				"14:00 – 16:00", "16:00 – 18:00", "18:00 – 20:00", "20:00 – 22:00",
				"22:00 – 00:00", "00:00 – 2:00", "2:00 – 4:00", "4:00 – 6:00"};

		ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<String>(context, ah);
		Log.d(TAG, "3");
		ampmAdapter.setItemResource(R.layout.wheel_text_item);
		Log.d(TAG, "4");
		ampmAdapter.setItemTextResource(R.id.text_wheel);
		Log.d(TAG, "5");
		horarioLlegada.setViewAdapter(ampmAdapter);
		horarioLlegada.setVisibleItems(3);
		horarioLlegada.setMinimumWidth(185);
		horarioLlegada.setBackgroundColor(0xFFFFFF);
		Log.d(TAG, "6");
		OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				valor = newValue;
				Log.w(TAG, valor+"");
			}
		};

		horarioLlegada.addChangingListener(wheelListener);
		//Boton
		final Button botonSalidas = (Button)v.findViewById(R.id.btn_salidas);


		//RadioButton
		final RadioGroup radioDia = (RadioGroup)v.findViewById(R.id.grupo_dia);
		radioDia.clearCheck();
		radioDia.check(R.id.radio1);

		//OnClickListeners <--AutoOrigen-->
		autoOrigen.setOnItemClickListener( new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("TAG","onItemClick "+ arg0.getItemAtPosition(arg2));
				setDestinos(autoDestino, arg0.getItemAtPosition(arg2).toString());
				autoDestino.setHint("Cualquiera");
				autoDestino.setHintTextColor(Color.argb(185,0, 0, 0));
				autoDestino.setEnabled(true);
				botonSalidas.setEnabled(true);
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(autoOrigen.getWindowToken(), 0);

			}
		});

		autoOrigen.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				autoOrigen.setText("");
				autoDestino.setText("");
				autoOrigen.refreshDrawableState();
				autoDestino.refreshDrawableState();
				return false;
			}
		});
		
		autoDestino.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("TAG","onItemClick "+ arg0.getItemAtPosition(arg2));

				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(autoOrigen.getWindowToken(), 0);

			}
		});

		autoDestino.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				autoDestino.setText("");
				autoDestino.refreshDrawableState();
				return false;
			}
		});
		
		//<-- Boton -->
		botonSalidas.setOnClickListener( new OnClickListener(){
			public void onClick(View arg0) {
				if(autoOrigen.getText().toString().equals("")){
					Toast toast1 = Toast.makeText(context.getApplicationContext(), "No has seleccionado ningún origen", Toast.LENGTH_SHORT);
					toast1.show();
				}else{
					Intent intent = new Intent(context, ResultadoActivity.class);
					Bundle extras = new Bundle();
					extras.putString("destino", autoDestino.getText().toString());
					extras.putString("origen", autoOrigen.getText().toString());
					extras.putString("horario", horariosNum[valor]);
					extras.putString("tipo", "Origen:  ");
					Log.w(TAG, horariosNum[valor]);
					RadioButton rad = (RadioButton)(radioDia.findViewById(radioDia.getCheckedRadioButtonId()));
					extras.putString("dia", rad.getText().toString());
					intent.putExtras(extras);
					if(!tieneRed()){
						Toast toast1 = Toast.makeText(context.getApplicationContext(), "No hay red. No puedes hacer búsquedas", Toast.LENGTH_SHORT);
						toast1.show();
					}else{
						context.startActivity(intent);
					}	
				}
			} 
		});

		//Menus Contextuales.
		autoOrigen.setOnLongClickListener(null);
	}

	public void setOrigenes(AutoCompleteTextView autoDestino, String in){
		if(destinosArray.size()>0){
			destinosArray = new ArrayList<String>();
		}
		String cod = in.substring((in.indexOf("(")+1), in.indexOf(")"));
		Log.v(TAG, "setAdapter " +cod);

		String s=""; 
		/*InputStream inp = context.getResources().openRawResource(R.raw.destinoorigenes); */
		String jsonContentType = "";
		int indice = 0;
		try { 
			InputStreamReader inp = new InputStreamReader(context.openFileInput("destinoorigenes.txt"));
			BufferedReader entrada = null; 

			/*entrada = new BufferedReader(new InputStreamReader(inp));*/ 
			entrada = new BufferedReader(inp);
			Log.v(TAG, "setAdapter " +entrada.toString());
			s = entrada.readLine();
			Log.v(TAG, "setAdapter " +s);

		} catch (FileNotFoundException e) { 
			Log.e(TAG, "Fallo al leer el fichero: " +e.getMessage());
		} catch (IOException e) { 
		} 

		try {
			JSONArray jsonContent = new JSONArray(s);
			while (!jsonContentType.equals(cod)){
				JSONObject item = jsonContent.getJSONObject(indice);
				jsonContentType = item.getString("cod");
				Log.v(TAG, "getAeropuertosDeDestino: " +jsonContentType);
				indice++;
			}
			indice--;
			JSONObject item = jsonContent.getJSONObject(indice);
			Log.d(TAG,item.toString());
			jsonContentType = item.getString("destinos");
			Log.d(TAG, jsonContentType);
			boolean salir = false;
			int ind = 0;
			jsonContent = new JSONArray(jsonContentType);
			while(!salir){
				item = jsonContent.getJSONObject(ind);
				jsonContentType = item.getString("nombre");
				Log.d(TAG, "Json "+jsonContentType);
				if(item.equals(null)){
					salir = true;
				}else {
					ind++;
					destinosArray.add(jsonContentType);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Poner el adaptador al Autocomplete.
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.list_item, destinosArray);
		autoDestino.setAdapter(adapter);   	 
	}

	public void setDestinos(AutoCompleteTextView autoOrigen, String in){
		if(destinosArray.size()>0){
			destinosArray = new ArrayList<String>();
		}
		String cod = in.substring((in.indexOf("(")+1), in.indexOf(")"));
		Log.v(TAG, "setAdapter " +cod);

		String s="";
		/*		InputStream inp = context.getResources().openRawResource(R.raw.origendestinos); 
		 */		String jsonContentType = "";
		 int indice = 0;
		 try { 
			 InputStreamReader inp = new InputStreamReader(context.openFileInput("origendestinos.txt"));
			 BufferedReader entrada = null; 

			 /*entrada = new BufferedReader(new InputStreamReader(inp));*/ 
			 entrada = new BufferedReader(inp);
			 //entrada = new BufferedReader(new InputStreamReader(inp)); 
			 Log.v(TAG, "setAdapter " +entrada.toString());
			 s = entrada.readLine();
			 Log.v(TAG, "setAdapter " +s);

		 } catch (FileNotFoundException e) { 
			 Log.e(TAG, "Fallo al leer el fichero: " +e.getMessage());
		 } catch (IOException e) { 
		 } 

		 try {
			 JSONArray jsonContent = new JSONArray(s);
			 while (!jsonContentType.equals(cod)){
				 JSONObject item = jsonContent.getJSONObject(indice);
				 jsonContentType = item.getString("cod");
				 Log.v(TAG, "getAeropuertosDeDestino: " +jsonContentType);
				 indice++;
			 }
			 indice--;
			 JSONObject item = jsonContent.getJSONObject(indice);
			 Log.d(TAG,item.toString());
			 jsonContentType = item.getString("destinos");
			 Log.d(TAG, jsonContentType);
			 boolean salir = false;
			 int ind = 0;
			 jsonContent = new JSONArray(jsonContentType);
			 while(!salir){
				 item = jsonContent.getJSONObject(ind);
				 jsonContentType = item.getString("nombre");
				 Log.d(TAG, "Json "+jsonContentType);
				 if(item.equals(null)){
					 salir = true;
				 }else {
					 ind++;
					 destinosArray.add(jsonContentType);
				 }
			 }
		 } catch (JSONException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 //Poner el adaptador al Autocomplete.
		 ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.list_item, destinosArray);
		 autoOrigen.setAdapter(adapter);   	 
	}

	/**
	 * Reinicializa la interfaz
	 * @param pCod  Layout del codigo
	 * @param pSalidas  Layout de salidas
	 * @param pLlegadas Layout de llegadas
	 */
	public void borrarCampos(){ 
		LinearLayout lCod = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.columna1, null);
		LinearLayout lSal = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.columna2, null);
		LinearLayout lLle = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.columna3, null);

		EditText edit = (EditText)lCod.findViewById(R.id.edittext_codigo);
		Button botonLlegadas = (Button) lSal.findViewById(R.id.btn_llegadas);
		AutoCompleteTextView autoOrigen = (AutoCompleteTextView) lSal.findViewById(R.id.autocomplete_origen_llegadas);
		AutoCompleteTextView autoDestinos= (AutoCompleteTextView) lSal.findViewById(R.id.autocomplete_destino_llegadas);
		AutoCompleteTextView autoOrigenLlegadas = (AutoCompleteTextView) lLle.findViewById(R.id.autocomplete_origen);
		AutoCompleteTextView autoDestinoLlegadas = (AutoCompleteTextView) lLle.findViewById(R.id.autocomplete_destino);
		Button botonSalidas = (Button)lLle.findViewById(R.id.btn_salidas);

		edit.setText("");
		//botonLlegadas.setEnabled(true);
		//autoOrigen.setText("");
		//autoDestinos.setText("");
		//autoOrigenLlegadas.setText("");
		//autoDestinoLlegadas.setText("");
		//botonSalidas.setEnabled(false);

	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		//( (ViewPager) collection ).removeView( (ListView) view );
		if (position == 1){
			((ViewPager)collection).removeView( (TextView)view );

		}else{
			((ViewPager)collection).removeView((LinearLayout) view);}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals( object );
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}


	private static class miAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<DatosVuelo> datosVuelos = new ArrayList<DatosVuelo>();
		ArrayList<String> dias;

		miAdapter(Context context, ArrayList<DatosVuelo> datos, ArrayList<String> pDia) {
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//listaVuelos = getInfoVuelos("", "BIO", "8", "hoy", "");
			Log.w("VuelosAndroid", "LLega al adapter"); 
			Log.e("VuelosAndroid", "LLega al adapter");
			Log.d("VuelosAndroid", "LLega al adapter"); 
			Log.i("VuelosAndroid", "LLega al adapter");
			Log.i("VuelosAndroid", datos.isEmpty()+"");

			dias = pDia;
			datosVuelos = datos;
			Log.i("VuelosAndroid", 			datos.toString());
			Log.i("VuelosAndroid", 			 datosVuelos.size()+"");

		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textNombre; 
			TextView textCodigo;	 
			TextView textHora;
			//	RadioGroup radioDia;
			RadioButton radioRecienteHoy;
			RadioButton radioRecienteMnn;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_reciente, null);
			}
			textNombre = (TextView) convertView.findViewById(R.id.text_item_reciente_nombre);
			textCodigo = (TextView) convertView.findViewById(R.id.text_item_reciente_codigo);
			textHora = (TextView) convertView.findViewById(R.id.text_item_reciente_hora);
			//	radioDia =(RadioGroup) convertView.findViewById(R.id.grupo_reciente);
/*			radioRecienteHoy = (RadioButton) convertView.findViewById(R.id.radio_reciente_hoy);
			radioRecienteMnn = (RadioButton) convertView.findViewById(R.id.radio_reciente_manana);
*/			/*			if(dias.get(position).contains("ho")){
				radioRecienteHoy.setChecked(true);
				radioRecienteMnn.setChecked(false);
				Log.i("VuelosAndroid", "hoy");
			}else{
				radioRecienteHoy.setChecked(false);
				radioRecienteMnn.setChecked(true);
				Log.i("VuelosAndroid", "mañana");

			}*/
			if(datosVuelos.get(position).getAeropuertoOrigen().equals("NoHay")){
				textNombre.setText("No has realizado ninguna búsqueda");
				textCodigo.setVisibility(View.GONE);
				textHora.setVisibility(View.GONE);
				return convertView;
			}
			String text = datosVuelos.get(position).getAeropuertoOrigen().
					substring(0, datosVuelos.get(position).getAeropuertoOrigen().indexOf("(")-1);
			String text2 = datosVuelos.get(position).getAeropuertoDestino().
					substring(0, datosVuelos.get(position).getAeropuertoDestino().indexOf("(")-1);
			if (text2.contains("esti")){
				text2 = text2.replace("Destino: ", "");
			}

			if(text.contains("Origen:")){
				text = text.replace("Origen: ", "");
			}	
			textNombre.setText(text + " - " + text2);
			textCodigo.setText(datosVuelos.get(position).getNombreVuelo());
			textHora.setText(datosVuelos.get(position).getHoraOrigen());



			//text2.setText(datosVuelos.get(position).getNombreCompany());

			//textHoraLlegada.setText("Hora de llegada:   " + datosVuelos.get(position).getHoraDestino());
			return convertView;	 
		}


		public int getCount() {
			return datosVuelos.size();
		}


		public Object getItem(int position) {
			return position;
		}


		public long getItemId(int position) {
			return position;
		}

	}
}