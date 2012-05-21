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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlarmasSqlAux extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "alarmas_aux.db";
	public static final String ID = "id";
	public static final String URL = "url";
	public static final String NOMBREVUELO = "nombre_vuelo";
	public static final String NOMBRECOMPANY = "nombre_company";
	public static final String AEROPUERTOORIGEN = "aeropuerto_origen";
	public static final String FECHAORIGEN = "fecha_origen";
	public static final String HORAORIGEN = "hora_origen";
	public static final String HORADESTINO = "hora_destino";
	public static final String AEROPUERTODESTINO = "aeropuerto_destino";
	public static final String ESTADOORIGEN = "estado_origen";
	public static final String ESTADODESTINO = "estado_destino";
	public static final String ALARMA = "alarma"; //Necesita alarma
	public static final String EMPEZADO = "empezado"; 
	public static final String SALIDO = "salido";   //Si ha despegado
	public static final String ATERRIZADOSIN = "aterrizado_sin";  //Aterrizado sin conexion
	public static final String DESPEGADOSIN = "despegado_sin";  //Despegado sin conexion
	public static final String ATERRIZAR = "aterrizar"; //Aviso al aterrizar
	public static final String DESPEGAR = "despegar";  //Aviso al despegar
	public static final String CAMBIOS = "retrasos"; //Aviso al retrasar
	public static final String SONIDO = "sonido"; //Sonido
	public static final String MINUTOS = "minutos"; 
	public static final String DESPEGADO = "despegado";  //Si ha despegado
	public static final String ESTADO = "estado";
	
	public static final String TAG = "VuelosAndroid";
	
	private String sqlCreate = "CREATE TABLE alarmas_aux (id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT ," +
			" nombre_vuelo TEXT, nombre_company TEXT, " +
			"aeropuerto_origen TEXT, fecha_origen TEXT, hora_origen TEXT, aeropuerto_destino TEXT, hora_destino TEXT," +
			"aterrizado_sin TEXT, alarma INTEGER,  empezado INTEGER, salido INTEGER, aterrizar INTEGER, despegar INTEGER, " +
			"retrasos INTEGER, sonido INTEGER, minutos INTEGER, estado_origen TEXT, estado_destino TEXT, " +
			"despegado_sin TEXT, despegado TEXT, estado INTEGER)";

	
	public AlarmasSqlAux(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	
	public AlarmasSqlAux(Context context){
		super(context, DATABASE_NAME, null, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Creando BD");
		db.execSQL(sqlCreate);
	}
	
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
    	Log.i(TAG, "Actualizando la BD");
    	
        //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS alarmas_aux");
 
        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }



}
