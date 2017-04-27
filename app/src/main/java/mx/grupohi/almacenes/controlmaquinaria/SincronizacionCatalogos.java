package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by LERDES2 on 21/03/2017.
 */

public class SincronizacionCatalogos {
    Context context;
    SQLiteDatabase db;
    DBMaqSqlite db_maq;


    int actualizado = 0;
    int nuevos = 0;
    int totalAlmacen = 0;
    public SincronizacionCatalogos(Context context){
        this.context = context;
        db_maq = new DBMaqSqlite(context, "maq", null, 1);
    }

    /**
     * Metodo que sincroniza la actualización del catalogo de maquinaria de la obra seleccionada,
     * de manera que agrega registros de maquinaria nueva o actualiza los datos de la maquinaria
     * registrada en BD
     */
    public void sincCatalogosMaquinaria(){
        try {
            URL url = new URL(context.getString(R.string.url_almacenes));
            ContentValues datos = Util.getDatosObra(context);

            if(datos == null) return;

            JSONObject resp = HttpConnection.GET(url, datos);
            JSONArray almacen = resp.getJSONArray("almacenes");

            for(int i = 0; i < almacen.length(); i++) {
                totalAlmacen = almacen.length();
                JSONObject maquina = almacen.getJSONObject(i);
                if(!compararEconomico(maquina.getInt("id_almacen"), maquina.getString("descripcion"))){
                    if(insertarNuevo(maquina.getInt("id_almacen"), maquina.getString("descripcion"))){
                        nuevos++;
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo de apoyo para la sincronización del catalogo de maquinaria que compara el numero económico de la maquinaria en
     * la BD con la que se recupera del web service, con lo cual se valida si esa maquinaria ya esta registrada en BD o no,
     * si esta registrada,, entonces vlida que la descripcion sea la misma, de lo contrario actualiza la el campo descripcion.
     * @param economico numero economico de la maquinaria que se obtiene del web service
     * @param descripcion descripcion de la maquinaria ligada al numero económico de la maquinaria
     * @return
     */
    private boolean compararEconomico(int economico, String descripcion){
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT almacenes.* FROM almacenes WHERE almacenes.economico = " + economico + " ;", null);
        try {
            if(c != null && c.moveToFirst()){
                if(!descripcion.equals(c.getString(2))){
                    ContentValues datos = new ContentValues();
                    datos.put("economico", economico);
                    datos.put("descripcion", descripcion);
                    if(db.update("almacenes", datos, " economico = " + economico, null) > -1){
                        actualizado++;
                        return true;
                    }
                }
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            c.close();
            db.close();
        }
        return false;
    }

    /**
     * Metodo de apoyo para la sincronización del catalogo de maquinaria que inserta un nuevo registro en caso de que
     * no hara registro de la maquina en la BD
     * @param economico
     * @param descripcion
     * @return
     */
    private boolean insertarNuevo(int economico, String descripcion){
        db = db_maq.getWritableDatabase();
        ContentValues datos = new ContentValues();
        datos.put("economico", economico);
        datos.put("descripcion", descripcion);
        try {
             return db.insert("almacenes", null, datos) > -1;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.close();
        }
        return false;
    }

    public int getActualizado() {
        return actualizado;
    }

    public int getNuevos() {
        return nuevos;
    }

    public int getTotalAlmacen() {
        return totalAlmacen;
    }
}
