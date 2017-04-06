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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

public class ActividadesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ProcesosActividades procesosActividades;
    private Usuario usuario;
    public Spinner spMaquinaria;

    private ArrayList<ContentValues> maquinaria;

    private SincronizacionCatalogos sincCatalogos;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades);

        usuario = Util.obtenerUsuario(getApplicationContext());
        initDrawerNav();


        spMaquinaria = (Spinner)findViewById(R.id.sp_actividadesMaquinaria);

        procesosActividades = new ProcesosActividades(getApplicationContext());
        actualizarMaquinaria();

        spMaquinaria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    if(position != 0) {
                        ContentValues actividad = maquinaria.get(position-1);
                        int id_actividad = (int) actividad.get("id_actividad");
                        Bundle bundle = new Bundle();
                        bundle.putInt("idActividad", id_actividad);
                        iniciarFragment(bundle, id_actividad);

                    }else{
                        Message("Existen campos con datos, si cambia de maquinaria sin guardar se perdera la informacion");
                    }
                }else{

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     *  Recuper la lista de maquinaria disponible en la base de datos que ya tenga una actividad iniciada
     */
    public void actualizarMaquinaria(){
         maquinaria =  procesosActividades.getMaquinariaActiva();
         int size = maquinaria!=null?maquinaria.size():0;
         String[] maquinas = new String[size+1];
         maquinas[0] = "Selecciones una Maquinaria";
         for(int i = 0; i< size; i++){
             ContentValues valorMaq = maquinaria.get(i);
             maquinas[i+1] = valorMaq.get("economico") + " " + valorMaq.get("descripcion");
         }
         setSpinnerData(maquinas);
     }

    /**
     *  Metodo que inicializa el fragment de inicio o cierre de actividades, la validacion se hace de acuerdo a
     *  la respues de base de datos, si la mquinaria ya tiene una actividad inciada o no
     * @param bundle son los datos adicionales necesarios para el fragment que se va a iniciar
     * @param actividad el id de la actividad seleccionada con la cual se valida si ya tiene un registro en BD
     */
    private void iniciarFragment(Bundle bundle, int actividad){
        String resp = procesosActividades.validarActividadMaquina(actividad);
        if(!resp.equals("vacio")){
            // iniciar fragment de cierre de actividades
            CierreActividadesFragment fragment = new CierreActividadesFragment();
            bundle.putString("json", resp);
            fragment.setArguments(bundle);
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragm_actividades, fragment);
            fragmentTransaction.commit();
            spMaquinaria.setEnabled(false);
        }else {
            // iniciar fragment de inicio de actividades
            InicioActividadesFragment fragment = new InicioActividadesFragment();
            fragment.setArguments(bundle);
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragm_actividades, fragment);
            fragmentTransaction.commit();
            spMaquinaria.setEnabled(false);
        }
    }

    /**
     *  Adaptador para llenar el spinner con los datos de la maquinaria recuperados de la BD
     * @param datos Arreglo de Strings con los datos de las maquinas recuperadas de BD
     */
    private void setSpinnerData(String[] datos){
        final ArrayAdapter<String> a = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, datos);
        a.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spMaquinaria.setAdapter(a);
    }

    public void Message(final String message) {
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
            startActivity(new Intent(ActividadesActivity.this, ActividadActivity.class));
            this.finish();
        } else if (id == R.id.nav_actividades) {
        } else if (id == R.id.nav_reportadas) {
            startActivity(new Intent(ActividadesActivity.this, ActividadesIniciadasActivity.class));
            this.finish();
        } else if (id == R.id.nav_cierre) {
            startActivity(new Intent(ActividadesActivity.this, CierreActivity.class));
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
            mProgressDialog = ProgressDialog.show(ActividadesActivity.this, "Actualizando", "Por favor espere...", true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.dismiss();
            if(sincCatalogos.getTotalAlmacen() > 0){
                new AlertDialog.Builder(ActividadesActivity.this)
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
            sincCatalogos = new SincronizacionCatalogos(ActividadesActivity.this);
            sincCatalogos.sincCatalogosMaquinaria();
            return true;
        }
    }
}
