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

package com.vuelosDroid.backEnd.behind;

import com.vuelosDroid.backEnd.scrapper.DatosVuelo;

/**
 * 
 * @author Xabi
 *
 */
public class DatosAlarma {

	private DatosVuelo datos;
	private int id;
	private int sonido;
	private int aterrizar;
	private int despegar;
	private int cambios;
	private int minutos;
	private int estado;
	private int aterrizadoSin;
	private int despegadoSin;
	
	public DatosAlarma(DatosVuelo pDatos, int pId){
		this(pDatos, pId, 0, 0, 0, 0, 0, 0, 0, 0);
	}
	
	public DatosAlarma(DatosVuelo pDatos, int pId, int pSonido, int pAterrizar,
						int pDespegar, int pCambios,int pMinutos, int pEstado, int pAterrizadoSin, int pDespegadoSin){
		this.datos = pDatos;
		this.id = pId;
		this.sonido = pSonido;
		this.aterrizar = pAterrizar;
		this.despegar = pDespegar;
		this.cambios = pCambios;
		this.minutos = pMinutos;
		this.estado = pEstado;
		this.setAterrizadoSin(pAterrizadoSin);
		this.setDespegadoSin(pDespegadoSin);
	}

	public DatosVuelo getDatos() {
		return datos;
	}

	public void setDatos(DatosVuelo datos) {
		this.datos = datos;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSonido() {
		return sonido;
	}

	public void setSonido(int sonido) {
		this.sonido = sonido;
	}

	public int getAterrizar() {
		return aterrizar;
	}

	public void setAterrizar(int aterrizar) {
		this.aterrizar = aterrizar;
	}

	public int getDespegar() {
		return despegar;
	}

	public void setDespegar(int despegar) {
		this.despegar = despegar;
	}

	public int getCambios() {
		return cambios;
	}

	public void setCambios(int cambios) {
		this.cambios = cambios;
	}

	public int getMinutos() {
		return minutos;
	}

	public void setMinutos(int minutos) {
		this.minutos = minutos;
	}

	public int getEstado() {
		return estado;
	}

	public void setEstado(int estado) {
		this.estado = estado;
	}

	public int getAterrizadoSin() {
		return aterrizadoSin;
	}

	public void setAterrizadoSin(int aterrizadoSin) {
		this.aterrizadoSin = aterrizadoSin;
	}

	public int getDespegadoSin() {
		return despegadoSin;
	}

	public void setDespegadoSin(int despegadoSin) {
		this.despegadoSin = despegadoSin;
	}
	
}
