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

import org.jsoup.select.Elements;

import android.util.Log;

/**
 * 
 * @author Urko Guinea & Xabier Pena
 *
 */
public class DatosVuelo {


	/** The nombre vuelo. */
	String nombreVuelo = "--";

	/** The nombre company. */
	String nombreCompany = "--";

	/** The aeropuerto origen. */
	String aeropuertoOrigen = "--";

	/** The fecha origen. */
	String fechaOrigen = "--";

	/** The hora origen. */
	String horaOrigen = "--";

	/** The terminal origen. */
	String terminalOrigen = "--";

	/** The puerta origen. */
	String puertaOrigen = "--";

	/** The estado vuelo origen. */
	String estadoVueloOrigen = "--";

	/** The link info vuelo. */
	String linkInfoVuelo = "--";

	/** The fecha destino. */
	String fechaDestino = "--";

	/** The hora destino. */
	String horaDestino = "--";

	/** The terminal destino. */
	String terminalDestino = "--";

	/** The sala destino. */
	String salaDestino = "--";

	/** The cinta destino. */
	String cintaDestino = "--";

	/** The estado vuelo destino. */
	String estadoVueloDestino = "--";

	/** The aeropuerto destino. */
	String aeropuertoDestino = "--";

	/** Estado aeropuerto intermedio **/
	String aeropuertoIntermedio = "--";

	/** Escala */
	Boolean escala = false;



