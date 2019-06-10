package com.jorgeblascoespinosa.ep_handle.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jorgeblascoespinosa.ep_handle.Constantes;
import com.jorgeblascoespinosa.ep_handle.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class SesionActivity extends AppCompatActivity {
    private BroadcastReceiver mBluetoothStateReceiver, mBluetoothDeviceStatusReceiver;
    private BluetoothSocket socket;
    private Button bnPause,bnStop;
    private Switch swSound,swVib,swLight;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private SharedPreferences defaultPreferences, oldPreferences;
    private InputStream input = null;
    private OutputStream output = null;
    private Map<Character,Boolean> cambios;
    private TreeMap<String,Object> datos = new TreeMap<>();
    private Chronometer crono;
    private View.OnClickListener listenerPause, listenerPlay;
    private boolean isPaused = false;
    boolean stopWorker;
    int readBufferPosition;
    byte[] readBuffer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Relacionar views
        setContentView(R.layout.activity_sesion);
        TextView tvTitle = findViewById(R.id.tv_session_title);
        TextView tvSessionTime = findViewById(R.id.tv_session_time);
        bnPause = findViewById(R.id.bn_session_pause);
        bnStop = findViewById(R.id.bn_session_stop);
        swSound = findViewById(R.id.sw_sound);
        swLight = findViewById(R.id.sw_light);
        swVib = findViewById(R.id.sw_vib);
        crono = findViewById(R.id.chronometer);
        //Iniciar el crono
        crono.start();

        //Crear el listener para los switches
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                byte data;
                try {
                    switch (buttonView.getId()) {
                        case R.id.sw_light:
                            data = Constantes.TOGGLE_LIGHT;
                            output.write(data);
                            break;
                        case R.id.sw_sound:
                            data = Constantes.TOGGLE_BUZZER;
                            output.write(data);
                            break;
                        case R.id.sw_vib:
                            data = Constantes.TOGGLE_VIBRATION;
                            output.write(data);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        //Crear el listener para el boton pause
        listenerPause = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPaused=true;
                crono.stop();
                bnPause.setText(R.string.bn_session_play);
                bnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_black_24dp,0,0,0);
                bnPause.setOnClickListener(listenerPlay);
            }
        };

        //Crear el listener para el boton play
        listenerPlay = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPaused=false;
                crono.start();
                bnPause.setText(R.string.bn_session_pause);
                bnPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_black_24dp,0,0,0);
                bnPause.setOnClickListener(listenerPause);
            }
        };

        //Recuperamos del intent el titulo de la actividad
        tvTitle.setText(getIntent().getStringExtra(Constantes.SESSION_EXTRA));

        //Establecemos el tiempo
        tvSessionTime.setText(String.valueOf(getIntent().getIntExtra(Constantes.TIME_EXTRA,0)));

        bnPause.setOnClickListener(listenerPause);

        bnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Aumentamos el numero de sesiones
                SharedPreferences prefs = getSharedPreferences(Constantes.EP_HANDLE_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                int numSesiones = prefs.getInt(Constantes.PREF_SESSION_NUMBER,0);
                editor.putInt(Constantes.PREF_SESSION_NUMBER,++numSesiones);
                editor.apply();
                //Guardamos los datos en el intent, y finalizamos
                Intent result = new Intent();
                result.putExtra(Constantes.DATA_EXTRA,datos);
                setResult(Activity.RESULT_OK,result);
                finish();
            }
        });

        cambios = new HashMap<>();
        BluetoothDevice dispositivo = getIntent().getParcelableExtra(Constantes.DEVICE_EXTRA);

        //Declaro el BroadcastReceiver para los cambios de estado del bluetooth
        mBluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            bnPause.performClick();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            listenerPlay.onClick(bnPause);
                            break;
                        case BluetoothAdapter.ERROR:
                            bnPause.performClick();
                            break;
                    }
                }
            }
        };

        //Declarao el BroadcastReceiver para los cambios de conexion del dispositivo
        mBluetoothDeviceStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                    bnStop.performClick();
                }
            }
        };

        //Obtengo dos preferencias, para poder comparar las antiguas con las nuevas
        defaultPreferences = getSharedPreferences(Constantes.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        oldPreferences = getSharedPreferences(Constantes.EP_HANDLE_OLD_PREFS,Context.MODE_PRIVATE);
        comparaPreferencias();

        //Intento conectar con el dispositivo
        if (conectar(dispositivo)){
            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();
                //Si se conecta, le mando la nueva configuración
                sendConfig();
            } catch (IOException e) {
                Log.e(Constantes.TAG,"Ha habido un problema al establecer la comunicación con los Streams");
                finish();
            }
        }

        //Declarar y registrar el evento de cambio de estado de bluetooth.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, filter);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBluetoothDeviceStatusReceiver, filter2);

        //Comenzar a recibir datos
        recibirDatos();

        //Asignar los listener
        swSound.setOnCheckedChangeListener(listener);
        swLight.setOnCheckedChangeListener(listener);
        swVib.setOnCheckedChangeListener(listener);
    }

    /**
     * Método que manda la configuración guardada al dispositivo
     */
    private void sendConfig() {
        StringBuilder builder = new StringBuilder();
        if (cambios.containsKey(Constantes.TOGGLE_BUZZER))
            builder.append(Constantes.TOGGLE_BUZZER);
        if (cambios.containsKey(Constantes.TOGGLE_LIGHT))
            builder.append(Constantes.TOGGLE_LIGHT);
        if (cambios.containsKey(Constantes.TOGGLE_VIBRATION))
            builder.append(Constantes.TOGGLE_VIBRATION);
        //A este le sumo 1000 porque el numero resultante debe tener 4 cifras
        if (cambios.containsKey(Constantes.SET_TONE))
            builder.append(""+Constantes.SET_TONE +
                    ((oldPreferences.getInt(Constantes.PREF_SOUND_VOLUME,1000))+1000)
            );
        if (cambios.containsKey(Constantes.SET_BRIGHTNESS))
            builder.append(Constantes.SET_BRIGHTNESS +
                    (oldPreferences.getInt(Constantes.PREF_LIGHT_BRIGHTNESS,3))
            );
        if (cambios.containsKey(Constantes.SET_VIB_POWER))
            builder.append(Constantes.SET_VIB_POWER +
                    (oldPreferences.getInt(Constantes.PREF_VIBR_INTENSITY, 49))
            );
        Log.d(Constantes.TAG,"Configuración enviada: " + builder.toString());

        //Convertimos la string resultante a bytes
        byte[] data = builder.toString().getBytes();
        try {
            output.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        //Cerramos todos los Streams
        try {
            if (output != null)
                output.close();
            if (input != null){
                input.close();
            }
        } catch (IOException e) {
            Log.d(Constantes.TAG,e.getMessage());
            e.printStackTrace();
        }

        //Des-registramos los receivers
        if (mBluetoothStateReceiver.isOrderedBroadcast())
            unregisterReceiver(mBluetoothStateReceiver);
        if (mBluetoothDeviceStatusReceiver.isOrderedBroadcast())
            unregisterReceiver(mBluetoothDeviceStatusReceiver);

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (output != null)
                output.close();
            if (input != null){
                input.close();
            }
        } catch (IOException e) {
            Log.d(Constantes.TAG,e.getMessage());
            e.printStackTrace();
        }
        if (mBluetoothStateReceiver.isOrderedBroadcast())
            unregisterReceiver(mBluetoothStateReceiver);
        if (mBluetoothDeviceStatusReceiver.isOrderedBroadcast())
            unregisterReceiver(mBluetoothDeviceStatusReceiver);
    }

    /**
     * Compara las prefencias nuevas con las antiguas, para ver cuáles han cambiado, y mandar sólo los cambios nuevos.
     */
    private void comparaPreferencias() { //TODO Este método puede optimizarse, creando directamente aquí la string.
        SharedPreferences p = defaultPreferences;
        SharedPreferences o = oldPreferences;
        SharedPreferences.Editor editor = oldPreferences.edit();
        if (p.getBoolean(Constantes.PREF_ENABLE_VIBRATION,false) != o.getBoolean(Constantes.PREF_ENABLE_VIBRATION,false)){
            editor.putBoolean(Constantes.PREF_ENABLE_VIBRATION,p.getBoolean(Constantes.PREF_ENABLE_VIBRATION,false));
            swVib.setChecked(p.getBoolean(Constantes.PREF_ENABLE_VIBRATION,false));
            cambios.put(Constantes.TOGGLE_VIBRATION,true);
        }
        if (p.getBoolean(Constantes.PREF_ENABLE_LIGHT,true) != o.getBoolean(Constantes.PREF_ENABLE_LIGHT,true)){
            editor.putBoolean(Constantes.PREF_ENABLE_LIGHT,p.getBoolean(Constantes.PREF_ENABLE_LIGHT,true));
            swLight.setChecked(p.getBoolean(Constantes.PREF_ENABLE_LIGHT,true));
            cambios.put(Constantes.TOGGLE_LIGHT,true);
        }
        if (p.getBoolean(Constantes.PREF_ENABLE_SOUND,true) != o.getBoolean(Constantes.PREF_ENABLE_SOUND,true)){
            editor.putBoolean(Constantes.PREF_ENABLE_SOUND,p.getBoolean(Constantes.PREF_ENABLE_SOUND,true));
            swSound.setChecked(p.getBoolean(Constantes.PREF_ENABLE_SOUND,true));
            cambios.put(Constantes.TOGGLE_BUZZER,true);
        }
        if (p.getInt(Constantes.PREF_VIBR_INTENSITY,49) != o.getInt(Constantes.PREF_VIBR_INTENSITY,49)){
            editor.putInt(Constantes.PREF_VIBR_INTENSITY,p.getInt(Constantes.PREF_VIBR_INTENSITY,49));
            cambios.put(Constantes.SET_VIB_POWER,true);
        }
        if (p.getInt(Constantes.PREF_LIGHT_BRIGHTNESS,3) != o.getInt(Constantes.PREF_LIGHT_BRIGHTNESS,3)){
            editor.putInt(Constantes.PREF_LIGHT_BRIGHTNESS,p.getInt(Constantes.PREF_LIGHT_BRIGHTNESS,3));
            cambios.put(Constantes.SET_BRIGHTNESS,true);
        }
        if (p.getInt(Constantes.PREF_SOUND_VOLUME,1000) != o.getInt(Constantes.PREF_VIBR_INTENSITY,1000)){
            editor.putInt(Constantes.PREF_SOUND_VOLUME,p.getInt(Constantes.PREF_SOUND_VOLUME,1000));
            cambios.put(Constantes.SET_TONE,true);
        }
        editor.apply();
    }

    /**
     * Carga las preferencias por defecto
     */
    private void cargaPreferenciasPorDefecto() {
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


    /**
     * Método encargado de recibir los datos y guardarlos en un MAP
     */
    void recibirDatos()
    {
        final Handler handler = new Handler();
        final byte delimiter = 42; //Este es el código ASCII para el caracter '*'

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        Thread workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = input.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            input.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if (!isPaused)
                                            datos.put(""+System.currentTimeMillis(),data);
                                            Log.d(Constantes.TAG,"Recibido: " + data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


}
