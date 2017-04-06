package mx.grupohi.almacenes.controlmaquinaria;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class InicioActividadesFragment extends Fragment {


    private int turno;
    private int id_Actividad;
    private boolean checked = false;
    private String horaInicio;

    private Spinner spTipoHora;
    private TextView hInicial;
    private EditText cveActividad;
    private EditText observ;
    private CheckBox cargo_empresa;
    private Button guardar;
    private Button cancelar;
    private RadioGroup turnos;

    private ProcesosActividades procesosActividades;


    public InicioActividadesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_inicio_actividades, container, false);

        procesosActividades = new ProcesosActividades(getActivity());

        Bundle bundle = this.getArguments();
        id_Actividad = bundle.getInt("idActividad");

        // Iniciar componentes del Fragment
        spTipoHora = (Spinner)view.findViewById(R.id.sp_actividadInicioHora);
        hInicial = (TextView)view.findViewById(R.id.tv_actividadesInicioHora);
        cveActividad = (EditText)view.findViewById(R.id.et_actividadesClaveActividad);
        observ = (EditText)view.findViewById(R.id.et_actividadesObservaciones);
        cargo_empresa = (CheckBox)view.findViewById(R.id.cb_actividadesEmpresa);
        guardar = (Button)view.findViewById(R.id.btn_actividadesInicioGuardar);
        turnos = (RadioGroup)view.findViewById(R.id.rg_ActividadesInicioRadBotones);
        cancelar = (Button)view.findViewById(R.id.btn_actividadInicioCancelar);

        horaInicio = Util.getHora();
        hInicial.setText(horaInicio);

        rgTurnos();
        registrarActividad();

        return view;
    }

    /**
     *  Metodo para iniciar los botones guardar y cancelar actividad
     */
    private void registrarActividad(){
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hora = spTipoHora.getSelectedItemPosition();
                String th = spTipoHora.getSelectedItem().toString();
                int ct = turno;
                String clave = cveActividad.getText().toString();
                String obser = observ.getText().toString();
                String cargo = cargo_empresa.isChecked()? "Si": "No";
                String hour = hInicial.getText().toString();
                if(hora != 0 && ct != 0 && !TextUtils.isEmpty(clave)){
                    System.out.println(th);
                    ContentValues datos = new ContentValues();
                    datos.put("id_reporte", id_Actividad);
                    datos.put("clave_actividad", clave);
                    datos.put("tipo_hora", th);
                    datos.put("turno", ct);
                    datos.put("hora_inicial", hour);
                    datos.put("con_cargo_empresa", cargo);
                    datos.put("observaciones", obser);
                    datos.put("created_at", Util.getDateTime());
                    datos.put("creado_por", Util.getIdUsuario(getActivity()));
                    datos.put("imei", Util.deviceImei(getActivity()));
                    datos.put("estatus", 0);

                    if(procesosActividades.guardarInicioActividades(datos)){
                        ((ActividadesActivity)getActivity()).spMaquinaria.setEnabled(true);
                        ((ActividadesActivity)getActivity()).spMaquinaria.setSelection(0);
                        ((ActividadesActivity)getActivity()).Message("Actividad iniciada correctamente.");
                        getActivity().getSupportFragmentManager().beginTransaction().remove(InicioActividadesFragment.this).commit();

                    }
                }else{
                    Message("Existen campos vacios, favor de verificar");
                }
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActividadesActivity)getActivity()).spMaquinaria.setEnabled(true);
                ((ActividadesActivity)getActivity()).spMaquinaria.setSelection(0);
                getActivity().getSupportFragmentManager().beginTransaction().remove(InicioActividadesFragment.this).commit();

            }
        });

    }

    /**
     *  ;etodo para validar que opcion de turno esta seleccionado
     */
    private void rgTurnos(){
        turnos.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                switch(checkedId) {
                    case R.id.rb_primerTurno:
                        checked = true;
                        turno =1;
                        break;
                    case R.id.rb_segundoTurno:
                        checked = true;
                        turno = 2;// Fragment 2
                        break;
                    case R.id.rb_tercerTurno:
                        checked = true;
                        turno = 3;
                        break;
                }
            }
        });
    }
    private void Message(final String message) {

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

}
