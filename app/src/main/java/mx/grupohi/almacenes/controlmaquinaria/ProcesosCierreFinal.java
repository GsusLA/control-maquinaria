package mx.grupohi.almacenes.controlmaquinaria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Actividad;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Almacenes;

/**
 * Created by LERDES2 on 13/03/2017.
 */

public class ProcesosCierreFinal {

    SQLiteDatabase db;
    DBMaqSqlite db_maq;
    Context context;

    private Double[] horas;
    private Double horast = 0.0;

    ArrayList<Almacenes> listaCierre;

    public ProcesosCierreFinal(Context context){
        this.context = context;
        db_maq = new DBMaqSqlite(context, "maq", null, 1);
    }

    /**
     * Registra en BD el cierre de una actividad de la maquinaria seleccionada
     * @param datos contiene los datos de cierre de actividad
     * @param id identificador de la actividad que se cerrará
     * @return boolean que indica si el registro fue exitoso
     */
    public boolean cerrarActividad(ContentValues datos, int id){
        db = db_maq.getWritableDatabase();
        return db.update("reportes_actividad", datos, " id = " + id , null) > -1;
    }

    /**
     * Recupera de la BD el listado de la maquinaria que cuenta con una actividad iniciada
     * @return Arreglo de todas la maquinaria con actividad registrada
     */
    public ArrayList<Almacenes> getMaquinariaIniciada(){
        listaCierre = new ArrayList<>();
        Almacenes almacen;
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select reportes_actividad.id, almacenes.economico, almacenes.descripcion from almacenes inner join reportes_actividad on reportes_actividad.id_almacen = almacenes.id where reportes_actividad.horometro_final is null;", null);
        if(c != null && c.moveToFirst()){
            do{
                almacen = new Almacenes();
                almacen.setId(c.getInt(0));
                almacen.setEconomico(c.getInt(1));
                almacen.setDescripcion(c.getString(2));

                listaCierre.add(almacen);

            }while (c.moveToNext());
        }else{
            return null;
        }
        db.close();
        c.close();
        return listaCierre;
    }

    /**
     * Recupera la información de la actividad iniciada que se solicita
     * @param id identificador de la actividad que se requiere la información para el cierre
     * @return el objeto Actividad con los datos recuperados de la BD
     */
    public Actividad getActividad(int id){
        Actividad actv = new Actividad();
        db = db_maq.getWritableDatabase();
        Cursor c = db.rawQuery("select * from reportes_actividad where reportes_actividad.id = " + id, null);
        try {
            if(c != null && c.moveToFirst()){
                actv.setId(c.getInt(0));
                actv.setId_almacen(c.getInt(1));
                actv.setHorometro_inicial(c.getDouble(3));
                actv.setKilometraje_inicial(c.getDouble(5));
                actv.setOperador(c.getString(7));
                actv.setObservaciones(c.getString(8));
                return actv;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            c.close();
            db.close();
        }
        return null;
    }

    /**
     * Metodo que verifica si la maquinaria seleccionada tiene sus actividades finalizadas, y al mismo tiempo hace la
     * sumatoria de las horas que se reportarón
     * @param idReporte identificador de la actividad que se requiere validar si tiene todas sus actividades finalizadas
     * @return boolean si tiene o no las actividades concluidas.
     */
    public boolean validarActividadesFinalizadas(int idReporte){
        db = db_maq.getWritableDatabase();
        int i = 0;
        Cursor c = db.rawQuery("select actividades.cantidad from actividades where actividades.id_reporte = " + idReporte + ";", null);
        try {
            if(c != null && c.moveToFirst()){
                horas = new Double[c.getCount()];
                do{
                    if((horas[i] = c.getDouble(0)) != 0.0) {
                        i++;
                    }
                    else{
                        return false;
                    }

                }while (c.moveToNext());

            }else if (c.getCount() == 0){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.close();
            c.close();
        }
        return getHorasTotales(horas);
    }

    /**
     * Calcula la cantidad de horas totales de las actividades realizadas por la maquinaria en el dia
     * @param hras Listado de las horas de las actividades hechas
     * @return boolean que valida el fin del calculo de las horas totales
     */
    private boolean getHorasTotales(Double[] hras){
        int hrs = 0;
        int mins = 0;
        for(int i=0; i< hras.length; i++){
                double valor = hras[i];
                hrs += (int) valor;
                mins += (valor - (int) valor)*100;
        }
        if(mins > 59) {
            do {
                mins -= 60;
                hrs += 1;
            } while (mins > 59);
        }
            if(mins < 10)
                horast = Double.parseDouble(hrs + ".0" + mins);
            else
                horast = Double.parseDouble(hrs + "." + mins);

        return true;
    }

    /**
     * Regresa el total de horas calculadas de las actividades de la maquinaria seleccionada
     * @return la cantidad de horas totales
     */
    public Double getHorasTotales() {
        return horast;
    }
}
