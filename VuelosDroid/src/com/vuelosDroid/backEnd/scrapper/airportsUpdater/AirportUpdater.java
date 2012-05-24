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

package com.vuelosDroid.backEnd.scrapper.airportsUpdater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class AirportUpdater {

	public AirportUpdater() {
		super();
		// TODO Auto-generated constructor stub
	}

	private List<Aeropuerto> obtenernEnlacesAeropuerto(String codOrigen,
			String Origen,boolean isOrigenes) {
		Document doc;
		String url ;

		if (isOrigenes) {

			url = "http://pda.aena.es/csee/Satellite?Language=ES_ES&c=Page&cid=1112&pagename=Herramientas%2FPDA%2FVuelosTiempoReal";
		} else {
			url= "http://pda.aena.es/csee/Satellite?Language=ES_ES&c=Page&cid=1112&pagename=Herramientas%2FPDA%2FVuelosTiempoReal&radioSelection=L";
		}

		List<Aeropuerto> lstDestinos = new ArrayList<Aeropuerto>();
		try {
			doc = Jsoup.connect(url).data("origin", codOrigen).post();

			Elements destinos = doc.select("select#destination option");
			//System.out.println("ENTRA "+destinos.text());
			for (Element destino : destinos) {
				lstDestinos.add(new Aeropuerto(destino.text(), destino
						.attr("value")));
			}
			lstDestinos.remove(0);// borro el primero pq es el combobox default
			// que pone "elige aeropuerto"

			;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lstDestinos;
	}

	public String obtenerAeropuertos(boolean isOrigenes) {
		Document doc;

		String url;
		if (isOrigenes) {

			url = "http://pda.aena.es/csee/Satellite?Language=ES_ES&c=Page&cid=1112&pagename=Herramientas%2FPDA%2FVuelosTiempoReal";
		} else {
			url= "http://pda.aena.es/csee/Satellite?Language=ES_ES&c=Page&cid=1112&pagename=Herramientas%2FPDA%2FVuelosTiempoReal&radioSelection=L";
		}
		Gson gson = new Gson();
		List<Aeropuerto> lstOrigenes = new ArrayList<Aeropuerto>();
		try {
			doc = Jsoup.connect(url).get();
			Elements origenes = doc.select("select#origin option");

			for (int i = 1; i < origenes.size(); i++) {
				Aeropuerto aeropuerto = new Aeropuerto(origenes.get(i).text(),
						origenes.get(i).attr("value"));
				aeropuerto.setDestinos(this.obtenernEnlacesAeropuerto(
						aeropuerto.getCod(), aeropuerto.getNombre(),isOrigenes));
				lstOrigenes.add(aeropuerto);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//JSONSerializer serializer = new JSONSerializer();
		//return serializer.serialize(lstOrigenes);
		return gson.toJson(lstOrigenes);
	}
}
