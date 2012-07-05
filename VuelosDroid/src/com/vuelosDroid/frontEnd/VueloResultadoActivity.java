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

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.behind.AlarmaService;
import com.vuelosDroid.backEnd.behind.AlarmasSql;
import com.vuelosDroid.backEnd.behind.AlarmasSqlAux;
import com.vuelosDroid.backEnd.behind.BusquedaRecienteSql;
import com.vuelosDroid.backEnd.scrapper.DatosVuelo;
import com.vuelosDroid.backEnd.scrapper.MoreFlightsException;
import com.vuelosDroid.backEnd.scrapper.NoHayVueloException;

/**
 * Activity del resultado del vuelo
 * @author Xabi
 *
 */
public class VueloResultadoActivity extends ResultadosAbstractActivity {

	Bundle bundle = new Bundle();
	String url = "";
	String codigo = "";
	String dia = "";
	int or = 0;
	DatosVuelo datos;
	Bundle bun;
	Context cont;
	LinearLayout lay;
	LinearLayout lay2;
	LinearLayout layMarcador;
	LinearLayout layManana;
	LinearLayout layAlarmas;
	RadioGroup grupoAlarma;
	RadioButton radAlarma;
	private RadioButton radMarcador;
	ImageButton favorito;
	ImageButton botonAlarma;
	DemoPopupWindow dw;
	private int id;

	boolean alarma = false;
	boolean marcador = false;
	//private ProgressDialog progressDialog;
	VueloResultadoActivity vra;

	public int estado;
	public static int ALARMA = 0;
	public static int SEGUIMIENTO = 1;
	public static int NADA = 2;
	public boolean reciente;
	public boolean rapido = false;

	private static final int ONTIME = 0;
	private static final int DELAYED = 1;
	private static final int CANCELED = 2;
	private static final int DESPEGADO = 3;
	private static final int ATERRIZADO = 4;

	//private int estdado;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("onCreate VueloResultadoActivity");
		bun = savedInstanceState;
		cont = getApplicationContext();
		setContentView(R.layout.activity_resultado_vuelo);
		lay = (LinearLayout) findViewById(R.id.layout_progress_vuelo_resultado);
		lay2 = (LinearLayout) findViewById(R.id.layout_vuelo_resultado_sup);
		layMarcador = (LinearLayout) findViewById(R.id.layout_marcador);
		layAlarmas = (LinearLayout) findViewById(R.id.layout_resultado_alarmas);
		lay2.setVisibility(View.INVISIBLE);
		layAlarmas.setVisibility(View.INVISIBLE);
		radAlarma = (RadioButton) findViewById(R.id.radio_alarma);
		radMarcador = (RadioButton) findViewById(R.id.radio_marcador);

