package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Almacenes;

/**
 * Created by LERDES2 on 09/03/2017.
 */

public class ProcesosActividad {
    private SQLiteDatabase db;
    private DBMaqSqlite db_maq;
    private Context context;
    private ArrayList<Almacenes> maquinas;

    public ProcesosActividad(Context context){
        db_maq = new DBMaqSqlite(context, "maq", null, 1);
        this.context = context;
    }

    /**
     * Busca en la base de datos la maquinaria que aun no tiene iniciada una acividad
     * @return Array String[] con los datos de la maquinaria recuperada de BD
     */
    public String[] getMaquinaria(){
        maquinas = new ArrayList<>();
        Almacenes almacen;
        String[] maquinaria = null;
        int val = 1;
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT almacenes.id, almacenes.economico, almacenes.descripcion FROM almacenes left join reportes_actividad on reportes_actividad.id_almacen = almacenes.id and reportes_actividad.horometro_final is null where reportes_actividad.id is null;", null);
        if(c != null && c.moveToFirst()){
            try {
                maquinaria = new String[c.getCount()+1];
                maquinaria[0] = "Seleccione una Maquinaria";
                do{
                    almacen = new Almacenes();
                    almacen.setId(c.getInt(0));
                    almacen.setEconomico(c.getInt(1));
                    almacen.setDescripcion(c.getString(2));
                    maquinas.add(almacen);
                    maquinaria[val] = c.getString(1) + " " + c.getString(2);
                    val++;
                }
                while(c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
                db.close();
            }
        }

        return maquinaria;
    }

    /**
     * Metodo que regresa un Array de objetos con los datos completos de la maquinaria recuperada de la busquda hecha en el metodo
     * getMaquinaria()
     * @return ArrayList<Almacenes> con los datos de la maquinara recuperada de BD
     */
    public ArrayList<Almacenes> getMaquinas(){
        if(maquinas != null)
            return maquinas;
        return null;
    }

    /**
     * Guarda la actividad de la maquina seleccionada
     * @param datos parametro con los datos de la maquinaria que va a iniciar actividad
     * @return boolean que indica si se guardó correctamente la información
     */
    public boolean registrarActividad(ContentValues datos){
        db = db_maq.getWritableDatabase();
        return db.insert("reportes_actividad", null, datos) > -1;
    }
}
