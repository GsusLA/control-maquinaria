package mx.grupohi.almacenes.controlmaquinaria.Serializables;

import java.io.Serializable;

/**
 * Created by LERDES2 on 14/03/2017.
 *
 * Clase que serializa el objeto Actividad con los datos de la actividad iniciada de la maquinaria
 */

public class Actividad implements Serializable {
    private int id;
    private int id_almacen;
    private String fecha;
    private Double horometro_inicial;
    private Double horometro_final;
    private Double kilometraje_inicial;
    private Double kilometraje_final;
    private String operador;
    private String observaciones;
    private String created_at;
    private int creado_por;
    private int imei;
    private int estatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_almacen() {
        return id_almacen;
    }

    public void setId_almacen(int id_almacen) {
        this.id_almacen = id_almacen;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Double getHorometro_inicial() {
        return horometro_inicial;
    }

    public void setHorometro_inicial(Double horometro_inicial) {
        this.horometro_inicial = horometro_inicial;
    }

    public Double getHorometro_final() {
        return horometro_final;
    }

    public void setHorometro_final(Double horometro_final) {
        this.horometro_final = horometro_final;
    }

    public Double getKilometraje_inicial() {
        return kilometraje_inicial;
    }

    public void setKilometraje_inicial(Double kilometraje_inicial) {
        this.kilometraje_inicial = kilometraje_inicial;
    }

    public Double getKilometraje_final() {
        return kilometraje_final;
    }

    public void setKilometraje_final(Double kilometraje_final) {
        this.kilometraje_final = kilometraje_final;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getCreado_por() {
        return creado_por;
    }

    public void setCreado_por(int creado_por) {
        this.creado_por = creado_por;
    }

    public int getImei() {
        return imei;
    }

    public void setImei(int imei) {
        this.imei = imei;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }
}
