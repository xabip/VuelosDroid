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

package com.vuelosDroid.frontEnd;

import com.viewpagerindicator.TitlePageIndicator;
import com.vuelosDroid.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 *
 * @author Xabi
 *
 */
public class BusquedaActivity extends AbstractActivity {

	private ViewPager cols;
	private ViewPagerAdapter miAdapter;
	Bundle bun;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("onCreate VuelosAndroidActivity");
		bun= savedInstanceState;
		setContentView(R.layout.main);
		if (!RED){
			Toast toast1 = Toast.makeText(getApplicationContext(), "No hay red. No podrás hacer busquedas", Toast.LENGTH_SHORT);
			toast1.show();
		}
		miAdapter = new ViewPagerAdapter(this);
		Log.d(TAG, "BusquedaActivity - onCreate - miAdapter: " + miAdapter.getCount());
		cols = (ViewPager)findViewById(R.id.columnas);
		cols.setAdapter(miAdapter);	
		cols.setAdapter(miAdapter);

		TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titulos);
		titleIndicator.setViewPager(cols);	      
		titleIndicator.setFooterIndicatorPadding(7);
		//titleIndicator.setPadding(10, 0, 0, 7);
		//titleIndicator.setTitlePadding(titlePadding)
		Log.d(TAG, "BusquedaActivity - TextSize: " + titleIndicator.getTextSize());
		Log.d(TAG, "BusquedaActivity - TitlePadding: " + titleIndicator.getTitlePadding());
		Log.d(TAG, "BusquedaActivity - TopPaggind: " + titleIndicator.getTopPadding());
		Log.d(TAG, "BusquedaActivity - ClipPadding: " + titleIndicator.getClipPadding());
		Log.d(TAG, "BusquedaActivity - FooterIndicatorPaddingPaggind: " + titleIndicator.getFooterIndicatorPadding());

		titleIndicator.setTopPadding(3);
		//titleIndicator.setTitlePadding(1);
		titleIndicator.setTextSize(16);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(titleIndicator.getWindowToken(), 0);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}   

	public void onClickBorrar(View v){
		int pos = cols.getCurrentItem();
		//onCreate(bun);
		//cols.setAdapter(cols.getAdapter());
		// cols.setp
		// miAdapter = new ViewPagerAdapter(this);
		Log.d(TAG, "BusquedaActivity - onCreate - miAdapter: " + miAdapter.getCount());
		// cols.setAdapter(miAda.pter);	
		cols.invalidate();
		cols.postInvalidate();

		cols.buildDrawingCache();
		cols.refreshDrawableState();
		cols.forceLayout();

	//	cols.setCurrentItem(pos+1);
	}

	public void onClickActualizar(View v){
		onCreate(bun);
	}

	public void onClickPreferencias(View v){
		Intent intent = new Intent(getApplicationContext(), PreferenciasActivity.class);
		startActivity(intent);
	}
}