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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import android.util.Log;


public class AenaPage {

	public AenaPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getURLVuelo(String pCodVuelo, String pDia) {

		String fechaMoficada = "20" + pDia.substring(6) + pDia.substring(3, 5)
				+ pDia.substring(0, 2);
		return  "http://www.aena-aeropuertos.es/csee/Satellite/infovuelos/es/Busqueda-rapida.html?num="+pCodVuelo+"&accion=busquedarapida&acccion=busquedarapida&dia="
				+fechaMoficada+"&Language=ES_ES&pagename=infovuelos";





		/*return "http://www.aena-aeropuertos.es/csee/Satellite/infovuelos/es/Detalle.html?accion=detalle"
				+ "&dia=" // el formato del dia, para hacer la consulta tiene
							// distinto formato que el que se muestra en la web
							// hay que adaptarlo
				+ fechaMoficada

				+ "&numBusqueda="
				+ pCodVuelo
				+ "&nvuelo="
				+ pCodVuelo.substring(pCodVuelo.length() - 4)
				+ "&ordenacionBack=hprevisto" + "&strVuelo=" + pCodVuelo;
	*/
	}

	public String getURLListaVuelos(String pDestino, String pHora,
			String pOrigen, String pDia, String pCompany, int pag) {
		return "http://www.aena-aeropuertos.es/csee/Satellite/infovuelos/es/Busqueda-avanzada.html?accion"
				+ "=busqueda&company="
				+ pCompany
				+ "&companyBusqueda="
				+ pCompany
				+ "&destiny="
				+ pDestino
				+ "&destinyBusqueda="
				+ pDestino
				+ "&hour="
				+ pHora
				+ "&hourBusqueda="
				+ pHora
				+ "&ordenacion=hprevisto"
				+ "&origin="
				+ pOrigen
				+ "&originBusqueda=" + pOrigen + "&pag=" + pag;
	}

	
	public String getURLListaVuelosDestino(String pDestino, String pHora,
			String pOrigen, String pDia, String pCompany, int pag) {
		return "http://www.aena-aeropuertos.es/csee/Satellite/infovuelos/es/Busqueda-avanzada.html?accion"
				+ "=busqueda&mov=L&movBusqueda=L&company="
				+ pCompany
				+ "&companyBusqueda="
				+ pCompany
				+ "&destiny="
				+ pDestino
				+ "&destinyBusqueda="
				+ pDestino
				+ "&hour="
				+ pHora
				+ "&hourBusqueda="
				+ pHora
				+ "&ordenacion=hprevisto"
				+ "&origin="
				+ pOrigen
				+ "&originBusqueda=" + pOrigen + "&pag=" + pag;
	}
	
	public String getNombreVuelo(Document doc) throws NoHayVueloException,NullPointerException {
		return doc.getElementsByClass("number").first().text();
	}

	public Elements getTablaDatosVuelo(Document doc) {
		return doc.select("td");
	}

	public Elements getOrigenDestino(Document doc) {
		return doc.select("caption");
	}

	public String getNombreCompany(Document doc) {
		return doc.select("li.company").get(0).text();
	}

	public Elements getVuelosPorDia(Document pDoc) {
		return pDoc.select(".infoVuelosResults");
	}

	public String getDia(Element pDatosVueloDeUnDia) {
		return pDatosVueloDeUnDia.select(".dateFlight>span").text();
	}

	public Elements getFilaDatosVuelos(Element pTabla) {

		return pTabla.select("tbody>tr");
	}

	public Elements getCeldasDatosVuelos(Element pFila) {
		return pFila.select("td");
	}

	public Elements getSiguientePagina(Document doc) {
		return doc.select(".arrowRight>span>a");
	}

	public  Elements obtenerDatosCeldaVuelosMultiples(Document doc) {
		// TODO Auto-generated method stub
		return doc.select("table tr td");
	}
	
}
