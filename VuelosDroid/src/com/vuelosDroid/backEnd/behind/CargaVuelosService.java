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

package com.vuelosDroid.backEnd.behind;

import com.vuelosDroid.backEnd.scrapper.DatosVuelo;
import com.vuelosDroid.backEnd.scrapper.VuelosJSoup;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class CargaVuelosService extends Service{

	private static final String TAG = "VuelosAndroid";
	private String url;
	private VuelosJSoup vuelosJsoup;
	private long endTime;


	private DatosVuelo datos;
	private boolean depegado;
	private boolean aterrizado;
	private int estado = INICIAL;

	//Notificaciones
    private NotificationManager mNotificationManager;
	private int SIMPLE_NOTFICATION_ID;
	private Intent pIntent;

	//Constantes de estado
	private static final int INICIAL = 0;		//No se sabe el estado
	private static final int MINIMO = 7;		//Actualizacion cada 3 horas
	private static final int MUYALTA = 6; 		//Vuelo a menos de 5 minutos de la llegada prevista
	private static final int ALTA = 5;  		//Vuelo a menos de 20 minutos de la llegada prevista
	private static final int MEDIA = 4; 		//Vuelo despegado
	private static final int BAJA = 3;			//Vuelo a falta de 30 minutos para despegar
	private static final int MUYBAJA = 2;		//Vuelo a falta de más de 30 minutos para despegar
	private static final int TERMINADO = 1;		//Vuelo a ha aterrizado


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Señal de inicio de servicio recibida (en el servicio)");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Avoids service termination
		pIntent = intent;
		Bundle bundle = pIntent.getExtras();
		if (!bundle.isEmpty()){
			url = bundle.getString("url");
			Log.i(TAG, "Servicio inciado y bundle con url");
			
		}else{
			Log.i(TAG, "Servicio reiniciado y bundle vacio");
		}
		startForeground(0, null);
		
		return START_FLAG_REDELIVERY; 
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Servicio finalizado");
		
		super.onDestroy();
	}
	

}
