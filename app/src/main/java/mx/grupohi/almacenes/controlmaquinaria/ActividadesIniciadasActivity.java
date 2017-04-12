package mx.grupohi.almacenes.controlmaquinaria;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Actividades;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Almacenes;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

public class ActividadesIniciadasActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ProcesosActividadesIniciadas procesosActividadesIniciadas;
    private ArrayList<Almacenes> maquinariaTotal;
    private ArrayList<Actividades> actividades;
    private Almacenes almacenes;
    private Usuario usuario;
    private SincronizacionCatalogos sincCatalogos;

    private ProgressDialog mProgressDialog;

    private Spinner spMaquinas;
    private ListView lista;
    private TextView cantidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades_iniciadas);
        usuario = Util.obtenerUsuario(getApplicationContext());
        initDrawerNav();


        procesosActividadesIniciadas = new ProcesosActividadesIniciadas(getApplicationContext());

        spMaquinas = (Spinner)findViewById(R.id.sp_iniciadasMaquinaria);
        lista = (ListView)findViewById(R.id.lv_iniciadasMaquinarias);
        cantidad = (TextView)findViewById(R.id.tv_iniciadasHorasTot);

        iniciarSpinner();

        spMaquinas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0 ){
                    almacenes = maquinariaTotal.get(position-1);
                    actividades = procesosActividadesIniciadas.obtenerDetalleActividades(almacenes.getId());
                    if(actividades != null) {
                        cantidad.setText(horasTotales(actividades).toString());
                        ListaAdapter adaptador = new ListaAdapter(getApplicationContext(), R.layout.actividades_item, actividades);
                        lista.setAdapter(adaptador);
                    }else{
                        lista.setAdapter(null);
                        Message("La máquina seleccionada no tiene actividades iniciadas");
                    }
                }else{
                    lista.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     *  Calcula la sumatoria de las horas reportadas por cada actividad que haya realizado la maquinaria
     * @param actvidades ArrayList con las actividades recuperadas de la BD de la maquinaria seleccionada
     * @return
     */
    private Double horasTotales(ArrayList<Actividades> actvidades){
        int horas = 0;
        int minutos = 0;
        for (Actividades act: actvidades) {
            double hora = act.getCantidad();
            horas += (int) hora;
            minutos += (int)((hora - (int)hora) * 100);
        }
        if(minutos > 59) {
            do {
                minutos -= 60;
                horas += 1;
            } while (minutos > 59);
        }
        if(minutos < 10)
            return Double.parseDouble(horas + ".0" + minutos);

        return Double.parseDouble(horas + "." + minutos);

    }

    private void Message(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void iniciarSpinner(){
        maquinariaTotal = procesosActividadesIniciadas.obtenerMaquinaria();
        int size = maquinariaTotal!=null?maquinariaTotal.size():0;
        String[] maquinas = new String[size+1];
        maquinas[0] = "Selecciones una Maquinaria";
        for(int i = 0; i< size; i++){
            Almacenes valorMaq = maquinariaTotal.get(i);
            maquinas[i+1] = valorMaq.getEconomico() + " " + valorMaq.getDescripcion();
        }
        final ArrayAdapter<String> a = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, maquinas);
        a.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spMaquinas.setAdapter(a);

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
            startActivity(new Intent(ActividadesIniciadasActivity.this, ActividadActivity.class));
            this.finish();
        } else if (id == R.id.nav_actividades) {
            startActivity(new Intent(ActividadesIniciadasActivity.this, ActividadesActivity.class));
            this.finish();
        } else if (id == R.id.nav_reportadas) {
        } else if (id == R.id.nav_cierre) {
            startActivity(new Intent(ActividadesIniciadasActivity.this, CierreActivity.class));
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
            mProgressDialog = ProgressDialog.show(ActividadesIniciadasActivity.this, "Actualizando", "Por favor espere...", true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.dismiss();
            if(sincCatalogos.getTotalAlmacen() > 0){
                new AlertDialog.Builder(ActividadesIniciadasActivity.this)
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
            sincCatalogos = new SincronizacionCatalogos(ActividadesIniciadasActivity.this);
            sincCatalogos.sincCatalogosMaquinaria();
            return true;
        }
    }

    /**
     *  Tarea que llena el listview con los datos de las actividades recuperadas de la BD
     */
    public class ListaAdapter extends ArrayAdapter<Actividades>{
        private int layoutResource;

        public ListaAdapter(Context context, int layoutResource, ArrayList<Actividades> threeStringsList) {
            super(context, layoutResource, threeStringsList);
            this.layoutResource = layoutResource;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(layoutResource, null);
            }

            Actividades activ = getItem(position);

            if(activ != null){
                TextView clave = (TextView) view.findViewById(R.id.tv_inicadasItemClave);
                TextView tipo = (TextView) view.findViewById(R.id.tv_iniciadasItemTipo);
                TextView turno = (TextView) view.findViewById(R.id.tv_iniciadasItemTurno);
                TextView inicio = (TextView) view.findViewById(R.id.tv_iniciadasItemInicio);
                TextView termino = (TextView) view.findViewById(R.id.tv_iniciadasItemTermino);
                TextView cantidad = (TextView) view.findViewById(R.id.tv_inicadasItemCantidad);
                TextView observ = (TextView) view.findViewById(R.id.tv_inicadasItemObserv);

                clave.setText(activ.getClave_actividad());
                tipo.setText(activ.getTipo_hora());
                turno.setText(activ.getTurno()+"");
                inicio.setText(activ.getHora_inicial());
                termino.setText(activ.getHora_final());
                cantidad.setText(activ.getCantidad().toString());
                observ.setText(activ.getObservaciones());

            }
            return view;
        }
    }
}
