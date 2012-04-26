package com.vuelosDroid.frontEnd;

import java.util.ArrayList;
import java.util.List;

import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.scrapper.DatosGroup;
import com.vuelosDroid.backEnd.scrapper.DatosVuelo;
import com.vuelosDroid.backEnd.scrapper.NoHayMasPaginasDeVuelosException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ResultadoActivity extends ResultadosAbstractActivity{

	DatosGroup listaVuelos;
	List<DatosVuelo> datosVuelos;
	Context context;
	ListView miLista;

	Bundle bundle = new Bundle();
	String origen ="";
	String destino = "";
	String dia = "";
	String horario = "";
	String codOrigen = "";
	String codDestino = "";
	static String tipo = "";

	int pag = 0;
	boolean cargando = false;
	miAdapter adapter;
	LinearLayout lay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("onCreate VuelosAndroidActivity");
		context = this;
		setContentView(R.layout.activity_resultado);
		TextView text = (TextView) findViewById(R.id.text_busqueda_resultado_vuelo);
		bundle = this.getIntent().getExtras();
		Log.w(TAG, "HOLA");

		origen = bundle.getString("origen");
		destino = bundle.getString("destino");
		horario = bundle.getString("horario");
		tipo = bundle.getString("tipo");
		dia = bundle.getString("dia");

		if (tipo.contains("Destino")){
			text.setText(tipo + destino);
		}else{
			text.setText(tipo + origen);

		}
		Log.e(TAG, "Seguimos vivos...");

		lay = (LinearLayout) findViewById(R.id.layout_progress_resultado);

		//Coger los codigos
		if(!origen.equals("")){
			codOrigen = origen.substring((origen.indexOf("(")+1), origen.indexOf(")"));
		}
		if(!destino.equals("")){
			codDestino = destino.substring((destino.indexOf("(")+1), destino.indexOf(")"));
		}
		if(dia.equals("Mañana")){
			dia = "manana";
		}
		/*		Log.e(TAG, destino);
		Log.e(TAG, horario);
		Log.e(TAG, dia);*/

		//Si viene de busqueda de un vuelo
		if(origen.equals("") && destino.equals("")){
			Log.w(TAG, "Viene de un vuelo");
			loadData2(bundle.getString("codigo"), bundle.getString("dia").toLowerCase());
		}
		//Si viene de la busqueda por aeropuertos de origen y destino
		else{
			Log.w(TAG, "Viene de varios");
			//listaVuelos = getInfoVuelos(codOrigen, codDestino, "-1", dia.toLowerCase(), "");
			loadData(codOrigen, codDestino, horario, dia.toLowerCase(), "", pag);
		}

		miLista = (ListView)findViewById(R.id.lista_resultados);


		//Operaciones necesarias para iniciar otra activity desde el listView
		final Intent intent = new Intent(context, VueloResultadoActivity.class);

		miLista.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bundle extras = new Bundle();
				//Log.w(TAG, datosVuelos.get(arg2).getLinkInfoVuelo());
				extras.putString("url",datosVuelos.get(arg2).getLinkInfoVuelo());
				extras.putString("codigo", "");
				extras.putString("dia", dia);
				intent.putExtras(extras);
				if(!tieneRed()){
					Toast toast1 = Toast.makeText(getApplicationContext(), "Necesitas tener red para poder continuar", Toast.LENGTH_SHORT);
					toast1.show();

				}else{
					context.startActivity(intent);
				}	
			}
		});

		miLista.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.i(TAG, "ResultadoActivity - OnScrollListener - OnScroll - onScrollStateChanged "+scrollState);

			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Log.i(TAG, "ResultadoActivity - OnScrollListener - OnScroll "+firstVisibleItem + " " + visibleItemCount + " " + totalItemCount);
				if(pag>0){
					if (!cargando && (firstVisibleItem/pag)>15){
						loadData(codOrigen, codDestino, horario, dia.toLowerCase(), "", pag);
						cargando = true; 
					}
				}
			}
		});
	}   

	private final Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.e(TAG, "Dentro del Handler1");
			if (msg.obj != null) {
				if(msg.arg1 == 0){
					Log.e(TAG, "Dentro del Handler");
					listaVuelos = (DatosGroup)msg.obj;
					Log.e(TAG, listaVuelos.getValues().isEmpty()+"");
					Log.e(TAG, "Final del handler");
					//progressDialog.dismiss();
					lay.setVisibility(View.GONE);
					adapter = new miAdapter(context, listaVuelos);
					miLista.setAdapter(adapter);
					datosVuelos = (List<DatosVuelo>) listaVuelos.getValues();
					pag++;
				}else{
					if(!(msg.arg2==9)){
					Log.e(TAG, "ResultadoActivity - Dentro del Handler - Pagina " + msg.arg1);
					DatosGroup listaVuelosH;
					listaVuelosH = (DatosGroup)msg.obj;
					Log.e(TAG, "ResultadoActivity - Dentro del Handler - Tamaño - " +listaVuelos.getValues().size());
					listaVuelos.getValues().addAll(listaVuelosH.getValues());
					Log.e(TAG, "ResultadoActivity - Dentro del Handler - Pagina " +listaVuelos.getValues().isEmpty()+"");
					Log.e(TAG, "ResultadoActivity - Dentro del Handler - Pagina - Final del handler" +listaVuelos.getValues().size());
					//progressDialog.dismiss();
					//lay.setVisibility(View.GONE);
					pag++;
					adapter.notifyDataSetChanged();
					datosVuelos = (List<DatosVuelo>) listaVuelos.getValues();
					cargando = false;
					}
				}
			}
			else {
				Log.e(TAG, "Dentro del Handler NUll"+msg.obj);

			}
		}
	};
	private void loadData(final String codOrigen, final String codDestino, final String horario, 
			final String dia, final String company, final int pTipo) {
		Log.e(TAG, "Dentro del LoadData principio");
		new Thread(new Runnable(){
			public void run() {
				Log.e(TAG, "Dentro del new Thread");
				Message msg = progressHandler.obtainMessage();
				try {
					msg.obj = getInfoVuelos(codOrigen, codDestino, horario, dia, company, tipo);
				} catch (NoHayMasPaginasDeVuelosException e) {
					Log.e(TAG, "ResultadosActivity - loadData - NoHayMasPaginasDeVuelosExteption"+ e.toString());
					msg.arg2=9;
					msg.arg1 = pTipo;
					//DatosGroup dats = (DatosGroup)msg.obj;
					//Log.e(TAG, dats.getValues().isEmpty()+"");
					//Log.e(TAG, "Dentro del LoadData antes de mandar el mensaje");
					//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
					progressHandler.sendMessage(msg);
				}
				msg.arg1 = pTipo;
				DatosGroup dats = (DatosGroup)msg.obj;
				Log.e(TAG, dats.getValues().isEmpty()+"");
				Log.e(TAG, "Dentro del LoadData antes de mandar el mensaje");
				//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
				progressHandler.sendMessage(msg);

			}}).start();
	}

	private void loadData2(final String pCod, final String pDia) {
		Log.e(TAG, "Dentro del LoadData2 principio");
		new Thread(new Runnable(){
			public void run() {
				Log.e(TAG, "Dentro del new Thread");
				Message msg = progressHandler.obtainMessage();
				msg.obj = getInfoMasVuelos(pCod, pDia);
				Log.e(TAG, "Dentro del LoadData antes de mandar el mensaje");
				//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);

				progressHandler.sendMessage(msg);
			}}).start();
	}

	private static class miAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<DatosVuelo> datosVuelos;

		miAdapter(Context context, DatosGroup datos) {
			mInflater = LayoutInflater.from(context);
			//listaVuelos = getInfoVuelos("", "BIO", "8", "hoy", "");

			datosVuelos = (List<DatosVuelo>) datos.getValues();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView text;
			TextView text2;	 
			TextView text3;
			TextView textHoraLlegada;
			TextView textCod;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_resultados, null);
			}
