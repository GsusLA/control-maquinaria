package mx.grupohi.almacenes.controlmaquinaria;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import mx.grupohi.almacenes.controlmaquinaria.Alarma.AlarmReciever;

/**
 * Created by LERDES2 on 27/03/2017.
 */

public class ActividadesService extends Service {

    AlarmReciever alarmActividades = new AlarmReciever();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarmActividades.setSincAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("Main: ", "Servicio Cerrado");
        alarmActividades.CancelSincAlarm(this);
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
