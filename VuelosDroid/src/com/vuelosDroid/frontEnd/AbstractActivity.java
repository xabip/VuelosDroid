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


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.vuelosDroid.R;

/** Actividad Base de la aplicacion. En ella están implemetados los metodos necesarios para 
 * poder utilizar el menu superior en todas las actividades por igual. 
 * 
 * @author Xabier Pena
 */
public abstract class AbstractActivity extends Activity{

	//Constantes
	protected static final String TAG = "VuelosAndroid";
	public static boolean RED = true;


	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		debug("OnCreate Abstract");
		RED = tieneRed();
	}

	protected void onDestroy(){
		super.onDestroy();
	}

	protected void onPause(){
		super.onPause();
	}

	protected void onRestart(){
		super.onRestart();
	}

	protected void onResume(){
		super.onResume();
	}

	protected void onStart(){
		super.onStart();
	}

	protected void onStop(){
		super.onStop();
	}

	public void onClickPreferencias(View v){
		Intent intent = new Intent(getApplicationContext(), PreferenciasActivity.class);
		startActivity(intent);
	}

	public void onClickBusqueda(View v){
		startActivity (new Intent(getApplicationContext(), BusquedaActivity.class));
	}

	public void onClickHome(View v){
		goHome(this);
	}

	public boolean tieneRed() {
		/*	ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		return true;
		}

		return true;*/
		boolean wifi = false;
		boolean mobile = false;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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

	/**
	 * Handle the click on the About button.
	 * 
	 * @param v View
	 * @return void
	 */

	public void onClickAbout(View v){
		startActivity (new Intent(getApplicationContext(), AboutActivity.class));
	}

	/**
	 * Handle the click of a Feature button.
	 * 
	 * @param v View
	 * @return void
	 */

	public void onClickFeature (View v){
		if (v.getId() == R.id.busqueda_btn) {
			startActivity (new Intent(getApplicationContext(), BusquedaActivity.class));
		}else if (v.getId() == R.id.preferencias_btn){
			startActivity (new Intent(getApplicationContext(), AlarmasActivity.class)); 
			/*		}else if (v.getId() == R.id.seguimiento_btn){
			startActivity (new Intent(getApplicationContext(), PreferenciasActivity.class));
		case R.id.about_btn :
		    Bundle extras = new Bundle();
		    //extras.putString("url","");
		    Intent intent = new Intent(getApplicationContext(), VueloResultadoActivity.class);
		    intent.putExtras(extras); 
			startActivity (intent);
			break;*/
		}/*else if(v.getId() == R.id.ayuda_btn ){
			startActivity (new Intent(getApplicationContext(), AboutActivity.class));
		}*/

	} 

	/**
	 */
	// More Methods

	/**
	 * Go back to the home activity.
	 * 
	 * @param context Context
	 * @return void
	 */

	public void goHome(Context context){
		final Intent intent = new Intent(context, PrincipalActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	/**
	 * Use the activity label to set the text in the activity's title text view.
	 * The argument gives the name of the view.
	 *
	 * <p> This method is needed because we have a custom title bar rather than the default Android title bar.
	 * See the theme definitons in styles.xml.
	 * 
	 * @param textViewId int
	 * @return void
	 */

	public void setTitleFromActivityLabel(int textViewId){
		TextView tv = (TextView)findViewById(textViewId);
		if (tv != null) tv.setText(getTitle());
	} // end setTitleText

	/**
	 * Show a string on the screen via Toast.
	 * 
	 * @param msg String
	 * @return void
	 */

	public void toast(String msg){
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	} // end toast

	/**
	 * Envia el mensaje al debug
	 */
	public void debug(String msg){
		Log.i(TAG, msg);
		//toast(msg);
	}

} 
