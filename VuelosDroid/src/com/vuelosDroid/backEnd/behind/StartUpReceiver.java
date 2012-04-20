package com.vuelosDroid.backEnd.behind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
 
public class StartUpReceiver extends BroadcastReceiver {
 
	private static final String TAG = "VuelosAndroid";

	Context mContext;
	private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Broadcast: Señal de inicio recibida.");
		
		mContext = context;
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.pack.VuelosAndroid.AlarmaService");
		context.startService(serviceIntent);
		
		/*mContext = context;
		String action = intent.getAction();
		if (action.equalsIgnoreCase(BOOT_ACTION)) {
			Log.i(TAG, "Broadcast: Señal de inicio recibida.");

	        Intent serviceIntent = new Intent();
	        serviceIntent.setAction("com.packAena.AenaService");
	        context.startService(serviceIntent);
		}*/
    }
 
}
