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
		// Recibe el id para buscar en la base de datos e inicia AlarmaService
		serviceIntent.putExtra("id", intent.getIntExtra("id", 0));	
		Log.d("VuelosAndroid", "MiReceiver - onReceive - id: " + intent.getIntExtra("id", 0));
		serviceIntent.setAction("com.vuelosDroid.backEnd.behind.AlarmaService");
		context.startService(new Intent(context, com.vuelosDroid.backEnd.behind.AlarmaService.class));

	}
}