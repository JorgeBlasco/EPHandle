package com.jorgeblascoespinosa.ep_handle.Activities;

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

import com.jorgeblascoespinosa.ep_handle.AdaptadorDispositivos;
import com.jorgeblascoespinosa.ep_handle.Constantes;
import com.jorgeblascoespinosa.ep_handle.R;

import java.util.ArrayList;
import java.util.Set;

public class DispositivoActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice dispositivo;
    private BroadcastReceiver mDeviceFoundReceiver;
    private Button bnBuscar;
    private ProgressBar pbDiscovery;
    private RecyclerView disponibles;
    private AdaptadorDispositivos adapterDisponibles;
    private View.OnClickListener buscarListener, detenerListener;
    private ArrayList<BluetoothDevice> bDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivo);

        //Obtenemos el adaptador bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Relacionamos los componentes
        Toolbar toolbar = findViewById(R.id.toolbar_dispositivo);
        bnBuscar = findViewById(R.id.bn_dispositivo_buscar);
        pbDiscovery = findViewById(R.id.pb_discovery);
        disponibles = findViewById(R.id.rv_found_devices);

        //Configuramos el toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Activando esta opción aumenta el rendimiento,
        //si sabemos que el layout no va a cambiar de tamaño
        disponibles.setHasFixedSize(true);

        //Asignamos el LayoutManager al RecyclerView
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        disponibles.setLayoutManager(manager);

        //Definimos el listener que responderá ante las pulsaciones de cada elemento.
        OnItemClickListener listenerNuevos = new OnItemClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = disponibles.getChildLayoutPosition(v);
                dispositivo = bDevices.get(itemPosition);

                //Hay algunos dispositivos que no nos proporcionan el nombre, esos no son compatibles.
                if (dispositivo.getName() == null)
                    Snackbar.make(findViewById(R.id.dispositivo_layout), getString(R.string.snackbar_device_not_compatible), Snackbar.LENGTH_SHORT).show();

                //Si el nombre del dispositivo contiene "EP-Handle" es un dispositivo compatible.
                else if (dispositivo.getName().contains("EP-Handle")) {

                    //Quitamos el broadcast receiver
                    unregisterReceiver(mDeviceFoundReceiver);

                    //Guardamos el dispositivo en el intent y finalizamos.
                    Intent result = new Intent();
                    result.putExtra(Constantes.DEVICE_EXTRA, dispositivo);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
                //Si no cumple con lo anterior, tampoco es un dispositivo compatible
                else
                    Snackbar.make(findViewById(R.id.dispositivo_layout), getString(R.string.snackbar_device_not_compatible), Snackbar.LENGTH_SHORT).show();
            }
        };

        ArrayList<String> listaDisponibles = new ArrayList<>();
        adapterDisponibles = new AdaptadorDispositivos(listaDisponibles, listenerNuevos);
        disponibles.setAdapter(adapterDisponibles);

        //Listener para el botón buscar dispositivos
        buscarListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DispositivoActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constantes.REQUEST_PERMISSION);
                }
                //Mostramos el progressBar
                pbDiscovery.setVisibility(View.VISIBLE);
                //Borramos el contenido de la lista
                adapterDisponibles.clear();
                buscarDispositivos();
                //Cambiamos el texto y el listener del botón, para detener la búsqueda
                bnBuscar.setText(getString(R.string.bn_search_devices_stop));
                bnBuscar.setOnClickListener(detenerListener);
            }
        };

        //Listener para detener la búsqueda
        detenerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.cancelDiscovery();
                bnBuscar.setText(getString(R.string.bn_search_devices));
                bnBuscar.setOnClickListener(buscarListener);
                pbDiscovery.setVisibility(View.INVISIBLE);
            }
        };

        //Definición del BroadcastReceiver para responder ante nuevos dispositivos encontrados
        mDeviceFoundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //Al encontrar un dispositivo
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //Recuperamos el dispositivo del intent
                    BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //Añadimos el nombre y la dirección al adapter.
                    adapterDisponibles.addItem((d.getName() == null ? "Desconocido" : d.getName()) + "\n" + d.getAddress());
                    Log.d(Constantes.TAG, "Dispositivo encontrado: " + d.getName() + " : " + d.getAddress());
                    bDevices.add(d);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    pbDiscovery.setVisibility(View.INVISIBLE);
                    bnBuscar.setText(getString(R.string.bn_search_devices));
                    bnBuscar.setOnClickListener(buscarListener);
                }
            }
        };

        //Registro del BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDeviceFoundReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mDeviceFoundReceiver, filter);
        bnBuscar.setOnClickListener(buscarListener);
    }

    /**
     * Método que inicia la búsqueda de dispositivos
     */
    private void buscarDispositivos() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Devuelve una lista con los dispositivos sincronizados que sean compatibles, aunque no estén disponibles.
     * @return Lista de dispositivos compatibles
     */
    private ArrayList<String> getDispostivosSincronizados() {
        ArrayList<String> dispositivos = new ArrayList<>();
        //Coger los dispositivos sincronizados
        Set<BluetoothDevice> dispSincronizados = mBluetoothAdapter.getBondedDevices();
        //Filtrar los dispositivos sincronizados para ver solo los que tengan que ver con la app.
        for (BluetoothDevice device : dispSincronizados) {
            if (device.getName().contains("EP-Handle")) {
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


    //Creo mi propia interfaz listener para asignarla a los elementos de la lista
    public interface OnItemClickListener extends View.OnClickListener {
        @Override
        void onClick(View v);
    }
}
