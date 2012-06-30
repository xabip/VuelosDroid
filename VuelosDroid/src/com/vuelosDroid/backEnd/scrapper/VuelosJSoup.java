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

package com.vuelosDroid.backEnd.scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Example program to list links from a URL.
 */
/**
 * @author kdreamer
 * 
 */
public class VuelosJSoup {


	/** The url. */
	String url = null;

	/** The web aena. */
	AenaPage webAena;

	/** The search. */
	String search;

	/** The origen. */
	String origen = "";

	/** The destino. */
	String destino = "";

	/** The hay mas paginas. */
	Boolean hayMasPaginas = false;

	/** The pagina actual. */
	int paginaActual = 1;

	/** The doc. */
	Document doc;



	public VuelosJSoup() {

		webAena = new AenaPage();
	}

	public List<DatosVuelo> getListaVuelosSalidas(/*String pDestino, String pHora,
			String pOrigen, String pDia, String pCompany*/
			String pOrigen,String pDestino,
			String pFecha,String pHora, String pCompany)
					throws NoHayVueloException, IOException,
					NoHayMasPaginasDeVuelosException {
		Log.i("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - Inicio");
		ArrayList<DatosVuelo> listaVuelos = new ArrayList<DatosVuelo>();
		Elements haySiguientePag;
		int pag = paginaActual;

		if (pOrigen.equals(this.origen) && pDestino.equals(this.destino)) {
			// es el mismo vuelo,
			Log.i("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - pOrigen.equals(this.origen) && pDestino.equals(this.destino) true");
			if (!this.hayMasPaginas) {
				throw new NoHayMasPaginasDeVuelosException();
			}
		} else {
			Log.i("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - pOrigen.equals(this.origen) && pDestino.equals(this.destino) false");
			this.origen = pOrigen;
			this.destino = pDestino;
			// es un vuelo distinto, inicializo el num de pagina
			this.paginaActual = 1;
		}

		do {
			pag = paginaActual;
			url = webAena.getURLListaVuelos(pDestino, pHora, pOrigen, pFecha,
					pCompany, pag);

			System.out.println("Fetching " + url + "...");

			doc = Jsoup.connect(url).timeout(60000).get();  //1000000000
			String ciudadOrigen = doc.getElementsByClass("city").first().text()
					+ "(" + pOrigen + ")";
			Elements datosVueloPorDia = webAena.getVuelosPorDia(doc);
			if (datosVueloPorDia.size() == 0) {
				throw new NoHayVueloException();
			}
			for (int i = 0; i < datosVueloPorDia.size(); i++) {
				Log.d("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - datosVueloPorDia.size() " + datosVueloPorDia.size());
				
				System.out.println(webAena.getDia(datosVueloPorDia.get(i)));// Dia
				
				if ((webAena.getDia(datosVueloPorDia.get(i)).equals(pFecha))) {
					Log.i("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - dentro del dia");
					Elements filaVuelo = webAena
							.getFilaDatosVuelos(datosVueloPorDia.get(i)); // obtengo

					for (int a = 0; a < filaVuelo.size(); a++) {
						Elements celdas = webAena
								.getCeldasDatosVuelos(filaVuelo.get(a));// obtengo
						// las celdas de la fila
						listaVuelos.add(new DatosVuelo(celdas, ciudadOrigen));
					}
				}
			}
			haySiguientePag = webAena.getSiguientePagina(doc);
			Log.d("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - haySiguientePah.size() + " + haySiguientePag.size());
			this.paginaActual++;
			Log.d("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - paginaActual: + " + paginaActual);

			if (haySiguientePag.size() > 0) {
				Log.i("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - haysigpag > 0");
				this.hayMasPaginas = true;
			} else {
				Log.i("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - haysigpag > 0");

				this.hayMasPaginas = false;
			}
			// haya mas paginas

			System.out.println(paginaActual);
			System.out.println("estoy " + listaVuelos.size() + " "
					+ hayMasPaginas);
		} while (listaVuelos.size() < 1 && hayMasPaginas);
		Log.i("VuelosAndroid" , "VuelosJsoup - getListaVuelosSalidas - Final");
		return listaVuelos;

	}



/*	public List<DatosVuelo> getListaVuelosOrigen(int limite, String pDestino,
			String pHora, String pOrigen, String pDia, String pCompany)
					throws NoHayVueloException, IOException {

		int pag = paginaActual;
		boolean limiteSobrepasado = false;
		ArrayList<DatosVuelo> listaVuelos = new ArrayList<DatosVuelo>();
		Elements haySiguientePag;

		do {
			url = webAena.getURLListaVuelos(pDestino, pHora, pOrigen, pDia,
					pCompany, pag);

			System.out.println("Fetching " + url + "...");

			doc = Jsoup.connect(url).timeout(60000).get();  //1000000000

			Elements datosVueloPorDia = webAena.getVuelosPorDia(doc);
			if (datosVueloPorDia.size() == 0) {
				throw new NoHayVueloException();
			}
			for (int i = 0; i < datosVueloPorDia.size() && !limiteSobrepasado; i++) {
				System.out.println(webAena.getDia(datosVueloPorDia.get(i)));// Dia
				if ((webAena.getDia(datosVueloPorDia.get(i)).equals(pDia))) {

					Elements filaVuelo = webAena
							.getFilaDatosVuelos(datosVueloPorDia.get(i)); // obtengo

					for (int a = 0; a < filaVuelo.size()
							&& listaVuelos.size() < limite; a++) {
						Elements celdas = webAena
								.getCeldasDatosVuelos(filaVuelo.get(a));// obtengo
						// las
						// celdas
						// de la
						// fila

						listaVuelos.add(new DatosVuelo(celdas, pCompany));
						if (listaVuelos.size() == limite) {
							limiteSobrepasado = true;
						}
					}
				}
			}
			haySiguientePag = webAena.getSiguientePagina(doc);
			pag++;
		} while (haySiguientePag.size() > 0 && !limiteSobrepasado); // mientras
		// haya mas
		// paginas
		System.out.println(pag);
		Log.w("VuelosAndrodid", listaVuelos.size()+"");
		return listaVuelos;

	}
*/

