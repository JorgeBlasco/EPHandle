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
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    FirebaseFirestore db;
    TextView etiqueta, datos, bt;
    BluetoothAdapter mBluetoothAdapter;
    boolean btCompatible = true;
    private BroadcastReceiver mBluetoothStateReceiver, mDeviceFoundReceiver;
    Set<BluetoothDevice> dispositivosSincronizados;
    BluetoothDevice dispositvo;
    UUID uuid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        etiqueta = findViewById(R.id.etiqueta);
        datos = findViewById(R.id.datos);
        bt = findViewById(R.id.bluetooth);
        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Coger los dispositivos sincronizados
        dispositivosSincronizados = mBluetoothAdapter.getBondedDevices();
        //Filtrar los dispositivos sincronizados para ver solo los que tengan que ver con la app.
        for (BluetoothDevice device : dispositivosSincronizados){
            if (device.getName().contains("EP-HANDLE")){

            }
        }

        mDeviceFoundReceiver = new BroadcastReceiver() {
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

        //TODO Hacer mi propio broadcast receiver que sólo recoja acciones de bluetooth
        //http://www.proyectosimio.com/es/programacion-android-broadcastreceiver/
        //https://www.youtube.com/watch?v=sXs7S048eIo
        mBluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
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

        //Para saber si el dispositivo es compatible con bluetooth
        if (mBluetoothAdapter == null) {
            btCompatible = false;
            //TODO no permitimos lanzar la actividad de empezar la sesión de terapia.
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
                .add(user);*/
        db.collection("users").whereEqualTo("primero","Ada").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                            for (QueryDocumentSnapshot document : task.getResult()){
                                datos.setText((String)document.get("primero"));
                            }
                    }
                });
        bt.setText("Hay bluetooth? " + (btCompatible?"Si":"No"));

    }

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
