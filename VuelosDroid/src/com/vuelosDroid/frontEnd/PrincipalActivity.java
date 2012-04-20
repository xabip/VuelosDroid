/*
 * Copyright (C) 2011 Wglxy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vuelosDroid.frontEnd;

import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.behind.AlarmaService;

import android.content.Intent;
import android.os.Bundle;


/**
 * This is a simple activity that demonstrates the dashboard user interface pattern.
 */

public class PrincipalActivity extends AbstractActivity{

/**
 * onCreate - called when the activity is first created.
 * Called when the activity is first created. 
 * This is where you should do all of your normal static set up: create views, bind data to lists, etc. 
 * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
 * 
 * Always followed by onStart().
 *
 */

	
protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    debug("OnCreate Principal");
    setContentView(R.layout.activity_home);
	
    /////////////////////////////////////////////////
    
    Bundle bundle = new Bundle();
	bundle.putString("url", "");
	//Creamos el intent necesario para lanzar el servicio y le metemos el bundle.
    Intent intent = new Intent(this, AlarmaService.class);
    intent.putExtras(bundle);
    startService(intent);
}

} 