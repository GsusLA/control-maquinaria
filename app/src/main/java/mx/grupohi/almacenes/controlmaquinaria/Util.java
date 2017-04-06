package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

import static android.R.attr.id;

/**
 * Created by LERDES2 on 07/03/2017.
 *
 *  Clase que contiene metodos staticos para pedir datos diversos utiles para la ejecucion de la aplicación
 */

public class Util {
    static String getQuery(ContentValues values) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : values.valueSet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }
        return result.toString();
    }

    static boolean isNetworkStatusAvialable (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if(netInfos != null)
                if(netInfos.isConnected())
                    return true;
        }
        return false;
    }

    /**
     *  Recupera el IMEI del dispositivo
     * @param context el contexto de la actividad que requiere la información
     * @return
     */
    static String deviceImei(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     *  Obtiene el ID de usuario registrado en la base de datos
     * @param context el contexto de la actividad que requiere la información
     * @return String con el id del usuario registrado
     */
    static String getIdUsuario(Context context){
        DBMaqSqlite db_sca = new DBMaqSqlite(context, "maq", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT idusuario FROM usuario;", null);
        try{
            if(c != null && c.moveToFirst()){
                return c.getString(c.getColumnIndex("idusuario"));
            }
        }finally {
            assert c != null;
            c.close();
            db.close();
        }
        return "FALSE";
    }

    /**
     *  Recupera los datos del usuario registrado en la base de datos
     * @param context el contexto de la actividad que requiere la información
     * @return El objeto usuario conteniendo los datos recuperados
     */
    static Usuario obtenerUsuario(Context context){
        DBMaqSqlite db_sca = new DBMaqSqlite(context, "maq", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Usuario usuario = new Usuario();
        Cursor c = db.rawQuery("SELECT * FROM usuario;", null);
        try{
            if(c != null && c.moveToFirst()){
                usuario.setNombre_usuario(c.getString(3));
                usuario.setDescripcion(c.getString(2));
                usuario.setIdusuario(c.getInt(1));
                return usuario;
            }
        }finally {
            assert c != null;
            c.close();
            db.close();
        }
        return null;
    }

    /**
     *  Actualiza el token de conexion a la API con la cual se crea la sesón en el servicio web, se actualiza
     *  cada vez que se requiere que el usuario reinicie su sesión en la aplicación
     * @param context el contexto de la actividad que requiere la información
     * @param token String con el token recuperado al momento de reinciar sesión
     * @param usuario String con el dato del usuario que esta reiniciando sesión
     * @return boolean que indica si la actualizacion del token fue exitosa
     */
    static boolean guardarToken(Context context, String token, String usuario){
        DBMaqSqlite db_sca = new DBMaqSqlite(context, "maq", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Usuario usr = obtenerUsuario(context);

         if(usr == null)
             return false;

        if(!TextUtils.equals(usuario, usr.getNombre_usuario()))
            return false;

        ContentValues datos = new ContentValues();
        datos.put("token", token);
        return db.update("obra", datos, " id = 1;", null ) > -1;
        //return  false;
    }

    /**
     *  Obtiene la fecha actual del sistema operativo
     * @return String con la fecha en formato dd/MMM/aa en español
     */
    static String getfecha(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy", new Locale("es","ES"));
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    /**
     *  Obtiene la hora actual del sistema operativo
     * @return String con la hora actual en formato HH:mm AM/PM
     */
    static String getHora(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm aa");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    /**
     *  Obtiene la fecha y hora actual del sistema operativo
     * @return String con los datos de fecha y hora actuales en formato dd/MM/aaaa HH:mm AM/PM en español
     */
    static String getDateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm aa" , new Locale("es","ES"));
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }
}