	/**
	 * Obtener todos los vuelos con origen en pOrigen, origen y dia son campos
	 * obligatorios, los demas optativos
	 * 
	 * 
	 * @param pDestino
	 *            the destino
	 * @param pHora
	 *            the hora
	 * @param pOrigen
	 *            the origen
	 * @param pDia
	 *            the dia
	 * @param pCompany
	 *            the company
	 * @return the lista vuelos origen
	 * @throws NoHayVueloException
	 *             the no hay vuelo exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws NoHayMasPaginasDeVuelosException
	 *             the no hay mas paginas de vuelos exception
	 */
	public List<DatosVuelo> getListaVuelosLlegadas(/*String pDestino, String pHora,
			String pOrigen, String pDia, String pCompany*/String pDestino,String pOrigen,String pFecha, String pHora, String pCompany)
					throws NoHayVueloException, IOException,
					NoHayMasPaginasDeVuelosException {

		ArrayList<DatosVuelo> listaVuelos = new ArrayList<DatosVuelo>();
		Elements haySiguientePag;

		if (pOrigen.equals(this.origen) && pDestino.equals(this.destino)) {
			// es el mismo vuelo,
			if (!this.hayMasPaginas) {
				throw new NoHayMasPaginasDeVuelosException();
			}
		} else {
			this.origen = pOrigen;
			this.destino = pDestino;
			// es un vuelo distinto, inicializo el num de pagina
			this.paginaActual = 1;
		}

		do {
			
			url = webAena.getURLListaVuelosDestino(pDestino, pHora, pOrigen,
					pFecha, pCompany, paginaActual);

			System.out.println("Fetching " + url + "...");

			doc = Jsoup.connect(url).timeout(60000).get();  //1000000000
			String ciudadOrigen = doc.getElementsByClass("city").first().text()
					+ "(" + pOrigen + ")";
			Elements datosVueloPorDia = webAena.getVuelosPorDia(doc);
			if (datosVueloPorDia.size() == 0) {
				throw new NoHayVueloException();
			}
			for (int i = 0; i < datosVueloPorDia.size(); i++) {
				System.out.println(webAena.getDia(datosVueloPorDia.get(i)));// Dia
				if ((webAena.getDia(datosVueloPorDia.get(i)).equals(pFecha))) {

					Elements filaVuelo = webAena
							.getFilaDatosVuelos(datosVueloPorDia.get(i)); // obtengo

					for (int a = 0; a < filaVuelo.size(); a++) {
						Elements celdas = webAena
								.getCeldasDatosVuelos(filaVuelo.get(a));// obtengo
						// las celdas de la fila
						listaVuelos.add(new DatosVuelo(celdas, ciudadOrigen));
					}
				}
			}
			haySiguientePag = webAena.getSiguientePagina(doc);
			this.paginaActual++;
			if (haySiguientePag.size() > 0) {
				this.hayMasPaginas = true;
			} else {
				this.hayMasPaginas = false;
			}

			// haya mas paginas

			System.out.println(paginaActual);
			System.out.println("estoy " + listaVuelos.size() + " "
					+ hayMasPaginas);
		} while (listaVuelos.size() < 1 && hayMasPaginas);
		return listaVuelos;

	}

