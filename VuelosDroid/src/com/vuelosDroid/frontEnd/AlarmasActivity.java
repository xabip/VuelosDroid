package com.vuelosDroid.frontEnd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.behind.AlarmasSql;
import com.vuelosDroid.backEnd.behind.AlarmasSqlAux;
import com.vuelosDroid.backEnd.scrapper.DatosGroup;
import com.vuelosDroid.backEnd.scrapper.DatosVuelo;
import com.vuelosDroid.backEnd.scrapper.VuelosJSoup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AlarmasActivity extends AbstractActivity {

	ListView miLista;
	ListView miListaAlarmas;
	List<DatosVuelo> datosVuelos;
	List<DatosVuelo> datosVuelosAlarmas;
	SharedPreferences prefer;
	VuelosJSoup vuelos;
	static Context context; 
	Bundle bun;
	LinearLayout layAlarm;
	LinearLayout laySeg;
	TextView textoNoHayAlarmas;
	TextView textoNoHaySeguimiento;

	int prefs = ESTATICO;

	private static int DINAMICO = 0;
	private static int ESTATICO = 1;

	private int red;
	private static final int CONECTADO = 0;
	private static final int DESCONECTADO = 1;
	
	private static final int ONTIME = 0;
	private static final int DELAYED = 1;
	private static final int CANCELED = 2;



	protected DatosGroup listaVuelos;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "AlarmasActivity - onCreate");
		bun = savedInstanceState;
		context = this;
		setContentView(R.layout.activity_alarmas);
		vuelos = new VuelosJSoup();
		prefer = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
		prefs = prefer.getInt("modo", 0);
		Log.e(TAG, ""+prefs);
		layAlarm = (LinearLayout) findViewById(R.id.layout_progress_alarmas_alarmas);
		laySeg = (LinearLayout) findViewById(R.id.layout_progress_alarmas_seguimiento);
		textoNoHayAlarmas = (TextView) findViewById(R.id.text_no_hay_alarmas);
		textoNoHaySeguimiento = (TextView) findViewById(R.id.text_no_hay_seguimiento);
		textoNoHayAlarmas.setVisibility(View.GONE);
		textoNoHaySeguimiento.setVisibility(View.GONE);

		datosVuelos = new ArrayList<DatosVuelo>();
		datosVuelosAlarmas = new ArrayList<DatosVuelo>();


		getAlarma();
		getSeguimiento();

		if(prefs == 1){
			layAlarm.setVisibility(View.GONE);
			controlAlarm();

			laySeg.setVisibility(View.GONE);
			controlSeg();
		}else{
			if (RED){
				loadData(datosVuelosAlarmas, 0);
				loadData(datosVuelos, 1);

			}else{
				layAlarm.setVisibility(View.GONE);
				controlAlarm();

				laySeg.setVisibility(View.GONE);
				controlSeg();	
				Toast toast1 = Toast.makeText(getApplicationContext(), "No hay ninguna conexión de red", Toast.LENGTH_SHORT);
				toast1.show();

			}
		}



	}  

	public void controlAlarm(){
		if(datosVuelosAlarmas.isEmpty()){
			Log.w(TAG, "AlarmasActivity - controlAlarm - La lista de alarmas NO tiene vuelos");
			textoNoHayAlarmas.setVisibility(View.VISIBLE);
		}else{
			Log.w(TAG, "AlarmasActivity - controlAlarm - La lista de alarmas tiene vuelos");
			miListaAlarmas = (ListView)findViewById(R.id.lista_resultados_alarmas);
			miListaAlarmas.setAdapter(new miAdapter(this, datosVuelosAlarmas));
			setListenersAlarma();
		}
	}

	public void controlSeg(){
		if(datosVuelos.isEmpty()){
			Log.w(TAG, "AlarmasActivity - controlSeg - La lista de seguimiento NO tiene vuelos");
			textoNoHaySeguimiento.setVisibility(View.VISIBLE);
		}else{
			Log.w(TAG, "AlarmasActivity - controlSeg - La lista de seguimiento tiene vuelos");
			miLista = (ListView)findViewById(R.id.lista_resultados_seguimiento);
			miLista.setAdapter(new miAdapter(this, datosVuelos));
			setListenersSeguimiento();
		}
	}

	public void setAdapAlarm(){
		miListaAlarmas = (ListView)findViewById(R.id.lista_resultados_alarmas);
		miListaAlarmas.setAdapter(new miAdapter(this, datosVuelosAlarmas));
		setListenersAlarma();
	}


	public void setListenersSeguimiento(){
		final Intent intent = new Intent(context, VueloResultadoActivity.class);

		miLista.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bundle extras = new Bundle();
				Log.d(TAG, "AlarmasActivity - setListenersSeguimiento - miLista.setOnItemClickListener - Pulsada la posicion: " + arg2);
				extras.putString("url", datosVuelos.get(arg2).getLinkInfoVuelo());
				extras.putString("codigo", "");
				extras.putString("dia", "hoy");
				intent.putExtras(extras);
				if(!tieneRed()){
					Toast toast1 = Toast.makeText(context, "Necesitas tener red para poder continuar", Toast.LENGTH_SHORT);
					toast1.show();
				}else{
					context.startActivity(intent);
				}	
			}
		});

		miLista.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Log.i(TAG, "AlarmasActivity - miLista.setOnItemLongClickListener - Borrando Alarma  Alarma");
				borrarSeguimiento(datosVuelos.get(arg2).getLinkInfoVuelo());
				return true;
			}
		});
	}

	public void setListenersAlarma(){
		miListaAlarmas.setOnItemClickListener(new OnItemClickListener() {

			final Intent intent = new Intent(context, VueloResultadoActivity.class);

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bundle extras = new Bundle();
				Log.d(TAG, "AlarmasActivity - setListenersAlarma - Pulsada la posicion: " + arg2);
				extras.putString("url",datosVuelosAlarmas.get(arg2).getLinkInfoVuelo());
				extras.putString("codigo", "");
				extras.putString("dia", "");
				intent.putExtras(extras);
				if(!RED){
					Toast toast1 = Toast.makeText(getApplicationContext(), "No hay red.", Toast.LENGTH_SHORT);
					toast1.show();

				}else{
					context.startActivity(intent);
				}			
			}
		});

		miListaAlarmas.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Log.i(TAG, "AlarmasActivity - setListenersAlarma - onItemLongClick - Borrando Alarma");
				borrarAlarma(datosVuelosAlarmas.get(arg2).getLinkInfoVuelo());
				return true;
			}
		});
	}

	public void getSeguimiento(){
		AlarmasSql alarms =  new AlarmasSql(this); 
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.i(TAG, "AlarmasActivity - getSeguimiento - Funciona la llamada");

		String[] args = new String[] {AlarmasSql.URL, AlarmasSql.NOMBREVUELO, 
				AlarmasSql.FECHAORIGEN, AlarmasSql.HORAORIGEN, AlarmasSql.NOMBRECOMPANY};
		//Log.(TAG, "Funciona la llamada");

		Cursor c = db.query("alarmas", args, null, null, null, null, null);
		//Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {
			//Recorremos el cursor hasta que no haya más registros
			do {
				DatosVuelo dat = new DatosVuelo();

				Log.d(TAG, "AlarmasActivity - getSeguimiento - url: " + c.getString(0));
				dat.setLinkInfoVuelo(c.getString(0));
				Log.d(TAG, "AlarmasActivity - getSeguimiento - codigo: " + c.getString(1));
				dat.setNombreVuelo(c.getString(1));
				dat.setFechaOrigen(c.getString(2));
				dat.setHoraOrigen(c.getString(3));
				dat.setNombreCompany(c.getString(4));
				datosVuelos.add(dat);

			} while(c.moveToNext());
		}
		db.close();
	}

	public void getAlarma(){
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.i(TAG, "AlarmasActivity - getAlarma - Funciona la llamada");

		String[] args = new String[] {AlarmasSqlAux.URL, AlarmasSqlAux.NOMBREVUELO, 
				AlarmasSqlAux.FECHAORIGEN, AlarmasSqlAux.HORAORIGEN, AlarmasSqlAux.NOMBRECOMPANY};

		Cursor c = db.query("alarmas_aux", args, null, null, null, null, null);
		//Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {
			//Recorremos el cursor hasta que no haya más registros
			do {
				DatosVuelo dat = new DatosVuelo();

				Log.d(TAG, c.getString(0));
				dat.setLinkInfoVuelo(c.getString(0));
				Log.d(TAG, c.getString(1));
				dat.setNombreVuelo(c.getString(1));
				dat.setFechaOrigen(c.getString(2));
				dat.setHoraOrigen(c.getString(3));
				dat.setNombreCompany(c.getString(4));
				datosVuelosAlarmas.add(dat);

			} while(c.moveToNext());
		}
		db.close();
	}

	public void borrarSeguimiento(String pUrl){
		AlarmasSql alarms =  new AlarmasSql(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		db.execSQL("DELETE FROM alarmas WHERE "+ AlarmasSql.URL+"='"+pUrl+"' ");
		onCreate(bun);
	}

	public void borrarAlarma(String pUrl){
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		db.execSQL("DELETE FROM alarmas_aux WHERE "+ AlarmasSql.URL+"='"+pUrl+"' ");
		onCreate(bun);  
	}

	public void onClickActualizar(View v){
		onCreate(bun);
	}

	public void onClickSearch(View v){
		startActivity (new Intent(getApplicationContext(), AboutActivity.class));
	}

	private final Handler progressHandler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			Log.i(TAG, "AlarmasActivity - Handler - Dentro del Handler principio");
			if (msg.obj != null) {
				switch (msg.arg1) {
				case 0:
					//listaVuelos = (DatosGroup)msg.obj;
					datosVuelosAlarmas = (List<DatosVuelo>)msg.obj;
					layAlarm.setVisibility(View.GONE);
					controlAlarm();
					break;
				case 1:

					datosVuelos = (List<DatosVuelo>)msg.obj;
					laySeg.setVisibility(View.GONE);
					controlSeg();
					break;
				}
				//Log.e(TAG, "Dentro del Handler");
				//datosVuelos = (List<DatosVuelo>)msg.obj;
				Log.i(TAG, "AlarmasActivity - Handler - Final del handler");
				//progressDialog.dismiss();
				//lay.setVisibility(View.GONE);

			}
			else {
				Log.w(TAG, "Dentro del Handler NUll"+msg.obj);

			}
		}
	};
	private void loadData(final List<DatosVuelo> pLista, final int pTipo) {
		Log.i(TAG, "AlarmasActivity - loadData - Dentro del LoadData principio");
		new Thread(new Runnable(){
			public void run() {
				Log.i(TAG, "AlarmasActivity - loadData - Dentro del new Thread");
				Message msg = progressHandler.obtainMessage();
				List<DatosVuelo> miLista = new ArrayList<DatosVuelo>();
				for (DatosVuelo datosVuelo : pLista) {
					try {
						miLista.add(vuelos.getDatosVuelo(datosVuelo.getLinkInfoVuelo()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				msg.obj = miLista;
				msg.arg1 = pTipo;

				Log.i(TAG, "AlarmasActivity - loadData - Dentro del LoadData antes de mandar el mensaje");
				//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);

				progressHandler.sendMessage(msg);
			}}).start();
	}



	public void setRed(){
		if (tieneRed()){
			red = CONECTADO;
		}else {
			Log.w(TAG, "AlarmasActivity - setRed - No hay red");

			red = DESCONECTADO;
		}

	}

	public boolean tieneRed() {
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}

		return false;
	}

	public boolean verSiDespegado(String pEstado){
		Log.d(TAG, "AlarmasActivity - verSiDespegado - despegado: "+ pEstado.contains("despegado"));

		return pEstado.contains("despegado");
	}

	public boolean verSiCancelado(String pEstado){
		return pEstado.contains("cancel");
	}

	public boolean verSiRetrasado(String pEstado){
		return pEstado.contains("retra");
	}

	public int controlEstado(String pEstado, String pHora){
		int dif = getDiferencia(pEstado, pHora);
		Log.d(TAG, "AlarmasActivity - controlEstado - diferencia: " + dif);
		if(dif<10){
			return DELAYED;
		}
		else{
			return ONTIME;
		}
	}

	public boolean verSiAterrizado(String pEstado){
		switch (red) {
		case CONECTADO:
			Log.d(TAG, "AlarmasActivity - verSiAterrizado - CONECTADO -aterrizado:  "+ pEstado.contains("aterrizado"));
			return pEstado.contains("aterrizado");

		case DESCONECTADO:
		default:
			return false;
		}
	}

	public String controlDepegado(String pEstado){
		int dif = getDiferencia(pEstado);
		String text;
		if(dif == 0){
			return "No hay datos";
		}
		if (verSiDespegado(pEstado)){
			if((dif/60) > 0){
				text = "Hace: ";
			}else{
				text = "Hace " + (dif/60)*(-1) + " h y ";
			}
			text = text + (dif%60)*(-1) + " mins";

			return text;
		}else{
			if((dif/60)*(-1) > 0){
				text = "Quedan: ";
			}else{
				text = "Quedan: " + (dif/60) + " h y ";
			}

			text = text + (dif%60) + " mins";
			return text;
		}
	}

	public String controlAterrizado(String pEstado){
		int dif = getDiferencia(pEstado);
		String text;
		if(dif == 0){
			return "No hay datos";
		}
		if (verSiAterrizado(pEstado)){
			if((dif/60) > 0){
				text = "Hace: ";
			}else{
				text = "Hace " + (dif/60) + " h y ";
			}

			text = text + (dif%60)*(-1) + " mins";

			return text;
		}else{
			if((dif/60)*(-1) > 0){
				text = "Quedan: ";
			}else{
				text = "Quedan: " + (dif/60) + " h y ";
			}

			text = text + (dif%60) + " mins";
			return text;
		}
	}

	public int getDiferencia(String pEstado){
		if(pEstado.contains("a las ")){
			String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ")+6).split(":");
			int minutos = 0;
			minutos += (((Integer.parseInt(horaVuelo[0])) - (new Date().getHours())))*60;
			minutos += (((Integer.parseInt(horaVuelo[1])) - (new Date().getMinutes())));
			Log.d(TAG, "AlarmaActivity - getDiferencia - minutos de diferencia: "+ minutos);
			return (minutos);
		}
		else {
			return 0;
		}
	}

	public int getDiferencia(String pEstado, String pHora){
		Log.d(TAG, "AlarmasActivity - getDiferencia(2) - pEstado: " + pEstado);
		Log.d(TAG, "AlarmasActivity - getDiferencia(2) - pHora: " + pHora);

			String[] horaVuelo = pEstado.split(":");
			String[] horaPrevista = pHora.split(":");
			int minutos = 0;
			minutos += (((Integer.parseInt(horaPrevista[0])) - (Integer.parseInt(horaVuelo[0]))))*60;
			Log.d(TAG, "AlarmasActivity - getDiferencia(2) - mins: " + minutos);
			minutos += (((Integer.parseInt(horaPrevista[1])) - (Integer.parseInt(horaVuelo[1]))));
			Log.d(TAG, "AlarmaActivity - getDiferencia(2) - minutos de diferencia: "+ minutos);
			return (minutos);
		
	}

	public String getHora(String pEstado){
		//Log.i(TAG, "Servicio "+pEstado.substring(pEstado.indexOf("a las ")+6));
		Date horaActual = new Date();
		if(!pEstado.contains("a las ")){
			return "Sin informacion";
		}
		/*		Log.i(TAG, "AlarmaAbstract - getHora - horaActual: " + horaActual.getHours()+":"+horaActual.getMinutes());
		Log.i(TAG, "AlarmaAbstract - getHora -  estado: " +   pEstado.substring(pEstado.indexOf("a las ")+6));
		String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ")+6).split(":");
		//int a = pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]:").length;
		Log.i(TAG, "AlarmaAbstract - getHora - " + (Integer.parseInt(horaVuelo[0]) - horaActual.getHours()));
		Log.i(TAG, "AlarmaAbstract - getHora - " +  (Integer.parseInt(horaVuelo[1]) - horaActual.getMinutes()));

		for (int i = 0; i < pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]").length; i++) {
			Log.i(TAG, pEstado.substring(pEstado.indexOf("a las ")+6).split("[0-9]?[0-9]")[i]);
		}*/

		return pEstado.substring(pEstado.indexOf("a las ")+6);
	}

	private class miAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<DatosVuelo> datosVuelos;

		miAdapter(Context context, List<DatosVuelo> datos) {
			mInflater = LayoutInflater.from(context);
			//listaVuelos = getInfoVuelos("", "BIO", "8", "hoy", "");
			datosVuelos = datos;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView text;
			TextView text2;	 
			TextView text3;
			TextView text4;
			TextView text5;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_alarmas, null);
			} 

			text = (TextView) convertView.findViewById(R.id.text_item_alarmas_codigo);
			text2 = (TextView) convertView.findViewById(R.id.text_item_alarmas_nombre);
			text3 = (TextView) convertView.findViewById(R.id.text_item_alarmas_fecha);
			text4 = (TextView) convertView.findViewById(R.id.text_item_alarmas_hora_salida);
			text5 = (TextView) convertView.findViewById(R.id.text_item_alarmas_hora_llegada);

			String textOrigen = datosVuelos.get(position).getAeropuertoOrigen().
					substring(0, datosVuelos.get(position).getAeropuertoOrigen().indexOf("(")-1);
			String textDestino = datosVuelos.get(position).getAeropuertoDestino().
					substring(0, datosVuelos.get(position).getAeropuertoDestino().indexOf("(")-1);

			int estado = controlEstado(getHora(datosVuelos.get(position).getEstadoVueloOrigen()), datosVuelos.get(position).getHoraOrigen());
			Log.d(TAG, "AlarmasActivity - getView - estado: " + estado + " del vuelo: " + datosVuelos.get(position).getFechaOrigen());
			text.setText(datosVuelos.get(position).getNombreVuelo() + "  -  " + datosVuelos.get(position).getNombreCompany());
			text2.setText(textOrigen + " - " + textDestino);
			text3.setText("Fecha de salida: " + datosVuelos.get(position).getFechaOrigen());	 
			text4.setText("Hora Salida: " + getHora(datosVuelos.get(position).getEstadoVueloOrigen()) + " (" +
					controlDepegado(datosVuelos.get(position).getEstadoVueloOrigen())+")");
			text5.setText("Hora Llegada: " + getHora(datosVuelos.get(position).getEstadoVueloDestino()) + " (" +
					controlAterrizado(datosVuelos.get(position).getEstadoVueloDestino())+")");

			return convertView;	 
		}


		public int getCount() {
			return datosVuelos.size();
		}

		public Object getItem(int position) {
			return position;
		}


		public long getItemId(int position) {
			return position;
		}


	}
}