	/**
	 * Instantiates a new datos vuelo.
	 *
	 * @param nombreVuelo the nombre vuelo
	 * @param tablaDatosVuelo the tabla datos vuelo
	 * @param origenDestino the origen destino
	 * @param company the company
	 */
	public DatosVuelo(String nombreVuelo, Elements tablaDatosVuelo,
			Elements origenDestino, String company) {
		try {
			if (!company.equals("")) {
				this.setNombreCompany(company);

			}
		} catch (IndexOutOfBoundsException ex) {
			System.out.println("ERROR, Celda sin datos");
		}
		
		try {
			if (!nombreVuelo.equals("")) {
				this.setNombreVuelo(nombreVuelo);
			}
		} catch (IndexOutOfBoundsException ex3) {
			System.out.println("ERROR, Celda sin datos");
		}
		
		int t = tablaDatosVuelo.size();
		String estadoA = "--";
		String estadoB = "--";
		if(comprueba(tablaDatosVuelo, 5)){
			estadoA = tablaDatosVuelo.get(5).text();
		}
		
		if (comprueba(tablaDatosVuelo, t - 1)){
			estadoB = tablaDatosVuelo.get(t - 1).text();
		}
		Log.w("VuelosDroid", "DatosVuelo - Constructor - estadoA: " + estadoA);
		Log.w("VuelosDroid", "DatosVuelo - Constructor - estadoB: " + estadoB);
		
		//Tiene escalas
		if(origenDestino.size()>2){
			escala = true;
			if(comprueba(origenDestino, origenDestino.size()-1)){
				this.setAeropuertoDestino(origenDestino.get(origenDestino.size()-1).text());
			}
			if(comprueba(origenDestino, origenDestino.size()-2)){
				this.setAeropuertoOrigen(origenDestino.get(origenDestino.size()-2).text());
			}
			if(comprueba(origenDestino, origenDestino.size()-4)){
				this.setAeropuertoIntermedio(origenDestino.get(origenDestino.size()-4).text());
			}
			
			// Si es de origen español
			if(estadoA.contains("spegado") || estadoA.contains("alida") || estadoA.contains("celado")){
				if (comprueba(origenDestino, 0)){
					this.setAeropuertoOrigen(origenDestino.get(0).text());
				}
				if (comprueba(origenDestino, origenDestino.size()-1)){
					this.setAeropuertoDestino(origenDestino.get(origenDestino.size()-1).text());
				}
				if (comprueba(origenDestino, origenDestino.size()-2)){
					this.setAeropuertoIntermedio(origenDestino.get(origenDestino.size()-2).text());
				}
				if(comprueba(tablaDatosVuelo, 0)){
					this.setFechaOrigen(tablaDatosVuelo.get(0).text());
					this.setFechaDestino(tablaDatosVuelo.get(0).text());
				}
				if(comprueba(tablaDatosVuelo, 1)){
					this.setHoraOrigen(tablaDatosVuelo.get(1).text());
				}
				if(comprueba(tablaDatosVuelo, 2)){
					this.setTerminalOrigen(tablaDatosVuelo.get(2).text());
				}
				if(comprueba(tablaDatosVuelo, 4)){
					this.setPuertaOrigen(tablaDatosVuelo.get(4).text());
				}
				if(comprueba(tablaDatosVuelo, 5)){
					this.setEstadoVueloOrigen(tablaDatosVuelo.get(5).text());
				}

				// Si es de destino español
			} if (estadoB.contains("legada") || estadoB.contains("aterrizado") || estadoB.contains("celado")){
				if(comprueba(tablaDatosVuelo, t - 6)){
					this.setFechaDestino(tablaDatosVuelo.get(t - 6).text());
					this.setFechaOrigen(tablaDatosVuelo.get(t - 6).text());
				}
				if(comprueba(tablaDatosVuelo, t - 5)){
					this.setHoraDestino(tablaDatosVuelo.get(t - 5).text());
				}
				if(comprueba(tablaDatosVuelo, t - 4)){
					this.setTerminalDestino(tablaDatosVuelo.get(t - 4).text());
				}
				if(comprueba(tablaDatosVuelo, t - 3)){
					this.setSalaDestino(tablaDatosVuelo.get(t - 3).text());
				}
				if(comprueba(tablaDatosVuelo, t - 2)){
					this.setCintaDestino(tablaDatosVuelo.get(t - 2).text());
				}
				if(comprueba(tablaDatosVuelo, t - 1)){
					this.setEstadoVueloDestino(tablaDatosVuelo.get(t - 1).text());
				}
			}

			// No tiene escalas
		} else {
			escala = false;
			if(comprueba(origenDestino, 0)){
				this.setAeropuertoOrigen(origenDestino.get(0).text());
			}
			if(comprueba(origenDestino, 1)){
				this.setAeropuertoDestino(origenDestino.get(1).text());
			}
			
			//Origen Español
			if(estadoA.contains("spegado") || estadoA.contains("alida") || estadoA.contains("celado")){

				if(comprueba(tablaDatosVuelo, 0)){
					this.setFechaOrigen(tablaDatosVuelo.get(0).text());
					this.setFechaDestino(tablaDatosVuelo.get(0).text());
				}
				if(comprueba(tablaDatosVuelo, 1)){
					this.setHoraOrigen(tablaDatosVuelo.get(1).text());
				}
				if(comprueba(tablaDatosVuelo, 2)){
					this.setTerminalOrigen(tablaDatosVuelo.get(2).text());
				}
				if(comprueba(tablaDatosVuelo, 4)){
					this.setPuertaOrigen(tablaDatosVuelo.get(4).text());
				}
				if(comprueba(tablaDatosVuelo, 5)){
					this.setEstadoVueloOrigen(tablaDatosVuelo.get(5).text());
				}
			}
			//Destino Español
			if (estadoB.contains("legada") || estadoB.contains("aterrizado") || estadoB.contains("celado")){
				if(comprueba(tablaDatosVuelo, t - 6)){
					this.setFechaDestino(tablaDatosVuelo.get(t-6).text());
					if(comprueba(tablaDatosVuelo, 0)){
						this.setFechaOrigen(tablaDatosVuelo.get(0).text());
					}
				}
				if(comprueba(tablaDatosVuelo, t - 5)){
					this.setHoraDestino(tablaDatosVuelo.get(t-5).text());
				}
				if(comprueba(tablaDatosVuelo, t - 4)){
					this.setTerminalDestino(tablaDatosVuelo.get(t-4).text());
				}
				if(comprueba(tablaDatosVuelo, t - 3)){
					this.setSalaDestino(tablaDatosVuelo.get(t-3).text());
				}
				if(comprueba(tablaDatosVuelo, t - 2)){
					this.setCintaDestino(tablaDatosVuelo.get(t-2).text());
				}
				if(comprueba(tablaDatosVuelo, t - 1)){
					this.setEstadoVueloDestino(tablaDatosVuelo.get(t-1).text());
				}
			}
		}
	}

	/**
	 * Instantiates a new datos vuelo.
	 *
	 * @param celdas the celdas
	 * @param pCiudad the ciudad
	 */
	public DatosVuelo(Elements celdas,String pCiudad) {
		this.setNombreVuelo(celdas.get(0).text());
		this.setLinkInfoVuelo("http://www.aena-aeropuertos.es"
				+ celdas.get(0).childNode(0).attr("href").toString());
		this.setHoraOrigen(celdas.get(1).text());
		this.setAeropuertoOrigen(pCiudad);
		this.setAeropuertoDestino(celdas.get(2).text());
		this.setNombreCompany(celdas.get(3).text());
		this.setTerminalOrigen(celdas.get(4).text());
	}

