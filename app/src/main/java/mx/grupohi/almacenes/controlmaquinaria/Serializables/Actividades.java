package mx.grupohi.almacenes.controlmaquinaria.Serializables;

/**
 * Created by LERDES2 on 07/03/2017.
 *
 * Clase que serializa el objeto Actividades con los datos de las actividades registradas
 */

public class Actividades {
    private int id;
    private int id_reporte;
    private String clave_actividad;
    private String tipo_hora;
    private int turno;
    private String hora_inicial;
    private String hora_final;
    private Double cantidad;
    private String con_cargo_empresa;
    private String observaciones;
    private String created_at;
    private String creado_por;
    private int imei;
    private int estatus;

    public Actividades(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_reporte() {
        return id_reporte;
    }

    public void setId_reporte(int id_reporte) {
        this.id_reporte = id_reporte;
    }

    public String getClave_actividad() {
        return clave_actividad;
    }

    public void setClave_actividad(String clave_actividad) {
        this.clave_actividad = clave_actividad;
    }

    public String getTipo_hora() {
        return tipo_hora;
    }

    public void setTipo_hora(String tipo_hora) {
        this.tipo_hora = tipo_hora;
    }

    public int getTurno() {
        return turno;
    }

    public void setTurno(int turno) {
        this.turno = turno;
    }

    public String getHora_inicial() {
        return hora_inicial;
    }

    public void setHora_inicial(String hora_inicial) {
        this.hora_inicial = hora_inicial;
    }

    public String getHora_final() {
        return hora_final;
    }

    public void setHora_final(String hora_final) {
        this.hora_final = hora_final;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getCon_cargo_empresa() {
        return con_cargo_empresa;
    }

    public void setCon_cargo_empresa(String con_cargo_empresa) {
        this.con_cargo_empresa = con_cargo_empresa;
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

    public String getCreado_por() {
        return creado_por;
    }

    public void setCreado_por(String creado_por) {
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
