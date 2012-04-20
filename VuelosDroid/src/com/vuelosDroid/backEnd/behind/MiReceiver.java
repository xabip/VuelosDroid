package com.vuelosDroid.backEnd.behind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MiReceiver extends BroadcastReceiver {
	
	Context mContext;
	private static final String TAG = "VuelosAndroid";

	@Override
    public void onReceive(android.content.Context context, android.content.Intent intent) {
		Log.i("VuelosAndroid", "MiReceiver - onReceive - recibida la alarma");
		mContext = context;
		int flags = intent.getFlags();
		//String action = intent.getAction();
		Intent serviceIntent = new Intent();
		serviceIntent.putExtra("id", intent.getIntExtra("id", 0));	
		Log.d("VuelosAndroid", "MiReceiver - onReceive - id: " + intent.getIntExtra("id", 0));
		serviceIntent.setAction("com.vuelosDroid.backEnd.behind.AlarmaService");
		context.startService(new Intent(context, com.vuelosDroid.backEnd.behind.AlarmaService.class));

	}
}