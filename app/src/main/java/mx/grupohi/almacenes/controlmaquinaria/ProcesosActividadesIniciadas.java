package mx.grupohi.almacenes.controlmaquinaria;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Actividades;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Almacenes;

/**
 * Created by LERDES2 on 15/03/2017.
 */

public class ProcesosActividadesIniciadas {
    SQLiteDatabase db;
    DBMaqSqlite db_maq;
    Context context;

    public ProcesosActividadesIniciadas(Context context){
        this.context = context;
        db_maq = new DBMaqSqlite(context, "maq", null, 1);
    }

    /**
     *  Recupera de la BD un listado de maquinaria que ya tiene actividad iniciada
     * @return Arreglo de objetos Almacenes con los datos de la maquinaria recuperada de BD
     */
    public ArrayList<Almacenes> obtenerMaquinaria(){
        ArrayList<Almacenes> maquinas = new ArrayList<>();
        Almacenes maquina;
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select almacenes.* from almacenes inner join reportes_actividad on reportes_actividad.id_almacen = almacenes.id;", null);
        try {
            if(c != null && c.moveToFirst()){
                do{
                    maquina = new Almacenes();
                    maquina.setId(c.getInt(0));
                    maquina.setEconomico(c.getInt(1));
                    maquina.setDescripcion(c.getString(2));
                    maquinas.add(maquina);
                }while (c.moveToNext());
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            c.close();
            db.close();
        }
        return maquinas;
    }

    /**
     * Obtiene el detalle de todas las actividades realizadas por la maquinaria seleccionada
     * @param id_almacen es el identificador de la maquinaria que se sequiere el detalle de actividades
     *                   realizadas.
     * @return Arreglo con la informacion de todas las actividades registradas de la maquinaria seleccionada
     */
    public ArrayList<Actividades> obtenerDetalleActividades(int id_almacen){
        ArrayList<Actividades> actividades = new ArrayList<>();
        Actividades activ;
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select actividades.* from actividades inner join reportes_actividad on reportes_actividad.id = actividades.id_reporte where reportes_actividad.id_almacen = " + id_almacen, null);
        try {
            if(c != null && c.moveToFirst()){
                do{
                    activ = new Actividades();
                    activ.setId(c.getInt(0));
                    activ.setId_reporte(c.getInt(1));
                    activ.setClave_actividad(c.getString(2));
                    activ.setTipo_hora(c.getString(3));
                    activ.setTurno(c.getInt(4));
                    activ.setHora_inicial(c.getString(5));
                    activ.setHora_final(c.getString(6));
                    activ.setCantidad(c.getDouble(7));
                    activ.setCon_cargo_empresa(c.getString(8));
                    activ.setObservaciones(c.getString(9));
                    actividades.add(activ);

                }while (c.moveToNext());
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            c.close();
            db.close();
        }
        return actividades;
    }

}
