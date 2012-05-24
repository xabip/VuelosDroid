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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class InternetReceiver extends BroadcastReceiver{
	
	Context mContext;

	@Override 
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("VuelosAndroid", "InternetReceiver - onReceive");
		mContext = context;
		if(tieneRed()){
			Log.i("VuelosAndroid", "InternetReceiver - onReceive - conRed");
			Intent serviceIntent = new Intent();
			serviceIntent.setAction("com.pack.VuelosDroid.backEnd.behind.AlarmaService");
			context.startService(serviceIntent);
		}
	}
	
	public boolean tieneRed() {
		boolean wifi = false;
		boolean mobile = false;

		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

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
}