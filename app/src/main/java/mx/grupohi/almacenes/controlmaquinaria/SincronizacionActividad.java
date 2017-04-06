package mx.grupohi.almacenes.controlmaquinaria;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Actividad;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Actividades;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.SerializacionActividad;

/**
 * Created by LERDES2 on 24/03/2017.
 */

public class SincronizacionActividad {
    private Context context;
    private SQLiteDatabase db;
    private DBMaqSqlite db_maq;



    public SincronizacionActividad(Context context){
        this.context = context;
        db_maq = new DBMaqSqlite(context, "maq", null, 1);
    }

    /**
     * Recupera la informacón de la BD de la maquinaria que a iniciado actividad y la envia al webservice
     * @return boolean de que la tarea se ejecutó correctamente
     */
    public boolean sincronizarActividad(){
        Actividad activid;
        ArrayList<Actividad> listActividad = new ArrayList<>();
        SerializacionActividad serActividad = new SerializacionActividad();
        String url = context.getString(R.string.url_sinc_actividad);
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select reportes_actividad.id, almacenes.economico, reportes_actividad.fecha, reportes_actividad.horometro_inicial, reportes_actividad.horometro_final, reportes_actividad.kilometraje_inicial,\n" +
                "        reportes_actividad.kilometraje_final, reportes_actividad.operador, reportes_actividad.observaciones, reportes_actividad.created_at, reportes_actividad.creado_por, reportes_actividad.imei, reportes_actividad.estatus \n" +
                "        from reportes_actividad inner join almacenes on almacenes.id = reportes_actividad.id_almacen where reportes_actividad.estatus != 1;", null);
       try {
           if(c != null && c.moveToFirst()){
               do{
                   activid =  new Actividad();
                   activid.setId(c.getInt(0));
                   activid.setId_almacen(c.getInt(1));
                   activid.setFecha(c.getString(2));
                   activid.setHorometro_inicial(c.getDouble(3));
                   activid.setHorometro_final(c.getDouble(4));
                   activid.setKilometraje_inicial(c.getDouble(5));
                   activid.setKilometraje_final(c.getDouble(6));
                   activid.setOperador(c.getString(7));
                   activid.setObservaciones(c.getString(8));
                   activid.setCreated_at(c.getString(9));
                   activid.setCreado_por(c.getInt(10));
                   activid.setImei(c.getInt(11));
                   activid.setEstatus(c.getInt(12));

                   Gson gson = new Gson();
                   String json = gson.toJson(activid);

                   System.out.println(json);

               }while(c.moveToNext());

           }
       }catch (Exception e){
           Log.i("Error Gson  ", e.toString());
       }

        return true;

    }

    /**
     * Recupera de la BD la información de las actividades iniciadas de cada actividad de la maquinaria
     * @return boolean que indica si la tarea se ejecutó correctamente
     */
    public boolean sincronizarActividades(){
        Actividades actividades;
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select actividades.* from actividades where actividades.estatus != 2;", null);
        if(c!= null && c.moveToFirst()){
            do{
                actividades = new Actividades();
                actividades.setId(c.getInt(0));
                actividades.setId_reporte(c.getInt(1));
                actividades.setClave_actividad(c.getString(2));
                actividades.setTipo_hora(c.getString(3));
                actividades.setTurno(c.getInt(4));
                actividades.setHora_inicial(c.getString(5));
                actividades.setHora_final(c.getString(6));
                actividades.setCantidad(c.getDouble(7));
                actividades.setCon_cargo_empresa(c.getString(8));
                actividades.setObservaciones(c.getString(9));
                actividades.setCreated_at(c.getString(10));
                actividades.setCreado_por(c.getString(11));
                actividades.setImei(c.getInt(12));
                actividades.setEstatus(c.getInt(13));

                Gson gson = new Gson();
                String json = gson.toJson(actividades);

                System.out.println(json);

                //enviar a webservice


            }while (c.moveToNext());
        }

        return true;
    }

}
