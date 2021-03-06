package mx.grupohi.almacenes.controlmaquinaria;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by LERDES2 on 07/03/2017.
 */

public class DBMaqSqlite extends SQLiteOpenHelper {
    public DBMaqSqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static String[] queries = new String[]{
            "CREATE TABLE usuario (id INTEGER PRIMARY KEY AUTOINCREMENT, idusuario INTEGER, descripcion VARCHAR, nombre_usuario VARCHAR, clave VARCHAR, imei VARCHAR);",
            "CREATE TABLE obra (ID INTEGER PRIMARY KEY AUTOINCREMENT, idobra INTEGER, nombre TEXT, descripcion VARCHAR, base TEXT, token VARCHAR)",
            "CREATE TABLE almacenes (id INTEGER PRIMARY KEY AUTOINCREMENT, economico INTEGER, descripcion VARCHAR);",
            "CREATE TABLE reportes_actividad (id INTEGER  PRIMARY KEY AUTOINCREMENT, id_almacen INTEGER, fecha DATE, horometro_inicial DOUBLE, horometro_final DOUBLE, kilometraje_inicial INT, kilometraje_final INT, operador VARCHAR, observaciones TEXT, created_at VARCHAR, creado_por VARCHAR, imei VARCHAR, estatus INTEGER);",
            "CREATE TABLE actividades ( id INTEGER PRIMARY KEY AUTOINCREMENT, id_reporte INTEGER, clave_actividad VARCHAR, tipo_hora VARCHAR, turno INTEGER, hora_inicial STRING, hora_final STRING, cantidad DOUBLE, con_cargo_empresa VARCHAR, observaciones TEXT, created_at VARCHAR, creado_por VARCHAR, imei VARCHAR, estatus INTEGER);",
            "CREATE TABLE sesiones_movil ( id INTEGER PRIMARY KEY AUTOINCREMENT, usuario VARCHAR, imei VARCHAR, fecha DATE, estatus INT, created_at VARCHAR);"
    };

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String query: queries){
            // prueba
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
