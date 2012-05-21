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

import com.vuelosDroid.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MiReceiverAntelacion extends BroadcastReceiver {

	Context mContext;
	private static final String TAG = "VuelosAndroid";
	private String origen;
	private String destino;
	private String hora;
	private String url;
	private int id;
	private int minutos;
	private int SIMPLE_NOTFICATION_ID;


	@Override
	public void onReceive(android.content.Context context, android.content.Intent intent) {
		Log.i("VuelosAndroid", "MiReceiver - onReceive - recibida la alarma");
		mContext = context;
		int flags = intent.getFlags();
		//String action = intent.getAction();
		origen = intent.getStringExtra("origen");	
		destino = intent.getStringExtra("destino");
		hora = intent.getStringExtra("hora");
		minutos = intent.getIntExtra("minutos", 0);
		url = intent.getStringExtra("url");
		id = intent.getIntExtra("id", 0);
		Log.d("VuelosAndroid", "MiReceiverAntelacion - onReceive - origen: " + origen);
		Log.d("VuelosAndroid", "MiReceiverAntelacion - onReceive - destino: " + destino);
		Log.d("VuelosAndroid", "MiReceiverAntelacion - onReceive - hora: " + hora);
		Log.d("VuelosAndroid", "MiReceiverAntelacion - onReceive - minutos: " + minutos);

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

			final Context context = mContext;
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
			mNotificacion.defaults |= Notification.DEFAULT_SOUND;
			mNotificacion.defaults |= Notification.DEFAULT_VIBRATE;
			mNotificacion.defaults |= Notification.DEFAULT_LIGHTS;
			mNotificacion.defaults |= Notification.FLAG_INSISTENT;
			mNotificacion.tickerText = ("El vuelo " +  text + " -" + text2 + " saldrá en: " + minutos + " minutos");
			mNotificationManager.notify(id, mNotificacion);

			/*AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
			alertbox.setMessage("Aviso de vuelo");
			alertbox.setTitle("El vuelo " +  text + " -" + text2 + " saldrá en: " + minutos + " minutos");
			alertbox.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
	
				}
			});
	
			alertbox.setNegativeButton("Retrasar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					Bundle bun = new Bundle();
					bun.putString("hora", MiReceiverAntelacion.this.hora);
					bun.putString("origen", origen);
					bun.putString("destino", destino);
					bun.putInt("minutos", minutos - 10);
					bun.putString("url", url);
					bun.putString("dia", "hoy");
					Intent intentA = new Intent(context, MiReceiverAntelacion.class);
					intentA.putExtras(bun);
					if (minutos > 11) {
						//intentA.set(getApplicationContext(), com.vuelosDroid.frontEnd.VueloResultadoActivity.class);
	
						PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id + 999, intentA,
								PendingIntent.FLAG_CANCEL_CURRENT);
						AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
						alarmManager.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis() + (1 * 100000), pendingIntent);
					}
				}
			});
			alertbox.show();*/
		}
	}
}