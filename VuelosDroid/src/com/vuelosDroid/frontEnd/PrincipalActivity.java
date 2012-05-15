package com.vuelosDroid.frontEnd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.vuelosDroid.R;
import com.vuelosDroid.backEnd.behind.AlarmaService;
import com.vuelosDroid.backEnd.scrapper.airportsUpdater.AirportUpdater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


/**
 * 
 * @author Xabi
 *
 */
public class PrincipalActivity extends AbstractActivity{

	public static boolean ORIGENES = true;
	public static boolean DESTINOS = false;
	boolean sdDisponible = false;
	boolean sdAccesoEscritura = false;
	Context context;

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
		context = this;
		controlEstado();
	}

	private void controlEstado(){
		String estado = Environment.getExternalStorageState();

		if (estado.equals(Environment.MEDIA_MOUNTED)) {
			sdDisponible = true;
			sdAccesoEscritura = true;
		} else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			sdDisponible = true;
			sdAccesoEscritura = false;
		} else {
			sdDisponible = false;
			sdAccesoEscritura = false;
		}

		try	{
			InputStreamReader inp = new InputStreamReader(openFileInput("origendestinos.txt"));
			BufferedReader fin = new BufferedReader(inp);
			fin.readLine();
			fin.close();
		} catch (Exception ex) {
			Log.e(TAG, "PrincipalActivity - controlEstado - Error al leer fichero desde memoria interna");
			crearFicheros();
		}
		/*if(sdAccesoEscritura && sdDisponible){
			try {
				File ruta_sd = Environment.getExternalStorageDirectory();
				File f = new File(ruta_sd.getAbsolutePath(), "origenesdestinos.txt");
				if (!f.exists()){
					crearFicheros();
				}
			}
			catch (Exception ex) {
				Log.e("Ficheros", "Error al leer fichero desde tarjeta SD");
				crearFicheros();
			}
		}*/
	}

	private void crearFicheros(){
		try {
			InputStream fraw = getResources().openRawResource(R.raw.origendestinos);
			BufferedReader brin = new BufferedReader(new InputStreamReader(fraw));
			String linea = brin.readLine();
			OutputStreamWriter fout= new OutputStreamWriter(openFileOutput("origendestinos.txt", Context.MODE_PRIVATE));
			fout.write(linea);
			fout.close();
			brin.close();
			Log.i(TAG, "PrincipalActivity - crearFicheros - Origen - Fichero origenDestinos creado");
		} catch (Exception ex) {
			Log.e(TAG, "PrincipalActivity - crearFicheros - Origen - Error al escribir fichero a memoria interna");
		}try {
			InputStream fraw = getResources().openRawResource(R.raw.destinoorigenes);
			BufferedReader brin = new BufferedReader(new InputStreamReader(fraw));
			String linea = brin.readLine();
			OutputStreamWriter fout = new OutputStreamWriter(openFileOutput("destinoorigenes.txt", Context.MODE_PRIVATE));
			fout.write(linea);
			fout.close();
			brin.close();
			Log.i(TAG, "PrincipalActivity - crearFicheros - Destino - Fichero destinosOrigenes creado");

		} catch (Exception ex) {
			Log.e(TAG, "PrincipalActivity - crearFicheros - Destino - Error al escribir fichero a memoria interna");
		}

		/*try {
			InputStream fraw = getResources().openRawResource(R.raw.origendestinos);
			BufferedReader brin = new BufferedReader(new InputStreamReader(fraw));
			String linea = brin.readLine();
			if(sdAccesoEscritura && sdDisponible){
				File ruta_sd = Environment.getExternalStorageDirectory();
				File f = new File(ruta_sd.getAbsolutePath(), "origenesdestinos.txt");
				OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(f));
				fout.write(linea);
				fout.close();
			}
			fraw.close();
		} catch (Exception ex) {
			Log.e("Ficheros", "Error al leer fichero desde recurso raw");} 

		try {
			InputStream fraw = getResources().openRawResource(R.raw.destinoorigenes);
			BufferedReader brin = new BufferedReader(new InputStreamReader(fraw));
			String linea = brin.readLine();
			if(sdAccesoEscritura && sdDisponible){
				File ruta_sd = Environment.getExternalStorageDirectory();
				File f = new File(ruta_sd.getAbsolutePath(), "destinosorigenes.txt");
				OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(f));
				fout.write(linea);
				fout.close();
			}
			fraw.close();
		} catch (Exception ex) {
			Log.e("Ficheros", "Error al escribir fichero a tarjeta SD");
		}*/
	}

	/**
	 * Onclick para el boton de actualizar los aeropuertos.
	 * @param v
	 */
	public void onClickActualizarAeropuertos(View v){
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage("Esta operación puede tardar varios minutos (Podrás seguir utilizando la aplicación)");
		alertbox.setTitle("Actualizar Aeropuertos");
		alertbox.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				if(tieneRed()){
					actualizarAeropuertos(ORIGENES);
				} else {
					Toast toast = Toast.makeText(context, "Necesitas internet para actualizar.", Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});

		alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {

			}
		});
		alertbox.show();
	}

	/**
	 * 
	 * @param pTipo 
	 * False para destinos, true para origenes
	 * 
	 */
	private void actualizarAeropuertos(final boolean pTipo){
		Log.i(TAG, "PrincipalActivity - actualizarAeropuertos - Dentro de actualizar principio");
		if (pTipo == ORIGENES){
			Toast toast = Toast.makeText(context, "Actualizando Aeropuertos.", Toast.LENGTH_SHORT);
			toast.show();
		}
		new Thread(new Runnable(){
			public void run() {

				AirportUpdater ae = new AirportUpdater();
				String datos = ae.obtenerAeropuertos(pTipo);
				Log.i(TAG, "PrincipalActivity - actualizarAeropuertos -  Dentro del new Thread");
				Message msg = actualizarHandler.obtainMessage();
				msg.obj = datos;
				if (pTipo == ORIGENES){
					msg.arg1 = 1;
				} else {
					msg.arg1 = 0;
				}

				//msg.obj = getInfoUnVuelo("", pUrl);
				Log.i(TAG, "PrincipalActivity - actualizarAeropuertos - Antes de mandar el mensaje");
				//progressDialog = ProgressDialog.show(cont, "", "Por favor espere mientras se cargan los datos...", true);
				actualizarHandler.sendMessage(msg);
			}}).start();
	}

	private final Handler actualizarHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.i(TAG, "PrincipalActivity - actualizarHandler - Principio del Handler");
			if (msg.obj != null){
				if (msg.arg1 == 1){ 
					Toast toast = Toast.makeText(context, "Fase 1 de 2 terminada.", Toast.LENGTH_SHORT);
					toast.show();
					String datos = (String) msg.obj;
					Log.d(TAG, "PrincipalActivity - actualizarHandler - Origenes - obj: " + datos);

					try {
						OutputStreamWriter fout = new OutputStreamWriter(openFileOutput("origendestinos.txt", Context.MODE_PRIVATE));
						fout.write(datos);
						fout.close();
					} catch (Exception ex) {
						Log.e("Ficheros", "PrincipalActivity - actualizarHandler - Origenes  - Error al escribir fichero a tarjeta SD");
					}
					actualizarAeropuertos(DESTINOS);

				} else{
					Toast toast = Toast.makeText(context, "Actualización terminada", Toast.LENGTH_LONG);
					toast.show();
					String datos = (String) msg.obj;
					Log.d(TAG, "PrincipalActivity - actualizarHandler - Destinos - obj: " + datos);

					try {
						OutputStreamWriter fout = new OutputStreamWriter(openFileOutput("destinoorigenes.txt", Context.MODE_PRIVATE));
						fout.write(datos);
						fout.close();
					} catch (Exception ex) {
						Log.e("Ficheros", "PrincipalActivity - actualizarHandler - Destinos - Error al escribir fichero a tarjeta SD");
					}
				} 
			} else{
			Log.w(TAG, "Dentro del Handler NUll " + msg.obj);
		}
	}
};


} 