	public DatosVuelo(String nombreVuelo, Elements tds,
			Elements origenDestino, String company, String url) {
		this (nombreVuelo, tds, origenDestino, company);
		this.setLinkInfoVuelo(url);
	}


	public DatosVuelo() {
		super();
	}

	/**
	 * Gets the nombre vuelo.
	 *
	 * @return the nombre vuelo
	 */
	public String getNombreVuelo() {
		if (this.nombreVuelo.equals("")) {
			this.nombreVuelo = "--";
		}
		return nombreVuelo;
	}

	/**
	 * Sets the nombre vuelo.
	 *
	 * @param nombreVuelo the new nombre vuelo
	 */
	public void setNombreVuelo(String nombreVuelo) {
		this.nombreVuelo = nombreVuelo;
	}

	/**
	 * Gets the aeropuerto origen.
	 *
	 * @return the aeropuerto origen
	 */
	public String getAeropuertoOrigen() {
		if (this.aeropuertoOrigen.equals("")) {
			this.aeropuertoOrigen = "--";
		}
		return aeropuertoOrigen;
	}

	/**
	 * Sets the aeropuerto origen.
	 *
	 * @param aeropuertoOrigen the new aeropuerto origen
	 */
	public void setAeropuertoOrigen(String aeropuertoOrigen) {
		this.aeropuertoOrigen = aeropuertoOrigen;
	}

	/**
	 * Gets the fecha origen.
	 *
	 * @return the fecha origen
	 */
	public String getFechaOrigen() {
		if (this.fechaOrigen.equals("")) {
			this.fechaOrigen = "--";
		}
		return fechaOrigen;
	}

	/**
	 * Sets the fecha origen.
	 *
	 * @param fechaOrigen the new fecha origen
	 */
	public void setFechaOrigen(String fechaOrigen) {
		this.fechaOrigen = fechaOrigen;
	}

	/**
	 * Gets the hora origen.
	 *
	 * @return the hora origen
	 */
	public String getHoraOrigen() {
		if (this.horaOrigen.equals("")) {
			this.horaOrigen = "--";
		}
		return horaOrigen;
	}

	/**
	 * Sets the hora origen.
	 *
	 * @param horaOrigen the new hora origen
	 */
	public void setHoraOrigen(String horaOrigen) {
		this.horaOrigen = horaOrigen;
	}

	/**
	 * Gets the terminal origen.
	 *
	 * @return the terminal origen
	 */
	public String getTerminalOrigen() {
		if (this.terminalOrigen.equals("")) {
			this.terminalOrigen = "--";
		}
		return terminalOrigen;
	}

	/**
	 * Sets the terminal origen.
	 *
	 * @param terminalOrigen the new terminal origen
	 */
	public void setTerminalOrigen(String terminalOrigen) {
		this.terminalOrigen = terminalOrigen;
	}

	/**
	 * Gets the puerta origen.
	 *
	 * @return the puerta origen
	 */
	public String getPuertaOrigen() {
		System.out.println("LA puerta ES" + puertaOrigen);
		if (this.puertaOrigen.equals(" ")) {
			this.puertaOrigen = "--";
		}
		System.out.println("LA puerta ES" + puertaOrigen);
		return puertaOrigen;
	}

	/**
	 * Sets the puerta origen.
	 *
	 * @param puertaOrigen the new puerta origen
	 */
	public void setPuertaOrigen(String puertaOrigen) {
		this.puertaOrigen = puertaOrigen;
	}

	/**
	 * Gets the estado vuelo origen.
	 *
	 * @return the estado vuelo origen
	 */
	public String getEstadoVueloOrigen() {
		if (this.estadoVueloOrigen.equals("")) {
			this.estadoVueloOrigen = "--";
		}
		return estadoVueloOrigen;
	}

	/**
	 * Sets the estado vuelo origen.
	 *
	 * @param estadoVueloOrigen the new estado vuelo origen
	 */
	public void setEstadoVueloOrigen(String estadoVueloOrigen) {
		this.estadoVueloOrigen = estadoVueloOrigen;
	}

	/**
	 * Gets the fecha destino.
	 *
	 * @return the fecha destino
	 */
	public String getFechaDestino() {
		if (this.fechaDestino.equals("")) {
			this.fechaDestino = "--";
		}
		return fechaDestino;
	}

	/**
	 * Sets the fecha destino.
	 *
	 * @param fechaDestino the new fecha destino
	 */
	public void setFechaDestino(String fechaDestino) {
		this.fechaDestino = fechaDestino;
	}

