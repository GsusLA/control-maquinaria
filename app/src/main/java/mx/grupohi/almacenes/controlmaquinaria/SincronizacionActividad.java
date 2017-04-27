package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
    ContentValues datos;



    public SincronizacionActividad(Context context){
        this.context = context;
        db_maq = new DBMaqSqlite(context, "maq", null, 1);
        datos = Util.getDatosObra(context);
    }

    /**
     * Recupera la informacón de la BD de la maquinaria que a iniciado actividad y la envia al webservice
     * @return boolean de que la tarea se ejecutó correctamente
     */
    public boolean sincronizarActividad() {
        Actividad activid;
        ArrayList<Actividad> listActividad = new ArrayList<>();
        SerializacionActividad serActividad = new SerializacionActividad();

        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select reportes_actividad.id, almacenes.economico, reportes_actividad.fecha, reportes_actividad.horometro_inicial, reportes_actividad.horometro_final, reportes_actividad.kilometraje_inicial,\n" +
                "        reportes_actividad.kilometraje_final, reportes_actividad.operador, reportes_actividad.observaciones, reportes_actividad.created_at, reportes_actividad.creado_por, reportes_actividad.imei, reportes_actividad.estatus \n" +
                "        from reportes_actividad inner join almacenes on almacenes.id = reportes_actividad.id_almacen where reportes_actividad.estatus != 1 and reportes_actividad.kilometraje_final is not null;", null);
       try {
           URL url = new URL(context.getString(R.string.url_sinc_actividad));
           if(c != null && c.moveToFirst()){
               do{
                   activid =  new Actividad();
                   activid.setId(c.getInt(0));
                   activid.setId_almacen(c.getInt(1));
                   activid.setFecha(c.getString(2));
                   activid.setHorometro_inicial(c.getDouble(3));
                   activid.setHorometro_final(c.getDouble(4));
                   activid.setKilometraje_inicial(c.getInt(5));
                   activid.setKilometraje_final(c.getInt(6));
                   activid.setOperador(c.getString(7));
                   activid.setObservaciones(c.getString(8));
                   activid.setCreated_at(c.getString(9));
                   activid.setCreado_por(c.getInt(10));
                   activid.setImei(c.getString(11));
                   activid.setEstatus(c.getInt(12));

                   Gson gson = new Gson();
                   String json = gson.toJson(activid);

                   datos.put("json", json);
                   System.out.println(json);

                   JSONObject resp = HttpConnection.SINCPOST(url, datos);

                   if(resp.getInt("status_code") == 200){
                       ContentValues datos = new ContentValues();
                       datos.put("estatus", 1);
                        db.update("reportes_actividad",datos,  "id = " + activid.getId() , null);
                       sincronizarActividades(resp.getInt("last_insert_id"), activid.getId());
                   }else{
                       continue;
                   }

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
    public boolean sincronizarActividades(int id_sinc, int id_reporte){

        Actividades actividades;
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select actividades.* from actividades where actividades.estatus != 2 and actividades.hora_final is not null and actividades.id_reporte = " + id_reporte + " ;", null);
        try{
            URL url = new URL(context.getString(R.string.url_sinc_actividades, id_sinc ));
            if(c!= null && c.moveToFirst()){
                do{
                    actividades = new Actividades();
                    actividades.setId(c.getInt(0));
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
                    actividades.setImei(c.getString(12));
                    actividades.setEstatus(c.getInt(13));

                    Gson gson = new Gson();
                    String json = gson.toJson(actividades);
                    datos.put("json", json);
                    System.out.println(json);

                    //enviar a webservice
                    JSONObject resp = HttpConnection.SINCPOST(url, datos);

                    if(resp.getString("message").equals("success")){
                        ContentValues datos = new ContentValues();
                        datos.put("estatus", 1);
                        db.update("actividades", datos,  "id = " + actividades.getId(), null);
                    }



                }while (c.moveToNext());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
