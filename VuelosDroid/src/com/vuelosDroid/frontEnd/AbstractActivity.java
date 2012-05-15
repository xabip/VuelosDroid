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


	public void onClickPreferencias(View v){
		Intent intent = new Intent(getApplicationContext(), PreferenciasActivity.class);
		startActivity(intent);
	}

	public void onClickBusqueda(View v){
		startActivity (new Intent(getApplicationContext(), BusquedaActivity.class));
	}

	public void onClickHome(View v){
		final Intent intent = new Intent(this, PrincipalActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
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


	public void onClickFeature (View v){
		if (v.getId() == R.id.busqueda_btn) {
			startActivity (new Intent(getApplicationContext(), BusquedaActivity.class));
		}else if (v.getId() == R.id.preferencias_btn){
			startActivity (new Intent(getApplicationContext(), AlarmasActivity.class)); 
		}
	} 


	public void setTitleFromActivityLabel(int textViewId){
		TextView tv = (TextView)findViewById(textViewId);
		if (tv != null) tv.setText(getTitle());
	}

	/**
	 * Envia el mensaje al debug
	 */
	public void debug(String msg){
		Log.i(TAG, msg);
		//toast(msg);
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

} 
