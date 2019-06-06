package com.jorgeblascoespinosa.ep_handle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DispositivoActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice dispositivo;
    private Set<BluetoothDevice> dispSincronizados;
    private BroadcastReceiver mDeviceFoundReceiver;
    private Toolbar toolbar;
    private Button bnBuscar;
    private RecyclerView nuevos,sincronizados;
    private AdaptadorDispositivos adapter_nuevos, adapter_sincronizados;
    private RecyclerView.LayoutManager manager;
    private View.OnClickListener buscarListener, detenerListener;
    private ArrayList<String> listaNuevos,listaSincronizados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivo);
        toolbar = findViewById(R.id.toolbar_dispositivo);
        bnBuscar = findViewById(R.id.bn_dispositivo_buscar);
        manager = new LinearLayoutManager(this);
        nuevos = findViewById(R.id.rv_found_devices);
        nuevos.setHasFixedSize(true);
        nuevos.setLayoutManager(manager);
        sincronizados = findViewById(R.id.rv_synchronized_devices);
        sincronizados.setHasFixedSize(true);
        sincronizados.setLayoutManager(manager);
        listaNuevos = new ArrayList<>();
        listaSincronizados = getDispostivosSincronizados(); //TODO Esto tmb se puede hacer con el método addItem()
        adapter_nuevos = new AdaptadorDispositivos(listaNuevos);
        adapter_sincronizados = new AdaptadorDispositivos(listaSincronizados);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        buscarListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDispositivos();
                bnBuscar.setText(getString(R.string.bn_search_devices_stop));
                bnBuscar.setOnClickListener(detenerListener);
            }
        };
        detenerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.cancelDiscovery();
                bnBuscar.setText(getString(R.string.bn_search_devices));
                bnBuscar.setOnClickListener(buscarListener);
            }
        };
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDeviceFoundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //Al encontrar un dispositivo
                if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    //Recuperamos el dispositivo del intent
                    dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //Añadimos el nombre y la dirección al adapter.
                    adapter_nuevos.addItem(dispositivo.getName() + "\n" + dispositivo.getAddress());
                }
            }
        };
        IntentFilter filter = new IntentFilter((BluetoothDevice.ACTION_FOUND));
        registerReceiver(mDeviceFoundReceiver,filter);
        bnBuscar.setOnClickListener(buscarListener);
    }

    //TODO al hacer click en el dispositivo, se sincroniza. Se mueve de la lista de abajo, a la de arriba. Al volver a seleccionar de la lista de arriba, se abre un dialogo de confirmacion "desea vincular este dispotivo?" Meter el dispositivo en los EXTRA y volver a la actividad inicial.

    private void buscarDispositivos() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    private ArrayList<String> getDispostivosSincronizados(){
        ArrayList<String> dispositivos = new ArrayList<>();
        //Coger los dispositivos sincronizados
        dispSincronizados = mBluetoothAdapter.getBondedDevices();
        //Filtrar los dispositivos sincronizados para ver solo los que tengan que ver con la app.
        for (BluetoothDevice device : dispSincronizados){
            if (device.getName().contains("EP-HANDLE")){
                dispositivos.add(device.getName() + "\n" + device.getAddress());
            }
        }
        return dispositivos;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mDeviceFoundReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mDeviceFoundReceiver);
    }

    //TODO antes de establecer conexion con un dispositivo asegurarse de terminar el descubrimiento
    //Iniciar detección de dispositivos:
        //mBluetoothAdapter.startDiscovery(); //Es asincrónico
    //Detener la detección de dispositivos:
//        mBluetoothAdapter.cancelDiscovery();






    //http://www.proyectosimio.com/es/programacion-android-broadcastreceiver/
    //https://www.youtube.com/watch?v=sXs7S048eIo
}
