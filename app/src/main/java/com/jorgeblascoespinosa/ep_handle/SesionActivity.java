package com.jorgeblascoespinosa.ep_handle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class SesionActivity extends AppCompatActivity {
    private BroadcastReceiver mBluetoothStateReceiver;
    private BluetoothDevice dispositivo;
    private BluetoothSocket socket;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion);
        dispositivo = getIntent().getParcelableExtra(Constantes.DEVICE_EXTRA);

        //Declarar y registrar el evento de cambio de estado de bluetooth.
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
}
