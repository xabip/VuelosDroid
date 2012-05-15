package com.vuelosDroid.backEnd.behind;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MiReceiverAntelacion extends BroadcastReceiver {
	
	Context mContext;
	private static final String TAG = "VuelosAndroid";
	private String origen;
	private String destino;
	private String hora;
	private String url;
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
		Log.d("VuelosAndroid", "MiReceiverAntelacion - onReceive - origen: " + origen);
		Log.d("VuelosAndroid", "MiReceiverAntelacion - onReceive - destino: " + destino);
		Log.d("VuelosAndroid", "MiReceiverAntelacion - onReceive - hora: " + hora);
		Log.d("VuelosAndroid", "MiReceiverAntelacion - onReceive - minutos: " + minutos);

		notificar();
		
	}
	
	public void notificar (){
		
		String text = origen.substring(0, origen.indexOf("(")-1);
		String text2 = destino.substring(0, destino.indexOf("(")-1);

		Context context = mContext;
		String ns = Context.NOTIFICATION_SERVICE;
		int icono = android.R.drawable.btn_star_big_on;
		CharSequence contentTitle = "El vuelo " + text + " - " + destino
				+ " saldrá en: " + minutos + " minutos";
		CharSequence contentText = "Hora estimada: " + hora ;
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
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, mNotificacion);


	}
}