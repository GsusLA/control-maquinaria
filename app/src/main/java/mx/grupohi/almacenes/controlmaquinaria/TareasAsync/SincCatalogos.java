package mx.grupohi.almacenes.controlmaquinaria.TareasAsync;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import mx.grupohi.almacenes.controlmaquinaria.R;
import mx.grupohi.almacenes.controlmaquinaria.SincronizacionCatalogos;

/**
 * Created by LERDES2 on 30/03/2017.
 *
 * Clase que ejecuta la sincronización de catalogos, se descarga el catalogo de maquinaria de la obra
 * que se esta trabajando, se compara la información actual en BD con la recopilada del web service,
 * se hace el comparativo para actualizar los registros con cambios y agregar registros nuevos.
 */

public class SincCatalogos extends AsyncTask<Void, Void, Boolean> {

    private ProgressDialog mProgressDialog;
    private SincronizacionCatalogos sincCatalogos;

    private Context context;
    public SincCatalogos(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = ProgressDialog.show(context, "Actualizando", "Por favor espere...", true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        mProgressDialog.dismiss();
        if(sincCatalogos.getTotalAlmacen() > 0){
            new AlertDialog.Builder(context)
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
            System.out.println("Error de Sincronización de catalogos");
        }

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        sincCatalogos = new SincronizacionCatalogos(context);
        sincCatalogos.sincCatalogosMaquinaria();
        return true;
    }

}