	/**
	 * Obtiene un vuelo concreto a partir de la url del vuelo.
	 * 
	 * @param pUrl
	 *            the url
	 * @return the datos vuelo
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws Exception
	 *             the exception
	 */
	public DatosVuelo getDatosVuelo(String pUrl) throws IOException, Exception {

		url = pUrl;
		System.out.println("Fetching " + url + "...");

		doc = Jsoup.connect(url).timeout(30000).get();  //1000000000
		String nombreVuelo = webAena.getNombreVuelo(doc);
		Elements tablaDatosVuelo = webAena.getTablaDatosVuelo(doc);
		
		try {
			if (!tablaDatosVuelo.get(0).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo0: " + tablaDatosVuelo.get(0).text());
			}
		} catch (IndexOutOfBoundsException ex4) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo1: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(1).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo1: " + tablaDatosVuelo.get(1).text());
			}
		} catch (IndexOutOfBoundsException ex5) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo2: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(2).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo2: " + tablaDatosVuelo.get(2).text());
			}
		} catch (IndexOutOfBoundsException ex6) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo3: " + "ERROR, Celda sin datos");
		}try {
			if (!tablaDatosVuelo.get(3).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo3: " + tablaDatosVuelo.get(3).text());
			}
		} catch (IndexOutOfBoundsException ex7) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo3: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(4).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo4: " + tablaDatosVuelo.get(4).text());
			}
		} catch (IndexOutOfBoundsException ex7) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo4: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(5).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo5: " + tablaDatosVuelo.get(5).text());
			}
		} catch (IndexOutOfBoundsException ex8) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo5: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(6).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo6: " + tablaDatosVuelo.get(6).text());
			}
		} catch (IndexOutOfBoundsException ex9) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo6: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(7).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo7: " + tablaDatosVuelo.get(7).text());
			}
		} catch (IndexOutOfBoundsException ex10) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo7: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(8).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo8: " + tablaDatosVuelo.get(8).text());
			}
		} catch (IndexOutOfBoundsException ex11) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo8: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(9).text().equals(" ")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo9: " + tablaDatosVuelo.get(9).text());
			}
		} catch (IndexOutOfBoundsException ex12) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo9: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(10).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo10: " + tablaDatosVuelo.get(10).text());
			}
		} catch (IndexOutOfBoundsException ex14) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo10: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(11).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo11: " + tablaDatosVuelo.get(11).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo11: " + "ERROR, Celda sin datos");

		}
		try {
			if (!tablaDatosVuelo.get(12).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo12: " + tablaDatosVuelo.get(12).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo12: " + "ERROR, Celda sin datos");

		}	
		try {
			if (!tablaDatosVuelo.get(13).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo13: " + tablaDatosVuelo.get(13).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo13: " + "ERROR, Celda sin datos");

		}		
		try {
			if (!tablaDatosVuelo.get(14).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo14: " + tablaDatosVuelo.get(15).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo14: " + "ERROR, Celda sin datos");

		}		
		try {
			if (!tablaDatosVuelo.get(15).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo15: " + tablaDatosVuelo.get(15).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo15: " + "ERROR, Celda sin datos");

		}		try {
			if (!tablaDatosVuelo.get(16).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo16: " + tablaDatosVuelo.get(16).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo16: " + "ERROR, Celda sin datos");
		}
		try {
			if (!tablaDatosVuelo.get(17).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo17: " + tablaDatosVuelo.get(17).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo17: " + "ERROR, Celda sin datos");

		}		try {
			if (!tablaDatosVuelo.get(18).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo18: " + tablaDatosVuelo.get(18).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo18: " + "ERROR, Celda sin datos");

		}		try {
			if (!tablaDatosVuelo.get(19).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo19: " + tablaDatosVuelo.get(19).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo19: " + "ERROR, Celda sin datos");

		}		try {
			if (!tablaDatosVuelo.get(20).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo20: " + tablaDatosVuelo.get(20).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo20: " + "ERROR, Celda sin datos");

		}		try {
			if (!tablaDatosVuelo.get(21).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - getDatosvuelo21: " + tablaDatosVuelo.get(21).text());
			}
		} catch (IndexOutOfBoundsException ex26) {
			Log.w("VuelosAndroid", "VuelosJsoup - getDatosvuelo21: " + "ERROR, Celda sin datos");

		}
		Elements origenDestino = webAena.getOrigenDestino(doc);
		/*try {
			if (!origenDestino.get(0).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - Origenes0: " + origenDestino.get(0).text());
			}
		} catch (IndexOutOfBoundsException ex1) {
			Log.w("VuelosAndroid", "VuelosJsoup - Origenes0: " + "ERROR, Celda sin datos"); }
		try {
			if (!origenDestino.get(1).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - Origenes1: " + origenDestino.get(1).text());
			}
			}catch (IndexOutOfBoundsException ex1) {
				Log.w("VuelosAndroid", "VuelosJsoup - Origenes1: " + "ERROR, Celda sin datos"); }
		try {
				if (!origenDestino.get(2).text().equals("")) {
					Log.d("VuelosAndroid", "VuelosJsoup - Origenes2: " + origenDestino.get(2).text());
				}
			} catch (IndexOutOfBoundsException ex1) {
				Log.w("VuelosAndroid", "VuelosJsoup - Origenes2: " + "ERROR, Celda sin datos"); }
		try {
				if (!origenDestino.get(2).text().equals("")) {
					Log.d("VuelosAndroid", "VuelosJsoup - Origenes3: " + origenDestino.get(3).text());
				}
			} catch (IndexOutOfBoundsException ex1) {
				Log.w("VuelosAndroid", "VuelosJsoup - Origenes3: " + "ERROR, Celda sin datos"); }
		try {
			if (!origenDestino.get(2).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - Origenes4: " + origenDestino.get(4).text());
			}
		} catch (IndexOutOfBoundsException ex1) {
			Log.w("VuelosAndroid", "VuelosJsoup - Origenes4: " + "ERROR, Celda sin datos"); }
		try {
			if (!origenDestino.get(2).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - Origenes5: " + origenDestino.get(5).text());
			}
		} catch (IndexOutOfBoundsException ex1) {
			Log.w("VuelosAndroid", "VuelosJsoup - Origenes5: " + "ERROR, Celda sin datos"); }
		try {
			if (!origenDestino.get(2).text().equals("")) {
				Log.d("VuelosAndroid", "VuelosJsoup - Origenes6: " + origenDestino.get(6).text());
			}
		} catch (IndexOutOfBoundsException ex1) {
			Log.w("VuelosAndroid", "VuelosJsoup - Origenes6: " + "ERROR, Celda sin datos"); }*/
		String company = webAena.getNombreCompany(doc);

		Log.d("VuelosAndroid", "VuelosJsoup - numeroAeropuertos: " + origenDestino.size());
		Log.d("VuelosAndroid", "VuelosJsoup - numCeldas: " + tablaDatosVuelo.size());

		return new DatosVuelo(nombreVuelo, tablaDatosVuelo, origenDestino,
				company, pUrl);
	}

	/**
	 * Obtiene un vuelo concreto gracias al codVuelo y el dia del vuelo
	 * 
	 * @param pCodVuelo
	 *            the cod vuelo
	 * @param pDia
	 *            the dia
	 * @return the datos vuelo
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws MoreFlightsException
	 *             A pesar de introducir el dia, el sistema encuentra codigos
	 *             identicos en distintos dias
	 * @throws NoHayVueloException
	 *             the no hay vuelo exception
	 * @throws Exception
	 *             the exception
	 */
	public DatosVuelo getDatosVuelo(String pCodVuelo, String pDia)
			throws IOException,Exception {

		url = webAena.getURLVuelo(pCodVuelo, pDia);
		System.out.println("Fetching " + url + "...");
		String nombreVuelo = null;
		Elements tablaDatosVuelo = null;
		Elements origenDestino = null;
		String company = null;
		doc = Jsoup.connect(url).timeout(1000000000).get();
		try{
			nombreVuelo = webAena.getNombreVuelo(doc);
			tablaDatosVuelo = webAena.getTablaDatosVuelo(doc);
			origenDestino = webAena.getOrigenDestino(doc);
			company = webAena.getNombreCompany(doc);
		}catch(NullPointerException ex1){
			throw new MoreFlightsException("Hay mas vuelos",ex1);
		}

		System.out.println("Nombre Vuelo " + nombreVuelo);
		return new DatosVuelo(nombreVuelo, tablaDatosVuelo, origenDestino,
				company, url); /////////////////////////////////////////////////////////////////////////////////////////////
	}


	/**
	 * Recorrer multiples vuelos.
	 * 
	 * @param pCodVuelo
	 *            the cod vuelo
	 * @param pDia
	 *            the dia
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws NoHayVueloException
	 *             the no hay vuelo exception
	 */
	public List<DatosVuelo> recorrerMultiplesVuelos(String pCodVuelo, String pDia) throws IOException , NoHayVueloException{

		// System.out.println("Tabla " + doc.select("table").size());
		Document doc;
		String url = webAena.getURLVuelo(pCodVuelo, pDia);
		DatosVuelo vuelo1;
		DatosVuelo vuelo2;

		try {
			doc = Jsoup.connect(url).timeout(1000000000).get();
			Elements tds = webAena.obtenerDatosCeldaVuelosMultiples(doc);

			vuelo1 = new DatosVuelo(tds, "");

			vuelo2 = new DatosVuelo(tds, "");

		} catch (IndexOutOfBoundsException ex1) {
			throw new NoHayVueloException("No existe tal vuelo");
		}

		List<DatosVuelo> list = new ArrayList<DatosVuelo>();
		list.add(vuelo1);
		list.add(vuelo2);


		return list;
	}

	/**
	 * Cambiar fecha to url.
	 * 
	 * @param pUrl
	 *            the url
	 * @param pFecha
	 *            the fecha
	 * @return the string
	 */
	public String cambiarFechaToUrl(String pUrl, String pFecha) {

		String year =  "20" + pFecha.substring(6);
		String month= pFecha.substring(3, 5);
		String day= pFecha.substring(0, 2);
		String fechaModificada = year+month+day;

		String url2 = pUrl.replaceFirst("\\d{8}", fechaModificada);

		String newDate = year+"-"+month+"-"+day;
		String url3= url2.replaceFirst("\\d{4}-\\d{2}-\\d{2}", newDate);


		return url3;
	}


}