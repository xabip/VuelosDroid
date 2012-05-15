
package com.vuelosDroid.frontEnd;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.vuelosDroid.backEnd.scrapper.DatosGroup;
import com.vuelosDroid.backEnd.scrapper.DatosVuelo;
import com.vuelosDroid.backEnd.scrapper.MoreFlightsException;
import com.vuelosDroid.backEnd.scrapper.NoHayMasPaginasDeVuelosException;
import com.vuelosDroid.backEnd.scrapper.NoHayVueloException;
import com.vuelosDroid.backEnd.scrapper.VuelosJSoup;

public class ResultadosAbstractActivity extends AbstractActivity {

	private String horaFormat;
	private Date horaActual;
	private VuelosJSoup vuelosJsoup;
	SharedPreferences prefer;
	private int tamano;
	private GregorianCalendar cal;

	public String[] dias = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", 
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};

	public String[] meses = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};


	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.i(TAG, "ResultadosAbstractActivity - OnCreate");
		vuelosJsoup = new VuelosJSoup();
		prefer = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
		tamano = prefer.getInt("tamano", 10);
		cal = new GregorianCalendar();
	}


	public void formatearFecha(String dia){
		GregorianCalendar calen = new GregorianCalendar();
		horaActual = new Date();
		if (dia.equals("hoy")){
			horaFormat = (dias[cal.get(Calendar.DAY_OF_MONTH)]+"/"+
					(meses[cal.get(Calendar.MONTH)])+"/"+
					(cal.get(Calendar.YEAR)-2000));
		}else if (dia.equals("manana")){
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)+1);
			if(cal.get(Calendar.DAY_OF_MONTH) == 1){
				cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH)+1);
			}else{
				horaFormat = (dias[cal.get(Calendar.DAY_OF_MONTH)]+"/"+
						(meses[cal.get(Calendar.MONTH)])+"/"+
						(cal.get(Calendar.YEAR)-2000));
			}
		}else if (dia.equals("ayer")){
			cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)-1);
			if(cal.get(Calendar.DAY_OF_MONTH) == 0){
				cal.set(calen.get(Calendar.YEAR), calen.get(Calendar.MONTH), calen.get(Calendar.DAY_OF_MONTH));
			}
			horaFormat = (dias[cal.get(Calendar.DAY_OF_MONTH)]+"/"+
					(meses[cal.get(Calendar.MONTH)])+"/"+
					(cal.get(Calendar.YEAR)-2000));

		}
	}

	public void formatearFecha2(String dia){
		if (dia.equals("hoy")){
			horaFormat = ((horaActual.getYear()-100)+2000)+"0"+
					(horaActual.getMonth()+1)+""+
					(horaActual.getDate());
		}else if (dia.equals("manana")){
			horaFormat =((horaActual.getYear()-100)+2000)+"0"+
					(horaActual.getMonth()+1)+""+
					(horaActual.getDate()+1);
		}
	}

	public DatosVuelo getInfoUnVuelo(String in, String cod, String dia)  throws MoreFlightsException, NoHayVueloException{
		Log.d(TAG, "ResultadoAbstractActivity - getInfoUnVuelo - Busqueda codigo: in=" + in + " codigo: " +cod+ " dia: "+dia);
		//formatearFecha2(dia);
		DatosVuelo datoVue = new DatosVuelo();
		Log.i(TAG, "ResultadoAbstractActivity - getInfoUnVuelo - pasa");
		formatearFecha(dia);
		Log.d(TAG, "ResultadoAbstractActivity - getInfoUnVuelo - pasa2 - horaFormat: "+horaFormat);
		//Gson gson = new Gson();
		try {
			datoVue = vuelosJsoup.getDatosVuelo(cod,horaFormat);
			Log.d(TAG, "ResultadoAbstractActivity - getInfoUnVuelo - datos..: "+ datoVue.getAeropuertoDestino()+" "+datoVue.getAeropuertoOrigen()+
					datoVue.getNombreVuelo()+ datoVue.getTerminalOrigen()+ datoVue.getEstadoVueloDestino());

		} catch (NoHayVueloException e) {
			Log.e(TAG, "ResultadoAbstractActivity - getInfoUnVuelo - No hay Vuelos con esos parámetros " + e.getMessage());
			throw e;
		}catch(IOException ex1){
			Log.e(TAG, "ResultadoAbstractActivity - getInfoUnVuelo - IOException: " + ex1.getMessage());
			System.out.println("error no hay conexion a internet");

		}catch(MoreFlightsException ex1){
			Log.e(TAG, "ResultadoAbstractActivity - getInfoUnVuelo - MoreFligthsException: " + ex1.getMessage());
			System.out.println("error no hay conexion a internet");
			throw ex1;
		} catch (Exception e) {
			Log.e(TAG, "ResultadoAbstractActivity - getInfoUnVuelo - generalException: " + e.getMessage());
		}
		return datoVue;
	}

	public DatosVuelo getInfoUnVuelo(String in, String link){
		Log.d(TAG, "ResultadoAbstractActivity - getInfoUnVueloCod: in=" + in + " link: " +link);
		DatosVuelo datoVue = new DatosVuelo();
		Log.i(TAG, "ResultadoAbstractActivity - getInfoUnVueloCod - pasa");
		Log.d(TAG, "ResultadoAbstractActivity - getInfoUnVueloCod - horaFormat: "+horaFormat);
		try {
			datoVue = vuelosJsoup.getDatosVuelo(link);

			Log.i(TAG, "ResultadoAbstractActivity - getInfoUnVueloCod - datoVue"+ datoVue.getAeropuertoDestino()+" "+datoVue.getAeropuertoOrigen()+
					datoVue.getNombreVuelo()+ datoVue.getTerminalOrigen()
					+datoVue.getEstadoVueloDestino());


		} catch (NoHayVueloException e) {
			Log.e(TAG, "ResultadoAbstractActivity - getInfoUnVueloCod - No hay Vuelos con esos parámetros " + e.getMessage());
			System.out.println("No hay Vuelos con esos parámetros");
		}catch(IOException ex1){
			Log.e(TAG, "ResultadoAbstractActivity - getInfoUnVueloCod - IOException: " + ex1.getMessage());
			System.out.println("error no hay conexion a internet");

		} catch (Exception e) {
			Log.e(TAG, "ResultadoAbstractActivity - getInfoUnVueloCod - generalException: " + e.getMessage());
			Log.e(TAG, "Excepcion "+e.toString());
		}
		return datoVue;
	}

	public DatosGroup getInfoMasVuelos(String pCod, String pDia) throws NoHayVueloException{
		DatosGroup listaVuelos = new DatosGroup();
		Log.d(TAG, "ResultadoAbstractActivity -  getInfoMasVuelos - antes de buscar. "+ pCod + pDia);

		formatearFecha(pDia);
		try {
			Log.d(TAG, "ResultadoAbstractActivity - getInfoMasVuelos  - antes de buscar:  "+ pCod + horaFormat);
			listaVuelos.setValues(vuelosJsoup.recorrerMultiplesVuelos(pCod, horaFormat));
		} catch (NoHayVueloException e){
			Log.e(TAG, "ResultadosAbstractActivity - getInfoMasVuelos - No hay vuelo " + e.getMessage());
			throw e;
		}
		catch (Exception e) {
			Log.e(TAG, "ResultadosAbstractActivity - excepcion al buscar " + e.getMessage());
		}
		return listaVuelos;
	}

	public DatosGroup getInfoVuelos(String destino, String origen, String horario, String dia, String compania, String tipo) 
			throws NoHayMasPaginasDeVuelosException{
		Log.d(TAG, "ResultadoAbstractActivity - getInfoVuelos  - Todos los vuelos: destino: " + destino + " origen: " + origen + " horario: " + horario + 
				" dia: " + dia +  " compañia: "+ compania);
		formatearFecha(dia);
		Log.d(TAG, "ResultadoAbstractActivity - getInfoVuelos - Todos los vuelos: destino: " + destino + " origen: " + origen + " horario: "+ horario + 
				" dia: " + horaFormat+  " compañia: "+ compania);

		DatosGroup listaVuelos = new DatosGroup();

		if (destino.equals("No seleccionado")){
			destino="";
			Log.v(TAG, "Todos los vuelos: No seleccionado");
		}
		try {
			if (tipo.contains("Origen")){
				listaVuelos.setValues(vuelosJsoup.getListaVuelosSalidas(destino, origen, horaFormat, horario, ""));
			}
			else{
				listaVuelos.setValues(vuelosJsoup.getListaVuelosLlegadas(destino, origen , horaFormat, horario, ""));
			}
			//Log.v(TAG, "Todos los vuelos: " + listaVuelos.getValues());
		}catch (NoHayMasPaginasDeVuelosException e){
			Log.e(TAG, "ResultadosAbstractActivity - getInfoVuelos - NoHayMasPaginasDeVuelosExteption"+ e.getMessage());
			throw e;
		}catch (Exception e1) {
			Log.e(TAG, "ResultadoAbstractActivity - getInfoVuelos - FalloGeneral " + e1.getMessage());
			e1.printStackTrace();
		}
		return listaVuelos;
	}


	public String cambiarFechaToUrl(String pUrl, String pDia){
		pDia = pDia.toLowerCase();
		formatearFecha(pDia);
		Log.d(TAG, "ResultadosAbstractActivity - cambiarFechaToUrl - pDia: " + pDia);
		Log.e(TAG, "ResultadosAbstractActivity - cambiarFechaToUrl - horaFormat: " + horaFormat);
		return vuelosJsoup.cambiarFechaToUrl(pUrl, horaFormat);
	}

	public String cambiarFechaToUrl(String pUrl, String pDia, int hora){
		pDia = pDia.toLowerCase();
		formatearFecha(pDia);
		Log.d(TAG, "ResultadosAbstractActivity - cambiarFechaToUrl - pDia: " + pDia);
		Log.e(TAG, "ResultadosAbstractActivity - cambiarFechaToUrl - horaFormat: " + horaFormat);
		return vuelosJsoup.cambiarFechaToUrl(pUrl, horaFormat);
	}

}
