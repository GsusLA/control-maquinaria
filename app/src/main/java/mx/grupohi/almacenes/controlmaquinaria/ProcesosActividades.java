package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by LERDES2 on 10/03/2017.
 */

public class ProcesosActividades {
    private Context context;
    private SQLiteDatabase db;
    private DBMaqSqlite db_maq;

    public ProcesosActividades(Context context){
        db_maq = new DBMaqSqlite(context, "maq", null, 1);
        this.context = context;
    }

    /**
     *  Metodo que revisa si la maquinaria seleccionada tiene actividades iniciadas, en caso de que si se tenga una actividad registrada
     *  en BD se regresaran los datos recuperados para que se haga el cierre de la misma en el fragment CierreActividades, en caso de que
     *  no haya registro se retorna "vacio" para que se inicie el fragment de InicioActividades
     * @param id int con el id de la actividad iniciada
     * @return String con los datos recuperados de la BD o con "vacio" en caso de que no existan registros
     */
    public String validarActividadMaquina(int id){
        String resp = "vacio";
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select actividades.*, reportes_actividad.fecha from actividades inner join reportes_actividad on actividades.id_reporte = reportes_actividad.id AND actividades.hora_final is null where actividades.id_reporte = "+id+";", null);
        if(c.getCount() >0 && c.moveToFirst()){
            JSONObject actividad = new JSONObject();
            try {
                actividad.put("id_actividad", c.getInt(0));
                actividad.put("id_reporte", c.getInt(1));
                actividad.put("clave_actividad", c.getString(2));
                actividad.put("tipo_hora", c.getString(3));
                actividad.put("turno", c.getInt(4));
                actividad.put("hora_inicial", c.getString(5));
                actividad.put("con_cargo_empresa", c.getString(8));
                actividad.put("observaciones", c.getString(9));
                actividad.put("fecha", c.getString(14));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return actividad.toString();
        }
        return resp;
    }

    /**
     *  recupera un listado de la maquinaria que ya tenga iniciada su actividad
     * @return ArrayList con los datos de la maquinaria que ya tienen actividad iniciada
     */
    public ArrayList<ContentValues> getMaquinariaActiva(){
        ArrayList<ContentValues> listaActividad = null;
        ContentValues actividad;
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select reportes_actividad.id, almacenes.economico, almacenes.descripcion from almacenes " +
                                "inner join reportes_actividad on reportes_actividad.id_almacen = almacenes.id where reportes_actividad.horometro_final is null;", null);
        if(c != null && c.moveToFirst()){
            listaActividad = new ArrayList<>();
            do{
                actividad = new ContentValues();
                actividad.put("id_actividad", c.getInt(0));
                actividad.put("economico", c.getString(1));
                actividad.put("descripcion", c.getString(2));
                listaActividad.add(actividad);
            } while (c.moveToNext());
        }
        db.close();
        return listaActividad;
    }


    /**
     * A partir de aqui se incluiran los procesos relacionados con el fragment de incio de actividades
     *
     * Guarda la información de inicio de actividades de la maquinaria seleccionada
     */

    public boolean guardarInicioActividades(ContentValues datos){
            db_maq = new DBMaqSqlite(context, "maq", null, 1);
            db = db_maq.getWritableDatabase();
        return db.insert("actividades", null, datos ) > -1;
    }


    /**
     * A partir de aqui se incluiran los procesos relacionados con el fragment de Cierre de actividades
     *
     * Guarda la información del cierre de la actividades de la maquinaria seleccionada
     */

    public boolean guardarCierreActividades(ContentValues datos, int id){
        try {
            db_maq = new DBMaqSqlite(context, "maq", null, 1);
            db = db_maq.getWritableDatabase();
            return db.update("actividades", datos, " id = " + id, null ) > -1;
        }catch (Exception err){
            err.printStackTrace();
        }
        return false;
    }

}
