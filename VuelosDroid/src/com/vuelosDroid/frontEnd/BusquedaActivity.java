package com.vuelosDroid.frontEnd;

import com.viewpagerindicator.TitlePageIndicator;
import com.vuelosDroid.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;


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

	        cols = (ViewPager)findViewById(R.id.columnas);
	        cols.setAdapter(miAdapter);	
	        
	        
	        TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titulos);
	        titleIndicator.setViewPager(cols);	      
	    }   
	    
	    public void onClickActualizar(View v){
	    	onCreate(bun);
	    }
	    
	    public void onClickPreferencias(View v){
	    	Intent intent = new Intent(getApplicationContext(), PreferenciasActivity.class);
	    	startActivity(intent);
	    }
}