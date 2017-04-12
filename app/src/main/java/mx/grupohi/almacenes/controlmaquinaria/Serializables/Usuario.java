package mx.grupohi.almacenes.controlmaquinaria.Serializables;

import java.io.Serializable;

/**
 * Created by LERDES2 on 07/03/2017.
 *
 * Clase que serializa el objeto Usuario con los datos del usuario registrado en la aplicación
 */

public class Usuario implements Serializable {

    private String imei;
    private String nombre;
    private String usuario;
    private int idusuario;
    private String clave;

    public Usuario(){}

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDescripcion() {
        return nombre;
    }

    public void setDescripcion(String descripcion) {
        this.nombre = descripcion;
    }

    public String getNombre_usuario() {
        return usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.usuario = nombre_usuario;
    }

    public int getIdusuario() {
        return idusuario;
    }

    public void setIdusuario(int idusuario) {
        this.idusuario = idusuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
