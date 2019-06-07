package com.jorgeblascoespinosa.ep_handle;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class DispositivoActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice dispositivo;
    private BroadcastReceiver mDeviceFoundReceiver;
    private Button bnBuscar;
    private ProgressBar pbDiscovery;
    private RecyclerView nuevos,sincronizados;
    private AdaptadorDispositivos adapter_nuevos, adapter_sincronizados;
    private RecyclerView.LayoutManager manager1, manager2;
    private View.OnClickListener buscarListener, detenerListener;
    private ArrayList<String> listaNuevos,listaSincronizados;
    private ArrayList<BluetoothDevice> bDevices = new ArrayList<>();
    private UUID uuid = UUID.fromString(Constantes.UUID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivo);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Toolbar toolbar = findViewById(R.id.toolbar_dispositivo);
        bnBuscar = findViewById(R.id.bn_dispositivo_buscar);
        pbDiscovery = findViewById(R.id.pb_discovery);
        manager1 = new LinearLayoutManager(this);
        manager2 = new LinearLayoutManager(this);
        nuevos = findViewById(R.id.rv_found_devices);
        nuevos.setHasFixedSize(true);
        nuevos.setLayoutManager(manager1);
        sincronizados = findViewById(R.id.rv_synchronized_devices);
        sincronizados.setHasFixedSize(true);
        sincronizados.setLayoutManager(manager2);
        listaNuevos = new ArrayList<>();
        listaSincronizados = getDispostivosSincronizados(); // Esto tmb se puede hacer con el método addItem()
        OnItemClickListener listenerNuevos = new OnItemClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = nuevos.getChildLayoutPosition(v);
                try {
                    //TODO  Al hacer click sobre un dispositivo ,se abre un dialogo de confirmacion "desea vincular este dispotivo?" Meter el dispositivo en los EXTRA y volver a la actividad inicial.
                    dispositivo = bDevices.get(itemPosition);
                    if (dispositivo.getName()==null)
                        Snackbar.make(findViewById(R.id.dispositivo_layout),"Dispositivo no compatible", Snackbar.LENGTH_SHORT).show();
                    else if (dispositivo.getName().contains("EP-Handle")){
                        mBluetoothAdapter.cancelDiscovery();
                        unregisterReceiver(mDeviceFoundReceiver);
                        dispositivo.createRfcommSocketToServiceRecord(uuid);
                        Intent result = new Intent();
                        result.putExtra(Constantes.DEVICE_EXTRA,dispositivo);
                        setResult(Activity.RESULT_OK,result);
                        finish();
                    }
                    else {
                        Snackbar.make(findViewById(R.id.dispositivo_layout),"Dispositivo no compatible", Snackbar.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(Constantes.TAG,"Error al establecer la conexión.");
                    finish();
                }
            }
        };
        adapter_nuevos = new AdaptadorDispositivos(listaNuevos,listenerNuevos);
        adapter_sincronizados = new AdaptadorDispositivos(listaSincronizados,listenerNuevos);
        nuevos.setAdapter(adapter_nuevos);
        sincronizados.setAdapter(adapter_sincronizados);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        buscarListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DispositivoActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constantes.REQUEST_PERMISSION);
                }
                pbDiscovery.setVisibility(View.VISIBLE);
                adapter_nuevos.clear();
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
                pbDiscovery.setVisibility(View.INVISIBLE);
            }
        };
        mDeviceFoundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //Al encontrar un dispositivo
                if (BluetoothDevice.ACTION_FOUND.equals(action)){
                    //Recuperamos el dispositivo del intent
                    BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //Añadimos el nombre y la dirección al adapter.
                    adapter_nuevos.addItem((d.getName()==null?"Desconocido":d.getName()) + "\n" + d.getAddress());
                    Log.d(Constantes.TAG,"Dispositivo encontrado: " + d.getName() + " : " + d.getAddress());
                    bDevices.add(d);
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    pbDiscovery.setVisibility(View.INVISIBLE);
                    bnBuscar.setText(getString(R.string.bn_search_devices));
                    bnBuscar.setOnClickListener(buscarListener);
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDeviceFoundReceiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mDeviceFoundReceiver,filter);
        bnBuscar.setOnClickListener(buscarListener);
    }


    private void buscarDispositivos() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    private ArrayList<String> getDispostivosSincronizados(){
        ArrayList<String> dispositivos = new ArrayList<>();
        //Coger los dispositivos sincronizados
        Set<BluetoothDevice> dispSincronizados = mBluetoothAdapter.getBondedDevices();
        //Filtrar los dispositivos sincronizados para ver solo los que tengan que ver con la app.
        for (BluetoothDevice device : dispSincronizados){
            if (device.getName().contains("EP-Handle")){
                bDevices.add(device);
                dispositivos.add(device.getName() + "\n" + device.getAddress());
            }
        }
        return dispositivos;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        if (mDeviceFoundReceiver.isOrderedBroadcast())
        unregisterReceiver(mDeviceFoundReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothAdapter.cancelDiscovery();
        if (mDeviceFoundReceiver.isOrderedBroadcast())
            unregisterReceiver(mDeviceFoundReceiver);
    }


    public interface OnItemClickListener extends View.OnClickListener{
        @Override
        void onClick(View v);
    }
}
