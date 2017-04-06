package mx.grupohi.almacenes.controlmaquinaria;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by LERDES2 on 31/03/2017.
 *
 * Activty que se ejecuta, en el momento de que se require un inicio de sesión programado de acuerdo al tiempo establecido y
 * hereda  las caracteristicas del Main Activity que es de donde toma los metodos de reiniciarTimerSesion() y cancelarTimer()
 * que son con los que se controla el timeout para el reinicio de sesión.
 */

public class AlertDialogActivity extends MainActivity {

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        cancelarTimer();
        displayAlert();
    }

    /**
     * Metodo que muestra la alerta de reinicio de sesión programado
     */
    private void displayAlert()
    {
        final ImageView logo = new ImageView(this);
        final TextView mensaje = new TextView(this);
        final EditText usuario = new EditText(this);
        final EditText passw = new EditText(this);
        // Inicialización del layout de mensaje emergente
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Se carga el logo en el mensaje
        logo.setImageResource(R.mipmap.ic_logo);
        layout.addView(logo);

        // Etiqueta del mensaje
        mensaje.setText("Favor de reiniciar su sesión.");
        mensaje.setTop(18);
        mensaje.setTextSize(18);
        layout.addView(mensaje);

        // Configuración del campo de texto de usuario
        usuario.setHint("Usuario");
        layout.addView(usuario);

        // Configuración del campo de texto de contraseña
        passw.setHint("Contraseña");
        passw.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passw);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setInverseBackgroundForced(true);
        builder.setView(layout);
        builder.setCancelable(false)
                .setPositiveButton("Iniciar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // code here
                        String usr = usuario.getText().toString();
                        String psw = passw.getText().toString();

                        if(!TextUtils.isEmpty(usr) || !TextUtils.isEmpty(psw)) {
                            mProgressDialog = ProgressDialog.show(AlertDialogActivity.this, "Autenticando", "Por favor espere...", true);
                            renovarSesion(usr, psw) ;
                            dialog.cancel();

                        }else{
                            Message("Ingrese todos los datos, \nInicie sesión de nuevo.");
                            salir();
                        }
                    }
                }).setNegativeButton("Cerrar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        salir();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * ejecuta el reinicio e sesión
     * @param usuario usuario que se va a loggear
     * @param password password del usuario
     */
    private void renovarSesion(String usuario, String password){
        ContentValues sesion = new ContentValues();
        sesion.put("usuario", usuario);
        sesion.put("clave", password);

        new Relogin(sesion, this).execute();
    }

    /**
     * cierra la actividad de la sesión programada en caso de que no haya un reloggeo exitoso o cancele
     * el DialogAlert
     */
    private void salir(){
        cancelarTimer();
        Intent i = new Intent(AlertDialogActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        AlertDialogActivity.this.finish();

    }

    /**
     * Muestra mensajes con un Toast
     * @param message el string con el mensaje a desplegar
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
     * Tarea asincrona que ejecuta el reinicio de sesión
     */
    public class Relogin extends AsyncTask<Void, Void, Boolean>{
        ContentValues datos;
        String token;
        Context context;


        public Relogin(ContentValues data, Context context){
            this.datos = data;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(getApplicationContext().getString(R.string.url_login));
                final JSONObject JSON = HttpConnection.POST(url, datos);

                token = JSON.getString("token");
                JSONObject user = JSON.getJSONObject("user");

                return Util.guardarToken(context, token, user.getString("usuario"));

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }catch (Exception err){
                err.printStackTrace();
                return false;
            }
            //return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                reiniciarTimerSesion();
                AlertDialogActivity.this.finish();
            } else {
                Message("No se pudo reiniciar sesión,\nIntente de nuevo.");
                salir();
            }
            mProgressDialog.dismiss();
        }
    }
}
