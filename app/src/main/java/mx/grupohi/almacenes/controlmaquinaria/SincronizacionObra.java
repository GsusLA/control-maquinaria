package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Obra;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.SerializacionObra;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;


/**
 * Created by LERDES2 on 08/03/2017.
 */

public class SincronizacionObra {
    Context context;
    private SQLiteDatabase db;
    private DBMaqSqlite db_sca;

    ContentValues almacenMaq;


    public SincronizacionObra(Context context){
        this.context = context;
        db_sca = new DBMaqSqlite(context, "maq", null, 1);
    }

    /**
     * Guarda la información de la obra seleccionada en la base de datos
     * @param data objeto Obra con los datos de la obra seleccionada
     * @return boolean que indica si se guardó correctamente la información
     */
    public Boolean guardarObra(Obra data){
        db = db_sca.getWritableDatabase();
        ContentValues datos = new ContentValues();
        datos.put("idobra", data.getIdObra());
        datos.put("nombre", data.getNombre());
        datos.put("descripcion", data.getDescripcion());
        datos.put("base", data.getBase());
        datos.put("token", data.getToken());
        return db.insert("obra", null, datos)> -1;
    }

    /**
     * Guarda los datos de la maquina que se sincroniza del web service
     * @param data contenedor de datos con la información de la maquina a registrar en BD
     * @return boolean que indica si se registro correctamente la maquinaria en BD
     */
    public Boolean guardarMaquinaria(ContentValues data){
        db = db_sca.getWritableDatabase();
        return db.insert("almacenes", null, data)> -1;
    }

    /**
     * Actualiza el token de la obra cada que se reinicia sesión programada
     * @param token string con el valor del token
     * @return boolean que indica si se guardo correctamente el token
     */
    public boolean guardarToken(String token){
        ContentValues datos = new ContentValues();
        datos.put("token", token);
        db = db_sca.getWritableDatabase();
        boolean res = db.update("obra", datos, " id = 1", null) > -1;
        db.close();
        return res;
    }

    /**
     * Recupera del web service los datos de la maquinaria que esta asignada a la obra que se selecciono
     * para sincronizar
     * @param seleccionada objeto Obra con los datos necesarios de sincronización
     * @return boolean que indica si la sincronización de maquinaria se hizo correctamente
     */
    public boolean sincAlmacenes(Obra seleccionada){
        try {
            URL url = new URL(context.getString(R.string.url_almacenes));
            ContentValues datos = new ContentValues();
            datos.put("token", seleccionada.getToken());
            datos.put("base", seleccionada.getBase());
            datos.put("idObra", seleccionada.getIdObra());
            JSONObject resp =  HttpConnection.GET(url, datos);

            JSONArray almacen = resp.getJSONArray("almacenes");
            for(int i = 0; i < almacen.length(); i++){
                JSONObject maquina = almacen.getJSONObject(i);
                almacenMaq = new ContentValues();
                almacenMaq.put("economico", maquina.getInt("id_almacen"));
                almacenMaq.put("descripcion", maquina.getString("descripcion"));

                if(guardarMaquinaria(almacenMaq)){
                    System.out.println("Guardar Maquinaria  "+ "OK" + maquina.getString("descripcion"));
                }
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * envia el dato de la obra seleccionada por el usuario al web service
     * @param seleccionada
     * @param user
     * @return
     */
    public boolean sincObraLinea(Obra seleccionada, Usuario user){
        SerializacionObra sincronizacion = new SerializacionObra();
        sincronizacion.obra = seleccionada;
        sincronizacion.usuario = user;
        Gson gson = new Gson();
        String json = gson.toJson(sincronizacion);

        // enviar datos de obra del Json al web service

        return true;
    }

}
