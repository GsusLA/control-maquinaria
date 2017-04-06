package mx.grupohi.almacenes.controlmaquinaria;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Almacenes;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

public class ActividadActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    ProcesosActividad procesosActividad;
    Usuario usuario;
    ArrayList<Almacenes> maquinariaActividad;
    private SincronizacionCatalogos sincCatalogos;

    private ProgressDialog mProgressDialog;

    private TextView fecha;
    private AutoCompleteTextView horometro;
    private AutoCompleteTextView kilometraje;
    private EditText operador;
    private EditText observaciones;
    private Button cancelar;
    private Button guardar;
    private Spinner spmaquinaria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad);
        usuario = Util.obtenerUsuario(getApplicationContext());
        initDrawerNav();
        procesosActividad = new ProcesosActividad(this);


        fecha = (TextView)findViewById(R.id.tv_actividadFecha);
        horometro = (AutoCompleteTextView)findViewById(R.id.actividadHorometro);
        kilometraje = (AutoCompleteTextView)findViewById(R.id.actividadKilometraje);
        operador = (EditText)findViewById(R.id.et_actividadOperador);
        observaciones = (EditText)findViewById(R.id.et_actividadObservaciones);
        cancelar = (Button)findViewById(R.id.btn_actividadCancelar);
        guardar = (Button)findViewById(R.id.btn_actividadGuardar);

        spmaquinaria = (Spinner)findViewById(R.id.sp_actividadMaquinaria);

        //Inicializa el Spinner
        String[] datosMaquinaria = procesosActividad.getMaquinaria();
        setSpinnerData(datosMaquinaria);

        maquinariaActividad = procesosActividad.getMaquinas();

        fecha.setText( Util.getfecha());

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spmaquinaria.getSelectedItemPosition() == 0){
                    Message("Seleccione Antes una Maquinaria");
                }else{
                    String horom = horometro.getText().toString();
                    String kilom = kilometraje.getText().toString();
                    String oper = operador.getText().toString();
                    String observ = observaciones.getText().toString();


                    if(TextUtils.isEmpty(horom) || TextUtils.isEmpty(kilom) || TextUtils.isEmpty(oper)){
                        Message("¡Existen campos Vacios!");
                    }else{
                        ContentValues actividad = new ContentValues();
                        actividad.put("id_almacen", maquinariaActividad.get(spmaquinaria.getSelectedItemPosition()-1).getId());
                        actividad.put("fecha", fecha.getText().toString());
                        actividad.put("horometro_inicial", horom);
                        actividad.put("kilometraje_inicial", kilom);
                        actividad.put("operador", oper);
                        actividad.put("observaciones", observ);
                        actividad.put("created_at", Util.getDateTime());
                        actividad.put("creado_por", Util.getIdUsuario(getApplicationContext()));
                        actividad.put("imei", Util.deviceImei(getApplicationContext()));
                        actividad.put("estatus", 0);

                        if(procesosActividad.registrarActividad(actividad)){
                            alertMessageContinuar();
                        }else {
                            Message("¡Error al guardar, intente de nuevo!");
                        }
                    }
                }
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validarCampos()){
                    finish();
                }else{
                    alertMessageSalir();
                }
            }
        });

    }

    /**
     * Inicializa los datos de la maquinaria que aun no inicializan actividad
     * @param datos Array String con los datos recuperados de la BD
     */
    private void setSpinnerData(String[] datos){
        final ArrayAdapter<String> a = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, datos);
        a.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spmaquinaria.setAdapter(a);
    }

    /**
     * Mensaje de alerta que se muestra cuando se quiere cancelar el inicio de una actividad y
     * esta tiene datos pendientes de guardar
     */
    private void alertMessageSalir(){
        AlertDialog.Builder alerta = new AlertDialog.Builder(ActividadActivity.this);
        alerta.setTitle("Control Maquinaria");
        alerta.setMessage("Existen Campos con Información, ¿Desea Salir?");
        alerta.setCancelable(false);
        alerta.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                borrarDatos();
                finish();
            }
        });
        alerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alerta.show();
    }

    /**
     *  Mensaje de alerta que se muestra cuando se registra correctamente una actividad y
     *  pregunta al usuario si desea iniciar otra actividad, si la respuesta es NO se cierra
     *  el Activity y se retorna al Menu de opciones, de lo contrario se continua con el
     *  registro otra actividad
     */
    private void alertMessageContinuar(){
        final AlertDialog.Builder alerta = new AlertDialog.Builder(ActividadActivity.this);
        alerta.setTitle("Control Maquinaria");
        alerta.setMessage("¿Desea iniciar otra actividad?");
        alerta.setCancelable(false);
        alerta.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                actualizarMaquinaria();
                borrarDatos();
                spmaquinaria.setSelection(0);

            }
        });
        alerta.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                borrarDatos();
                dialog.dismiss();
                finish();
            }
        });
        alerta.show();
    }

    /**
     * Actualiza la maquinaria que se muestra en el Spinner
     */
    private void actualizarMaquinaria(){
        String[] datosMaquinaria = procesosActividad.getMaquinaria();
        setSpinnerData(datosMaquinaria);

        maquinariaActividad = procesosActividad.getMaquinas();
    }


    private void borrarDatos(){
        horometro.setText("");
        kilometraje.setText("");
        operador.setText("");
        observaciones.setText("");
    }

    private boolean validarCampos(){
        String horom = horometro.getText().toString();
        String kilom = kilometraje.getText().toString();
        String oper = operador.getText().toString();
        String observ = observaciones.getText().toString();


        if(TextUtils.isEmpty(horom) && TextUtils.isEmpty(kilom) && TextUtils.isEmpty(oper) && TextUtils.isEmpty(observ))
            return true;

        return false;
    }

    private void Message(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    void initDrawerNav(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if(drawer != null)
            drawer.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < drawer.getChildCount(); i++) {
                        View child = drawer.getChildAt(i);
                        TextView tvu = (TextView) child.findViewById(R.id.tv_navUser);
                        TextView tvv = (TextView) child.findViewById(R.id.tv_navVersion);

                        if (tvu != null) {
                            tvu.setText(usuario.getNombre_usuario());
                        }
                        if (tvv != null) {
                            tvv.setText("Versión " + String.valueOf(BuildConfig.VERSION_NAME));
                        }
                    }
                }
            });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            this.finish();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_inicio){
            this.finish();
        } else if (id == R.id.nav_actividad) {
        } else if (id == R.id.nav_actividades) {
            startActivity(new Intent(ActividadActivity.this, ActividadesActivity.class));
            this.finish();
        } else if (id == R.id.nav_reportadas) {
            startActivity(new Intent(ActividadActivity.this, ActividadesIniciadasActivity.class));
            this.finish();
        } else if (id == R.id.nav_cierre) {
            startActivity(new Intent(ActividadActivity.this, CierreActivity.class));
            this.finish();
        } else if (id == R.id.nav_sincActividades) {

        } else if (id == R.id.nav_sincCatalogos) {
            new SincCatalogos().execute();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class SincCatalogos extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(ActividadActivity.this, "Actualizando", "Por favor espere...", true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.dismiss();
            if(sincCatalogos.getTotalAlmacen() > 0){
                new AlertDialog.Builder(ActividadActivity.this)
                        .setTitle("Sincronización de Catalogos")
                        .setMessage("Se descargaron un total de " + sincCatalogos.getTotalAlmacen() + " registros con el siguiente resultado: \n\n" +
                                "Se insertaron " + sincCatalogos.getNuevos() + " registros nuevos y \n" +
                                "se actualizaron " + sincCatalogos.getActualizado() + " registros previos.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(R.drawable.info_alert)
                        .setCancelable(false)
                        .show();
            }else{
                Message("No se pudo sincronizar la información, reinicie sesion.");
            }

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            sincCatalogos = new SincronizacionCatalogos(ActividadActivity.this);
            sincCatalogos.sincCatalogosMaquinaria();
            return true;
        }
    }
}
