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
        Cursor c = db.rawQuery("SELECT nombre_usuario FROM usuario;", null);
        try{
            if(c != null && c.moveToFirst()){
                return c.getString(c.getColumnIndex("nombre_usuario"));
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
     * Recupera de la BD los datos e la obra que esta sincronizada en el dispositivo móvil
     * @return
     */
    static ContentValues getDatosObra(Context context){
        ContentValues obra = null;
        DBMaqSqlite db_sca = new DBMaqSqlite(context, "maq", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("Select obra.idobra, obra.base, obra.token from obra;", null);
        if(c != null && c.moveToFirst()){
            obra = new ContentValues();
            obra.put("idObra", c.getInt(0));
            obra.put("base", c.getString(1));
            obra.put("token", c.getString(2));
        }
        c.close();
        db.close();
        return obra;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("es","ES"));
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    /**
     *  Obtiene la hora actual del sistema operativo
     * @return String con la hora actual en formato HH:mm AM/PM
     */
    public static String getHora(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    /**
     *  Obtiene la fecha y hora actual del sistema operativo
     * @return String con los datos de fecha y hora actuales en formato dd/MM/aaaa HH:mm AM/PM en español
     */
    static String getDateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" , new Locale("es","ES"));
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    /**
     * Metodo que retorna la cantidad de horas entre dos cadenas de texto
     * @param hActual string con el dato de la hora inicial
     * @param hActividad string con el dato de la hora final
     * @return la cantidad de horas entre los dos valores
     */
    static Double getDoubleHora(String hActual, String hActividad){
        String[] init = hActividad.split(":| ");
        String[] fin = hActual.split(":| ");

        int horasInicio = Integer.parseInt(init[0]);
        int minutosInicio = Integer.parseInt(init[1]);

        int horasFin = Integer.parseInt(fin[0]);
        int minutosFin = Integer.parseInt(fin[1]);

        int horasTot = 0;
        int minutosTot = 0;

        if(horasInicio > horasFin){
            horasTot = (horasFin + 24) - horasInicio;
        }else{
            horasTot = horasFin - horasInicio;
        }

        if(minutosInicio > minutosFin){
            horasTot-= 1;
            minutosTot = (minutosFin + 60) - minutosInicio;
        }else{
            minutosTot = minutosFin - minutosInicio;
        }

        if(minutosTot < 10)
            return Double.parseDouble(horasTot+".0"+minutosTot);
        if(minutosTot == 0)
            return Double.parseDouble(horasTot+".01");

        return Double.parseDouble(horasTot+"."+minutosTot);
    }

    static String getDatoMaquinaria(Context context, int id){
        DBMaqSqlite db_sca = new DBMaqSqlite(context, "maq", null, 1);
        SQLiteDatabase db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("select * from almacenes where id = " + id, null);
        try{
            if(c != null && c.moveToFirst()){
                return "" + c.getInt(1) + "-" + c.getString(2);
            }
        }finally {
            assert c != null;
            c.close();
            db.close();
        }

        return "";
    }
}
