package mx.grupohi.almacenes.controlmaquinaria.Alarma;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import mx.grupohi.almacenes.controlmaquinaria.TareasAsync.SincActividades;

/**
 * Created by LERDES2 on 30/03/2017.
 */

public class AlarmReciever extends BroadcastReceiver {
    public static final String ID_KEY = "id";
    public static final int SINC_ACTIVIDADES = 0;
    public static final int VALIDAR_SESION = 1;

    /**
     * recibe el evento de la alarma programada e inicia las actividades programadas en la alarma
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        int sel = extras.getInt(ID_KEY);
        switch (sel){
            case SINC_ACTIVIDADES:
                // Sincronizar la informacion en linea
                System.out.println("Sincronizacion Actividades: ");
                new SincActividades(context).execute();
                break;
        }
    }

    /**
     * metodo que inicia la alarma que ejecuta la sincronización automatica de las actividades
     * @param context
     */
    public void setSincAlarm(Context context){
        Long time = (long)2*60*60*1000;

        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReciever.class);
        intent.putExtra(ID_KEY, SINC_ACTIVIDADES);
        PendingIntent pi = PendingIntent.getBroadcast(context, SINC_ACTIVIDADES, intent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000 * 60, time , pi);

    }

    /**
     * Cancela la alarma programada
     * @param context
     */
    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReciever.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, VALIDAR_SESION, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}