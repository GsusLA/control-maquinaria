package mx.grupohi.almacenes.controlmaquinaria.TareasAsync;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import mx.grupohi.almacenes.controlmaquinaria.SincronizacionActividad;

/**
 * Created by LERDES2 on 30/03/2017.
 *
 * Tarea asincrona que ejecuta el envío de la información de las actividades al web service
 */

public class SincActividades extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private SincronizacionActividad sincActividad;

    public SincActividades(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        sincActividad = new SincronizacionActividad(context);
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return sincActividad.sincronizarActividad();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if(aBoolean){
            System.out.println("Actividades sincronizadas");
            Toast.makeText(context, "Actividades Sincronizadas correctamente.", Toast.LENGTH_LONG).show();
        }
    }
}
