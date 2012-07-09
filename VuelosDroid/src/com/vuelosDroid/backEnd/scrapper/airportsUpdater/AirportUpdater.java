package com.vuelosDroid.backEnd.scrapper.airportsUpdater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import com.google.gson.Gson;

public class AirportUpdater {
	
	public static void main(String args[]){
		AirportUpdater air = new AirportUpdater();
		air.obtenerAeropuertos();
	}
	
	public AirportUpdater() {
		super();
		// TODO Auto-generated constructor stub
	}

	private List<Aeropuerto> obtenernEnlacesAeropuerto(String codOrigen,
			String Origen) {
		Document doc=null;
		String url ;

		
			int pag=1;
			
		List<Aeropuerto> lstConexiones= new ArrayList<Aeropuerto>();
		
		do{
			try {
				url = "http://www.aena-aeropuertos.es/csee/Satellite/Destinos/es/Aeropuerto.html?aero="+codOrigen+"&country=ES&pestana=aena&pageNum="+pag;
				System.out.println("url "+url); 
				
				doc = Jsoup.connect(url).timeout(1000000000).get();
				Elements destinos = doc.select("table.infoVuelosResults tbody tr");
				System.out.println("Num enlaces "+destinos.size());

				for(int i =1;i<destinos.size(); i++){
					
					lstConexiones.add(recorrerFila(destinos.get(i).select("td")));
					
				}

				
			

			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			
			pag++;
		}while(haySiguientePag(doc));
		return lstConexiones;
	}
	
	private Aeropuerto recorrerFila(Elements fila) {
	
		Aeropuerto air =new Aeropuerto();
		air.setNombre(fila.get(0).text());
		air.setCiudad(fila.get(1).text());
		air.setPais(fila.get(2).text());
		air.setCompanias(fila.get(3).text());
		
		return air;
				
	
	}

	private Boolean haySiguientePag(Document doc){
		if (doc.select("li.arrowRight span a").size()>0)
			return true;
		else 
			return false;
	
		
	}

	public String obtenerAeropuertos() {
		Document doc;

		String url;
		url="http://www.aena-aeropuertos.es/csee/Satellite/Destinos/es/Espana.html?pestana=aena";
		Gson gson = new Gson();
		List<Aeropuerto> lstAeropuertos = new ArrayList<Aeropuerto>();
		try {
			System.out.println("url "+url);
			doc = Jsoup.connect(url).get();
			Elements aeropuertos = doc.select("select#aero option");

			for (int i = 0; i < aeropuertos.size(); i++) {
				System.out.println("aeropuerto "+aeropuertos.get(i).text()+" "+aeropuertos.get(i).attr("value") );
				Aeropuerto aeropuerto = new Aeropuerto(aeropuertos.get(i).text(),
						aeropuertos.get(i).attr("value"));
				
				aeropuerto.setConexiones(this.obtenernEnlacesAeropuerto(aeropuerto.getCod(),aeropuerto.getNombre()));
				lstAeropuertos.add(aeropuerto);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*		for (Aeropuerto aeropuerto : lstAeropuertos) {
			System.out.println(aeropuerto.getNombre());
		}*/
		//JSONSerializer serializer = new JSONSerializer();
		//return serializer.serialize(lstOrigenes);
		//System.out.println(gson.toJson(lstAeropuertos));
		return gson.toJson(lstAeropuertos);
	}
	

}
