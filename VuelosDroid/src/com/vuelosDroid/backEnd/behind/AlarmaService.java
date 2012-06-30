/*
 
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.scrapper.*;

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
	/*private static final String TEXTO_RETRASADO = "El vuelo ha sido retrasado";
	private static final String TEXTO_ATERRIZADO = "El vuelo ha llegado";
	private static final String TEXTO_SALIDO = "El vuelo ha despegado";*/

	// Constantes de estado
	private static final int INICIAL = 0; // No se sabe el estado
	private static final int MINIMO = 7; // Actualizacion cada 3 horas
	private static final int MUYALTA = 6; // Vuelo a menos de 5 minutos de la
	// llegada prevista
	private static final int ALTA = 5; // Vuelo a menos de 20 minutos de la
	// llegada prevista
	private static final int MEDIAALTA = 8;
	private static final int MEDIA = 4; // Vuelo despegado

	private static final int BAJAMEDIA = 9; //No ha despegado quedan 15 minutos
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

	@Override public void onDestroy() {
		Log.i(TAG, "AlarmaService - onDestroy - Servicio finalizado");

		super.onDestroy();
	}

	/**
	 * Controla todas las alarmas poniendo sus estados y próximas comprobaciones
	 * @param pUrl
	 * @param id
	 * @param pDatos
	 */
	public void controlVuelo(String pUrl, int id, DatosAlarma pDatos) {
		int sonido = pDatos.getSonido();
		int despegar = pDatos.getDespegar();
		int aterrizar = pDatos.getAterrizar();
		int minutos = pDatos.getMinutos();
		int cambios = pDatos.getCambios();
		int estado = pDatos.getEstado();

		Log.d(TAG, "AlarmaService - controlVuelo - estado: " + estado);
		Log.d(TAG, "AlarmaService - controlVuelo - conectado: " + red);
		switch (pDatos.getEstado()) {

		//Nada mas poner la alarma -Establece el estado de cada alarma al principio.
		case INICIAL:
			synchronized (this) {
				try {
					Log.i(TAG, "AlarmaService - controlVuelo - estado INICAL");

					Log.d(TAG, "AlarmaService - controlVuelo - red: " + red);
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
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()), pDatos);
					} else {
						ponerEstadoAntes(getDiferencia(datos
								.getEstadoVueloOrigen()), pDatos);
					}
					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
					}
					if(verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
						estado = TERMINADO;
						actualizarBDEstado(pDatos);

					}

					controlVuelo(pUrl, id, pDatos);
				} catch (Exception e) {
				}
			}
			break;

			//En caso de que queden menos de 5 minutos para la llegada del vuelo 
			//(Se actualizara la informacion cada 30 segundos)
		case MUYALTA:
			Log.i(TAG, "AlarmaService - controlVuelo - estado MUYALTA");

			synchronized (this) {
				try {
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:
						break;
					}
					if (verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					}

					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					} else {
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (1 * 60000),   //30 segs
								pendingIntent);
					}
				} catch (Exception e) {
				}
			}

			break;

			//En caso de que queden entre 5 y 20 minutos para la llegada del vuelo 
			//(Se actualizara la informacion cada 3 minutos para posibles retrasos y llegada del vuelo)
		case ALTA:
			Log.i(TAG, "AlarmaService - controlVuelo - estado ALTA");
			synchronized (this) {
				try {

					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:
						break;
					}
					ponerEstado(getDiferencia(datos.getEstadoVueloDestino()), pDatos);

					if (verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);

					}

					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);

					} else {
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (3 * 60000),    //3 Mins
								pendingIntent);
					}

				} catch (Exception e) {
				}
			}
			break;

			//En caso de que queden entre 25 y 50 minutos para la llegada del vuelo 
			//(Se actualizara la informacion cada 10 minutos para ver retrasos)
		case MEDIAALTA:
			Log.i(TAG, "AlarmaService - controlVuelo - estado MEDIAALTA");
			synchronized (this) {
				try {
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:
						break;
					}

					ponerEstado(getDiferencia(datos.getEstadoVueloDestino()), pDatos);

					if (verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					}

					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					} else {
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (15 * 60000),   //15 Mins
								pendingIntent);
					}
				} catch (Exception e) {
				}
			}
			break;

			//En caso de que queden más de 50 minutos para la llegada del vuelo habiendo despegado
			// (Se actualizara la informacion cada 30 minutos)
		case MEDIA:
			Log.i(TAG, "AlarmaService - controlVuelo - estado MEDIA");
			synchronized (this) {
				try {
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:
						break;
					}

					ponerEstado(getDiferencia(datos.getEstadoVueloDestino()), pDatos);

					if (verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					}

					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					} else {
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (30 * 60000),   //30 Mins
								pendingIntent);
					}
				} catch (Exception e) {
				}
			}
			break;



		case BAJAMEDIA:
			Log.i(TAG, "AlarmaService - controlVuelo - estado BAJAMEDIA");
			synchronized (this) {
				try {
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:
						break;
					}

					if (verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					}

					if (verSiDespegado(datos.getEstadoVueloOrigen(), pDatos)) {
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()), pDatos);
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (20 * 10000),
								pendingIntent);

					} else {
						if(pDatos.getCambios() == SI){
							Log.i(TAG, "AlarmaService - ControlVuelo - BAJAMEDIA - con cambios");
							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (2 * 60000),
									pendingIntent);
						}
						else{
							Log.i(TAG, "AlarmaService - ControlVuelo - BAJAMEDIA - sin cambios");
							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (4 * 60000),
									pendingIntent);
						}
					}

					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);

					}
				} catch (Exception e) {
				}

			}
			break;

			//En caso de que queden menos de 50 minutos para el despege del vuelo
			//(Se actualizara la informacion cada 30 minutos)
		case BAJA:
			Log.i(TAG, "AlarmaService - controlVuelo - estado BAJA");
			synchronized (this) {
				try {
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:
						break;
					}

					if (verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					}

					if (verSiDespegado(datos.getEstadoVueloOrigen(), pDatos)) {
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()), pDatos);
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (20 * 10000),
								pendingIntent);

					} else {
						if(pDatos.getCambios() == SI){
							Log.i(TAG, "AlarmaService - ControlVuelo - BAJA - con cambios");

							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (4 * 60000),
									pendingIntent);
						}
						else{
							Log.i(TAG, "AlarmaService - ControlVuelo - BAJA - sin cambios");

							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (12 * 60000),
									pendingIntent);
						}
					}

					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					}
				} catch (Exception e) {
				}

			}
			break;

		case MUYBAJA:
			Log.i(TAG, "AlarmaService - controlVuelo - estado MUYBAJA");
			synchronized (this) {
				try {
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:
						break;
					}

					if (verSiDespegado(datos.getEstadoVueloOrigen(), pDatos)) {
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()), pDatos);

						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (20 * 10000),
								pendingIntent);

					} else {
						ponerEstadoAntes(getDiferencia(datos
								.getEstadoVueloOrigen()), pDatos);
						if(pDatos.getCambios() == SI){
							Log.i(TAG, "AlarmaService - ControlVuelo - MUYBAJA - con cambios");
							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (15 * 10000),
									pendingIntent);
						} else {
							Log.i(TAG, "AlarmaService - ControlVuelo - MUYBAJA - sin cambios");

							alarmManager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + (40 * 10000),
									pendingIntent);
						}
					}

					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);

					}

					if (verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);
					}

				} catch (Exception e) {
				}
			}
			break;

		case MINIMO:
			Log.i(TAG, "AlarmaService - controlVuelo - estado MINIMO");
			synchronized (this) {
				try {
					switch (red) {
					case CONECTADO:
						getDatos(pUrl, pDatos);
						break;

					case DESCONECTADO:
						break;
					}

					if (verSiDespegado(datos.getEstadoVueloOrigen(), pDatos)) {
						ponerEstado(getDiferencia(datos.getEstadoVueloDestino()), pDatos);

						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (20 * 60000),  //20 Mins
								pendingIntent);
					} else {
						ponerEstadoAntes(getDiferencia(datos
								.getEstadoVueloOrigen()), pDatos);

						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (180 * 60000),  //3 Horas
								pendingIntent);
					}
					if (verSiAterrizado(datos.getEstadoVueloDestino(), pDatos)) {
						pDatos.setEstado(TERMINADO);
						actualizarBDEstado(pDatos);
						controlVuelo(pUrl, id, pDatos);

					}
				} catch (Exception e) {
				}
			}
			break;

		case TERMINADO:
			Log.i(TAG, "AlarmaService - controlVuelo - estado TERMINADO");
			try {
				Log.i(TAG, "AlarmaService - Servicio finalizando");
				if (verSiCancelado(datos.getEstadoVueloOrigen(), pDatos)) {
					borrarAlarma(datos.getLinkInfoVuelo(), pDatos.getId(), pDatos);
				}
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
					actualizarBDRetrasoDestino(pDatos, datos.getEstadoVueloDestino());


					if(!datos.getEstadoVueloOrigen().contains("espe")  && salido.equals("si")){
						Log.i(TAG, "AlarmaService - getDatos - retrasoOrigen - contiene espe y salido");
						if (text2.contains("esti")){
							text2 = text2.replace("Destino:", "");
						}

						if(text.contains("Origen:")){
							text = text.replace("Origen:", "");
						}
						if(pDatos.getCambios() == SI){
							notificar(text + " - " + text2,"Modificado: " + datos.getEstadoVueloOrigen() + 
									" (" + getDiferenciaEstados(datos.getEstadoVueloOrigen(),
											datos.getEstadoVueloOrigen()) + " mins)" + "",
											sonido, pDatos, text + " - " + text2 + " ha sido modificado");
						}
					}

				} else if (!(pDatos.getDatos().getEstadoVueloDestino()).equals(datos.getEstadoVueloDestino())) {
					Log.d(TAG, "AlarmaService - getDatos - pDatos.getDatos().getEstadoVueloDestino(): " +
							pDatos.getDatos().getEstadoVueloDestino());
					Log.d(TAG, "AlarmaService - getDatos - datos.getEstadoVueloDestino(): " + datos.getEstadoVueloDestino());

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
						Log.i(TAG, "AlarmaService - getDatos - retrasoOrigen - Retraso destino");
						if (text2.contains("esti")){
							text2 = text2.replace("Destino:", "");
						}

						if(text.contains("Origen:")){
							text = text.replace("Origen:", "");
						}

						if(pDatos.getCambios() == SI){
							notificar(text + " - " + text2 , "Modificado: " + datos.getEstadoVueloDestino() + 
									" (" + getDiferenciaEstados(datos.getEstadoVueloDestino(),
											datos.getEstadoVueloDestino()) + " mins)" + "",
											sonido, pDatos, text + " - " + text2 + " ha sido modificado");
						}
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
		//pDatos.setDatos(datos);
		if (datos.getEstadoVueloDestino().contains("prevista")) {
			// setEstado();
		} else {
			//estado = TERMINADO;
		}
	}

	public void ponerEstado(int pTiempo, DatosAlarma pDatos) {
		if (pTiempo <= 5) {
			pDatos.setEstado(MUYALTA);
			estado = MUYALTA;

		} else if (pTiempo > 5 && pTiempo <= 60) {
			pDatos.setEstado(ALTA);
			estado = ALTA;
		} else if (pTiempo > 25 && pTiempo < 60){
			pDatos.setEstado(MEDIAALTA);
			estado = ALTA;
		} else {
			pDatos.setEstado(MEDIA);
			estado = MEDIA;
		}
		Log.d(TAG, "AlarmaService - ponerEstado - estado: " + estado);
		actualizarBDEstado(pDatos);
		//return estado;
	}

	public void ponerEstadoAntes(int pTiempo, DatosAlarma pDatos) {
		if (pTiempo <= 15){
			pDatos.setEstado(BAJAMEDIA);
			estado = BAJAMEDIA;
		} else if (pTiempo <= 50 && pTiempo > 15) {
			pDatos.setEstado(BAJA);
			estado = BAJA;
		} else if (pTiempo > 50 && pTiempo < 240) {
			pDatos.setEstado(MUYBAJA);
			estado = MUYBAJA;
		} else {
			pDatos.setEstado(MINIMO);
			estado = MINIMO;
		}
		Log.d(TAG, "AlarmaService - ponerEstadoAntes - estado: " + estado);
		actualizarBDEstado(pDatos);
	}

	public boolean verSiDespegado(String pEstado, DatosAlarma pDatos) {
		try{
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
			switch (red) {


			case CONECTADO:
				if(pEstado.contains("egado") && pDatos.getDatos().getEstadoVueloDestino().equalsIgnoreCase("--")){
					borrarAlarma(datos.getLinkInfoVuelo(), pDatos.getId(), pDatos);
				} else if(pEstado.contains("--") && pDatos.getDatos().getEstadoVueloDestino().equalsIgnoreCase("--")) {
					borrarAlarma(datos.getLinkInfoVuelo(), pDatos.getId(), pDatos);
				}
				Log.d(TAG,
						"AlarmaService - verSiDespegado - despegado: " + pEstado
						.contains("despegado"));

				if(pDatos.getDespegar() == SI){
					if(pEstado.contains("pegado") && !(pDatos.getDatos().getEstadoVueloOrigen().contains("pegado"))){
						actualizarBDRetrasoOrigen(pDatos, pEstado);
						ponerSalido(pDatos);
						if (text2.contains("esti")){
							text2 = text2.replace("Destino:", "");
						}

						if(text.contains("Origen:")){
							text = text.replace("Origen:", "");
						}
						if (datos.getEstadoVueloDestino().equals("--")){
							borrarAlarma(datos.getLinkInfoVuelo(), pDatos.getId(), pDatos);
						} else if (datos.getHoraDestino().equals("--")){
							borrarAlarma(datos.getLinkInfoVuelo(), pDatos.getId(), pDatos);
						}
						notificar(text + " - " + text2, "Despegado: " + datos.getEstadoVueloDestino(),
								pDatos.getSonido(), pDatos, "El vuelo " + text + " - " + text2 + " ha despegado");
					}	
				}
				return pEstado.contains("despegado");

			case DESCONECTADO:
				if (pDatos.getDespegadoSin() == 0){
					if ((getDiferencia(pEstado) < 0) && (despegadoSin.equals("no"))) {
						Log.d(TAG,
								"AlarmaService - verSiDespegado - DESCONECTADO - aterrizado: " + pEstado
								.contains("aterrizado"));
						int dif = getDiferencia(pEstado);
						Log.d(TAG,
								"AlarmaService - verSiDespegado - DESCONECTADO - dif: " + dif);
						if (text2.contains("esti")){
							text2 = text2.replace("Destino:", "");
						}

						if(text.contains("Origen:")){
							text = text.replace("Origen:", "");
						}
						if (dif <= 0 ) {
							ponerDespegadoSin(pDatos);
							notificar(text + " - " + text2, "Debería haber despegado (SIN CONEXION)",
									pDatos.getSonido(), pDatos, text + " - " + text2 + " debería haber despegado (SIN CONEXION)");
						}
						return false;
					} else {
						return false;
					}
				}

			default:
				return false;
			}
		} catch (Exception e){
			Log.e(TAG, "AlarmaService - verSiAterrizado - CONECTADO + e: " + e.getMessage());
			return false;
		}

	}

	public boolean verSiCancelado(String pEstado, DatosAlarma pDatos) {
		try{

			switch (red){

			case CONECTADO:
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
				if (pEstado.contains("cela")){
					notificar( text + " - " + text2 , pEstado,
							pDatos.getSonido(), pDatos, text + " - " + text2 + " ha sido cancelado");
					//borrarAlarma(datos.getLinkInfoVuelo(), pDatos.getId(), pDatos);
				}
			}
		}catch (Exception e) {
			Log.e(TAG, "AlarmaService - verSiCancelado - Exception");
		}
		return pEstado.contains("cela");
	}

	public boolean verSiAterrizado(String pEstado, DatosAlarma pDatos) {
		try{
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
			if (text2.contains("esti")){
				text2 = text2.replace("Destino:", "");
			}
			if(text.contains("Origen:")){
				text = text.replace("Origen:", "");
			}
			switch (red) {
			case CONECTADO:
				Log.d(TAG,
						"AlarmaService - verSiAterrizado - CONECTADO - aterrizado:  " + pEstado
						.contains("aterrizado"));

				if(pEstado.contains("aterrizado")){
					actualizarBDRetrasoDestino(pDatos, datos.getEstadoVueloDestino());
					notificar(text + " - " + text2 , pEstado, pDatos.getSonido(), pDatos, 
							text + " - " + text2 + " ha aterrizado");
					borrarAlarma(datos.getLinkInfoVuelo(), pDatos.getId(), pDatos);
				} else if(pEstado.equals("--") && pDatos.getDatos().getEstadoVueloOrigen().equals("--")){
					borrarAlarma(datos.getLinkInfoVuelo(), pDatos.getId(), pDatos);
				}
				return pEstado.contains("aterrizado");

			case DESCONECTADO:
				if(pDatos.getAterrizadoSin() == 0){
					if ((getDiferencia(pEstado) < 0) && (aterrizadoSin.equals("no"))) {
						Log.d(TAG,
								"AlarmaService - verSiAterrizado - DESCONECTADO - aterrizado: " + pEstado
								.contains("aterrizado"));
						int dif = getDiferencia(pEstado);
						Log.d(TAG,
								"AlarmaService - verSiArerrizado - DESCONECTADO - dif: " + dif);
						if (dif <= 0) {
							actualizarBDAterrizadoSin(pDatos);
							notificar(text + " - " + text2, "Debería haber aterrizado (SIN CONEXION)",
									pDatos.getSonido(), pDatos, text + " - " + text2 + " debería haber aterrizado (SIN CONEXION)");
						}
					}
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

	private void actualizarBDEstado(DatosAlarma pDatos){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - actualizarBDEstado - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ESTADO, pDatos.getEstado());
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void actualizarBDAterrizadoSin(DatosAlarma pDatos) {
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - actualizarBDAterrizadoSin - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ATERRIZADOSIN, "si");
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void actualizarBDRetrasoOrigen(DatosAlarma pDatos, String pOrigen){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - actualizarBDAterrizadoSin - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ESTADOORIGEN, pOrigen);
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void actualizarBDRetrasoDestino(DatosAlarma pDatos, String pDestino){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.i(TAG, "AlarmaService - actualizarBDRetrasoDestino - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.ESTADODESTINO, pDestino);
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void ponerDespegadoSin(DatosAlarma pDatos){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - ponerDespegadoSin - Funciona la llamada");
		ContentValues editor = new ContentValues();
		editor.put(AlarmasSqlAux.DESPEGADOSIN, "si");
		String[] args2 = { pDatos.getId() + "" };
		db.update("alarmas_aux", editor, "id=?", args2);
		db.close();
	}

	private void ponerSalido(DatosAlarma pDatos){
		AlarmasSqlAux alarms = new AlarmasSqlAux(this);
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "AlarmaService - ponerSalido - Funciona la llamada");
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
			Log.d(TAG, "AlarmaService - getDiferencia - di: " + di);

			if (!(di == (new GregorianCalendar().get(Calendar.DAY_OF_MONTH)))) {
				Log.d(TAG, "AlarmaService - getDiferencia - new GregorianCalendar().get(Calendar.DAY_OF_MONTH): "
						+ new GregorianCalendar().get(Calendar.DAY_OF_MONTH));
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
			Log.i(TAG,
					"AlarmaService - gerHora - " + (Integer.parseInt(horaVuelo[0]) - horaActual
							.getHours()));
			Log.i(TAG,
					"AlarmaService - gerHora - " + (Integer.parseInt(horaVuelo[1]) - horaActual
							.getMinutes()));

			return pEstado.substring(pEstado.indexOf("a las ") + 6);
		} catch (Exception e){
			Log.e(TAG, "AlarmaService - getHora(String pEstado): " + e.getMessage());
			return ":";
		}
	}

	public void notificar(String pMens, String pMens2, int pSonido, DatosAlarma pDatos, String pMarquesina) {
		Context context = getApplicationContext();
		String ns = Context.NOTIFICATION_SERVICE;
		int icono = R.drawable.ic_launcher;
		CharSequence contentTitle = pMens;
		CharSequence contentText = pMens2;
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
		mNotificacion.tickerText = pMarquesina;
		mNotificationManager.notify(pDatos.getId(), mNotificacion);

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
				AlarmasSqlAux.DESPEGADOSIN, AlarmasSqlAux.SALIDO, AlarmasSqlAux.ESTADO,
				AlarmasSqlAux.ALARMAVERDAD};

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
				int a = 0;
				int d = 0;
				if(aterrizadoSin.equalsIgnoreCase("si")){
					a = 1;
				}
				if(despegadoSin.equalsIgnoreCase("si")){
					d = 1;
				}
				salido = c.getString(18);
				url = datos.getLinkInfoVuelo();
				//int estado = c.getInt(19);
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
						c.getInt(12), c.getInt(19), a, d, c.getInt(20));
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
				AlarmasSqlAux.SALIDO, AlarmasSqlAux.ESTADO, AlarmasSqlAux.ALARMAVERDAD};
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
				int a = 0;
				int d = 0;
				if(aterrizadoSin.equalsIgnoreCase("si")){
					a = 1;
				}
				if(despegadoSin.equalsIgnoreCase("si")){
					d = 1;
				}
				salido = c.getString(18);
				url = datos.getLinkInfoVuelo();
				id = c.getInt(7);
				Log.d(TAG, "AlarmaService - getAlarmasId " + id);
				intent.putExtra("id", id);
				pendingIntent = PendingIntent.getBroadcast(this, id, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);
				DatosAlarma datosAlarma = new DatosAlarma(datos, id,
						c.getInt(8), c.getInt(9), c.getInt(10), c.getInt(11),
						c.getInt(12), c.getInt(19), a, d, c.getInt(20));
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
		bun.putInt("id", pDatos.getId());
		bun.putInt("alarma", pDatos.getAlarmaVerdad());
		bun.putInt("sonido", pDatos.getSonido());
		Intent intentA = new Intent(this, MiReceiverAntelacion.class);
		intentA.putExtras(bun);
		if (i > 0) {
			//intentA.set(getApplicationContext(), com.vuelosDroid.frontEnd.VueloResultadoActivity.class);

			pendingIntent = PendingIntent.getBroadcast(this, id + 999, intentA,
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis() + (i * 60000), pendingIntent);
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
		cv.put(AlarmasSql.ESTADOORIGEN, datos.getEstadoVueloOrigen());
		cv.put(AlarmasSql.ESTADODESTINO, datos.getEstadoVueloDestino());
		cv.put(AlarmasSql.HORADESTINO, pDatos.getDatos().getHoraDestino());
		cv.put(AlarmasSql.AEROPUERTOORIGEN, pDatos.getDatos().getAeropuertoOrigen());
		cv.put(AlarmasSql.AEROPUERTODESTINO, pDatos.getDatos().getAeropuertoDestino());

		db.insert("alarmas", AlarmasSql.URL, cv);

		db.close();

	}

}