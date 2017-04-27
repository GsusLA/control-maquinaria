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

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Actividad;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Almacenes;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

public class CierreActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private Spinner maquinaria;
    private AutoCompleteTextView horoInicial;
    private AutoCompleteTextView kiloInicial;
    private AutoCompleteTextView horoFinal;
    private AutoCompleteTextView kiloFinal;
    private EditText operador;
    private EditText observaciones;
    private TextView cantidad;
    private Button cancelar;
    private Button aprovar;

    private String observText;

    ArrayList<Almacenes> actividades;
    ProcesosCierreFinal procesosCierreFinal;
    Actividad actividad;
    Almacenes aSelecto;
    Usuario usuario;

    private SincronizacionCatalogos sincCatalogos;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cierre);
        usuario = Util.obtenerUsuario(getApplicationContext());
        initDrawerNav();

        maquinaria = (Spinner)findViewById(R.id.sp_actividadCierreMaquinaria);
        horoInicial = (AutoCompleteTextView)findViewById(R.id.cierreHoromInicial);
        kiloInicial = (AutoCompleteTextView)findViewById(R.id.cierreKilomInicial);
        horoFinal = (AutoCompleteTextView)findViewById(R.id.cierreHoromFinal);
        kiloFinal = (AutoCompleteTextView)findViewById(R.id.cierreKilomFinal);
        operador = (EditText)findViewById(R.id.et_actividadCierreOperador);
        observaciones = (EditText)findViewById(R.id.et_actividadCierreObservaciones);
        cantidad = (TextView)findViewById(R.id.tv_cierreHorasTot);
        cancelar = (Button)findViewById(R.id.btn_cerrarCancelar);
        aprovar = (Button)findViewById(R.id.btn_cerrarAprobar);

        procesosCierreFinal = new ProcesosCierreFinal(getApplicationContext());

        actualizarMaquinaria();

        maquinaria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    aSelecto = actividades.get(position-1);
                    if(!procesosCierreFinal.validarActividadesFinalizadas(aSelecto.getId())){
                        Message("Esta maquinaria aun tiene una actividad pendiente de cierre");
                        maquinaria.setSelection(0);
                        limpiarCampos();
                        aprovar.setEnabled(false);
                    }else{
                        actividad = procesosCierreFinal.getActividad(aSelecto.getId());
                        cantidad.setText(procesosCierreFinal.getHorasTotales().toString());
                        horoInicial.setText(actividad.getHorometro_inicial().toString());
                        kiloInicial.setText(actividad.getKilometraje_inicial()+ "");
                        operador.setText(actividad.getOperador());
                        observText = "Iniciales: " + actividad.getObservaciones();
                        aprovar.setEnabled(true);
                    }
                }else{
                    limpiarCampos();
                    aprovar.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        aprovar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double hInicia = Double.parseDouble(horoInicial.getText().toString());
                Double hFinal = 0.0;
                Double kFinal = 0.0;
                if(!horoFinal.getText().toString().equals(""))
                    hFinal = Double.parseDouble(horoFinal.getText().toString());

                Double kInicia = Double.parseDouble(kiloInicial.getText().toString());

                if(!kiloFinal.getText().toString().equals(""))
                    kFinal = Double.parseDouble(kiloFinal.getText().toString());

                Double cantTotal = Double.parseDouble(cantidad.getText().toString());
                Double sumHoras = suma(hInicia, cantTotal);
                if(validarCamposLlenos()){
                    if(hFinal > sumHoras){
                        if(cantTotal < 23.59) {
                            if (kFinal >= kInicia) {
                                ContentValues dato = new ContentValues();
                                dato.put("horometro_final", hFinal);
                                dato.put("kilometraje_final", kFinal);
                                dato.put("observaciones", observText + " - Finales: " + observaciones.getText().toString());
                                if (procesosCierreFinal.cerrarActividad(dato, aSelecto.getId())) {
                                    limpiarCampos();
                                    aprovar.setEnabled(false);
                                    maquinaria.setSelection(0);
                                    actualizarMaquinaria();
                                    Message("¡Actividad cerrada correctamente!");
                                } else {
                                    Message("Error al guardar actividad, Intente de nuevo.");
                                }
                            } else {
                                Message("Las kilometraje final es menor al reportado en el inicio del día");
                            }
                        }else{
                            Message("Las horas totales rebasan un día de labores");
                        }
                    }else{
                        Message("Las horometro final es menor al uso reportado del día");
                    }
                }else{
                    Message("Favor de ingresar todos los datos.");
                }
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Valida si hay campos que no contengan datos
     * @return boolean si existen datos faltantes
     */
    private boolean validarCamposLlenos(){
        String hFinal = horoFinal.getText().toString();
        String kFinal = kiloFinal.getText().toString();
        if(!TextUtils.isEmpty(hFinal) && !TextUtils.isEmpty(kFinal)){
            return true;
        }
        return false;
    }

    /**
     * metodo para quitar la informacion ca los campos una vez que se guardaron en la BD
     */
    private void limpiarCampos(){
        cantidad.setText("");
        horoInicial.setText("");
        kiloInicial.setText("");
        horoFinal.setText("");
        kiloFinal.setText("");
        operador.setText("");
        observaciones.setText("");
    }

    /**
     * Metodo que suma la cantidad de horas totales de las actividades reportadas de la maquinaria con el
     * horometro inicial, con esto se valida que el horometro final coincida con las horas que se reportan
     * @param inicial Double con la cantidad de horas reportadas en el horometro inicial
     * @param hfinal Double con las horas totales reportadas de las actividades de la maquinaria
     * @return Double la suma de las horas
     */
    private Double suma(double inicial, double hfinal){
        int hrsIni = (int) inicial;
        int minsIni = (int)((inicial - hrsIni) * 100);

        int hrsFin = (int) hfinal;
        int minsFin = (int)((hfinal - hrsFin) * 100);

        int horas = hrsIni + hrsFin;
        int mins = minsIni+ minsFin;
        if(mins > 59) {
            do {
                mins -= 60;
                horas += 1;
            } while (mins > 59);
        }
        if(mins < 10)
            return Double.parseDouble(horas + ".0" + mins);

        return Double.parseDouble(horas + "." + mins);
    }

    /**
     *  Inicializa los datos en el Spinner de acuerdo a los datos recuperados de la BD
     */
    public void actualizarMaquinaria(){
        actividades = procesosCierreFinal.getMaquinariaIniciada();
        int size = actividades!=null?actividades.size():0;
        String[] maquinas = new String[size+1];
        maquinas[0] = "Selecciones una Maquinaria";
        for(int i = 0; i< size; i++){
            Almacenes valorMaq = actividades.get(i);
            maquinas[i+1] = valorMaq.getEconomico() + " " + valorMaq.getDescripcion();
        }
        setSpinnerData(maquinas);
    }

    /**
     *  Adaptador con el cual se llenan los datos recuperados de la BD en el Spinner
     * @param datos
     */
    private void setSpinnerData(String[] datos){
        final ArrayAdapter<String> a = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, datos);
        a.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        maquinaria.setAdapter(a);
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
            startActivity(new Intent(CierreActivity.this, ActividadActivity.class));
            this.finish();
        } else if (id == R.id.nav_actividades) {
            startActivity(new Intent(CierreActivity.this, ActividadesActivity.class));
            this.finish();
        } else if (id == R.id.nav_reportadas) {
            startActivity(new Intent(CierreActivity.this, ActividadesIniciadasActivity.class));
            this.finish();
        } else if (id == R.id.nav_cierre) {

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
            mProgressDialog = ProgressDialog.show(CierreActivity.this, "Actualizando", "Por favor espere...", true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.dismiss();
            if(sincCatalogos.getTotalAlmacen() > 0){
                new AlertDialog.Builder(CierreActivity.this)
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
            sincCatalogos = new SincronizacionCatalogos(CierreActivity.this);
            sincCatalogos.sincCatalogosMaquinaria();
            return true;
        }
    }
}
