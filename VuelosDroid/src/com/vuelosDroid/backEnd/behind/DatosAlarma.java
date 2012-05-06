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
	
	public DatosAlarma(DatosVuelo pDatos, int pId){
		this(pDatos, pId, 0, 0, 0, 0, 0);
	}
	
	public DatosAlarma(DatosVuelo pDatos, int pId, int pSonido, int pAterrizar,
						int pDespegar, int pCambios,int pMinutos){
		this.datos = pDatos;
		this.id = pId;
		this.sonido = pSonido;
		this.aterrizar = pAterrizar;
		this.despegar = pDespegar;
		this.cambios = pCambios;
		this.minutos = pMinutos;
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
	
}
