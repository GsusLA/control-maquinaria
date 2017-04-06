package mx.grupohi.almacenes.controlmaquinaria.Serializables;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by LERDES2 on 24/03/2017.
 *
 * Clase auxiliar para la serializacion y creación del Json de la tabla actividad.
 */

public class SerializacionActividad implements Serializable {
    //public Actividad actividad;
    public ArrayList<Actividad> actividad;

    public ArrayList<Actividad> getActividad() {
        return actividad;
    }

    public void setActividad(ArrayList<Actividad> actividad) {
        this.actividad = actividad;
    }
}
