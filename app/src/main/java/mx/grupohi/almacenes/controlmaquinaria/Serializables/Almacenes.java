package mx.grupohi.almacenes.controlmaquinaria.Serializables;

/**
 * Created by LERDES2 on 07/03/2017.
 *
 * Clase que serializa el objeto Almacenes correspondiente a la maquinaria de la obra
 */

public class Almacenes {
    private int id;
    private String descripcion;
    private int economico;

    public Almacenes(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEconomico() {
        return economico;
    }

    public void setEconomico(int economico) {
        this.economico = economico;
    }
}
