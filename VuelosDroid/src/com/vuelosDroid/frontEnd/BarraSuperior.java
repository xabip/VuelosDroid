package com.vuelosDroid.frontEnd;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.vuelosDroid.R;

public class BarraSuperior extends LinearLayout {

	TextView titulo;
	
	public BarraSuperior(Context context) {
		super(context);
		inicializar();
		// TODO Auto-generated constructor stub
	}
	
	public BarraSuperior(Context context, AttributeSet  attrs){
		super(context, attrs);
		inicializar();
	}
	
	public void inicializar(){
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li = (LayoutInflater)getContext().getSystemService(infService);
		li.inflate(R.layout.barra_superior, this, true);
		
		//Obtener las referencias
		titulo = (TextView)findViewById(R.id.titulo_barra);
	}
	
	public void setTitulo(String pTitulo){
		titulo.setText(pTitulo);
	}
}
