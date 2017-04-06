package mx.grupohi.almacenes.controlmaquinaria;


import android.content.ContentValues;
import android.icu.text.RelativeDateTimeFormatter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class CierreActividadesFragment extends Fragment {

    private int id_Actividad;
    private String datos;
    private String fecha;
    private String hraInicio;
    private String hraTermino;
    private String obserFinales;

    //Datos complementarios de la actividad que se va a cerrar
    private int actId;
    private int idReporte;

    private TextView tHora;
    private TextView turno;
    private TextView cantidadH;
    private TextView claveActiv;
    private TextView horaInicio;
    private TextView horaTermino;
    private TextView observaciones;
    private CheckBox cargo_empresa;
    private Button guardar;
    private Button cancelar;



    public CierreActividadesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_cierre_actividades, container, false);

        Bundle bundle = this.getArguments();
        id_Actividad = bundle.getInt("idActividad");
        datos = bundle.getString("json");

        tHora = (TextView)view.findViewById(R.id.tv_actividadesCierreTipoHora);
        turno = (TextView)view.findViewById(R.id.tv_actividadesCierreTurno);
        cantidadH = (TextView)view.findViewById(R.id.tv_actividadesCierreHorasTot);
        claveActiv = (TextView)view.findViewById(R.id.tv_actividadesCierreClaveActividad);
        horaInicio = (TextView)view.findViewById(R.id.tv_actividadesCierreHoraInicio);
        horaTermino = (TextView)view.findViewById(R.id.tv_actividadesCierreHoraTermino);
        observaciones = (TextView)view.findViewById(R.id.et_actividadesObservacionesFinales);
        cargo_empresa = (CheckBox)view.findViewById(R.id.cb_actividadesEmpresa);
        guardar = (Button)view.findViewById(R.id.btn_actividadesCierreGuardar);
        cancelar = (Button)view.findViewById(R.id.btn_actividadesCierreCancelar);


        //cargar datos del json

        try {
            JSONObject json = new JSONObject(datos);
            actId = json.getInt("id_actividad");
            tHora.setText(json.getString("tipo_hora"));
            turno.setText(json.getInt("turno") +"");
            claveActiv.setText(json.getString("clave_actividad"));
            horaInicio.setText(json.getString("fecha") + " / " + json.getString("hora_inicial"));
            horaTermino.setText(Util.getfecha() + " / " + Util.getHora());
            obserFinales = "Iniciales: " + json.getString("observaciones") + " - ";
            fecha = json.getString("fecha");
            if(json.getString("con_cargo_empresa").equals("Si")) cargo_empresa.setChecked(true);


            hraInicio = json.getString("hora_inicial");
            hraTermino = Util.getHora();

            cantidadH.setText(getHTotales() +"");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues dato = new ContentValues();
                dato.put("hora_final", horaTermino.getText().toString());
                dato.put("cantidad", Double.parseDouble(cantidadH.getText().toString()));
                dato.put("observaciones", obserFinales + "Finales: " + observaciones.getText().toString());

                if(new ProcesosActividades(getActivity()).guardarCierreActividades(dato, actId)){
                    ((ActividadesActivity)getActivity()).spMaquinaria.setEnabled(true);
                    ((ActividadesActivity)getActivity()).spMaquinaria.setSelection(0);
                    ((ActividadesActivity)getActivity()).Message("Actividad cerrada correctamente.");
                    getActivity().getSupportFragmentManager().beginTransaction().remove(CierreActividadesFragment.this).commit();

                }
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActividadesActivity)getActivity()).spMaquinaria.setEnabled(true);
                ((ActividadesActivity)getActivity()).spMaquinaria.setSelection(0);
                getActivity().getSupportFragmentManager().beginTransaction().remove(CierreActividadesFragment.this).commit();

            }
        });

        return view;
    }

    /**
     *  Caldula las horas que la maquina estuvo en uso de acuerdo a la hora de inicio y fin de actividades
     * @return Double con la diferencia de horas entre inicio y fin de actividades
     */
    private double getHTotales(){
        String[] init = hraInicio.split(":| ");
        String[] fin = hraTermino.split(":| ");

        int horasInicio = Integer.parseInt(init[0]);
        int minutosInicio = Integer.parseInt(init[1]);

        int horasFin = Integer.parseInt(fin[0]);
        int minutosFin = Integer.parseInt(fin[1]);

        int horasTot = getHorasXDia();
        int minutosTot = 0;

        if(horasInicio > horasFin){
            horasTot -= 24;
            horasTot += (horasFin + 24) - horasInicio;
        }else{
            horasTot += horasFin - horasInicio;
        }

        if(minutosInicio > minutosFin){
            horasTot-= 1;
            minutosTot = (minutosFin + 60) - minutosInicio;
        }else{
            minutosTot = minutosFin - minutosInicio;
        }

        if(minutosTot < 10)
            return Double.parseDouble(horasTot+".0"+minutosTot);

        if(minutosTot == 0)
            return Double.parseDouble(horasTot+".01");

        return Double.parseDouble(horasTot+"."+minutosTot);
    }

    /**
     *  Metodo complementario el cual calcula cuantos dias hay de diferencia entre el inicio y fin
     *  de actividades de cada maqunaria
     * @return int la catidad de horas por dia
     */
    private int getHorasXDia(){
        int diasMes;
        int horas = 0;
        String[] diaInicio = fecha.split("/");
        String[] diaFin = Util.getfecha().split("/");

        if(TextUtils.equals(diaInicio[1], diaFin[1])){
            horas = (Integer.parseInt(diaFin[0]) - Integer.parseInt(diaInicio[0])) * 24;
        }else{
            if(Arrays.asList("Jan",  "Mar",  "May",  "Jul", "Aug", "Oct", "Dec").contains(diaInicio[1]))diasMes = 31;
            else if(Arrays.asList("Apr",  "Jun",  "Sep", "Nov").contains(diaInicio[1]))diasMes = 30;
            else diasMes = 28;

            horas = ((diasMes - Integer.parseInt(diaInicio[0])) + Integer.parseInt(diaFin[0])) * 24;
        }
        return horas;
    }

}
