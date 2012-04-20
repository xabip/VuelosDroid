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
	public static final String ALARMA = "alarma";
	public static final String EMPEZADO = "empezado";
	public static final String SALIDO = "salido";
	public static final String ATERRIZADOSIN = "aterrizado_sin";
	
	public static final String TAG = "VuelosAndroid";
	
	private String sqlCreate = "CREATE TABLE alarmas_aux (id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT ," +
			" nombre_vuelo TEXT, nombre_company TEXT, " +
			"aeropuerto_origen TEXT, fecha_origen TEXT, hora_origen TEXT, aeropuerto_destino TEXT, hora_destino TEXT," +
			"aterrizado_sin TEXT, alarma INTEGER,  empezado INTEGER, salido INTEGER)";

	
	public AlarmasSqlAux(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
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
    	
        //Se elimina la versi�n anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS alarmas_aux");
 
        //Se crea la nueva versi�n de la tabla
        db.execSQL(sqlCreate);
    }



}
