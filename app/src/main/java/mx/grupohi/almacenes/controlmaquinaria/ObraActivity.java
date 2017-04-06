package mx.grupohi.almacenes.controlmaquinaria;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import mx.grupohi.almacenes.controlmaquinaria.Serializables.Obra;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;

public class ObraActivity extends AppCompatActivity {
    private Button sincronizar;
    private ArrayList<Obra> obras;
    private Usuario usuario;
    private Spinner selctObra;
    private SincronizacionObra sincronizacionObra;
    String token;
    private ProgressDialog mProgressDialog;
    private Obra seleccionada;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obra);

        sincronizacionObra = new SincronizacionObra(getApplicationContext());

        obras = (ArrayList<Obra>)getIntent().getExtras().getSerializable("Obras");
        usuario = (Usuario)getIntent().getExtras().getSerializable("Usuario");
        token = getIntent().getStringExtra("token");
        selctObra = (Spinner)findViewById(R.id.sp_obraObras);

        int size = obras!=null?obras.size():0;

        String[] arrayObras = new String[size + 1];
        arrayObras[0] = "Seleccionar Obra";
        Obra obra;
        for(int i = 0; i < size;i++){
            obra = obras.get(i);
            arrayObras[i+1] = obra.getIdObra() + " " + obra.getNombre();
        }

        final ArrayAdapter<String> a = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, arrayObras);
        a.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        selctObra.setAdapter(a);


        sincronizar = (Button)findViewById(R.id.btn_obraSincronizar);
        sincronizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int seleccion = selctObra.getSelectedItemPosition();
                if(seleccion == 0) {
                    Message(getString(R.string.seleccionar_obra));
                    return;
                }
                seleccionada = obras.get(seleccion-1);


                new SincronizarObra().execute();

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
}
