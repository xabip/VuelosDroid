package com.vuelosDroid.backEnd.behind;

import java.io.IOException;
import java.util.Date;

import com.vuelosDroid.backEnd.scrapper.*;
import com.vuelosDroid.frontEnd.AbstractActivity;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class AlarmaService extends Service{

	private static final String TAG = "VuelosAndroid";
	private String url;
	private VuelosJSoup vuelosJsoup;
	private long endTime;
	private Intent intent;
	private PendingIntent pendingIntent;
	AlarmManager alarmManager;
	private int red;
	private String aterrizadoSin;
	private DatosVuelo datos;
	private boolean depegado;
	private boolean aterrizado;
	private int estado = INICIAL;
	private int id;

	//Notificaciones
	private NotificationManager mNotificationManager;
	private int SIMPLE_NOTFICATION_ID;
	private Intent pIntent;
	private static final String TEXTO_CANCELADO = "El vuelo ha sido cancelado";
	private static final String TEXTO_RETRASADO = "El vuelo ha sido retrasado";
	private static final String TEXTO_ATERRIZADO = "El vuelo ha llegado";
	private static final String TEXTO_SALIDO = "El vuelo ha despegado";



	//Constantes de estado
	private static final int INICIAL = 0;		//No se sabe el estado
	private static final int MINIMO = 7;		//Actualizacion cada 3 horas
	private static final int MUYALTA = 6; 		//Vuelo a menos de 5 minutos de la llegada prevista
	private static final int ALTA = 5;  		//Vuelo a menos de 20 minutos de la llegada prevista
	private static final int MEDIA = 4; 		//Vuelo despegado
	private static final int BAJA = 3;			//Vuelo a falta de 30 minutos para despegar
	private static final int MUYBAJA = 2;		//Vuelo a falta de más de 30 minutos para despegar
	private static final int TERMINADO = 1;		//Vuelo a ha aterrizado

	private static final int CONECTADO = 0;
	private static final int DESCONECTADO = 1;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		vuelosJsoup = new VuelosJSoup();
		Log.i(TAG, "AlarmaService - onCreate - Señal de inicio de servicio recibida");

		//Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStart(Intent intent, final int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "AlarmaService - onStart - started");
		pIntent = intent;
		intent = new Intent(this, MiReceiver.class);
		id = intent.getIntExtra("id", 999999999);
		intent.putExtra("id", 999);
		Log.d(TAG, "AlarmaService - onStart - id: " + id);
		setRed();

		if(id == 999999999){
			pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			Log.i(TAG, "AlarmaService - onStart - dentro de id defecto");
			Log.i(TAG, "AlarmaService - onStart - Antes de coger alarmas");
			getAlarmas(intent);
			if(!(datos == null)){
				/*while (estado != TERMINADO){
					controlVuelo(url);
				}*/
				Log.i(TAG, "AlarmaService - onStart - Alarmas procesadas");
				//getAlarmas();
			}else{
				Log.i(TAG, "AlarmaService - onStart - No hay alarmas");
				Log.i(TAG, "AlarmaService - No hay alarmas");
				stopService(pIntent);
			}
		}else{
			Log.d(TAG, "AlarmaService - onStart - dentro de id con id: " + id);
			pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		    Log.i(TAG, "AlarmaService - onStart - dentro de id - antes de llamar a getAlarmasId");
			getAlarmasId(id, intent);

			if((datos == null)){
				/*while (estado != TERMINADO){
					controlVuelo(url);
				}*/
			   
				//controlVuelo(url, id);
			}else{
				Log.i(TAG, "AlarmaService - onStart - No hay alarmas");
				stopService(pIntent);
			}	
		}
	}

	public void setRed(){
		if (tieneRed()){
			red = CONECTADO;
		}else {
			Log.w(TAG, "AlarmaService - setRed - No hay red");

			red = DESCONECTADO;
		}

	}

	public boolean tieneRed() {
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}

		return false;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "AlarmaService - onDestroy - Servicio finalizado");

		super.onDestroy();
	}

	public void controlVuelo(String pUrl, int id){
		Log.d(TAG, "AlarmaService - controlVuelo - estado: " + estado);

		//Log.w(TAG, "AlarmaService - controlVuelo " + estado);

		//pendingIntent = PendingIntent.getBroadcast(this, 0, intent, id);
		Log.d(TAG, "AlarmaService - controlVuelo - conectado: " + red);

		switch (estado){
		case INICIAL:
			//endTime = System.currentTimeMillis() + 60*10000; //10 minutos de refresco
			synchronized (this) {
				try {
					Log.d(TAG, "AlarmaService - controlVuelo - red: " + red);

					//wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						Log.d(TAG, "AlarmaService - controlVuelo - conectado: " + red);
						Log.d(TAG, "AlarmaService - controlVuelo - url: " + pUrl);

						getDatos(pUrl);
						Log.i(TAG, "AlarmaService - controlVuelo - pasa la url");

						break;

					case DESCONECTADO:
						Log.d(TAG, "AlarmaService - controlVuelo - desconectado: " + red);

						break;
					}

					if(verSiDespegado(datos.getEstadoVueloOrigen())){
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
						//notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
					}else{
						ponerEstadoAntes(getDiferencia(datos.getEstadoVueloOrigen()));
					}
					if(verSiAterrizado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
					}
					if(verSiCancelado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_CANCELADO, "");
					}
					if(verSiRetrasado(datos.getEstadoVueloDestino())){
						notificar(TEXTO_RETRASADO, datos.getEstadoVueloDestino());
					}
					controlVuelo(pUrl, id);
				} catch (Exception e) {
				}
			}			break;

		case MUYALTA:
			//endTime = System.currentTimeMillis() + 30*100; //30 segundos de refresco

			synchronized (this) {
				try {

					//wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl);
						break;

					case DESCONECTADO:

						break;
					}
					if(verSiCancelado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_CANCELADO, "");
					}
					if(verSiRetrasado(datos.getEstadoVueloDestino())){
						notificar(TEXTO_RETRASADO, datos.getEstadoVueloDestino());
					}
					if(verSiAterrizado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
					}else{
						alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (3 * 1000),pendingIntent);
					}
				} catch (Exception e) {
				} 
			}

			break;

		case ALTA:
			//endTime = System.currentTimeMillis() + 3*100; //3 minutos de refresco
			synchronized (this) {
				try {

					//wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl);
						break;

					case DESCONECTADO:

						break;
					}
					ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
					if(verSiCancelado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_CANCELADO, "");
					}
					if(verSiRetrasado(datos.getEstadoVueloDestino())){
						notificar(TEXTO_RETRASADO, datos.getEstadoVueloDestino());
					}
					if(verSiAterrizado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
					}else{
						alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (30 * 1000),pendingIntent);
					}

				} catch (Exception e) {
				}
			}
			break;

		case MEDIA:
			//endTime = System.currentTimeMillis() + 20*10000; //20 minutos de refresco
			synchronized (this) {
				try {
					//wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl);
						break;

					case DESCONECTADO:

						break;
					}
					ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
					if(verSiCancelado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_CANCELADO, "");
					}
					if(verSiRetrasado(datos.getEstadoVueloDestino())){
						notificar(TEXTO_RETRASADO, datos.getEstadoVueloDestino());
					}
					if(verSiAterrizado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
					}else{
						alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (20 * 10000),pendingIntent);
					}
				} catch (Exception e) {
				}
			}
			break;

		case BAJA:
			//endTime = System.currentTimeMillis() + 5*10000; //10 minutos de refresco
			synchronized (this) {
				try {
					//wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl);
						break;

					case DESCONECTADO:

						break;
					}
					if (verSiRetrasado(datos.getEstadoVueloOrigen())){
						notificar(TEXTO_RETRASADO, datos.getEstadoVueloOrigen());
					}
					if (verSiCancelado(datos.getEstadoVueloOrigen())){
						estado=TERMINADO;
						notificar("El vuelo ha sido cancelado", "");
					}
					if(verSiDespegado(datos.getEstadoVueloOrigen())){

						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
						notificar(TEXTO_SALIDO, datos.getEstadoVueloOrigen());
					}else{
						alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (5 * 10000),pendingIntent);
					}

					if(verSiAterrizado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
					}
				} catch (Exception e) {
				}

			}
			break;

		case MUYBAJA:
			//endTime = System.currentTimeMillis() + 60*10000; //60 minutos de refresco
			synchronized (this) {
				try {
					//wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl);
						break;

					case DESCONECTADO:

						break;
					}
					if(verSiDespegado(datos.getEstadoVueloOrigen())){
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
						notificar(TEXTO_SALIDO, datos.getEstadoVueloOrigen());
						alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (20 * 10000),pendingIntent);

					}else{
						ponerEstadoAntes(getDiferencia(datos.getEstadoVueloOrigen()));
						alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (60 * 10000),pendingIntent);
					}
					if(verSiAterrizado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
					}
				} catch (Exception e) {
				}
			}
			break;

		case MINIMO:
			//endTime = System.currentTimeMillis() + 180*10000; //3 horas horas de refresco
			synchronized (this) {
				try {
					//wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl);
						break;

					case DESCONECTADO:

						break;
					}

					if(verSiDespegado(datos.getEstadoVueloOrigen())){
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
						notificar(TEXTO_SALIDO, datos.getEstadoVueloOrigen());
						alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (20 * 10000),pendingIntent);
					}else{
						ponerEstadoAntes(getDiferencia(datos.getEstadoVueloOrigen()));
						alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + (180 * 10000),pendingIntent);
					}
					if(verSiAterrizado(datos.getEstadoVueloDestino())){
						estado = TERMINADO;
						notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
					}
				} catch (Exception e) {
				}
			}
			break;

		case TERMINADO:
			try {
				Log.i(TAG, "AlarmaService - Servicio finalizando");
				notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino());
				borrarAlarma(datos.getLinkInfoVuelo(), id);
				//stopService(pIntent);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}
	}

	public void getDatos(String pUrl){
		try {
			datos = vuelosJsoup.getDatosVuelo(pUrl);			
		} catch (NoHayVueloException e) {
			System.out.println("No hay Vuelos con esos parámetros");
		}catch(IOException ex1){
			System.out.println("error no hay conexion a internet");
		} catch (Exception e) {
			Log.e(TAG, "AlarmaService - getDatos - Excepcion "+e.toString());
		}
		if(datos.getEstadoVueloDestino().contains("prevista")){
			//setEstado();
		}else{
			estado = TERMINADO;
		}	
	}

	public void ponerEstado(int pTiempo){
		if (pTiempo <= 5){
			estado = MUYALTA;
		}else if (pTiempo > 5 && pTiempo < 20){
			estado = ALTA;
		}else {
			estado = MEDIA;
		}
	}

	public void ponerEstadoAntes(int pTiempo){
		if (pTiempo < 30){
			estado = BAJA;
		}else if (pTiempo >30 && pTiempo < 240){
			estado = MUYBAJA;
		}else {
			estado = MINIMO;
		}
	}

	public boolean verSiDespegado(String pEstado){
		Log.d(TAG, "AlarmaService - verSiDespegado - despegado: "+ pEstado.contains("despegado"));
		return pEstado.contains("despegado");
	}

	public boolean verSiCancelado(String pEstado){
		return pEstado.contains("cancel");
	}

	public boolean verSiRetrasado(String pEstado){
		return pEstado.contains("retra");
	}

	public boolean verSiAterrizado(String pEstado){
		switch (red) {
		case CONECTADO:
			Log.d(TAG, "AlarmaService - verSiAterrizado - CONECTADO -aterrizado:  "+ pEstado.contains("aterrizado"));
			return pEstado.contains("aterrizado");

		case DESCONECTADO:
			if ((getDiferencia(pEstado) <0)&& (aterrizadoSin.equals("no"))){
				Log.d(TAG, "AlarmaService - verSiAterrizado - DESCONECTADO - aterrizado: "+ pEstado.contains("aterrizado"));
				notificar("El vuelo deberia haber aterrizado ", "SIN CONEXION");

			}else {
				return false;
			}

		default:
			return false;
		}
	}

	public int getDiferencia(String pEstado){
		String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ")+6).split(":");
		int minutos = 0;
		minutos += (((Integer.parseInt(horaVuelo[0])) - (new Date().getHours())))*60;
		minutos += (((Integer.parseInt(horaVuelo[1])) - (new Date().getMinutes())));
		Log.d(TAG, "AlarmaServer - getDiferencia - minutos de diferencia: "+ minutos);

		return (minutos);
	}
	public String getHora(String pEstado){
		//Log.i(TAG, "Servicio "+pEstado.substring(pEstado.indexOf("a las ")+6));
		Date horaActual = new Date();

		Log.i(TAG, "AlarmaService - gerHora - horaActual: " + horaActual.getHours()+":"+horaActual.getMinutes());
		Log.i(TAG, "AlarmaService - gerHora -  estado: " +   pEstado.substring(pEstado.indexOf("a las ")+6));
		String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ")+6).split(":");
		//int a = pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]:").length;
		Log.i(TAG, "AlarmaService - gerHora - " + (Integer.parseInt(horaVuelo[0]) - horaActual.getHours()));
		Log.i(TAG, "AlarmaService - gerHora - " +  (Integer.parseInt(horaVuelo[1]) - horaActual.getMinutes()));

		/*for (int i = 0; i < pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]").length; i++) {
			Log.i(TAG, pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]")[i]);
		}*/

		return pEstado.substring(pEstado.indexOf("a las ")+6);
	}

	public void notificar (String pMens, String pMens2){
		Context context = getApplicationContext();
		String ns = Context.NOTIFICATION_SERVICE;
		int icono = android.R.drawable.btn_star_big_on;
		CharSequence contentTitle = pMens;
		CharSequence contentText = pMens2 ;
		long hora = System.currentTimeMillis();

		//Creacion de la notificacion
		mNotificationManager = (NotificationManager)getSystemService(ns);
		Notification mNotificacion = new Notification(icono, contentTitle, hora);

		//Creacion del intent
		Intent notIntent = new Intent(context, AbstractActivity.class);
		PendingIntent contIntent = PendingIntent.getActivity(context, 0, notIntent, 0);
		
		mNotificacion.setLatestEventInfo(context, contentTitle, contentText, contIntent);
		
		//AutoCancel: cuando se pulsa la notificaión ésta desaparece
		mNotificacion.flags |= Notification.FLAG_AUTO_CANCEL;

		//Añadir sonido, vibración y luces
		mNotificacion.defaults |= Notification.DEFAULT_SOUND;
		mNotificacion.defaults |= Notification.DEFAULT_VIBRATE;
		mNotificacion.defaults |= Notification.DEFAULT_LIGHTS;
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, mNotificacion);


	}

	public void getAlarmas(Intent intent){
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - getAlarmas - Funciona la llamada");

		String[] args = new String[] {AlarmasSqlAux.URL, AlarmasSql.NOMBREVUELO, 
				AlarmasSql.FECHAORIGEN, AlarmasSql.HORAORIGEN, AlarmasSql.NOMBRECOMPANY,
				AlarmasSqlAux.HORADESTINO, AlarmasSqlAux.ATERRIZADOSIN, AlarmasSqlAux.ID};

		Cursor c = db.query("alarmas_aux", args, null, null, null, null, null);
		//Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {
			//Recorremos el cursor hasta que no haya más registros
			do {
				datos = new DatosVuelo();
				Log.d(TAG, c.getString(0));
				datos.setLinkInfoVuelo(c.getString(0));
				Log.d(TAG, c.getString(1));
				datos.setNombreVuelo(c.getString(1));
				datos.setFechaOrigen(c.getString(2));
				datos.setHoraOrigen(c.getString(3));
				datos.setNombreCompany(c.getString(4));	
				datos.setHoraDestino(c.getString(5));
				aterrizadoSin = c.getString(6);
				url=datos.getLinkInfoVuelo();
				id = c.getInt(7);
				Log.d(TAG, "AlarmaService - getAlarmas - id: " + id);
				Log.d(TAG, "AlarmaService - getAlarmas - intent: " + intent.toString());
				Log.d(TAG, "AlarmaService - getAlarmas - this: " + this.toString());
				intent.putExtra("id", id);
				pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				
				controlVuelo(url, id);

			} while(c.moveToNext());
		}
		db.close();
		//estado = TERMINADO;
		stopService(pIntent);

	}

	public void getAlarmasId(int id, Intent intent){
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - getAlarmasId - Funciona la llamada");
		String[] args = new String[] {AlarmasSqlAux.URL, AlarmasSql.NOMBREVUELO, 
				AlarmasSql.FECHAORIGEN, AlarmasSql.HORAORIGEN, AlarmasSql.NOMBRECOMPANY,
				AlarmasSqlAux.HORADESTINO, AlarmasSqlAux.ATERRIZADOSIN, AlarmasSqlAux.ID};
		String[] args2 = {id+""};
		Cursor c = db.query("alarmas_aux", args, "id=?", args2, null, null, null);
		//Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {
			//Recorremos el cursor hasta que no haya más registros
			do {
				datos = new DatosVuelo();
				Log.d(TAG, "AlarmaService - dentro del vuelo - url: " + c.getString(0));
				datos.setLinkInfoVuelo(c.getString(0));
				Log.d(TAG, "AlarmaService - dentro del vuelo - NombreVuelo: " + c.getString(1));
				
				datos.setNombreVuelo(c.getString(1));
				datos.setFechaOrigen(c.getString(2));
				datos.setHoraOrigen(c.getString(3));
				datos.setNombreCompany(c.getString(4));	
				datos.setHoraDestino(c.getString(5));
				aterrizadoSin = c.getString(6);
				url=datos.getLinkInfoVuelo();
				id = c.getInt(7);
				Log.d(TAG, "AlarmaService - getAlarmasId " + id);
				intent.putExtra("id", id);
				pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				controlVuelo(url, id);
			} while(c.moveToNext());
		}
		db.close();
		stopService(pIntent);

	}

	public void borrarAlarma(String pUrl, int pId){
		Log.d(TAG, "AlarmaService - borrarAlarma - Empieza - url: " + pUrl);
		Log.d(TAG, "AlarmaService - borrarAlarma - Empieza - id: " + pId);
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		db.execSQL("DELETE FROM alarmas_aux WHERE "+ AlarmasSqlAux.ID + "='" + id + "' ");
		ponerSeg(pUrl);
		db.close();
	}


	public void ponerAterrizadoSin(){
		AlarmasSql alarms =  new AlarmasSql(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(AlarmasSql.URL, datos.getLinkInfoVuelo());
		cv.put(AlarmasSql.NOMBREVUELO, datos.getNombreVuelo());
		cv.put(AlarmasSql.ALARMA, 1);
		cv.put(AlarmasSql.EMPEZADO, 0);
		cv.put(AlarmasSql.HORAORIGEN, datos.getHoraOrigen());
		cv.put(AlarmasSql.FECHAORIGEN, datos.getFechaOrigen());
		cv.put(AlarmasSql.NOMBRECOMPANY, datos.getNombreCompany());
		cv.put(AlarmasSqlAux.HORADESTINO, datos.getHoraDestino());
		cv.put(AlarmasSqlAux.ATERRIZADOSIN, "si");


		String[] args = {datos.getLinkInfoVuelo()};
		db.update("alarmas", cv, AlarmasSqlAux.URL+"=?", args );
		db.close();
	}
	public void ponerSeg(String pUrl){
		getDatos(pUrl);
		AlarmasSql alarms =  new AlarmasSql(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(AlarmasSql.URL, datos.getLinkInfoVuelo());
		cv.put(AlarmasSql.ALARMA, 1);
		cv.put(AlarmasSql.EMPEZADO, 0);
		cv.put(AlarmasSql.HORAORIGEN, datos.getHoraOrigen());
		cv.put(AlarmasSql.NOMBREVUELO, datos.getNombreVuelo());
		cv.put(AlarmasSql.FECHAORIGEN, datos.getFechaOrigen());
		cv.put(AlarmasSql.NOMBRECOMPANY, datos.getNombreCompany());

		db.insert("alarmas", AlarmasSql.URL, cv);

		db.close();

	}

}