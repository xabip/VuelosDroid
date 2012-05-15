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
