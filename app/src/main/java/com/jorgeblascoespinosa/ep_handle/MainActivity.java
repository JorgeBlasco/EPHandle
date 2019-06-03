package com.jorgeblascoespinosa.ep_handle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    Toolbar mToolbar;
    FirebaseFirestore db;
    BluetoothAdapter mBluetoothAdapter;
    boolean btCompatible = true;
    private BroadcastReceiver mBluetoothStateReceiver, mDeviceFoundReceiver;
    //Set<BluetoothDevice> dispSincronizados;
    //BluetoothDevice dispositvo;
    //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView tv_bluetoothState, tv_deviceName, tv_day, tv_totalSessions, tv_sessionObjective;
    Spinner sp_sessionType;
    Button bn_start;
    CheckBox cb_setTime;
    EditText et_settedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relacionarViews();
        setSupportActionBar(mToolbar);
        //Obtener la referencia a la base de datos Firebase
        db = FirebaseFirestore.getInstance();
        //Obtener el adaptador bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter==null)
            btCompatible = false;
        else
            configBluetooth();

        if (btCompatible)
            bn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO abrir actividad de sesión, recogiendo el tipo de sesión, el tiempo fijado (si hay)
                }
            });



        actualizaUI();

        //Crear el socket
      /*  BluetoothSocket socket;
        try {
            socket = dispositvo.createRfcommSocketToServiceRecord(uuid);
            //Detener la detección de dispositivos
            socket.connect(); //Caduca despues de 12 segundos.
        } catch (IOException e) {
            e.printStackTrace();
        }
        else {
            // Registrar los eventos de cambio de estado de bluetooth y dispositivo encontrado
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBluetoothStateReceiver, filter);
            IntentFilter filter2 = new IntentFilter((BluetoothDevice.ACTION_FOUND));
            registerReceiver(mDeviceFoundReceiver,filter2); //TODO unregister during onDestroy & onPause
        }

        //Para lanzar el intent  que muestra un dialogo de activación del bluetooth (sin salir de la aplicacion)
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
*/


        //Insertar datos en la base de datos
       /* Map<String, Object> user = new HashMap<>();
        user.put("primero","Ada");
        user.put("segundo","Lovelace");
        user.put("tercero",1815);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        etiqueta.setText("Base de datos actualizada");
                    }
                });

        user.clear();
        user.put("primero","Pepe");
        user.put("segundo","Navarro");
        user.put("tercero",1815);

        db.collection("users")
                .add(user);

                //-----------bloque separado----------------------
                //Coger los dispositivos sincronizados
        dispSincronizados = mBluetoothAdapter.getBondedDevices();
        //Filtrar los dispositivos sincronizados para ver solo los que tengan que ver con la app.
        for (BluetoothDevice device : dispSincronizados){
            if (device.getName().contains("EP-HANDLE")){

            }
        }
        //Iniciar detección de dispositivos:
        mBluetoothAdapter.startDiscovery(); //Es asincrónico
                */
        db.collection("users").whereEqualTo("primero","Ada").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                            for (QueryDocumentSnapshot document : task.getResult()){
                               // datos.setText((String)document.get("primero"));
                            }
                    }
                });
     //   bt.setText("Hay bluetooth? " + (btCompatible?"Si":"No"));

    }

    private void actualizaUI() {
        if (!btCompatible)
            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_not_connected));
        //TODO recojo los datos de la base de datos y los muestro en la pantalla
    }

    private void configBluetooth() {
   /*     mDeviceFoundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //Al encontrar un dispositivo
                if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    //Recuperamos el dispositivo del intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    dispositvo = device;
                    //Añadimos el nombre y la dirección al array adapter.
                    // mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
        */


        //http://www.proyectosimio.com/es/programacion-android-broadcastreceiver/
        //https://www.youtube.com/watch?v=sXs7S048eIo
        mBluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        //TODO listener estado de bluetooth
                        case BluetoothAdapter.STATE_OFF:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_not_connected));
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_turning_off));
                            break;
                        case BluetoothAdapter.STATE_ON:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_connected));
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_turning_on));
                            break;
                        case BluetoothAdapter.ERROR:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_error));
                            break;
                    }
                }
            }
        };
    }

    private void relacionarViews() {
        mToolbar = findViewById(R.id.toolbar);
        tv_bluetoothState = findViewById(R.id.tv_bluetooth_state);
        tv_deviceName = findViewById(R.id.tv_device_name);
        tv_day = findViewById(R.id.tv_day);
        tv_totalSessions = findViewById(R.id.tv_total_sessions);
        tv_sessionObjective = findViewById(R.id.tv_session_objective);
        sp_sessionType = findViewById(R.id.sp_session_type);
        bn_start = findViewById(R.id.bn_start);
        cb_setTime = findViewById(R.id.cb_set_time);
        et_settedTime = findViewById(R.id.et_setted_time);
    }


    //Manejo del menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_item_mi_terapeuta:
                //TODO caso terapeuta
                break;
            case R.id.menu_item_dispositivo:
                //TODO caso dispositivo
                if (!btCompatible){
                    //TODO mostrar diálogo "Este dispositivo no es compatible con bluetooth, por favor, inténtalo con otro dispositivo"
                }
                else{
                    //TODO iniciar actividad de selección de dispositivo
                }
                break;
            case R.id.menu_item_config:
                //TODO caso configuración
                break;
            case R.id.menu_item_acercade:
                //TODO caso acerca de
                break;
            case R.id.menu_item_salir:
                //TODO caso salir
                break;
        }
        return true;
    }

    //Manejo de las acciones de retorno
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Qué actividad es la que retorna?
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                //Si el bluetooth ha sido activado
                if (resultCode == Activity.RESULT_OK){

                }
                //El bluetooth no ha sido activado o el dialogo se ha cancelado
                else if (resultCode == Activity.RESULT_CANCELED){

                }
                break;
            case 2:
                break;
        }
    }
}
