package com.vuelosDroid.backEnd.scrapper;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

		ArrayList<DatosVuelo> listaVuelos = new ArrayList<DatosVuelo>();
		Elements haySiguientePag;
		int pag = paginaActual;

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
			url = webAena.getURLListaVuelos(pDestino, pHora, pOrigen, pFecha,
					pCompany, pag);

			System.out.println("Fetching " + url + "...");

			doc = Jsoup.connect(url).timeout(1000000000).get();
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

	
	
	public List<DatosVuelo> getListaVuelosOrigen(int limite, String pDestino,
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

			doc = Jsoup.connect(url).timeout(1000000000).get();

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

			doc = Jsoup.connect(url).timeout(1000000000).get();
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

		doc = Jsoup.connect(url).timeout(1000000000).get();

		String nombreVuelo = webAena.getNombreVuelo(doc);
		Elements tablaDatosVuelo = webAena.getTablaDatosVuelo(doc);
		Elements origenDestino = webAena.getOrigenDestino(doc);
		String company = webAena.getNombreCompany(doc);

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