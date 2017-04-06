package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

/**
 * Created by LERDES2 on 08/03/2017.
 */

public class Login {
    Context context;
    private SQLiteDatabase db;
    private DBMaqSqlite db_sca;

    public Login(Context context){
        this.context = context;
        db_sca = new DBMaqSqlite(context, "maq", null, 1);
    }

    /**
     *  Registra en la base de datos el ucuario que inicia sesión
     * @param usuario Objeto que contiene los datos del usuario que se va a registrar
     * @return boolean si el registro de usuario cue exitoso
     */
    public Boolean guardarUsuario(Usuario usuario){
        db = db_sca.getWritableDatabase();
        Cursor a = db.rawQuery("SELECT * from usuario where nombre_usuario = '" + usuario.getNombre_usuario()+"';", null);
        if( a.getCount() == 0) {
            ContentValues data = new ContentValues();
            data.put("nombre_usuario", usuario.getNombre_usuario());
            data.put("descripcion", usuario.getDescripcion());
            data.put("imei", usuario.getImei());
            data.put("idusuario", usuario.getIdusuario());
            Boolean result = db.insert("usuario", null, data) > -1;
            db.close();
            return result;
        }else if( a.getCount() == 1){
            return  true;
        }
        a.close();
        db.close();
        return false;
    }

    /**
     *  Revisa si ya hay una obra registrada en la base de datos
     * @return boolean si ya existe una obra registrada en la base de datos
     */
    public Boolean validarObra(){
        db = db_sca.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM almacenes;", null);
        if(c.getCount() > 0){
            c.close();
            db.close();
            return true;
        }
        return false;
    }

}
