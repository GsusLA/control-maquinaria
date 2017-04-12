package mx.grupohi.almacenes.controlmaquinaria;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Actividades;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Obra;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

public class ObraActivity extends AppCompatActivity {
    private Button sincronizar;
    private ListView listaObras;
    private ArrayList<Obra> obras;
    private Usuario usuario;
    private SincronizacionObra sincronizacionObra;
    String token;
    private ProgressDialog mProgressDialog;
    private Obra seleccionada;
    private AlertDialog mensajeObra;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obra);

        sincronizacionObra = new SincronizacionObra(getApplicationContext());
        listaObras = (ListView)findViewById(R.id.lv_obras);
        sincronizar = (Button)findViewById(R.id.btn_obraSincronizar);

        obras = (ArrayList<Obra>)getIntent().getExtras().getSerializable("Obras");
        usuario = (Usuario)getIntent().getExtras().getSerializable("Usuario");
        token = getIntent().getStringExtra("token");

        int size = obras!=null?obras.size():0;

        ListaAdapter adaptador = new ListaAdapter(getApplicationContext(), R.layout.obras_item, obras);
        listaObras.setAdapter(adaptador);

        if(size > 1){
            sincronizar.setText("ACEPTAR");
            // lanzar alertdialog avisando que tiene mas de dos obras asignadas
            mensajeObra = new AlertDialog.Builder(ObraActivity.this)
                    .setTitle("Sincronización de Obras")
                    .setMessage("Ud. tiene más de una obra asignada\nPor favor contacte al administrador de obras para mayores detalles. ")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mensajeObra.dismiss();
                        }
                    })
                    .setIcon(R.drawable.info_alert)
                    .setCancelable(false)
                    .show();
        }



        sincronizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sincronizar.getText().equals("ACEPTAR")){
                    finish();
                }else{
                    seleccionada = obras.get(0);
                    new SincronizarObra().execute();
                }

            }
        });
    }

    /**
     *  Cierra la actividad actual e inicializa la siguiente una vez que se haya terminado la sincronización
     *  de la obra seleccionada
     */
    private void nextActivity(){
        Intent intent = new Intent(ObraActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Metodo para mostrar un mensaje con Toast
     * @param message string con el mensaje a mostrar
     */
    private void Message(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     *  Tarea Asincrona que sincroniza la obra seleccionada por el ususario, la sincronizacion de la obra solo
     *  se hace la primera vez que se inicie sesión y que no haya una obra previamente sincronizada
     */
    private class SincronizarObra extends AsyncTask<Void, Void, Boolean> {
        private Boolean  almacen, obraDb, almacenDb, sincLinea;

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(ObraActivity.this, "Autenticando", "Por favor espere...", true);
            mProgressDialog.setTitle("Actualizando");
            mProgressDialog.setMessage("Actualizando datos de la obra seleccionada...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            obraDb = sincronizacionObra.guardarObra(seleccionada);
            almacen = sincronizacionObra.sincAlmacenes(seleccionada);
            sincLinea = sincronizacionObra.sincObraLinea(seleccionada, usuario);

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.dismiss();
            if(almacen && obraDb && sincLinea){
                nextActivity();
            }else{
                Message(getString(R.string.general_exception));
            }
        }
    }

    /**
     * Tarea que llena el ListView con los datos de las obras que esten asignadas al usuario loggeado
     */
    private class ListaAdapter extends ArrayAdapter<Obra>{
        private int layoutResource;
        private ArrayList<Obra> obrasRec;

        public ListaAdapter(Context context, int resource, ArrayList<Obra> objects) {
            super(context, resource, objects);
            this.layoutResource = resource;
            this.obrasRec = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(layoutResource, null);
            }

            Obra obraregistrada = obrasRec.get(position);

            if(obraregistrada != null){
                TextView obraId = (TextView) view.findViewById(R.id.tv_itemObraID);
                TextView obraName = (TextView) view.findViewById(R.id.tv_itemObraNombre);

                obraId.setText(obraregistrada.getIdObra() + "");
                obraName.setText(obraregistrada.getNombre().toString());
            }
            return  view;
        }
    }
}
