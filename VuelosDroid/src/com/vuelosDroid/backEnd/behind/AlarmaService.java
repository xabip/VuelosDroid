package com.vuelosDroid.backEnd.behind;

import java.io.IOException;
import java.util.Date;

import com.vuelosDroid.backEnd.scrapper.*;
import com.vuelosDroid.frontEnd.AbstractActivity;
import com.vuelosDroid.frontEnd.VueloResultadoActivity;

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

public class AlarmaService extends Service {

	private static final String TAG = "VuelosAndroid";
	private String url;
	private VuelosJSoup vuelosJsoup;
	private long endTime;
	private Intent intent;
	private PendingIntent pendingIntent;
	AlarmManager alarmManager;
	private int red;
	private String aterrizadoSin;
	private String despegadoSin;
	private DatosVuelo datos;
	private boolean depegado;
	private boolean aterrizado;
	private String salido;
	private int estado = INICIAL;
	private int id;

	// Notificaciones
	private NotificationManager mNotificationManager;
	private int SIMPLE_NOTFICATION_ID;
	private Intent pIntent;
	private static final String TEXTO_CANCELADO = "El vuelo ha sido cancelado";
	private static final String TEXTO_RETRASADO = "El vuelo ha sido retrasado";
	private static final String TEXTO_ATERRIZADO = "El vuelo ha llegado";
	private static final String TEXTO_SALIDO = "El vuelo ha despegado";

	// Constantes de estado
	private static final int INICIAL = 0; // No se sabe el estado
	private static final int MINIMO = 7; // Actualizacion cada 3 horas
	private static final int MUYALTA = 6; // Vuelo a menos de 5 minutos de la
	// llegada prevista
	private static final int ALTA = 5; // Vuelo a menos de 20 minutos de la
	// llegada prevista
	private static final int MEDIA = 4; // Vuelo despegado
	private static final int BAJA = 3; // Vuelo a falta de 30 minutos para
	// despegar
	private static final int MUYBAJA = 2; // Vuelo a falta de más de 30 minutos
	// para despegar
	private static final int TERMINADO = 1; // Vuelo a ha aterrizado

	private static final int CONECTADO = 0;
	private static final int DESCONECTADO = 1;

	public static final int SI = 1;
	public static final int NO = 0;

	@Override public IBinder onBind(Intent intent) {
		return null;
	}

	@Override public void onCreate() {
		super.onCreate();
		vuelosJsoup = new VuelosJSoup();
		Log.i(TAG,
				"AlarmaService - onCreate - Señal de inicio de servicio recibida");

		// Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
	}

	@Override public void onStart(Intent intent, final int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "AlarmaService - onStart - started");
		pIntent = intent;
		intent = new Intent(this, MiReceiver.class);
		id = intent.getIntExtra("id", 999999999);
		intent.putExtra("id", 999);
		Log.d(TAG, "AlarmaService - onStart - id: " + id);
		setRed();

		if (id == 999999999) {
			pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			Log.i(TAG, "AlarmaService - onStart - dentro de id defecto");
			Log.i(TAG, "AlarmaService - onStart - Antes de coger alarmas");
			getAlarmas(intent);
			if (!(datos == null)) {
				/*
				 * while (estado != TERMINADO){ controlVuelo(url); }
				 */
				Log.i(TAG, "AlarmaService - onStart - Alarmas procesadas");
				// getAlarmas();
			} else {
				Log.i(TAG, "AlarmaService - onStart - No hay alarmas");
				Log.i(TAG, "AlarmaService - No hay alarmas");
				stopService(pIntent);
			}
		} else {
			Log.d(TAG, "AlarmaService - onStart - dentro de id con id: " + id);
			pendingIntent = PendingIntent.getBroadcast(this, id, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			Log.i(TAG,
					"AlarmaService - onStart - dentro de id - antes de llamar a getAlarmasId");
			getAlarmasId(id, intent);

			if ((datos == null)) {
				/*
				 * while (estado != TERMINADO){ controlVuelo(url); }
				 */

				// controlVuelo(url, id);
			} else {
				Log.i(TAG, "AlarmaService - onStart - No hay alarmas");
				stopService(pIntent);
			}
		}
	}

	public void setRed() {
		if (tieneRed()) {
			red = CONECTADO;
		} else {
			Log.w(TAG, "AlarmaService - setRed - No hay red");

			red = DESCONECTADO;
		}

	}

	public boolean tieneRed() {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}

