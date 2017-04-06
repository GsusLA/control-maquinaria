package mx.grupohi.almacenes.controlmaquinaria.Serializables;

import java.io.Serializable;

/**
 * Created by LERDES2 on 07/03/2017.
 *
 * Clase que serializa el objeto Obra con los datos de la obra actual
 */

public class Obra implements Serializable {
    private int id;
    private int id_obra;
    private String nombre;
    private String descripcion;
    private String databaseName;
    private String token;

    public Obra(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdObra() {
        return id_obra;
    }

    public void setIdObra(int idObra) {
        this.id_obra = idObra;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getBase() {
        return databaseName;
    }

    public void setBase(String base) {
        this.databaseName = base;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