		grupoAlarma = (RadioGroup) findViewById(R.id.grupo_marcador);
		favorito = (ImageButton) findViewById(R.id.boton_resultado_favorito);
		botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);

		bundle = this.getIntent().getExtras();
		url = bundle.getString("url");
		String dia = bundle.getString("dia");
		int or = bundle.getInt("or", 0);
		dia = dia.toLowerCase();
		Log.d(TAG, "VueloResultadoActivity - Oncreate - url: " + url);

		Log.d(TAG, "VueloResultadoActivity - onCreate - dia: " + dia);

		if(!dia.equals("no")){
			Log.d(TAG, "VueloResultadoActivity - Oncreate - Antes de cambiar url - dia: " + dia);
			Log.d(TAG, "VueloResultadoActivity - Oncreate - Antes de cambiar url - url: " + url);
			if(!url.equals(" ")){
				GregorianCalendar cal = new GregorianCalendar();
				if (cal.get(Calendar.HOUR_OF_DAY) < 4 && !dia.equals("manana") && or == 1){
					Log.i(TAG, "VueloResultadoActivity - Oncreate - Antes de cambiar url - diahoy");
					url = cambiarFechaToUrl(url, "ayer");
				}else if (cal.get(Calendar.HOUR_OF_DAY) < 4 && dia.equals("manana") && or == 1){
					Log.i(TAG, "VueloResultadoActivity - Oncreate - Antes de cambiar url - diamanana");
					url = cambiarFechaToUrl(url, "hoy");
				}else{
					url = cambiarFechaToUrl(url, dia);
				}
			}
			Log.d(TAG, "VueloResultadoActivity - Oncreate - Despues de cambiar url - dia: " + dia);

			Log.d(TAG, "VueloResultadoActivity - Oncreate - Despues de cambiar url - url: " + url);

		}
		//Log.e(TAG, "VueloResultadoActivity - onCreate - url: " + url);
		if(!RED){
			Toast toast1 = Toast.makeText(getApplicationContext(), "No hay red.", Toast.LENGTH_SHORT);
			lay.setVisibility(View.GONE);
			toast1.show();

		}else{
			vra = this;
			controlOperaciones();
		}	
	}  

	/**
	 * Gestiona la carga y comunicacion en segundo plano de los vuelos
	 */
	private final Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.i(TAG, "VueloResultadoActivity - progressHandler - Principio del Handler");
			if (msg.obj != null) {
				Log.i(TAG, "VueloResultadoActivity - progressHandler - Dentro del Handler");
				datos = (DatosVuelo)msg.obj;
				//Log.e(TAG, "Final del handler"+ datos.getAeropuertoDestino());
				//progressDialog.dismiss();
				lay.setVisibility(View.GONE);
				lay2.setVisibility(View.VISIBLE);
				layAlarmas.setVisibility(View.VISIBLE);
				controlEstado(url);	
				dia = bundle.getString("dia");
				Log.d(TAG, "VueloResultadoActivity - progressHandler - dia: " + dia);
				estado = controlEstado(datos.getEstadoVueloOrigen(), datos.getHoraOrigen());
				Log.d(TAG, "VueloResultadoAtivity - progressHandler - estado: " + estado);
				setLayout();
				controlReciente(datos);
				guardarReciente(datos);
				//guardarReciente(datos);
			}
			else {
				Log.w(TAG, "Dentro del Handler NUll"+msg.obj);
			}
		}
	};

	
	/**
	 * Busca en segundo plano los datos del vuelo
	 * @param pUrl
	 */
	private void loadData(final String pUrl) {
		Log.i(TAG, "VueloResultadoActivity - loadData - Dentro del LoadData principio");
		new Thread(new Runnable(){
			public void run() {
				Log.i(TAG, "VueloResultadoActivity - loadData -  Dentro del new Thread");
				Message msg = progressHandler.obtainMessage();
				msg.obj = getInfoUnVuelo("", pUrl);
				Log.i(TAG, "VueloResultadoActivity - loadData - Antes de mandar el mensaje");
				//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
				progressHandler.sendMessage(msg);
			}}).start();
	}

	/**
	 * Handler que gestina la busqueda por codigo
	 */
	private final Handler codHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.i(TAG, "VueloResultadoActivity - progressHandler - Principio del Handler");
			if (msg.obj != null) {
				Log.i(TAG, "VueloResultadoActivity - progressHandler - Dentro del Handler");
				datos = (DatosVuelo)msg.obj;
				//Log.e(TAG, "Final del handler"+ datos.getAeropuertoDestino());
				Log.i(TAG, "VueloResultadoActivity - controlOperaciones - despues de la llamada al codigo");
				//progressDialog.dismiss();
				lay.setVisibility(View.GONE);
				lay2.setVisibility(View.VISIBLE);
				layAlarmas.setVisibility(View.VISIBLE);
				if (datos.getAeropuertoOrigen().equalsIgnoreCase("no")){
					Log.i(TAG, "VueloResultadoActivity - controlOperaciones - origen no");
				}
				controlEstado(url);
				dia = bundle.getString("dia");
				Log.d(TAG, "VueloResultadoActivity - controlOperaciones - try - dia: " + dia);
				estado = controlEstado(datos.getEstadoVueloOrigen(), datos.getHoraOrigen());
				Log.d(TAG, "VueloResultadoAtivity - controlOperaciones - try - estado: " + estado);
				setLayout();
				controlReciente(datos);
				guardarReciente(datos);
			}
			else {
				Log.w(TAG, "Cod - Dentro del Handler NUll"+msg.obj);
				datos = new DatosVuelo();
				datos.setAeropuertoOrigen("no vuelo");
				//setLayout();
			}
		}
	};

	/**
	 * Metodo que hace la busuqda por codigo en segundo plano
	 * @param pCod
	 * @param pDia
	 */
	private void loadData2(final String pCod, final String pDia) {
		Log.i(TAG, "VueloResultadoActivity - loadData - Dentro del LoadData principio");
		new Thread(new Runnable(){
			public void run() {
				Log.i(TAG, "VueloResultadoActivity - loadData -  Dentro del new Thread");
				Message msg = progressHandler.obtainMessage();
				try {
					msg.obj = getInfoUnVuelo("", codigo.toUpperCase(), dia.toLowerCase());
				} catch (MoreFlightsException e) {
					Log.e(TAG, "VueloResultadoActivity - controlOperaciones - MoreFlightsException");
					Intent intent = new Intent(vra, ResultadoActivity.class);
					Bundle extras = new Bundle();
					extras.putString("codigo", codigo);
					extras.putString("dia", dia);
					extras.putString("url", "");
					extras.putString("origen", "");
					extras.putString("destino", "");
					extras.putString("horario", "");
					extras.putString("tipo", "codigo");
					intent.putExtras(extras);
					vra.startActivity(intent);
				} catch (NoHayVueloException e) {
					Log.e(TAG, "VueloResultadoActivity - controlOperaciones - No hay vuelo Exception " + e.getMessage());
					//datos.setAeropuertoOrigen("No");
				}
				Log.i(TAG, "VueloResultadoActivity - loadData - Antes de mandar el mensaje");
				//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
				codHandler.sendMessage(msg);
			}}).start();
	}

	/**
	 * Controla si viene desde busqueda rapida o desde una url predefinida
	 */
	public void controlOperaciones(){
		if (url.equals(" ")){
			Log.d(TAG, "VueloResultadoActivity - controlOperaciones - url: " + url);
			codigo = bundle.getString("codigo");
			dia = bundle.getString("dia");
			Log.d(TAG, "VueloResultadoActivity - controlOperaciones - codigo y dia: " +codigo+dia);
			if(dia.equals("Mañana")){
				dia = "manana";
			}
			loadData2(codigo.toUpperCase(), dia.toLowerCase());

		}else {
			Log.i(TAG, "VueloResultadoActivity - controlOperaciones - Con url");
			//setLayout();

			loadData(url);
			//datos = getInfoUnVuelo("", url);
			//controlEstado(url);
		}
	}

	/**
	 * Controla si esta en alarmas o no
	 * @param pUrl
	 */
	public void controlEstado(String pUrl){
		alarma = getAlarma();
		marcador = false;
		//marcador = getSeguimiento();	
	}

	/**
	 * Toast si no se puede poner alarma
	 * @param pText
	 */
	private void noAlarma(String pText){
		Toast toast1 = Toast.makeText(getApplicationContext(), pText, Toast.LENGTH_SHORT);
		toast1.show();
	}

	/**
	 * Pone el layout general de la activity
	 */
	public void setLayout(){
		TextView text1 = (TextView) findViewById(R.id.text_resultado1);
		TextView text2 = (TextView) findViewById(R.id.text_resultado2);
		TextView text3 = (TextView) findViewById(R.id.text_resultado3);
		TextView text4 = (TextView) findViewById(R.id.text_resultado4);
		TextView textEstado = (TextView) findViewById(R.id.text_resultado_vuelo_estado); 
		LinearLayout layoutManana = (LinearLayout) findViewById(R.id.layout_resultado_vuelo_manana);
		TextView textDiaOrigen = (TextView) findViewById(R.id.text_resultado_vuelo_origen_dia);
		TextView textDiaDestino = (TextView) findViewById(R.id.text_resultado_vuelo_destino_dia);
		TextView textDestinoNombre = (TextView) findViewById(R.id.text_resultado_destino_nombre);
		TextView textDestinoHora = (TextView) findViewById(R.id.text_resultado_destino_hora);
		TextView textDestinoTerminal = (TextView) findViewById(R.id.text_resultado_destino_terminal);
		TextView textDestinoSala = (TextView) findViewById(R.id.text_resultado_destino_sala);
		TextView textDestinoCinta = (TextView) findViewById(R.id.text_resultado_destino_cinta);
		//TextView textCodigoVuelo = (TextView) findViewById(R.id.text_resultado_codigo);
		TextView textCompany = (TextView) findViewById(R.id.text_resultado_company);
		TextView textLayManana = (TextView) findViewById(R.id.text_resultado_vuelo_dia);
		ImageButton btnHoy = (ImageButton) findViewById(R.id.boton_resultado_vuelo_manana);
		favorito = (ImageButton) findViewById(R.id.boton_resultado_favorito);
		botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);
		TextView textEscala = (TextView) findViewById(R.id.text_resultado_escala);

		//Button botonActualizar = (Button) findViewById(R.id.boton_actualizar);
		//TextView textCod = (TextView) findViewById(R.id.text_resultado_codigo);
		Log.w(TAG, "VueloResultadoActivity - setLayout " + dia);

		if(datos.getAeropuertoOrigen().equals("no vuelo")){
			textCompany.setText("No hay vuelos con esos datos");
			text1.setVisibility(View.GONE);
			text2.setVisibility(View.GONE);
			text3.setVisibility(View.GONE);
			text4.setVisibility(View.GONE);
			textEstado.setVisibility(View.GONE);
			layoutManana.setVisibility(View.GONE);
			textDiaOrigen.setVisibility(View.GONE);
			textDiaDestino.setVisibility(View.GONE);
			textDestinoNombre.setVisibility(View.GONE);
			textDestinoHora.setVisibility(View.GONE);
			textDestinoTerminal.setVisibility(View.GONE);
			textDestinoSala.setVisibility(View.GONE);
			textDestinoCinta.setVisibility(View.GONE);
			textCompany.setVisibility(View.GONE);
			textLayManana.setVisibility(View.GONE);
			btnHoy.setVisibility(View.GONE);
			favorito.setVisibility(View.GONE);
			botonAlarma.setVisibility(View.GONE);

		} else {
			if(!dia.equals("no")){
				Log.w(TAG, "VueloResultadoActivity - setLayout " + dia);

				if(dia.equalsIgnoreCase("hoy")){
					Log.w(TAG, "VueloResultadoActivity - setLayout " + dia);
					//layManana = (LinearLayout) findViewById(R.id.layout_resultado_vuelo_boton_manana);
					btnHoy.setVisibility(View.GONE);
					//layManana.setVisibility(View.VISIBLE);
				}else if(dia.equalsIgnoreCase("manana")){
					favorito.setVisibility(View.GONE);
					Log.w(TAG, "VueloResultadoActivity - setLayout " + dia);
					textLayManana.setText("Hoy");
				}
				else{
					layoutManana.setEnabled(false);
					favorito.setEnabled(false);
					favorito.setVisibility(View.GONE);
					layoutManana.setVisibility(View.GONE);
				}
			}else{
				layoutManana.setEnabled(false);
				favorito.setEnabled(false);
				//layoutManana.setVisibility(View.GONE);

			}
			if (alarma){
				botonAlarma.setPressed(true);
				botonAlarma.setSelected(true);
			}

			String escala = datos.getAeropuertoIntermedio();
			if(escala.contains("esti")){
				escala = escala.replace("Destino:", "");
			} else if(escala.contains("rige")){
				escala = escala.replace("Origen:", "");
			}

			if (estado == ONTIME){
				textEstado.setText("En Hora");
				textEstado.setTextColor(Color.argb(255, 00, 150, 33));
				textEstado.setTextSize(17);
				textEstado.setPadding(25, textEstado.getPaddingTop(), textEstado.getPaddingRight(), textEstado.getPaddingBottom());
			} else if(estado == DELAYED){
				textEstado.setText("Retrasado");
				textEstado.setTextColor(Color.argb(255, 255, 99, 33));	
				textEstado.setText(textEstado.getText() + " " +
						((-1)*(getDiferencia(datos.getEstadoVueloOrigen(), datos.getHoraOrigen()))) + " min.");
			} else if (estado == CANCELED){
				textEstado.setText("Cancelado");
				textEstado.setTextColor(Color.RED);
				textEstado.setPadding(25, textEstado.getPaddingTop(), 
						textEstado.getPaddingRight(), textEstado.getPaddingBottom());
				if (datos.getTipo() == DatosVuelo.ESCALANACIONAL){
					textEstado.setText("Desviado");
					textEstado.setTextColor(Color.BLUE);
					/*text2.setText(" (desviado a: " + escala + ")");
					text2.setPadding(0, 4, 0, 0);		*/	
				}
			} 

			Log.d(TAG, "VueloResultadoActivity - setLayout - diferencia: " + 
					getDiferencia(datos.getEstadoVueloOrigen(), datos.getHoraOrigen()));
			Log.d(TAG, "VueloResultadoActivity - setLayout - EstadoVueloOrigen: " + datos.getEstadoVueloOrigen());
			Log.d(TAG, "VueloResultadoActivity - setLayout - horaOrigen: " + datos.getHoraOrigen());

			SpannableString content = new SpannableString("VLC1423  -  " + datos.getNombreCompany());
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			String dest = datos.getAeropuertoDestino();
			String ori = datos.getAeropuertoOrigen();
			if (dest.contains("esti")){
				dest = dest.replace("Destino:", "");
			}

			if(ori.contains("Origen:")){
				ori = ori.replace("Origen:", "");
			}
				

			textCompany.setText(datos.getNombreVuelo() + "  -  " + datos.getNombreCompany());
			text1.setText(ori);
			
				text2.setText(datos.getEstadoVueloOrigen());
			
			text3.setText("Terminal: " + datos.getTerminalOrigen());
			text4.setText("Puerta: " + datos.getPuertaOrigen());
			textDiaOrigen.setText(datos.getFechaOrigen());
			textDiaDestino.setText(datos.getFechaDestino());
			textDestinoNombre.setText(dest);
			textDestinoHora.setText(datos.getEstadoVueloDestino());
			textDestinoTerminal.setText("Terminal: " + datos.getTerminalDestino());
			textDestinoSala.setText("Sala: " + datos.getSalaDestino());
			textDestinoCinta.setText("Cinta: " + datos.getCintaDestino());
			if (datos.getEstadoVueloOrigen().equals("--")){
				text2.setText("No hay datos");
				text2.setPadding(0, 4, 0, 0);			
			}
			if (datos.getEstadoVueloDestino().equals("--")){
				textDestinoHora.setText("No hay datos");
				textDestinoHora.setPadding(0, 2, 0, 0);			
			}
			if(datos.getEscala()){
				textEscala.setVisibility(View.VISIBLE);
				textEscala.setText("Escala: " + escala);
				/*if (datos.getTipo() == DatosVuelo.ESCALANACIONAL){
					text2.setText(datos.getEstadoVueloOrigen() + " (desviado a: " + escala + ")");
				}*/
			}
		}
	}

	/**
	 * onClick del boton de mañana
	 * @param v
	 */
	public void onClickBtnManana(View v){
		Log.d(TAG, "VueloResultadoActivity - onClickBtnManana ");

		Intent intent = new Intent(this, VueloResultadoActivity.class);
		Bundle extras = new Bundle();
		extras.putString("url", datos.getLinkInfoVuelo());

		extras.putString("codigo", "");
		if (or == 1){
			extras.putInt("or", 1);
			Log.i(TAG, "VueloResultadoActivity - onClickBtnManana - OR = 1");

		}else {
			Log.i(TAG, "VueloResultadoActivity - onClickBtnManana - OR = 0");
			extras.putInt("or", 0);
		}
		if(dia.equalsIgnoreCase("hoy")){
			extras.putString("dia", "manana");
		}else{
			extras.putString("dia", "hoy");
		}
		intent.putExtras(extras);
		if(!tieneRed()){
			Toast toast1 = Toast.makeText(this, "Necesitas tener red para poder continuar", Toast.LENGTH_SHORT);
			toast1.show();
		}else{
			Log.d(TAG, "VueloResultadoActivity - onClickBtnManana - antes de startActivity");
			this.startActivity(intent);
		}	
	}

	public void onClickSeguimientoVuelo(View v){
		Log.d(TAG, "VueloResultadoActivity - onClickSeguimiento - getLinkInfoVuelo: " + datos.getLinkInfoVuelo());
		ponerSeguimiento(datos);
	}

	/**
	 * Pone la alarma en la base de datos y la inicializa
	 * @param pUrl
	 */
	public void setAlarma(String pUrl){
		//Creamos el bundle para poder pasar parametros al servicio.
		Bundle bundle = new Bundle();
		bundle.putString("url", pUrl);
		//Creamos el intent necesario para lanzar el servicio y le metemos el bundle.
		Intent intent = new Intent(this, AlarmaService.class);
		intent.putExtras(bundle);
		startService(intent);

	}

	/**
	 * Mira a ver si un vuelo esta en seguimento
	 * @return
	 */
	public boolean getSeguimiento(){
		AlarmasSql alarms =  new AlarmasSql(this); 
		SQLiteDatabase db = alarms.getReadableDatabase();
		Log.d(TAG, "VueloResultadoActivity - getSeguimiento - getLinkInfoVuelo" + datos.getLinkInfoVuelo());

		String[] args = new String[] {"url"};
		//String[] args1 = new String[] {"0"};

		Cursor c = db.query("alarmas", args, null, null, null, null, null);
		//Nos aseguramos de que existe al menos un registro
		if (c.moveToFirst()) {
			//Recorremos el cursor hasta que no haya más registros
			do {
				Log.d(TAG, c.getString(0));
				if(datos.getLinkInfoVuelo().equals(c.getString(0))){
					Log.i(TAG, "VueloResultadoActivity - getSeguimiento - Ya esta en seguimiento");
					return true;
				}
			} while(c.moveToNext());
		}
		db.close();
		return false;
	}

	/**
	 * Mira a ver si el vuelo esta ya en alarmas
	 * @return
	 */
	public boolean getAlarma(){
		AlarmasSqlAux alarmsAux =  new AlarmasSqlAux(this); 
		SQLiteDatabase db2 = alarmsAux.getReadableDatabase();
		Log.d(TAG, "VueloResultadoActivity - getAlarma - getLinkInfoVuelo: " + datos.getLinkInfoVuelo());

		String[] args2 = new String[] {AlarmasSqlAux.NOMBREVUELO, AlarmasSqlAux.FECHAORIGEN, AlarmasSqlAux.ID};
		//String[] args1 = new String[] {"0"};

		Cursor c2 = db2.query("alarmas_aux", args2, null, null, null, null, null);
		//Nos aseguramos de que existe al menos un registro
		if (c2.moveToFirst()) {
			//Recorremos el cursor hasta que no haya más registros
			do {
				Log.d(TAG, "VueloResultadoActivity - getAlarma - c2.getString(0): " + c2.getString(0));
				Log.d(TAG, "VueloResultadoActivity - getAlarma - c2.getString(1): " + c2.getString(0));
				Log.d(TAG, "VueloResultadoActivity - getAlarma - nombreVuelo: " + datos.getNombreVuelo());
				Log.d(TAG, "VueloResultadoActivity - getAlarma - fechaOrigen: " + datos.getFechaOrigen());
				/*if(datos.getLinkInfoVuelo().equals(c2.getString(0))){
					return true;
				}*/
				if (datos.getNombreVuelo().equals(c2.getString(0)) && datos.getFechaOrigen().equals(c2.getString(1))){
					id = c2.getInt(2);
					return true;
				}
			} while(c2.moveToNext());
		}
		db2.close();
		return false;
	}

	/**
	 * Mira si estaba en recientes el vuelo
	 * @param pDatos
	 */
	public void controlReciente(DatosVuelo pDatos){
		if(!pDatos.getAeropuertoDestino().equals("--") && !pDatos.getAeropuertoOrigen().equals("--")){
			BusquedaRecienteSql alarms =  new BusquedaRecienteSql(this); 
			SQLiteDatabase db = alarms.getReadableDatabase();
			String[] args = new String[] {BusquedaRecienteSql.CODIGOVUELO};
			Cursor c = db.query("busqueda_reciente", args, null, null, null, null, null);

			Log.i(TAG, "VueloResultadoActivity - controlReciente ");
			if (c.moveToFirst()) {
				//Recorremos el cursor hasta que no haya más registros
				do {
					Log.d(TAG, c.getString(0));
					if(pDatos.getNombreVuelo().equals(c.getString(0))){
						Log.d(TAG, "VueloResultadoActivity - controlReciente - Ya estabaen recientes");
						reciente = true;
						borrarReciente(pDatos.getNombreVuelo());
					}
				} while(c.moveToNext());
			}
			db.close();
		}
		guardarReciente(pDatos);

	}

	/**
	 * Quita el vuelo de recientes
	 * @param pCodigo
	 */
	public void borrarReciente(String pCodigo){
		BusquedaRecienteSql alarms =  new BusquedaRecienteSql(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		db.execSQL("DELETE FROM busqueda_reciente WHERE "+BusquedaRecienteSql.CODIGOVUELO+ "='" + pCodigo+"'");
		db.close();
	}
	
	/**
	 * Guarda el vuelo en la lista de recientes
	 * @param pDatos
	 */
	public void guardarReciente(DatosVuelo pDatos){
		Log.d(TAG, "VueloResultadoActivity - guardarReciente - inicio" + pDatos.getLinkInfoVuelo());

		if(!pDatos.getAeropuertoDestino().equals("--") && !pDatos.getAeropuertoOrigen().equals("--")){
			BusquedaRecienteSql alarms =  new BusquedaRecienteSql(this); 
			SQLiteDatabase db = alarms.getWritableDatabase();
			Log.d(TAG, "VueloResultadoActivity - guardarReciente urlIsEmpty" + url + ".");
			if(!url.equals(" ")){
				pDatos.setLinkInfoVuelo(url);
			}
			ContentValues cv = new ContentValues();
			cv.put(BusquedaRecienteSql.URL, pDatos.getLinkInfoVuelo());
			cv.put(BusquedaRecienteSql.CODIGOVUELO, pDatos.getNombreVuelo());
			cv.put(BusquedaRecienteSql.HORAORIGEN, pDatos.getHoraOrigen());
			cv.put(BusquedaRecienteSql.FECHAORIGEN, pDatos.getFechaOrigen());
			cv.put(BusquedaRecienteSql.NOMBRECOMPANY, pDatos.getNombreCompany());
			cv.put(BusquedaRecienteSql.HORADESTINO, pDatos.getHoraDestino());
			cv.put(BusquedaRecienteSql.AEROPUERTODESTINO, pDatos.getAeropuertoDestino());
			cv.put(BusquedaRecienteSql.AEROPUERTOORIGEN, pDatos.getAeropuertoOrigen());
			Log.d(TAG, "VueloResultadoActivity - guardarReciente " + pDatos.getLinkInfoVuelo());
			Log.d(TAG, "VueloResultadoActivity - guardarReciente " + url);
			//cv.put(BusquedaRecienteSql.ID, 0);
			//cv.put(BusquedaRecienteSql.CODIGOVUELO, pDatos.getCodigo);
			cv.put(BusquedaRecienteSql.DIA, "hoy");
			db.insert("busqueda_reciente", BusquedaRecienteSql.CODIGOVUELO, cv);
			db.close();
		}
	}

	/**
	 * Pone la alarma (Se le llama desde el onClikAlarma
	 * @param pDatos
	 * @param pTipo
	 */
	public void ponerAlarma(DatosVuelo pDatos, int pTipo){
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		ContentValues cv = new ContentValues();
		
		cv.put(AlarmasSqlAux.URL, pDatos.getLinkInfoVuelo());
		cv.put(AlarmasSqlAux.NOMBREVUELO, pDatos.getNombreVuelo());
		cv.put(AlarmasSqlAux.ALARMA, 1);
		cv.put(AlarmasSqlAux.EMPEZADO, 0);
		cv.put(AlarmasSqlAux.HORAORIGEN, pDatos.getHoraOrigen());
		cv.put(AlarmasSqlAux.FECHAORIGEN, pDatos.getFechaOrigen());
		cv.put(AlarmasSqlAux.NOMBRECOMPANY, pDatos.getNombreCompany());
		cv.put(AlarmasSqlAux.HORADESTINO, pDatos.getHoraDestino());
		cv.put(AlarmasSqlAux.ATERRIZADOSIN, "no");
		cv.put(AlarmasSqlAux.DESPEGADOSIN, "no");
		cv.put(AlarmasSqlAux.SALIDO, "no");
		cv.put(AlarmasSqlAux.SONIDO, 1);
		if (pTipo == 1){
			Log.i(TAG, "VueloResultadoActivity - poner" +
					"Alarma - pTipo = 1");
			cv.put(AlarmasSqlAux.ATERRIZAR, 1);
			cv.put(AlarmasSqlAux.DESPEGAR, 0);
		} else if (pTipo == 2){
			Log.i(TAG, "VueloResultadoActivity - ponerAlarma - pTipo = 2");
			cv.put(AlarmasSqlAux.ATERRIZAR, 0);
			cv.put(AlarmasSqlAux.DESPEGAR, 1);
		} else {
			Log.i(TAG, "VueloResultadoActivity - ponerAlarma - pTipo = 0");
			cv.put(AlarmasSqlAux.ATERRIZAR, 1);
			cv.put(AlarmasSqlAux.DESPEGAR, 0);
		}
		cv.put(AlarmasSqlAux.CAMBIOS, 0);
		cv.put(AlarmasSqlAux.MINUTOS, 0);
		cv.put(AlarmasSqlAux.ESTADOORIGEN, pDatos.getEstadoVueloOrigen());
		cv.put(AlarmasSqlAux.ESTADODESTINO, pDatos.getEstadoVueloDestino());
		cv.put(AlarmasSqlAux.AEROPUERTOORIGEN, pDatos.getAeropuertoOrigen());
		cv.put(AlarmasSqlAux.AEROPUERTODESTINO, pDatos.getAeropuertoDestino());
		cv.put(AlarmasSqlAux.ESTADO, 0);
		cv.put(AlarmasSqlAux.ALARMAVERDAD, 0);

		db.insert("alarmas_aux", AlarmasSqlAux.URL, cv);
		db.close();

		//Creamos el intent necesario para lanzar el servicio y le metemos el bundle.
		Intent intent = new Intent(this, AlarmaService.class);
		startService(intent);
		getAlarma();
	}

	/**
	 * Pone el vuelo en antiguas alarmas
	 * @param pDatos
	 */
	public void ponerSeguimiento(DatosVuelo pDatos){
		AlarmasSql alarms =  new AlarmasSql(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();

		Log.d(TAG, "nombreVuelo: "+pDatos.getNombreVuelo());
		Log.d(TAG,"nombreCompany: "+pDatos.getNombreCompany());
		Log.d(TAG,"aeropuertoOrigen: "+pDatos.getAeropuertoOrigen());
		Log.d(TAG,"fechaOrigen: "+pDatos.getFechaOrigen());
		Log.d(TAG,"horaOrigen: "+pDatos.getHoraOrigen());
		Log.d(TAG,"terminalOrigen: "+pDatos.getTerminalOrigen());
		Log.d(TAG,"puertaOrigen: "+pDatos.getPuertaOrigen());
		Log.d(TAG,"estadoVueloOrigen: "+pDatos.getEstadoVueloOrigen());
		Log.d(TAG,"linkInfoVuelo: "+pDatos.getLinkInfoVuelo());
		Log.d(TAG,"fechaDestino: "+pDatos.getFechaDestino());
		Log.d(TAG,"horaDestino: "+pDatos.getHoraDestino());
		Log.d(TAG,"terminalDestino: "+pDatos.getTerminalDestino());
		Log.d(TAG,"salaDestino: "+pDatos.getSalaDestino());
		Log.d(TAG,"cintaDestino: "+pDatos.getCintaDestino());
		Log.d(TAG,"estadoVueloDestino: "+pDatos.getEstadoVueloDestino());
		Log.d(TAG,"aeropuertoDestino: "+pDatos.getAeropuertoDestino());

		ContentValues cv = new ContentValues();
		cv.put(AlarmasSql.URL, pDatos.getLinkInfoVuelo());
		cv.put(AlarmasSql.ALARMA, 1);
		cv.put(AlarmasSql.EMPEZADO, 0);
		cv.put(AlarmasSql.HORAORIGEN, pDatos.getHoraOrigen());
		cv.put(AlarmasSql.NOMBREVUELO, pDatos.getNombreVuelo());
		cv.put(AlarmasSql.FECHAORIGEN, pDatos.getFechaOrigen());
		cv.put(AlarmasSql.NOMBRECOMPANY, pDatos.getNombreCompany());
		cv.put(AlarmasSql.HORAORIGEN, pDatos.getHoraOrigen());
		cv.put(AlarmasSql.ESTADOORIGEN, pDatos.getEstadoVueloOrigen());
		cv.put(AlarmasSql.ESTADODESTINO, pDatos.getEstadoVueloDestino());
		cv.put(AlarmasSql.HORADESTINO, pDatos.getHoraDestino());
		cv.put(AlarmasSql.AEROPUERTOORIGEN, pDatos.getAeropuertoOrigen());
		cv.put(AlarmasSql.AEROPUERTODESTINO, pDatos.getAeropuertoDestino());



		db.insert("alarmas", AlarmasSql.URL, cv);
		db.close();

		radMarcador.setChecked(true);
		favorito.setPressed(true);

		Log.i(TAG, "VueloResultadoActivity - ponerSeguimiento - Fin de poner seguimiento");
		//getAlarma();

	}

	public void borrarMarcador(DatosVuelo pDatos){
		AlarmasSql alarms =  new AlarmasSql(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		//String[] args = {"*"};
		db.execSQL("DELETE FROM alarmas WHERE "+ AlarmasSql.URL+"='"+pDatos.getLinkInfoVuelo()+"' ");
		db.close();
		radMarcador.setChecked(false);
		favorito.setPressed(false);
		Log.i(TAG, "VueloResultadoActivity - borrarMarcador - Fin de borrar alarma");

	}

	public void borrarAlarma(DatosVuelo pDatos){
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		//String[] args = {"*"};
		//db.execSQL("DELETE FROM alarmas_aux WHERE "+ AlarmasSqlAux.URL+"='"+pDatos.getLinkInfoVuelo()+"' ");
		db.execSQL("DELETE FROM alarmas_aux WHERE "+ AlarmasSqlAux.NOMBREVUELO+"='"+pDatos.getNombreVuelo()+"' AND " 
				+ AlarmasSqlAux.FECHAORIGEN + "='"+pDatos.getFechaOrigen()+"' ");
		db.close();
		ponerSeguimiento(pDatos);
	}

	public void onClickMarcador(View v){
		Log.i(TAG, "VueloResultadoActivity - onClickMarcador");

		if (marcador){
			Log.i(TAG, "VueloResultadoActivity - onClickMarcador - Con seguimiento");
			marcador = false;
			borrarMarcador(datos);
			Log.d(TAG, "VueloResultadoActivity - onClickMarcador - radMarcador: " + radMarcador.isChecked()+"");


			setContentView(R.layout.activity_resultado_vuelo);
			lay = (LinearLayout) findViewById(R.id.layout_progress_vuelo_resultado);
			lay2 = (LinearLayout) findViewById(R.id.layout_vuelo_resultado_sup);
			layAlarmas = (LinearLayout) findViewById(R.id.layout_resultado_alarmas);
			radMarcador = (RadioButton) findViewById(R.id.radio_marcador);
			favorito = (ImageButton) findViewById(R.id.boton_resultado_favorito);
			radMarcador.setChecked(false);
			favorito.setPressed(false);

			Log.d(TAG, "VueloResultadoActivity - onClickMarcador - radMarcador " + radMarcador.isChecked());

			lay.setVisibility(View.GONE);
			lay2.setVisibility(View.VISIBLE);
			layAlarmas.setVisibility(View.VISIBLE);
			setLayout();


		}else{
			Log.i(TAG, "VueloResultadoActivity - onClickMarcador - Sin seguimiento");
			marcador = true;
			ponerSeguimiento(datos);
			Log.i(TAG, radMarcador.isChecked()+"");

			//radMarcador.setChecked(true);
			//favorito.setPressed(true);
			Log.i(TAG, radMarcador.isChecked()+"");
			setContentView(R.layout.activity_resultado_vuelo);
			lay = (LinearLayout) findViewById(R.id.layout_progress_vuelo_resultado);
			lay2 = (LinearLayout) findViewById(R.id.layout_vuelo_resultado_sup);
			layAlarmas = (LinearLayout) findViewById(R.id.layout_resultado_alarmas);

			radMarcador = (RadioButton) findViewById(R.id.radio_marcador);
			favorito = (ImageButton) findViewById(R.id.boton_resultado_favorito);
			radMarcador.setChecked(true);
			favorito.setPressed(true);
			Log.i(TAG, radMarcador.isChecked()+"");
			lay.setVisibility(View.GONE);
			lay2.setVisibility(View.VISIBLE);
			layAlarmas.setVisibility(View.VISIBLE);
			setLayout();
		}
	}


	public void onClickPopupPreferencias(View v) {
		Log.d(TAG, "AlarmasActivity - onClickPopupPreferencias - id: " + id);
		Intent i = new Intent(getApplicationContext(),
				PreferenciasActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		i.putExtras(bundle);
		startActivity(i);
		dw.dismiss();
	}

	public void onClickPopupBorrar(View v) {
		Log.d(TAG, "AlarmasActivity - onClickPopupBorrar - id: " + id);
		borrarAlarma(datos);
		dw.dismiss();
		ponerSeguimiento(datos);
		setContentView(R.layout.activity_resultado_vuelo);
		lay = (LinearLayout) findViewById(R.id.layout_progress_vuelo_resultado);
		lay2 = (LinearLayout) findViewById(R.id.layout_vuelo_resultado_sup);
		layAlarmas = (LinearLayout) findViewById(R.id.layout_resultado_alarmas);
		botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);
		botonAlarma.setPressed(false);
		Log.i(TAG, radAlarma.isChecked()+"");
		lay.setVisibility(View.GONE);
		alarma = false;
		setLayout();
	}

	public void onClickAlarma(View v){

		Log.i(TAG, "onClick Layout Alarma");

		if (alarma){
			getAlarma();
			dw = new DemoPopupWindow(v);
			Log.i(TAG, "VueloResultadoActivity - AlarmasOnLongClickListener - pos");
			botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);

			botonAlarma.setPressed(true);
			botonAlarma.setSelected(true);

			dw.showLikeQuickAction();
			botonAlarma.setPressed(true);
			botonAlarma.setSelected(true);
			dw.setOnDismissListener(new OnDismissListener() {
				public void onDismiss() {
					setLayout();
				}
			});
		} else{
			Log.i(TAG, "onClick Layout Alarma sin seguimiento");
			alarma = true;
			if(datos.getEstadoVueloDestino().equals("--")){
				noAlarma("El aviso de aterrizaje no funcionará");
				ponerAlarma(datos, 2);
				botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);
				botonAlarma.setPressed(true);
				botonAlarma.setSelected(true);
				lay.setVisibility(View.GONE);
				setLayout();
			} else if(datos.getEstadoVueloOrigen().equals("--")){
				noAlarma("El aviso de despege no funcionará");
				ponerAlarma(datos, 1);
				botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);
				botonAlarma.setPressed(true);
				botonAlarma.setSelected(true);
				lay.setVisibility(View.GONE);
				setLayout();
			} else if(datos.getEstadoVueloDestino().equals("--") && 
					datos.getEstadoVueloOrigen().equals("--") || 
					estado == CANCELED){
				noAlarma("No se pueden poner alarma a este vuelo");
			}else {
				ponerAlarma(datos, 0);
				setContentView(R.layout.activity_resultado_vuelo);
				botonAlarma.setPressed(true);
				botonAlarma.setSelected(true);
				
			}
			lay = (LinearLayout) findViewById(R.id.layout_progress_vuelo_resultado);
			lay2 = (LinearLayout) findViewById(R.id.layout_vuelo_resultado_sup);
			layAlarmas = (LinearLayout) findViewById(R.id.layout_resultado_alarmas);
			botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);
			Log.i(TAG, radAlarma.isChecked()+"");
			lay.setVisibility(View.GONE);
			dw = new DemoPopupWindow(v);
			dw.showLikeQuickAction();
			dw.setOnDismissListener(new OnDismissListener() {
				public void onDismiss() {
					setLayout();
				}
			});
			setLayout();
			
		}


	}

	public void onClickActualizar(View v){
		onCreate(bun);
	}

	public void onClickSearch(View v){
		startActivity (new Intent(getApplicationContext(), BusquedaActivity.class));
	}

	public int controlEstado(String pEstado, String pHora){

		if(pEstado.contains("celado")){
			return CANCELED;
		}
		try{
			int dif = getDiferencia(pEstado, pHora);
			Log.d(TAG, "VueloResultadoActivity - controlEstado - diferencia: " + dif);
			if(dif<(-9)){
				return DELAYED;
			}
			else{
				return ONTIME;
			}
		}catch (Exception e){
			return ONTIME;
		}
	}

	public int getDiferencia(String pEstado, String pHora){
		Log.d(TAG, "VueloResultadoActivity - getDiferencia(2) - pEstado: " + pEstado);
		Log.d(TAG, "VueloResultadoActivity - getDiferencia(2) - pHora: " + pHora);

		if(pEstado.equals("--")|| !(pEstado.contains("las"))){
			return 0;
		}
		else if (pHora.equals("--")){
			return 0;
		}
		String[] horaVuelo = pEstado.substring(pEstado.indexOf("a las ")+6).split(":");
		String[] horaPrevista = pHora.split(":");
		if (horaVuelo[0].contains(" ") || horaVuelo[0].contains("n") || horaVuelo[0].contains("d")){
			return 0;
		}
		int minutos = 0;
		Log.d(TAG, "VueloResultadoActivity - getDiferencia(2) - hora[0]: " + horaPrevista[0]);
		Log.d(TAG, "VueloResultadoActivity - getDiferencia(2) - horavuelo[0]: " + horaVuelo[0]);

		minutos += (((Integer.parseInt(horaPrevista[0])) - (Integer.parseInt(horaVuelo[0]))))*60;
		Log.d(TAG, "VueloResultadoActivity - getDiferencia(2) - mins: " + minutos);
		minutos += (((Integer.parseInt(horaPrevista[1])) - (Integer.parseInt(horaVuelo[1]))));
		Log.d(TAG, "VueloResultadoActivity - getDiferencia(2) - minutos de diferencia: "+ minutos);
		return (minutos);

	}

	protected void onResume(){
		onCreate(bun);
	}

	private static class DemoPopupWindow extends MenuContextual {
		public DemoPopupWindow(View anchor) {
			super(anchor);
		}
		@Override
		protected void onCreate() {
			// inflate layout
			Log.i(TAG, "AlarmasActivity - Popup - onCreate");

			LayoutInflater inflater = (LayoutInflater) this.anchor.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			ViewGroup root = (ViewGroup) inflater.inflate(
					R.layout.popup_grid_layout_reducido, null);

			for (int i = 0, icount = root.getChildCount(); i < icount; i++) {
				View v = root.getChildAt(i);
				if (v instanceof TableRow) {
					TableRow row = (TableRow) v;
				}
			}
			this.setContentView(root);
		}
	}
}
