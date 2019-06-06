package com.jorgeblascoespinosa.ep_handle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class SesionActivity extends AppCompatActivity {
    private BroadcastReceiver mBluetoothStateReceiver;
    private BluetoothSocket socket;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private SharedPreferences defaultPreferences, oldPreferences;
    private InputStream input = null;
    private OutputStream output = null;
    private boolean[] cambios;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion);
        cambios = new boolean[]{false,false,false,false,false,false}; //1:Vibracion 2:Luz 3:Sonido 4:IntensidadVibracion 5:IntensidadLuz 6:Tono
        BluetoothDevice dispositivo = getIntent().getParcelableExtra(Constantes.DEVICE_EXTRA);
        mBluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            //TODO registrar cambios de estado del bluetooth
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            break;
                        case BluetoothAdapter.STATE_ON:
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                        case BluetoothAdapter.ERROR:
                            break;
                    }
                }
            }
        };
        defaultPreferences = getSharedPreferences(Constantes.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        oldPreferences = getSharedPreferences(Constantes.EP_HANDLE_OLD_PREFS,Context.MODE_PRIVATE);
        comparaPreferencias();
        conectar(dispositivo);
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(Constantes.TAG,"Ha habido un problema al establecer la comunicación con los Streams");
            finish();
        }
        //TODO crear un thread que va guardando los datos recibidos en un MAP
        //Declarar y registrar el evento de cambio de estado de bluetooth.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, filter);
    }




    /**
     * Establece un socket con el dispositivo que se le pasa por parámetro.
     * @param dispositivo El es dispositivo bluetooth con el que conectar.
     * @return Devuelve true si se establece la conexión o false si algo falla.
     */
    private boolean conectar(BluetoothDevice dispositivo){
        try {
            //Crear el socket
            socket = dispositivo.createRfcommSocketToServiceRecord(uuid);
            //TODO Detener la detección de dispositivos
            socket.connect(); //Si tarda mas de 12 segundos, caduca.
            return true;
        } catch (IOException e) {
            Log.w(Constantes.TAG,"No se ha podido conectar con el dispositivo bluetooth.");
            Toast.makeText(this,"No se ha podido conectar con el dispositivo bluetooth.",Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothStateReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBluetoothStateReceiver);
    }

    private void comparaPreferencias() {
        SharedPreferences p = defaultPreferences;
        SharedPreferences o = oldPreferences;
        SharedPreferences.Editor editor = oldPreferences.edit();
        if (p.getBoolean(Constantes.PREF_ENABLE_VIBRATION,false) != o.getBoolean(Constantes.PREF_ENABLE_VIBRATION,false)){
            editor.putBoolean(Constantes.PREF_ENABLE_VIBRATION,p.getBoolean(Constantes.PREF_ENABLE_VIBRATION,false));
            cambios[0] = true;
        }
        if (p.getBoolean(Constantes.PREF_ENABLE_LIGHT,true) != o.getBoolean(Constantes.PREF_ENABLE_LIGHT,true)){
            editor.putBoolean(Constantes.PREF_ENABLE_LIGHT,p.getBoolean(Constantes.PREF_ENABLE_LIGHT,true));
            cambios[1] = true;
        }
        if (p.getBoolean(Constantes.PREF_ENABLE_SOUND,true) != o.getBoolean(Constantes.PREF_ENABLE_SOUND,true)){
            editor.putBoolean(Constantes.PREF_ENABLE_SOUND,p.getBoolean(Constantes.PREF_ENABLE_SOUND,true));
            cambios[2] = true;
        }
        if (p.getInt(Constantes.PREF_VIBR_INTENSITY,49) != o.getInt(Constantes.PREF_VIBR_INTENSITY,49)){
            editor.putInt(Constantes.PREF_VIBR_INTENSITY,p.getInt(Constantes.PREF_VIBR_INTENSITY,49));
            cambios[3] = true;
        }
        if (p.getInt(Constantes.PREF_LIGHT_BRIGHTNESS,3) != o.getInt(Constantes.PREF_LIGHT_BRIGHTNESS,3)){
            editor.putInt(Constantes.PREF_LIGHT_BRIGHTNESS,p.getInt(Constantes.PREF_LIGHT_BRIGHTNESS,3));
            cambios[4] = true;
        }
        if (p.getInt(Constantes.PREF_SOUND_VOLUME,1000) != o.getInt(Constantes.PREF_VIBR_INTENSITY,1000)){
            editor.putInt(Constantes.PREF_SOUND_VOLUME,p.getInt(Constantes.PREF_SOUND_VOLUME,1000));
            cambios[5] = true;
        }
        editor.apply();

        //TODO CUANDO EMPIECE LA TRANSMISION DE DATOS, MANDAR LA ¡¡¡NUEVA!!! CONFIGURACION (la por defecto ya la tiene)
    }

    private void cargaPreferenciasPorDefecto() { //TODO CUANDO CARGAMOS ESTO
        SharedPreferences.Editor editor = oldPreferences.edit();
        editor.putBoolean(Constantes.PREF_ENABLE_VIBRATION,false);
        editor.putBoolean(Constantes.PREF_ENABLE_SOUND,true);
        editor.putBoolean(Constantes.PREF_ENABLE_LIGHT,true);
        editor.putBoolean(Constantes.PREF_ENABLE_NOTIFICATIONS,true);
        editor.putInt(Constantes.PREF_VIBR_INTENSITY,49);
        editor.putInt(Constantes.PREF_SOUND_VOLUME,1000);
        editor.putInt(Constantes.PREF_LIGHT_BRIGHTNESS,3);
        editor.putBoolean(Constantes.LOAD_DEFAULT_SETTINGS,false);
        editor.apply();
    }
}
