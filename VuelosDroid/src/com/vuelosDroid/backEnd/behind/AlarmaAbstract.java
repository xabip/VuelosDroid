package com.vuelosDroid.backEnd.behind;

import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AlarmaAbstract {

	protected String TAG = "VuelosAndroid";
	private int red;
	private static final int CONECTADO = 0;
	private static final int DESCONECTADO = 1;
	Context context;
	
	public AlarmaAbstract(Context pContext){
		context = pContext;
	}
	
	public void setRed(){
		if (tieneRed()){
			red = CONECTADO;
		}else {
			Log.w(TAG, "AlarmaService - setRed - No hay red");

			red = DESCONECTADO;
		}

	}

	public boolean tieneRed() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}

		return false;
	}

	public boolean verSiDespegado(String pEstado){
		Log.d(TAG, "AlarmaService - verSiDespegado - despegado: "+ pEstado.contains("despegado"));
		return pEstado.contains("despegado");
	}

	public boolean verSiCancelado(String pEstado){
		return pEstado.contains("cancel");
	}

	public boolean verSiRetrasado(String pEstado){
		return pEstado.contains("retra");
	}

	public boolean verSiAterrizado(String pEstado){
		switch (red) {
		case CONECTADO:
			Log.d(TAG, "AlarmaService - verSiAterrizado - CONECTADO -aterrizado:  "+ pEstado.contains("aterrizado"));
			return pEstado.contains("aterrizado");

		case DESCONECTADO:
		default:
			return false;
		}
	}

	public int getDiferencia(String pEstado){
		String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ")+6).split(":");
		int minutos = 0;
		minutos += (((Integer.parseInt(horaVuelo[0])) - (new Date().getHours())))*60;
		minutos += (((Integer.parseInt(horaVuelo[1])) - (new Date().getMinutes())));
		Log.d(TAG, "AlarmaServer - getDiferencia - minutos de diferencia: "+ minutos);

		return (minutos);
	}
	
	public String getHora(String pEstado){
		//Log.i(TAG, "Servicio "+pEstado.substring(pEstado.indexOf("a las ")+6));
		Date horaActual = new Date();

		Log.i(TAG, "AlarmaAbstract - getHora - horaActual: " + horaActual.getHours()+":"+horaActual.getMinutes());
		Log.i(TAG, "AlarmaAbstract - getHora -  estado: " +   pEstado.substring(pEstado.indexOf("a las ")+6));
		String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ")+6).split(":");
		//int a = pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]:").length;
		Log.i(TAG, "AlarmaAbstract - getHora - " + (Integer.parseInt(horaVuelo[0]) - horaActual.getHours()));
		Log.i(TAG, "AlarmaAbstract - getHora - " +  (Integer.parseInt(horaVuelo[1]) - horaActual.getMinutes()));

		/*for (int i = 0; i < pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]").length; i++) {
			Log.i(TAG, pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]")[i]);
		}*/

		return pEstado.substring(pEstado.indexOf("a las ")+6);
	}
	
}