/*			if ((position%2 == 1)){
				convertView.setBackgroundColor(R.drawable.background_gris);
			}*/
			text = (TextView) convertView.findViewById(R.id.text_lista1);
			//text2 = (TextView) convertView.findViewById(R.id.text_lista2);
			text3 = (TextView) convertView.findViewById(R.id.text_lista3);
			textCod = (TextView) convertView.findViewById(R.id.text_item_resultados_codigo);
			//textHoraLlegada = (TextView) convertView.findViewById(R.id.text_item_resultados_hora_destino);
			textCod.setText(datosVuelos.get(position).getNombreVuelo() + "  -  " + datosVuelos.get(position).getNombreCompany());
			//text.setText(datosVuelos.get(position).getAeropuertoDestino());
			//text2.setText(datosVuelos.get(position).getNombreCompany());
			if(tipo.contains("Destino")){
				text3.setText("Hora de llegada:   " + datosVuelos.get(position).getHoraOrigen());
				text.setText(datosVuelos.get(position).getAeropuertoDestino());

			}
			else{
				text.setText(datosVuelos.get(position).getAeropuertoDestino());

				text3.setText("Hora de salida:   " + datosVuelos.get(position).getHoraOrigen());
				//text.setText(datosVuelos.get(position).getAeropuertoOrigen());

			}
			//textHoraLlegada.setText("Hora de llegada:   " + datosVuelos.get(position).getHoraDestino());
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
