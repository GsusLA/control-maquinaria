package mx.grupohi.almacenes.controlmaquinaria;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import mx.grupohi.almacenes.controlmaquinaria.Alarma.AlarmReciever;
import mx.grupohi.almacenes.controlmaquinaria.Alarma.NotificacionActividad;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Actividad;
import mx.grupohi.almacenes.controlmaquinaria.Serializables.Usuario;
import mx.grupohi.almacenes.controlmaquinaria.TareasAsync.SincActividades;
import mx.grupohi.almacenes.controlmaquinaria.TareasAsync.SincCatalogos;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ImageButton actividad, actividades, iniciadas, cierre;
    private Timer timer;
    private Timer timerNot;
    private static final int INICIO =0;
    private static final int ACTIVIDAD = 1;
    private static final int ACTIVIDADES = 2;
    private static final int INICIADAS = 3;
    private static final int CIERRE = 4;
    private static final int CATALOGOS = 6;

    private static final int NOTIFICATION_ID = 1;

    private static final int token_not_provided = 400;
    private static final int token_invalid = 401;

    private Usuario usuario;
    private AlarmReciever alarmaSesion;

    private NotificationManager mNotificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        alarmaSesion = new AlarmReciever();
        alarmaSesion.setSincAlarm(this);
        reiniciarTimerSesion();
        notificacionActividad();
        Intent serv = new Intent(this, ActividadesService.class);
        startService(serv);

        usuario = Util.obtenerUsuario(getApplicationContext());


        actividad =(ImageButton)findViewById(R.id.button_actividad);
        actividades = (ImageButton)findViewById(R.id.button_actividades);
        iniciadas = (ImageButton)findViewById(R.id.button_reportadas);
        cierre = (ImageButton)findViewById(R.id.button_cierre);

        initButtons();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if(drawer != null)
            drawer.addDrawerListener(toggle);
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


    /**
     * Metodos que inicializan y cancelar el Timer que controlan el reinicio de sesión programado
     */
    public void reiniciarTimerSesion() {
        timer = new Timer();
        Log.i("Main", "Invoking logout timer");
        LogOutTimerTask logoutTimeTask = new LogOutTimerTask();
        timer.schedule(logoutTimeTask, 1000 * 60 * 30 );
    }
    public void cancelarTimer(){
        if (timer != null) {
            timer.cancel();
            Log.i("Main", "cancel timer");
            timer = null;
        }
    }


    public void notificacionActividad(){
        timerNot = new Timer();
        Log.i("Main ", "Invocando Revisión de Noticicación.");
        NotificationTimer notificationTimer = new NotificationTimer();
        timerNot.scheduleAtFixedRate(notificationTimer, 1000 * 60, 1000 * 60 * 30); // Inicia al momento de entrar al menu de opciones y se ejecuta cada 5 minutos
    }

    /**
     * Inicia los botones de las opciones del menu principal
    **/
    void initButtons(){
        actividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActividad(ACTIVIDAD);
            }
        });
        actividades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActividad(ACTIVIDADES);
            }
        });
        iniciadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActividad(INICIADAS);
            }
        });
        cierre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarActividad(CIERRE);
            }
        });
    }

    /**
     * metodo que inicia las actividades de acuerdo a que opción seleccionada
     * @param opcion int que se recibe desde el metodo InitButtons
     **/
    void lanzarActividad(int opcion){
        switch (opcion){
            case INICIO:
                break;
            case ACTIVIDAD:
                startActivity(new Intent(MainActivity.this, ActividadActivity.class));
                break;
            case ACTIVIDADES:
                startActivity(new Intent(MainActivity.this, ActividadesActivity.class));
                break;
            case INICIADAS:
                startActivity(new Intent(MainActivity.this, ActividadesIniciadasActivity.class));
                break;
            case CIERRE:
                startActivity(new Intent(MainActivity.this, CierreActivity.class));
                break;
            case CATALOGOS:
                new SincCatalogos(MainActivity.this).execute();
                break;
        }
    }

    /**
     * Alerta que avisa al usuario si desea cerrar la sesión actual
     */
    public void cerrarSesionAlert(){
        final AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
        alerta.setTitle("Control Maquinaria");
        alerta.setMessage("¿Desea cerrar esta sesión?");
        alerta.setCancelable(false);
        alerta.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
                //startActivity(new Intent(MainActivity.this, LoginActivity.class));
                new CerrarSesion().execute();
            }
        });
        alerta.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alerta.show();
    }

    /**
     * Metodos utilizados para el manejo del Panel Lateral
     */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            cerrarSesionAlert();
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            cerrarSesionAlert();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_inicio){
            lanzarActividad(INICIO);
        } else if (id == R.id.nav_actividad) {
            lanzarActividad(ACTIVIDAD);
        } else if (id == R.id.nav_actividades) {
            lanzarActividad(ACTIVIDADES);
        } else if (id == R.id.nav_reportadas) {
            lanzarActividad(INICIADAS);
        } else if (id == R.id.nav_cierre) {
            lanzarActividad(CIERRE);
        } else if (id == R.id.nav_sincActividades) {
            new SincActividades(MainActivity.this).execute();
        } else if (id == R.id.nav_sincCatalogos) {
            lanzarActividad(CATALOGOS);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void Message(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Tarea Asincrona para el cierre de sesion, dando de baja el token actual
     */

    private class CerrarSesion extends AsyncTask<Void, Void, Boolean>{
        ProgressDialog cSesion;
        SincronizacionCatalogos values;
        ContentValues dato;
        String token;
        @Override
        protected void onPreExecute() {
            cSesion = ProgressDialog.show(MainActivity.this, "Cerrando Sesión", "Por favor espere...", true);
            cSesion.setCancelable(false);
            cSesion.setCanceledOnTouchOutside(false);

            dato = Util.getDatosObra(MainActivity.this);
            token = dato.getAsString("token");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            cSesion.dismiss();
            if (aBoolean) {
                cancelarTimer();
                finish();
            }
            else{
                Message("No se pudo cerrar sesión, intende de nuevo.");
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(getApplicationContext().getString(R.string.url_logout));
                JSONObject resp = HttpConnection.LOGOUT(url, token);

                String mensaje = resp.getString("message");
                int status_code = resp.getInt("status_code");

                if(TextUtils.equals(mensaje, "success")){
                    return true;
                }else if (status_code == token_not_provided){
                    return true;
                }else if(status_code == token_invalid){
                    return true;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }
    }

    /**
     * Esta clase se ejecuta cuando el timpo de sesión actual a caducado y pide
     * un reinicio de sesión.
     */

    private class LogOutTimerTask extends TimerTask {
        @Override
        public void run() {
            Intent intent=new Intent(MainActivity.this,AlertDialogActivity.class);
            startActivityForResult(intent, 2);

        }
    }




    private class NotificationTimer extends TimerTask{
        List<String> actividadNotif = new ArrayList<>();
        ProcesosActividad procesosActividad = new ProcesosActividad(MainActivity.this);
        @Override
        public void run() {
            ArrayList<Actividad> listActividad = procesosActividad.listaActividad();
            if(listActividad != null) {
                actividadNotif.clear();
                for (Actividad actividad : listActividad) {
                    String[] creado = actividad.getCreated_at().split(" ");
                    Double hActividad = Util.getDoubleHora(Util.getHora(), creado[1]);
                    if (hActividad > 0.1) {
                        actividadNotif.add(Util.getDatoMaquinaria(MainActivity.this, actividad.getId_almacen()));
                    }
                }
                if (actividadNotif.size() > 0) {
                    notificacion(actividadNotif);
                }
            }
        }

        private void notificacion(List<String> maquinas){
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
            mBuilder.setContentTitle("Hermes");
            mBuilder.setContentText("Notificación de Actividad");
            mBuilder.setTicker("New Message Alert!");
            mBuilder.setSmallIcon(R.mipmap.ic_logo);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle("Maquinaria Activa");
            inboxStyle.addLine("Maquinaria por cumplir 24 hr Activa");
            for (String maquina: maquinas) {
                inboxStyle.addLine(maquina);
            }
            mBuilder.setStyle(inboxStyle);
            Intent resultIntent = new Intent(MainActivity.this, NotificacionActividad.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
            stackBuilder.addParentStack(NotificacionActividad.class);

   /* Adds the Intent that starts the Activity to the top of the stack */
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

            //mBuilder.setContentIntent(resultPendingIntent);
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

   /* notificationID allows you to update the notification later on. */
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }

    }

}
