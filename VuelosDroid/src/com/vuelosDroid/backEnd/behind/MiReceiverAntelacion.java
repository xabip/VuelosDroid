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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.vuelosDroid.R;


/**
 * Receiver que da el aviso con antelación de un vuelo
 * @author Xabi
 *
 */
public class MiReceiverAntelacion extends BroadcastReceiver {

	Context mContext;
	Context mContext2;

	private static final String TAG = "VuelosAndroid";
	private String origen;
	private String destino;
	private String hora;
	private String url;
	private int sonido;
	private int id;
	private int minutos;
	public int alarma;
	private int SIMPLE_NOTFICATION_ID;

	/**
	 * Constantes de estado
	 */
	public static final int SI = 1;
	public static final int NO = 0;

	@Override
	public void onReceive(android.content.Context context, android.content.Intent intent) {
		Log.i("VuelosAndroid", "MiReceiver - onReceive - recibida la alarma");
		mContext2 = context;
		try {
			mContext = context.createPackageContext("com.pack.VuelosDroid", 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		// Datos para gesinar la notificación y su retraso
		origen = intent.getStringExtra("origen");	
		destino = intent.getStringExtra("destino");
		hora = intent.getStringExtra("hora");
		minutos = intent.getIntExtra("minutos", 0);
		url = intent.getStringExtra("url");
		id = intent.getIntExtra("id", 0);
		alarma = intent.getIntExtra("alarma", 0);
		sonido = intent.getIntExtra("sonido", 0);
		Log.d(TAG, "MiReceiverAntelacion - onReceive - origen: " + origen);
		Log.d(TAG, "MiReceiverAntelacion - onReceive - destino: " + destino);
		Log.d(TAG, "MiReceiverAntelacion - onReceive - hora: " + hora);
		Log.d(TAG, "MiReceiverAntelacion - onReceive - minutos: " + minutos);
		Log.d(TAG, "MiReceiverAntelacion - onReceive - alarma: " + alarma);

		notificar();

	}

	public void notificar (){
		if(minutos != 0){
			String text = origen.substring(0, origen.indexOf("(")-1);
			String text2 = destino.substring(0, destino.indexOf("(")-1);
			if (text2.contains("esti")){
				text2 = text2.replace("Destino:", "");
			}

			if(text.contains("Origen:")){
				text = text.replace("Origen:", "");
			}

			final Context context = mContext2;
			String ns = Context.NOTIFICATION_SERVICE;
			int icono = R.drawable.ic_launcher;
			CharSequence contentTitle = text + " -" + text2;
			CharSequence contentText = "Hora estimada: " + hora + " (En " +  minutos + " minutos)";
			long hora = System.currentTimeMillis();


			//Creacion de la notificacion
			NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(ns);
			Notification mNotificacion = new Notification(icono, contentTitle, hora);

			//Creacion del intent

			Intent notIntent = new Intent(context, com.vuelosDroid.frontEnd.VueloResultadoActivity.class);
			notIntent.setAction(Intent.ACTION_MAIN);
			Bundle bun = new Bundle();
			bun.putString("url", url);
			bun.putString("dia", "hoy");
			bun.putString("codigo" ,"");
			notIntent.putExtras(bun);
			PendingIntent contIntent = PendingIntent.getActivity(context, 0, notIntent, 0);

			mNotificacion.setLatestEventInfo(context, contentTitle, contentText, contIntent);

			//AutoCancel: cuando se pulsa la notificaión ésta desaparece
			mNotificacion.flags |= Notification.FLAG_AUTO_CANCEL;

			//Añadir sonido, vibración y luces
			if(sonido == SI){
				mNotificacion.defaults |= Notification.DEFAULT_SOUND;
			}
			mNotificacion.defaults |= Notification.DEFAULT_VIBRATE;
			mNotificacion.defaults |= Notification.DEFAULT_LIGHTS;
			mNotificacion.defaults |= Notification.FLAG_INSISTENT;
			mNotificacion.tickerText = ("El vuelo " +  text + " -" + text2 + " saldrá en: " + getMin(minutos));
			mNotificationManager.notify(id, mNotificacion);

			Bundle bund = new Bundle();
			bund.putString("hora", this.hora);
			bund.putString("origen", origen);
			bund.putString("destino", destino);
			bund.putInt("minutos", minutos);
			bund.putString("url", url);
			bund.putString("dia", "hoy");
			bun.putInt("id", id);
			bund.putInt("alarma", alarma);
			bund.putInt("sonido", sonido);

			if(alarma == SI){
				Intent serviceIntent = new Intent(context, AntelacionActivity.class);
				serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				serviceIntent.putExtras(bund);
				serviceIntent.setAction("com.VuelosDroid.backEnd.behind.AntelacionActivity");
				context.startActivity(serviceIntent);
			}
		}
	}
	public String getMin (int pMin){
		if (pMin == 0){
			return "0 mins";
		} else {
			if (pMin / 60 == 0){
				if (pMin % 60 == 1){
					return "1 minuto";
				} else {
					return (pMin % 60 + " minuto");
				}
			}
			else if (pMin / 60 == 1){
				if (pMin % 60 == 1){
					return "1 hora y un 1 minuto";
				} else {
					return ("1 hora " + pMin % 60 + " minutos");
				}
			} else {
				if (pMin % 60 == 1){
					return (pMin/60 + " hora " + " 1 minuto");
				} else {
					return (pMin/60 + " hora " + pMin % 60 + " minutos");
				}
			}
		}
	}
}