	/**
	 * Gets the hora destino.
	 *
	 * @return the hora destino
	 */
	public String getHoraDestino() {
		if (this.horaDestino.equals("")) {
			this.horaDestino = "--";
		}
		return horaDestino;
	}

	/**
	 * Sets the hora destino.
	 *
	 * @param horaDestino the new hora destino
	 */
	public void setHoraDestino(String horaDestino) {
		this.horaDestino = horaDestino;
	}

	/**
	 * Gets the terminal destino.
	 *
	 * @return the terminal destino
	 */
	public String getTerminalDestino() {
		if (this.terminalDestino.equals("")) {
			this.terminalDestino = "--";
		}
		return terminalDestino;
	}

	/**
	 * Sets the terminal destino.
	 *
	 * @param terminalDestino the new terminal destino
	 */
	public void setTerminalDestino(String terminalDestino) {
		this.terminalDestino = terminalDestino;
	}

	/**
	 * Gets the sala destino.
	 *
	 * @return the sala destino
	 */
	public String getSalaDestino() {
		if (this.salaDestino.equals("")) {
			this.salaDestino = "--";
		}
		return salaDestino;
	}

	/**
	 * Sets the sala destino.
	 *
	 * @param salaDestino the new sala destino
	 */
	public void setSalaDestino(String salaDestino) {
		this.salaDestino = salaDestino;
	}

	/**
	 * Gets the cinta destino.
	 *
	 * @return the cinta destino
	 */
	public String getCintaDestino() {
		if (this.cintaDestino.equals("")) {
			this.cintaDestino = "--";
		}
		return cintaDestino;
	}

	/**
	 * Sets the cinta destino.
	 *
	 * @param cintaDestino the new cinta destino
	 */
	public void setCintaDestino(String cintaDestino) {
		this.cintaDestino = cintaDestino;
	}

	/**
	 * Gets the estado vuelo destino.
	 *
	 * @return the estado vuelo destino
	 */
	public String getEstadoVueloDestino() {
		if (this.estadoVueloDestino.equals("")) {
			this.estadoVueloDestino = "--";
		}
		return estadoVueloDestino;
	}

	/**
	 * Sets the estado vuelo destino.
	 *
	 * @param estadoVueloDestino the new estado vuelo destino
	 */
	public void setEstadoVueloDestino(String estadoVueloDestino) {
		this.estadoVueloDestino = estadoVueloDestino;
	}

	/**
	 * Gets the aeropuerto destino.
	 *
	 * @return the aeropuerto destino
	 */
	public String getAeropuertoDestino() {
		if (this.aeropuertoDestino.equals("")) {
			this.aeropuertoDestino = "--";
		}
		return aeropuertoDestino;
	}

	/**
	 * Sets the aeropuerto destino.
	 *
	 * @param aeropuertoDestino the new aeropuerto destino
	 */
	public void setAeropuertoDestino(String aeropuertoDestino) {
		this.aeropuertoDestino = aeropuertoDestino;
	}

	/**
	 * Gets the nombre company.
	 *
	 * @return the nombre company
	 */
	public String getNombreCompany() {
		if (this.nombreCompany.equals("")) {
			this.nombreCompany = "--";
		}
		return nombreCompany;
	}

	/**
	 * Sets the nombre company.
	 *
	 * @param nombreCompany the new nombre company
	 */
	public void setNombreCompany(String nombreCompany) {
		this.nombreCompany = nombreCompany;
	}

	/**
	 * Gets the link info vuelo.
	 *
	 * @return the link info vuelo
	 */
	public String getLinkInfoVuelo() {
		return linkInfoVuelo;
	}

	/**
	 * Sets the link info vuelo.
	 *
	 * @param linkInfoVuelo the new link info vuelo
	 */
	public void setLinkInfoVuelo(String linkInfoVuelo) {
		this.linkInfoVuelo = linkInfoVuelo;
	}

	public Boolean getEscala() {
		return escala;
	}

	public void setEscala(Boolean escala) {
		this.escala = escala;
	}

	public String getAeropuertoIntermedio() {
		return aeropuertoIntermedio;
	}

	public void setAeropuertoIntermedio(String aeropuertoIntermedio) {
		this.aeropuertoIntermedio = aeropuertoIntermedio;
	}


	private boolean comprueba (Elements pDatos, int pPos){
		try {
			if (!pDatos.get(pPos).text().equals("")) {
				return true;
			} 
			return false;
		} catch (IndexOutOfBoundsException ex11) {
			Log.w("VuelosAndroid", "DatosVuelo - Constructor: " + "ERROR, Celda sin datos numero " + pPos);
			return false;
		}
	}

}