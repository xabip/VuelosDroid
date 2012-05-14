package com.vuelosDroid.frontEnd;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
 * 
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

	boolean alarma = false;
	boolean marcador = false;
	//private ProgressDialog progressDialog;


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

	private int estdado;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("onCreate VueloResultadoActivity");
		bun = savedInstanceState;
		cont = getApplicationContext();
		setContentView(R.layout.activity_resultado_vuelo);
		lay = (LinearLayout) findViewById(R.id.layout_progress_vuelo_resultado);
		lay2 = (LinearLayout) findViewById(R.id.layout_vuelo_resultado_sup);
		layMarcador = (LinearLayout) findViewById(R.id.layout_marcador);
		//		layManana = (LinearLayout) findViewById(R.id.layout_resultado_vuelo_boton_manana);
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
			if(!url.equals(" " )){
				GregorianCalendar cal = new GregorianCalendar();
				if (cal.get(Calendar.HOUR_OF_DAY) < 2 && !dia.equals("manana") && or == 1){
					Log.i(TAG, "VueloResultadoActivity - Oncreate - Antes de cambiar url - diahoy");
					url = cambiarFechaToUrl(url, "ayer");
				}else if (cal.get(Calendar.HOUR_OF_DAY) < 2 && dia.equals("manana") && or == 1){
					Log.i(TAG, "VueloResultadoActivity - Oncreate - Antes de cambiar url - diamanana");
					url = cambiarFechaToUrl(url, "hoy");
				}else{
					url = cambiarFechaToUrl(url, dia);
				}
			}
			Log.d(TAG, "VueloResultadoActivity - Oncreate - Despues de cambiar url - dia: " + dia);

			Log.d(TAG, "VueloResultadoActivity - Oncreate - Despues de cambiar url - url: " + url);

		}
		Log.e(TAG, "VueloResultadoActivity - onCreate - url: " + url);
		if(!RED){
			Toast toast1 = Toast.makeText(getApplicationContext(), "No hay red.", Toast.LENGTH_SHORT);
			lay.setVisibility(View.GONE);
			toast1.show();

		}else{
			controlOperaciones();
		}	
	}  

	/**
	 * 
	 */
	private final Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.i(TAG, "VueloResultadoActivity - progressHandler - Principio del Handler");
			if (msg.obj != null) {
				Log.i(TAG, "VueloResultadoActivity - progressHandler - Dentro del Handler");
				datos = (DatosVuelo)msg.obj;
				Log.e(TAG, "Final del handler"+ datos.getAeropuertoDestino());
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

	public void controlOperaciones(){
		if (url.equals(" ")){
			Log.d(TAG, "VueloResultadoActivity - controlOperaciones - url: " + url);
			codigo = bundle.getString("codigo");
			dia = bundle.getString("dia");
			Log.d(TAG, "VueloResultadoActivity - controlOperaciones - codigo y dia: " +codigo+dia);
			if(dia.equals("Mañana")){
				dia = "manana";
			}
			try {
				datos=getInfoUnVuelo("", codigo.toUpperCase(), dia.toLowerCase());
				Log.i(TAG, "VueloResultadoActivity - controlOperaciones - despues de la llamada al codigo");
				//progressDialog.dismiss();
				lay.setVisibility(View.GONE);
				lay2.setVisibility(View.VISIBLE);
				layAlarmas.setVisibility(View.VISIBLE);
				controlEstado(url);
				dia = bundle.getString("dia");
				Log.d(TAG, "VueloResultadoActivity - controlOperaciones - try - dia: " + dia);
				estado = controlEstado(datos.getEstadoVueloOrigen(), datos.getHoraOrigen());
				Log.d(TAG, "VueloResultadoAtivity - controlOperaciones - try - estado: " + estado);
				setLayout();
				controlReciente(datos);
				guardarReciente(datos);
			} catch (MoreFlightsException e) {
				//e.printStackTrace();
				Log.e(TAG, "VueloResultadoActivity - controlOperaciones - MoreFlightsException");
				Intent intent = new Intent(this, ResultadoActivity.class);
				Bundle extras = new Bundle();
				extras.putString("codigo", codigo);
				extras.putString("dia", dia);
				extras.putString("url", "");
				extras.putString("origen", "");
				extras.putString("destino", "");
				extras.putString("horario", "");
				extras.putString("tipo", "codigo");
				intent.putExtras(extras);
				this.startActivity(intent);
			} catch (NoHayVueloException e){
				Log.e(TAG, "VueloResultadoActivity - controlOperaciones - No hay vuelo Exception " + e.getMessage());
			}

			//setLayout();
		}else {
			Log.i(TAG, "VueloResultadoActivity - controlOperaciones - Con url");
			//setLayout();

			loadData(url);
			//datos = getInfoUnVuelo("", url);
			//controlEstado(url);
		}
	}

	public void controlEstado(String pUrl){
		alarma = getAlarma();
		marcador = false;
		//marcador = getSeguimiento();	
	}

	private void noAlarma(){
		Toast toast1 = Toast.makeText(getApplicationContext(), "No se pueden poner alarma a este vuelo", Toast.LENGTH_SHORT);
		toast1.show();
	}

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

		//Button botonActualizar = (Button) findViewById(R.id.boton_actualizar);
		//TextView textCod = (TextView) findViewById(R.id.text_resultado_codigo);
		Log.w(TAG, "VueloResultadoActivity - setLayout " + dia);

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

		if (estado == ONTIME){
			textEstado.setText("En Hora");
			textEstado.setTextColor(Color.argb(255, 00, 150, 33));
			textEstado.setTextSize(17);
		} else if(estado == DELAYED){
			textEstado.setText("Retrasado");
			textEstado.setTextColor(Color.argb(255, 255, 99, 33));

		} else if (estado == CANCELED){
			textEstado.setText("Cancelado");
			textEstado.setTextColor(Color.RED);

		}

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


		//textCompany.setText(datos.getNombreCompany());
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
		//textCod.setText(datos.)
		Log.i(TAG, "VueloResultadoActivity - setLayout - radAlarma: " + radAlarma.isChecked()+"");
		Log.i(TAG, "VueloResultadoActivity - setLayout - radMarcador: " + radMarcador.isChecked()+"");
	}

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

	public void onClickAlarmaVuelo(View v){
		Log.e(TAG, "VueloResultadoActivity - onClickAlarmaVuelo - getLinkInfoVuelo: " + datos.getLinkInfoVuelo());
		if(datos.getEstadoVueloDestino().equals("--")){
			noAlarma();
		} else {
			ponerAlarma(datos);
		}
	}

	public void setAlarma(String pUrl){
		//Creamos el bundle para poder pasar parametros al servicio.
		//String aaa = "http://www.aena-aeropuertos.es/csee/Satellite/infovuelos/es/Detalle.html?accion=detalle&company_code=IBE&dia=20120307&hora_prev=2012-03-07+21%3A40&ncia=IBE&numBusqueda=IBE0425&nvuelo=0425&ordenacionBack=hprevisto&origin=BIO&originBack=BIO&originBusqueda=BIO&strVuelo=IBE0425";
		Bundle bundle = new Bundle();
		bundle.putString("url", pUrl);
		//Creamos el intent necesario para lanzar el servicio y le metemos el bundle.
		Intent intent = new Intent(this, AlarmaService.class);
		intent.putExtras(bundle);
		startService(intent);

	}

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

	public boolean getAlarma(){
		AlarmasSqlAux alarmsAux =  new AlarmasSqlAux(this); 
		SQLiteDatabase db2 = alarmsAux.getReadableDatabase();
		Log.d(TAG, "VueloResultadoActivity - getAlarma - getLinkInfoVuelo: " + datos.getLinkInfoVuelo());

		String[] args2 = new String[] {AlarmasSqlAux.NOMBREVUELO, AlarmasSqlAux.FECHAORIGEN};
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
					return true;
				}
			} while(c2.moveToNext());
		}
		db2.close();
		return false;
	}

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

	public void borrarReciente(String pCodigo){
		BusquedaRecienteSql alarms =  new BusquedaRecienteSql(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		db.execSQL("DELETE FROM busqueda_reciente WHERE "+BusquedaRecienteSql.CODIGOVUELO+ "='" + pCodigo+"'");
		db.close();
	}

	public void guardarReciente(DatosVuelo pDatos){
		if(!pDatos.getAeropuertoDestino().equals("--") && !pDatos.getAeropuertoOrigen().equals("--")){
			BusquedaRecienteSql alarms =  new BusquedaRecienteSql(this); 
			SQLiteDatabase db = alarms.getWritableDatabase();
			pDatos.setLinkInfoVuelo(url);
			ContentValues cv = new ContentValues();
			cv.put(BusquedaRecienteSql.URL, pDatos.getLinkInfoVuelo());
			cv.put(BusquedaRecienteSql.CODIGOVUELO, pDatos.getNombreVuelo());
			cv.put(BusquedaRecienteSql.HORAORIGEN, pDatos.getHoraOrigen());
			cv.put(BusquedaRecienteSql.FECHAORIGEN, pDatos.getFechaOrigen());
			cv.put(BusquedaRecienteSql.NOMBRECOMPANY, pDatos.getNombreCompany());
			cv.put(BusquedaRecienteSql.HORADESTINO, pDatos.getHoraDestino());
			cv.put(BusquedaRecienteSql.AEROPUERTODESTINO, pDatos.getAeropuertoDestino());
			cv.put(BusquedaRecienteSql.AEROPUERTOORIGEN, pDatos.getAeropuertoOrigen());
			Log.w(TAG, "VueloResultadoActivity - guardarReciente " + pDatos.getLinkInfoVuelo());
			Log.w(TAG, "VueloResultadoActivity - guardarReciente " + url);
			//cv.put(BusquedaRecienteSql.ID, 0);
			//cv.put(BusquedaRecienteSql.CODIGOVUELO, pDatos.getCodigo);
			cv.put(BusquedaRecienteSql.DIA, "hoy");
			db.insert("busqueda_reciente", BusquedaRecienteSql.CODIGOVUELO, cv);
			db.close();
		}
	}

	public void ponerAlarma(DatosVuelo pDatos){
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
		cv.put(AlarmasSqlAux.ATERRIZAR, 1);
		cv.put(AlarmasSqlAux.DESPEGAR, 0);
		cv.put(AlarmasSqlAux.CAMBIOS, 0);
		cv.put(AlarmasSqlAux.MINUTOS, 0);
		cv.put(AlarmasSqlAux.ESTADOORIGEN, pDatos.getEstadoVueloOrigen());
		cv.put(AlarmasSqlAux.ESTADODESTINO, pDatos.getEstadoVueloDestino());
		cv.put(AlarmasSqlAux.AEROPUERTOORIGEN, pDatos.getAeropuertoOrigen());
		cv.put(AlarmasSqlAux.AEROPUERTODESTINO, pDatos.getAeropuertoDestino());


		db.insert("alarmas_aux", AlarmasSqlAux.URL, cv);
		db.close();
		radAlarma.setChecked(true);
		botonAlarma.setPressed(true);
		botonAlarma.setSelected(true);

		//Creamos el intent necesario para lanzar el servicio y le metemos el bundle.
		Intent intent = new Intent(this, AlarmaService.class);
		startService(intent);
	}

	public void borrarAlarma(){
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		db.execSQL("DELETE * FROM alarmas_aux");
		db.close();
		radAlarma.setChecked(false);
		botonAlarma.setPressed(false);


	}

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
		String[] args = {"*"};
		db.execSQL("DELETE FROM alarmas WHERE "+ AlarmasSql.URL+"='"+pDatos.getLinkInfoVuelo()+"' ");
		db.close();
		radMarcador.setChecked(false);
		favorito.setPressed(false);
		Log.i(TAG, "VueloResultadoActivity - borrarMarcador - Fin de borrar alarma");

	}

	public void borrarAlarma(DatosVuelo pDatos){
		AlarmasSqlAux alarms =  new AlarmasSqlAux(this); 
		SQLiteDatabase db = alarms.getWritableDatabase();
		String[] args = {"*"};
		db.execSQL("DELETE FROM alarmas_aux WHERE "+ AlarmasSqlAux.URL+"='"+pDatos.getLinkInfoVuelo()+"' ");
		db.execSQL("DELETE FROM alarmas_aux WHERE "+ AlarmasSqlAux.NOMBREVUELO+"='"+pDatos.getNombreVuelo()+"' AND " 
				+ AlarmasSqlAux.FECHAORIGEN + "='"+pDatos.getFechaOrigen()+"' ");
		db.close();
		radAlarma.setChecked(false);
		ponerSeguimiento(pDatos);
		//botonAlarma.setPressed(false);
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

	public void onClickAlarma(View v){
		Log.i(TAG, "onClick Layout Alarma");
		if(datos.getEstadoVueloDestino().equals("--")){
			Toast toast1 = Toast.makeText(getApplicationContext(), "No funcionará el aviso de aterrizaje", Toast.LENGTH_SHORT);
			toast1.show();
		}
		if(datos.getEstadoVueloOrigen().equals("--")){
			Toast toast1 = Toast.makeText(getApplicationContext(), "No funcionará el aviso de despege", Toast.LENGTH_SHORT);
			toast1.show();
		}
		if(datos.getEstadoVueloDestino().equals("--") && datos.getEstadoVueloOrigen().equals("--")){
			Toast toast1 = Toast.makeText(getApplicationContext(), "No se puede poner la alarma", Toast.LENGTH_SHORT);
			toast1.show();
		}else{
			if (alarma){
				Log.i(TAG, "onClick Layout Alarma en ella");
				alarma = false;
				borrarAlarma(datos);
				Log.i(TAG, radAlarma.isChecked()+"");

				setContentView(R.layout.activity_resultado_vuelo);
				lay = (LinearLayout) findViewById(R.id.layout_progress_vuelo_resultado);
				lay2 = (LinearLayout) findViewById(R.id.layout_vuelo_resultado_sup);
				layAlarmas = (LinearLayout) findViewById(R.id.layout_resultado_alarmas);

				radAlarma = (RadioButton) findViewById(R.id.radio_alarma);
				botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);
				//radAlarma.setChecked(false);
				botonAlarma.setPressed(false);
				Log.i(TAG, radAlarma.isChecked()+"");

				lay.setVisibility(View.GONE);
				//lay2.setVisibility(View.VISIBLE);
				//layAlarmas.setVisibility(View.VISIBLE);
				setLayout();


			}else{
				Log.i(TAG, "onClick Layout Alarma sin seguimiento");
				alarma = true;
				ponerAlarma(datos);
				Log.i(TAG, radMarcador.isChecked()+"");
				//radAlarma.setChecked(true);
				//botonAlarma.setPressed(true);
				Log.i(TAG, radAlarma.isChecked()+"");
				setContentView(R.layout.activity_resultado_vuelo);
				lay = (LinearLayout) findViewById(R.id.layout_progress_vuelo_resultado);
				lay2 = (LinearLayout) findViewById(R.id.layout_vuelo_resultado_sup);
				layAlarmas = (LinearLayout) findViewById(R.id.layout_resultado_alarmas);
				//radAlarma = (RadioButton) findViewById(R.id.radio_alarma);
				botonAlarma = (ImageButton) findViewById(R.id.boton_resultado_alarma);
				botonAlarma.setPressed(true);
				botonAlarma.setSelected(true);
				//radAlarma.setChecked(true);
				Log.i(TAG, radAlarma.isChecked()+"");
				lay.setVisibility(View.GONE);
				//lay2.setVisibility(View.VISIBLE);
				//layAlarmas.setVisibility(View.VISIBLE);
				setLayout();
			}
		}

	}

	public void onClickActualizar(View v){
		onCreate(bun);
	}

	public void onClickSearch(View v){

	}

	public int controlEstado(String pEstado, String pHora){
		/*if(pEstado.contains("egado")){
			return DESPEGADO;
		}
		if(pEstado.contains("rrizado")){
			return ATERRIZADO;
		}*/
		if(pEstado.contains("celado")){
			return CANCELED;
		}
		try{
			int dif = getDiferencia(pEstado, pHora);
			Log.d(TAG, "VueloResultadoActivity - controlEstado - diferencia: " + dif);
			if(dif>10){
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

		if(pEstado.equals("--")|| !(pEstado.contains("a las "))){
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
		minutos += (((Integer.parseInt(horaPrevista[0])) - (Integer.parseInt(horaVuelo[0]))))*60;
		Log.d(TAG, "VueloResultadoActivity - getDiferencia(2) - mins: " + minutos);
		minutos += (((Integer.parseInt(horaPrevista[1])) - (Integer.parseInt(horaVuelo[1]))));
		Log.d(TAG, "VueloResultadoActivity - getDiferencia(2) - minutos de diferencia: "+ minutos);
		return (0);

	}

	protected void onResume(){
		onCreate(bun);
	}

}