		return false;
	}

	@Override public void onDestroy() {
		Log.i(TAG, "AlarmaService - onDestroy - Servicio finalizado");

		super.onDestroy();
	}

	public void controlVuelo(String pUrl, int id, DatosAlarma pDatos) {
		int sonido = pDatos.getSonido();
		int despegar = pDatos.getDespegar();
		int aterrizar = pDatos.getAterrizar();
		int minutos = pDatos.getMinutos();
		int cambios = pDatos.getCambios();

		Log.d(TAG, "AlarmaService - controlVuelo - estado: " + estado);
		Log.d(TAG, "AlarmaService - controlVuelo - conectado: " + red);
		switch (estado) {
		case INICIAL:
			// endTime = System.currentTimeMillis() + 60*10000; //10 minutos de
			// refresco
			synchronized (this) {
				try {
					Log.d(TAG, "AlarmaService - controlVuelo - red: " + red);

					// wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						Log.d(TAG,
								"AlarmaService - controlVuelo - conectado: " + red);
						Log.d(TAG,
								"AlarmaService - controlVuelo - url: " + pUrl);

						getDatos(pUrl, pDatos);
						Log.i(TAG, "AlarmaService - controlVuelo - pasa la url");

						break;

					case DESCONECTADO:
						Log.d(TAG,
								"AlarmaService - controlVuelo - desconectado: " + red);

						break;
					}

					if (verSiDespegado(datos.getEstadoVueloOrigen(), pDatos)) {
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
						// notificar(TEXTO_ATERRIZADO,
						// datos.getEstadoVueloDestino());
					} else {
						ponerEstadoAntes(getDiferencia(datos
								.getEstadoVueloOrigen()));
					}
					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						estado = TERMINADO;
						/*notificar(TEXTO_ATERRIZADO,
								datos.getEstadoVueloDestino(), sonido);*/
					}
					if (verSiCancelado(datos.getEstadoVueloDestino())) {
						estado = TERMINADO;
						notificar(TEXTO_CANCELADO, "", sonido, pDatos);
					}
					if (verSiRetrasado(datos.getEstadoVueloDestino())) {
						/*notificar(TEXTO_RETRASADO,
								datos.getEstadoVueloDestino(), sonido);*/
					}
					controlVuelo(pUrl, id, pDatos);
				} catch (Exception e) {
				}
			}
			break;

		case MUYALTA:
			// endTime = System.currentTimeMillis() + 30*100; //30 segundos de
			// refresco

			synchronized (this) {
				try {

					// wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:

						break;
					}
					if (verSiCancelado(datos.getEstadoVueloDestino())) {
						estado = TERMINADO;
						notificar(TEXTO_CANCELADO, "", sonido, pDatos);
					}
					if (verSiRetrasado(datos.getEstadoVueloDestino())) {
						/*notificar(TEXTO_RETRASADO,
								datos.getEstadoVueloDestino(), sonido);*/
					}
					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						estado = TERMINADO;
						/*notificar(TEXTO_ATERRIZADO,
								datos.getEstadoVueloDestino(), sonido);*/
					} else {
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (3 * 1000),
								pendingIntent);
					}
				} catch (Exception e) {
				}
			}

			break;

		case ALTA:
			// endTime = System.currentTimeMillis() + 3*100; //3 minutos de
			// refresco
			synchronized (this) {
				try {

					// wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:

						break;
					}
					ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
					if (verSiCancelado(datos.getEstadoVueloDestino())) {
						estado = TERMINADO;
						notificar(TEXTO_CANCELADO, "", sonido, pDatos);
					}
					if (verSiRetrasado(datos.getEstadoVueloDestino())) {
						/*notificar(TEXTO_RETRASADO,
								datos.getEstadoVueloDestino(), sonido);*/
					}
					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						estado = TERMINADO;
						/*notificar(TEXTO_ATERRIZADO,
								datos.getEstadoVueloDestino(), sonido);*/
					} else {
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (30 * 1000),
								pendingIntent);
					}

				} catch (Exception e) {
				}
			}
			break;

		case MEDIA:
			// endTime = System.currentTimeMillis() + 20*10000; //20 minutos de
			// refresco
			synchronized (this) {
				try {
					// wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:

						break;
					}
					ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
					if (verSiCancelado(datos.getEstadoVueloDestino())) {
						estado = TERMINADO;
						notificar(TEXTO_CANCELADO, "", sonido, pDatos);
					}
					if (verSiRetrasado(datos.getEstadoVueloDestino())) {
						/*notificar(TEXTO_RETRASADO,
								datos.getEstadoVueloDestino(), sonido);*/
					}
					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						estado = TERMINADO;
						/*notificar(TEXTO_ATERRIZADO,
								datos.getEstadoVueloDestino(), sonido);*/
					} else {
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (20 * 10000),
								pendingIntent);
					}
				} catch (Exception e) {
				}
			}
			break;

		case BAJA:
			// endTime = System.currentTimeMillis() + 5*10000; //10 minutos de
			// refresco
			synchronized (this) {
				try {
					// wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:

						break;
					}
					if (verSiRetrasado(datos.getEstadoVueloOrigen())) {
						/*notificar(TEXTO_RETRASADO,
								datos.getEstadoVueloOrigen(), sonido);*/
					}
					if (verSiCancelado(datos.getEstadoVueloOrigen())) {
						estado = TERMINADO;
						notificar("El vuelo ha sido cancelado", "", sonido, pDatos);
					}
					if (verSiDespegado(datos.getEstadoVueloOrigen(), pDatos)) {

						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
						/*notificar(TEXTO_SALIDO, datos.getEstadoVueloOrigen(),
								sonido);*/
					} else {
						if(pDatos.getCambios() == SI){
							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (2 * 10000),
									pendingIntent);
						}
						else{
							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (5 * 10000),
									pendingIntent);
						}

					}

					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						estado = TERMINADO;
						/*notificar(TEXTO_ATERRIZADO,
								datos.getEstadoVueloDestino(), sonido);*/
					}
				} catch (Exception e) {
				}

			}
			break;

		case MUYBAJA:
			// endTime = System.currentTimeMillis() + 60*10000; //60 minutos de
			// refresco
			synchronized (this) {
				try {
					// wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:

						break;
					}
					if (verSiDespegado(datos.getEstadoVueloOrigen(), pDatos)) {
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
						/*notificar(TEXTO_SALIDO, datos.getEstadoVueloOrigen(),
								sonido);*/
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (20 * 10000),
								pendingIntent);

					} else {
						ponerEstadoAntes(getDiferencia(datos
								.getEstadoVueloOrigen()));
						if(pDatos.getCambios() == SI){
							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (20 * 10000),
									pendingIntent);
						} else {
							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (60 * 10000),
									pendingIntent);
						}
					}
					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						estado = TERMINADO;
						/*notificar(TEXTO_ATERRIZADO,
								datos.getEstadoVueloDestino(), sonido);*/
					}
				} catch (Exception e) {
				}
			}
			break;

		case MINIMO:
			// endTime = System.currentTimeMillis() + 180*10000; //3 horas horas
			// de refresco
			synchronized (this) {
				try {
					// wait(endTime - System.currentTimeMillis());
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:

						break;
					}

					if (verSiDespegado(datos.getEstadoVueloOrigen(), pDatos)) {
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()));
						notificar(TEXTO_SALIDO, datos.getEstadoVueloOrigen(),
								sonido, pDatos);
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (20 * 10000),
								pendingIntent);
					} else {
						ponerEstadoAntes(getDiferencia(datos
								.getEstadoVueloOrigen()));

						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (180 * 10000),
								pendingIntent);
					}
					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						estado = TERMINADO;
						/*notificar(TEXTO_ATERRIZADO,
								datos.getEstadoVueloDestino(), sonido);*/
					}
				} catch (Exception e) {
				}
			}
			break;

		case TERMINADO:
			try {
				Log.i(TAG, "AlarmaService - Servicio finalizando");
				/*notificar(TEXTO_ATERRIZADO, datos.getEstadoVueloDestino(),
						sonido);*/
				borrarAlarma(datos.getLinkInfoVuelo(), id, pDatos);
				// stopService(pIntent);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}
	}

	public void getDatos(String pUrl, DatosAlarma pDatos) {
		int sonido = pDatos.getSonido();
		try {
			switch (red) {
			case DESCONECTADO:
				Log.i(TAG,
						"AlarmaService - getRed - swich(red) - Case Desconectado");
				datos = pDatos.getDatos();
				break;

			case CONECTADO:
				Log.i(TAG, "AlarmaService - getRed - swich(red) - Case Conectado");
				datos = vuelosJsoup.getDatosVuelo(pUrl);
				Log.d(TAG, "AlarmaService - getDatos - CONECTADO - estadoOrigenA: " + 
						pDatos.getDatos().getEstadoVueloOrigen());
				Log.d(TAG, "AlarmaService - getDatos - CONECTADO - estadoOrigenB: " + 
						datos.getEstadoVueloOrigen());
				Log.d(TAG, "AlarmaService - getDatos - CONECTADO - compA: " + 
						(pDatos.getDatos().getEstadoVueloOrigen()).equals(datos.getEstadoVueloOrigen()));
				Log.d(TAG, "AlarmaService - getDatos - CONECTADO - compB: " + 
						(pDatos.getDatos().getEstadoVueloDestino()).equals(datos.getEstadoVueloDestino()));

				if (!(pDatos.getDatos().getEstadoVueloOrigen()).equals(datos.getEstadoVueloOrigen())) {
					String text = pDatos
							.getDatos()
							.getAeropuertoOrigen()
							.substring(
									0,
									pDatos.getDatos().getAeropuertoOrigen()
									.indexOf("(") - 1);
					String text2 = pDatos
							.getDatos()
							.getAeropuertoDestino()
							.substring(
									0,
									pDatos.getDatos().getAeropuertoDestino()
									.indexOf("(") - 1);
					actualizarBDRetrasoOrigen(pDatos, datos.getEstadoVueloOrigen());
					if(!datos.getEstadoVueloOrigen().contains("espe") && !salido.equals("si")){
						notificar(
								"El vuelo " + text + " - " + text2 + " ha sido modificado.",
								datos.getEstadoVueloOrigen() + " (" + getDiferenciaEstados(
										datos.getEstadoVueloOrigen(),
										datos.getEstadoVueloOrigen()) + " mins)" + "",
										sonido, pDatos);
					}
					
				} else if (!(pDatos.getDatos().getEstadoVueloDestino()).equals(datos.getEstadoVueloDestino())) {
					String text = pDatos
							.getDatos() 
							.getAeropuertoOrigen()
							.substring(
									0,
									pDatos.getDatos().getAeropuertoOrigen()
									.indexOf("(") - 1);
					String text2 = pDatos
							.getDatos()
							.getAeropuertoDestino()
							.substring(
									0,
									pDatos.getDatos().getAeropuertoDestino()
									.indexOf("(") - 1);
					actualizarBDRetrasoDestino(pDatos, datos.getEstadoVueloDestino());
					if(!datos.getEstadoVueloDestino().contains("aterrizado")){
						notificar(
								"El vuelo " + text + " - " + text2 + " ha sido modificado.",
								datos.getEstadoVueloOrigen() + " (" + getDiferenciaEstados(
										datos.getEstadoVueloDestino(),
										datos.getEstadoVueloDestino()) + " mins)" + "",
										sonido, pDatos);
					}
					
				}
			default:
				break;
			}
		} catch (NoHayVueloException e) {
			System.out.println("No hay Vuelos con esos parámetros");
		} catch (IOException ex1) {
			System.out.println("error no hay conexion a internet");
		} catch (Exception e) {
			Log.e(TAG, "AlarmaService - getDatos - Excepcion " + e.toString());
		}
		if (datos.getEstadoVueloDestino().contains("prevista")) {
			// setEstado();
		} else {
			estado = TERMINADO;
		}
	}

	public void ponerEstado(int pTiempo) {
		if (pTiempo <= 5) {
			estado = MUYALTA;
		} else if (pTiempo > 5 && pTiempo < 20) {
			estado = ALTA;
		} else {
			estado = MEDIA;
		}
	}

	public void ponerEstadoAntes(int pTiempo) {
		if (pTiempo < 30) {
			estado = BAJA;
		} else if (pTiempo > 30 && pTiempo < 240) {
			estado = MUYBAJA;
		} else {
			estado = MINIMO;
		}
	}

	public boolean verSiDespegado(String pEstado, DatosAlarma pDatos) {
		try{
			switch (red) {
			case CONECTADO:
				Log.d(TAG,
						"AlarmaService - verSiDespegado - despegado: " + pEstado
						.contains("despegado"));
				String text = pDatos
						.getDatos()
						.getAeropuertoOrigen()
						.substring(
								pDatos.getDatos().getAeropuertoDestino()
								.indexOf("ORIGEN") + 1,
								pDatos.getDatos().getAeropuertoOrigen()
								.indexOf("(") - 1);
				String text2 = pDatos
						.getDatos()
						.getAeropuertoDestino()
						.substring(pDatos.getDatos().getAeropuertoDestino()
								.indexOf("DESTINO") + 1	,
								pDatos.getDatos().getAeropuertoDestino()
								.indexOf("(") - 1);
				if(pDatos.getDespegar() == SI){
					if(pEstado.contains("pegado") && !(pDatos.getDatos().getEstadoVueloOrigen().contains("pegado"))){
						actualizarBDRetrasoOrigen(pDatos, pEstado);
						ponerSalido(pDatos);
						notificar("El vuelo " + text + " - " + text2 + " ha despegado.", 
								"A las: " + getHora(pDatos.getDatos().getEstadoVueloOrigen()),
								pDatos.getSonido(), pDatos);
					}	
				}
				return pEstado.contains("despegado");

			case DESCONECTADO:
				if ((getDiferencia(pEstado) < 0) && (despegadoSin.equals("no"))) {
					Log.d(TAG,
							"AlarmaService - verSiDespegado - DESCONECTADO - aterrizado: " + pEstado
							.contains("aterrizado"));
					int dif = getDiferencia(pEstado);
					ponerDespegadoSin(pDatos);
					Log.d(TAG,
							"AlarmaService - verSiDespegado - DESCONECTADO - dif: " + dif);
					if (dif <= 0) {
						notificar("El vuelo debería haber despegado ",
								"SIN CONEXION", pDatos.getSonido(), pDatos);
					}
					return false;

				} else {
					return false;
				}

			default:
				return false;
			}
		} catch (Exception e){
			Log.e(TAG, "AlarmaService - verSiAterrizado - CONECTADO + e: " + e.getMessage());
			return false;
		}

	}

	public boolean verSiCancelado(String pEstado) {
		return pEstado.contains("cancel");
	}

	public boolean verSiRetrasado(String pEstado) {
		return pEstado.contains("retra");
	}

	public boolean verSiAterrizado(String pEstado, DatosAlarma pDatos) {
		try{
			switch (red) {
			case CONECTADO:
				Log.d(TAG,
						"AlarmaService - verSiAterrizado - CONECTADO -aterrizado:  " + pEstado
						.contains("aterrizado"));
				String text = pDatos
						.getDatos()
						.getAeropuertoOrigen()
						.substring(
								0,
								pDatos.getDatos().getAeropuertoOrigen()
								.indexOf("(") - 1);
				String text2 = pDatos
						.getDatos()
						.getAeropuertoDestino()
						.substring(
								0,
								pDatos.getDatos().getAeropuertoDestino()
								.indexOf("(") - 1);
				if(pDatos.getDespegar() == SI){
					if(pEstado.contains("aterrizado")){
						actualizarBDRetrasoDestino(pDatos, datos.getEstadoVueloDestino());
						notificar("El vuelo " + text + " - " + text2 + " ha aterrizado.", 
								"A las: " + getHora(pDatos.getDatos().getEstadoVueloOrigen()),
								pDatos.getSonido(), pDatos);
					}
				}
				return pEstado.contains("aterrizado");

			case DESCONECTADO:
				if ((getDiferencia(pEstado) < 0) && (aterrizadoSin.equals("no"))) {
					Log.d(TAG,
							"AlarmaService - verSiAterrizado - DESCONECTADO - aterrizado: " + pEstado
							.contains("aterrizado"));
					int dif = getDiferencia(pEstado);
					actualizarBDAterrizadoSin(pDatos);
					Log.d(TAG,
							"AlarmaService - verSiArerrizado - DESCONECTADO - dif: " + dif);
					if (dif <= 0) {
						notificar("El vuelo deberia haber aterrizado ",
								"SIN CONEXION", pDatos.getSonido(), pDatos);
					}
					return false;

				} else {
					return false;
				}

			default:
				return false;
			}
		} catch (Exception e){
			Log.e(TAG, "AlarmaService - verSiAterrizado - exception - e: " + e.getMessage());
			return false;
		}
	}

	private void actualizarBDAterrizadoSin(DatosAlarma pDatos) {
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG,
				"AlarmaService - actualizarBDAterrizadoSin - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ATERRIZADOSIN, "si");
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void actualizarBDRetrasoOrigen(DatosAlarma pDatos, String pOrigen){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG,
				"AlarmaService - actualizarBDAterrizadoSin - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ESTADOORIGEN, pOrigen);
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void actualizarBDRetrasoDestino(DatosAlarma pDatos, String pDestino){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG,
				"AlarmaService - actualizarBDAterrizadoSin - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ESTADODESTINO, pDestino);
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void ponerDespegadoSin(DatosAlarma pDatos){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG,
				"AlarmaService - ponerDespegadoSin - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ATERRIZADOSIN, "si");
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void ponerSalido(DatosAlarma pDatos){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG,
				"AlarmaService - ponerSalido - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.SALIDO, "si");
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}
	
	public int getDiferencia(String pEstado) {
		try{
			String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ") + 6)
					.split(":");
			int minutos = 0;
			String dia = datos.getFechaOrigen().substring(0,
					datos.getFechaOrigen().indexOf("/"));
			Log.d(TAG, "AlarmaService - getDiferencia - dia: " + dia);
			int di = Integer.parseInt(dia);
			if (!(di == (new Date().getDay()))) {
				return 300;
			}
			minutos += (((Integer.parseInt(horaVuelo[0])) - (new Date().getHours()))) * 60;
			minutos += (((Integer.parseInt(horaVuelo[1])) - (new Date()
			.getMinutes())));
			Log.d(TAG,
					"AlarmaServer - getDiferencia - minutos de diferencia: " + minutos);
			return (minutos);
		} catch (Exception e){
			Log.e(TAG, "AlarmaService - getDiferencia(String pEstado): " + e.getMessage());
			return 0;
		}
	}

	public int getDiferencia(String pHora, String pHoraAlarma) {
		try{
			Log.d(TAG,
					"AlarmasActivity - getDiferenciaAntelacion - pHoraAlarma: " + pHoraAlarma);
			Log.d(TAG,
					"AlarmasActivity - getDiferenciaAntelacion - pHora: " + pHora);

			String[] horaPrevista = pHora.split(":");
			String[] horaVuelo = pHoraAlarma.split(":");

			int minutos = 0;
			minutos += (((Integer.parseInt(horaPrevista[0])) - (Integer
					.parseInt(horaVuelo[0])))) * 60;
			Log.d(TAG, "AlarmasActivity - getDiferencia(2) - mins: " + minutos);
			minutos += (((Integer.parseInt(horaPrevista[1])) - (Integer
					.parseInt(horaVuelo[1]))));
			Log.d(TAG,
					"AlarmaActivity - getDiferencia(2) - minutos de diferencia: " + minutos);
			return (minutos);
		} catch (Exception e){
			Log.e(TAG, "AlarmaService - getDiferencia(String pHora, String pHoraAlarma): " + e.getMessage());
			return 0;
		}

	}

	public int getDiferenciaEstados(String pHoraA, String pHoraB) {
		Log.d(TAG,
				"AlarmasActivity - getDiferenciaAntelacion - pHoraA: " + pHoraA);
		Log.d(TAG,
				"AlarmasActivity - getDiferenciaAntelacion - pHoraB: " + pHoraB);
		try{
			String[] horaVueloA = pHoraA.substring(pHoraA.indexOf("a las ") + 6)
					.split(":");
			String[] horaVueloB = pHoraB.substring(pHoraB.indexOf("a las ") + 6)
					.split(":");
			int minutos = 0;
			minutos += (((Integer.parseInt(horaVueloA[0])) - (Integer
					.parseInt(horaVueloB[0])))) * 60;
			Log.d(TAG,
					"AlarmasActivity - getDiferenciaEstados(2) - mins: " + minutos);
			minutos += (((Integer.parseInt(horaVueloA[1])) - (Integer
					.parseInt(horaVueloB[1]))));
			Log.d(TAG,
					"AlarmaActivity - getDiferenciaEstados(2) - minutos de diferencia: " + minutos);
			return (minutos);
		} catch(Exception e){
			Log.e(TAG, "AlarmaService - getDiferenciaEstados(String pHoraA, String pHoraB): " + e.getMessage());
			return 0;
		}

	}

	public String getHora(String pEstado) {
		// Log.i(TAG,
		// "Servicio "+pEstado.substring(pEstado.indexOf("a las ")+6));
		try {
			Date horaActual = new Date();

			Log.i(TAG,
					"AlarmaService - gerHora - horaActual: " + horaActual
					.getHours() + ":" + horaActual.getMinutes());
			Log.i(TAG,
					"AlarmaService - gerHora -  estado: " + pEstado
					.substring(pEstado.indexOf("a las ") + 6));
			String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ") + 6)
					.split(":");
			// int a =
			// pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]:").length;
			Log.i(TAG,
					"AlarmaService - gerHora - " + (Integer.parseInt(horaVuelo[0]) - horaActual
							.getHours()));
			Log.i(TAG,
					"AlarmaService - gerHora - " + (Integer.parseInt(horaVuelo[1]) - horaActual
							.getMinutes()));

			/*
			 * for (int i = 0; i <
			 * pEstado.substring(pEstado.indexOf("a las ")+6).split
			 * ("[0-9]?[0-9]").length; i++) { Log.i(TAG,
			 * pEstado.substring(pEstado.indexOf
			 * ("a las ")+6).split("[0-9]?[0-9]")[i]); }
			 */

			return pEstado.substring(pEstado.indexOf("a las ") + 6);
		} catch (Exception e){
			Log.e(TAG, "AlarmaService - getHora(String pEstado): " + e.getMessage());
			return ":";
		}
	}

	public void notificar(String pMens, String pMens2, int pSonido, DatosAlarma pDatos) {
		Context context = getApplicationContext();
		String ns = Context.NOTIFICATION_SERVICE;
		int icono = android.R.drawable.btn_star_big_on;
		CharSequence contentTitle = pMens;
		CharSequence contentText = pDatos.getDatos().getEstadoVueloDestino();
		long hora = System.currentTimeMillis();

		// Creacion de la notificacion
		mNotificationManager = (NotificationManager) getSystemService(ns);
		Notification mNotificacion = new Notification(icono, contentTitle, hora);

		// Creacion del intent
		Intent notIntent = new Intent(context, com.vuelosDroid.frontEnd.VueloResultadoActivity.class);
		notIntent.setAction(Intent.ACTION_MAIN);
		Bundle bun = new Bundle();
		bun.putString("url", pDatos.getDatos().getLinkInfoVuelo());
		bun.putString("dia", "hoy");
		bun.putString("codigo" ,"");
		notIntent.putExtras(bun);
		PendingIntent contIntent = PendingIntent.getActivity(context, 0,
				notIntent, 0);
		
		

		mNotificacion.setLatestEventInfo(context, contentTitle, contentText,
				contIntent);

		// AutoCancel: cuando se pulsa la notificaión ésta desaparece
		mNotificacion.flags |= Notification.FLAG_AUTO_CANCEL;

		// Añadir sonido, vibración y luces
		if (pSonido == SI) {
			mNotificacion.defaults |= Notification.DEFAULT_SOUND;
		}
		mNotificacion.defaults |= Notification.DEFAULT_VIBRATE;
		mNotificacion.defaults |= Notification.DEFAULT_LIGHTS;
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, mNotificacion);

	}

	public void getAlarmas(Intent intent) {
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - getAlarmas - Funciona la llamada");

		String[] args = new String[] { AlarmasSqlAux.URL,
				AlarmasSql.NOMBREVUELO, AlarmasSql.FECHAORIGEN,
				AlarmasSql.HORAORIGEN, AlarmasSql.NOMBRECOMPANY,
				AlarmasSqlAux.HORADESTINO, AlarmasSqlAux.ATERRIZADOSIN,
				AlarmasSqlAux.ID, AlarmasSqlAux.SONIDO,
				AlarmasSqlAux.ATERRIZAR, AlarmasSqlAux.DESPEGAR,
				AlarmasSqlAux.CAMBIOS, AlarmasSqlAux.MINUTOS, 
				AlarmasSqlAux.ESTADOORIGEN, AlarmasSqlAux.ESTADODESTINO,
				AlarmasSqlAux.AEROPUERTOORIGEN, AlarmasSqlAux.AEROPUERTODESTINO,
				AlarmasSqlAux.DESPEGADOSIN, AlarmasSqlAux.SALIDO};

		Cursor c = db.query("alarmas_aux", args, null, null, null, null, null);
		// Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {
			// Recorremos el cursor hasta que no haya más registros
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
				datos.setEstadoVueloOrigen(c.getString(13));
				datos.setEstadoVueloDestino(c.getString(14));
				datos.setAeropuertoOrigen(c.getString(15));
				datos.setAeropuertoDestino(c.getString(16));
				aterrizadoSin = c.getString(6);
				despegadoSin = c.getString(17);
				salido = c.getString(18);
				url = datos.getLinkInfoVuelo();
				id = c.getInt(7);
				Log.d(TAG, "AlarmaService - getAlarmas - id: " + id);
				Log.d(TAG,
						"AlarmaService - getAlarmas - intent: " + intent
						.toString());
				Log.d(TAG,
						"AlarmaService - getAlarmas - this: " + this.toString());
				intent.putExtra("id", id);
				Log.d(TAG, "AlarmaService - getAlarmas - SONIDO:" + c.getInt(8));
				Log.d(TAG,
						"AlarmaService - getAlarmas - ATERRIZAR:" + c.getInt(9));
				Log.d(TAG,
						"AlarmaService - getAlarmas - DESPEGAR:" + c.getInt(10));
				Log.d(TAG,
						"AlarmaService - getAlarmas - CAMBIOS:" + c.getInt(11));
				Log.d(TAG,
						"AlarmaService - getAlarmas - MINUTOS:" + c.getInt(12));

				pendingIntent = PendingIntent.getBroadcast(this, id, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);
				DatosAlarma datosAlarma = new DatosAlarma(datos, id,
						c.getInt(8), c.getInt(9), c.getInt(10), c.getInt(11),
						c.getInt(12));
				controlVuelo(url, id, datosAlarma);
				ponerAlarmaAntelacion(datosAlarma);
			} while (c.moveToNext());
		}
		db.close();
		// estado = TERMINADO;
		stopService(pIntent);
	}

	public void getAlarmasId(int id, Intent intent) {
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - getAlarmasId - Funciona la llamada");
		String[] args = new String[] { AlarmasSqlAux.URL,
				AlarmasSql.NOMBREVUELO, AlarmasSql.FECHAORIGEN,
				AlarmasSql.HORAORIGEN, AlarmasSql.NOMBRECOMPANY,
				AlarmasSqlAux.HORADESTINO, AlarmasSqlAux.ATERRIZADOSIN,
				AlarmasSqlAux.ID, AlarmasSqlAux.SONIDO,
				AlarmasSqlAux.ATERRIZAR, AlarmasSqlAux.DESPEGAR,
				AlarmasSqlAux.MINUTOS, AlarmasSqlAux.ESTADOORIGEN,
				AlarmasSqlAux.ESTADODESTINO, AlarmasSqlAux.AEROPUERTOORIGEN,
				AlarmasSqlAux.AEROPUERTODESTINO, AlarmasSqlAux.DESPEGADOSIN,
				AlarmasSqlAux.SALIDO};
		String[] args2 = { id + "" };
		Cursor c = db.query("alarmas_aux", args, "id=?", args2, null, null,
				null);
		// Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {
			// Recorremos el cursor hasta que no haya más registros
			do {
				datos = new DatosVuelo();
				Log.d(TAG,
						"AlarmaService - dentro del vuelo - url: " + c
						.getString(0));
				datos.setLinkInfoVuelo(c.getString(0));
				Log.d(TAG,
						"AlarmaService - dentro del vuelo - NombreVuelo: " + c
						.getString(1));

				datos.setNombreVuelo(c.getString(1));
				datos.setFechaOrigen(c.getString(2));
				datos.setHoraOrigen(c.getString(3));
				datos.setNombreCompany(c.getString(4));
				datos.setHoraDestino(c.getString(5));
				datos.setEstadoVueloOrigen(c.getString(13));
				datos.setEstadoVueloDestino(c.getString(14));
				datos.setAeropuertoOrigen(c.getString(15));
				datos.setAeropuertoDestino(c.getString(16));

				aterrizadoSin = c.getString(6);
				despegadoSin = c.getString(17);
				salido = c.getString(18);
				url = datos.getLinkInfoVuelo();
				id = c.getInt(7);
				Log.d(TAG, "AlarmaService - getAlarmasId " + id);
				intent.putExtra("id", id);
				pendingIntent = PendingIntent.getBroadcast(this, id, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);
				DatosAlarma datosAlarma = new DatosAlarma(datos, id,
						c.getInt(8), c.getInt(9), c.getInt(10), c.getInt(11),
						c.getInt(12));
				controlVuelo(url, id, datosAlarma);
				ponerAlarmaAntelacion(datosAlarma);
			} while (c.moveToNext());
		}
		db.close();
		stopService(pIntent);
	}

	private void ponerAlarmaAntelacion(DatosAlarma pDatos) {
		Date horaActual = new Date();
		int i = getDiferencia(
				horaActual.getHours() + ":" + horaActual.getMinutes(), pDatos
				.getDatos().getHoraOrigen());
		Log.d(TAG, "AlarmaService - ponerAlarmaAntelacion - i: " + i);
		Log.d(TAG,
				"AlarmaService - ponerAlarmaAntelacion - minutos: " + pDatos
				.getMinutos());
		i = (i) * (-1) - pDatos.getMinutos();
		Log.d(TAG, "AlarmaService - ponerAlarmaAntelacion - i: " + i);
		Bundle bun = new Bundle();
		bun.putString("hora", pDatos.getDatos().getHoraOrigen());
		bun.putString("origen", pDatos.getDatos().getAeropuertoOrigen());
		bun.putString("destino", pDatos.getDatos().getAeropuertoDestino());
		bun.putInt("minutos", pDatos.getMinutos());
		bun.putString("url", pDatos.getDatos().getLinkInfoVuelo());
		bun.putString("dia", "hoy");
		Intent intentA = new Intent(this, MiReceiverAntelacion.class);
		intentA.putExtras(bun);
		if (i > 0) {
			//intentA.set(getApplicationContext(), com.vuelosDroid.frontEnd.VueloResultadoActivity.class);
			
			pendingIntent = PendingIntent.getBroadcast(this, id + 999, intentA,
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis() + (i * 100000), pendingIntent);
		}
	}

	public void borrarAlarma(String pUrl, int pId, DatosAlarma pDatos) {
		Log.d(TAG, "AlarmaService - borrarAlarma - Empieza - url: " + pUrl);
		Log.d(TAG, "AlarmaService - borrarAlarma - Empieza - id: " + pId);
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getWritableDatabase();
		db.execSQL("DELETE FROM alarmas_aux WHERE " + AlarmasSqlAux.ID + "='" + id + "' ");
		ponerSeg(pUrl, pDatos);
		db.close();
	}

	public void ponerSeg(String pUrl, DatosAlarma pDatos) {
		getDatos(pUrl, pDatos);
		AlarmasSql alarms = new AlarmasSql(this);
		SQLiteDatabase db = alarms.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(AlarmasSql.URL, datos.getLinkInfoVuelo());
		cv.put(AlarmasSql.ALARMA, 1);
		cv.put(AlarmasSql.EMPEZADO, 0);
		cv.put(AlarmasSql.HORAORIGEN, datos.getHoraOrigen());
		cv.put(AlarmasSql.NOMBREVUELO, datos.getNombreVuelo());
		cv.put(AlarmasSql.FECHAORIGEN, datos.getFechaOrigen());
		cv.put(AlarmasSql.NOMBRECOMPANY, datos.getNombreCompany());
		cv.put(AlarmasSql.HORAORIGEN, pDatos.getDatos().getHoraOrigen());
		cv.put(AlarmasSql.ESTADOORIGEN, pDatos.getDatos().getEstadoVueloOrigen());
		cv.put(AlarmasSql.ESTADODESTINO, pDatos.getDatos().getEstadoVueloDestino());
		cv.put(AlarmasSql.HORADESTINO, pDatos.getDatos().getHoraDestino());
		cv.put(AlarmasSql.AEROPUERTOORIGEN, pDatos.getDatos().getAeropuertoOrigen());
		cv.put(AlarmasSql.AEROPUERTODESTINO, pDatos.getDatos().getAeropuertoDestino());

		db.insert("alarmas", AlarmasSql.URL, cv);

		db.close();

	}

}