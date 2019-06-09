package com.jorgeblascoespinosa.ep_handle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private int backButtonCount = 0;
    private boolean btConectado, btCompatible = false;

    private BroadcastReceiver mBluetoothStateReceiver;
    private BluetoothDevice dispositvo;

    private TextView tv_bluetoothState, tv_deviceName, tv_day, tv_totalSessions;
    private Spinner sp_sessionType;
    private Button bn_start;
    private CheckBox cb_setTime;
    private EditText et_settedTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relacionarViews();
        setSupportActionBar(mToolbar);
        //Guardamos la fecha de registro en las preferencias (sólo la primera vez que se inicia la app)
        SharedPreferences prefs = getSharedPreferences(Constantes.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        if (prefs.getString(Constantes.PREF_FECHA_REGISTRO, null) == null) {
            SharedPreferences.Editor editor = prefs.edit();
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            editor.putString(Constantes.PREF_FECHA_REGISTRO, df.format(c));
            editor.apply();
        }

        //Configuramos el listener del checkbox
        cb_setTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                et_settedTime.setEnabled(isChecked);
            }
        });
        //Obtener el adaptador bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            btCompatible = false;
        else{
            btCompatible = true;
            //Lanzamos el intent  que muestra un diálogo de activación del bluetooth (sin salir de la aplicación)
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constantes.REQUEST_ENABLE_BT);
            }
            configBluetooth();
        }
        //Cargamos los datos en sus respectivos contenedores.
        actualizaUI(savedInstanceState);
    }

    /**
     * Actualiza la vista con los datos de la base de datos.
     * @param savedInstanceState
     */
    private void actualizaUI(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(Constantes.EP_HANDLE_PREFS,MODE_PRIVATE);
        int numSesiones = prefs.getInt(Constantes.PREF_SESSION_NUMBER,0);
        tv_totalSessions.setText(String.valueOf(numSesiones));
        if (!btCompatible) {
            tv_bluetoothState.setText(getString(R.string.bluetooth_state_not_available));
            tv_bluetoothState.setTextColor(Color.RED);
        }else {
            tv_bluetoothState.setText(getString(R.string.bluetooth_state_available));
            tv_bluetoothState.setTextColor(Color.GREEN);
        }
        if (dispositvo != null) {
            tv_deviceName.setText(dispositvo.getName()==null?"Desconocido":dispositvo.getName());
            tv_bluetoothState.setText(getString(R.string.bluetooth_state_connected));
        }
        if (savedInstanceState!=null){
            dispositvo = savedInstanceState.getParcelable(Constantes.DEVICE_EXTRA);

            if (savedInstanceState.containsKey(Constantes.SESSION_POSITION_EXTRA))
                sp_sessionType.setSelection(savedInstanceState.getInt(Constantes.SESSION_POSITION_EXTRA));
            if (savedInstanceState.containsKey(Constantes.TIME_EXTRA)){
                cb_setTime.setChecked(true);
                et_settedTime.setText(String.valueOf(savedInstanceState.getInt(Constantes.TIME_EXTRA)));
            }
        }
        else
            tv_deviceName.setText(R.string.bluetooth_state_not_connected);

        String fecha = prefs.getString(Constantes.PREF_FECHA_REGISTRO,null);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date date;
        try {
            date=df.parse(fecha);
            int dias = (int)((new Date().getTime()-date.getTime())/86400000);
            dias++;
            tv_day.setText(String.valueOf(dias));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configura lo perteneciente al bluetooth.
     */
    private void configBluetooth() {
        //El listener del botón no se asigna si no existe el adaptador bluetooth, o si el bluetooth no está activado.
        bn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (sp_sessionType.getSelectedItem().toString().equals(getApplicationContext().getResources().getStringArray(R.array.session_types)[0]))
                        Toast.makeText(MainActivity.this, "Selecciona un tipo de sesión.", Toast.LENGTH_SHORT).show();
                    else {
                        if (dispositvo!=null) {
                            Intent sesion = new Intent(MainActivity.this, SesionActivity.class);
                            // Bundle extras = new Bundle();
                            // extras.putParcelable(Constantes.DEVICE_EXTRA, dispositvo);
                            sesion.putExtra(Constantes.DEVICE_EXTRA, dispositvo);
                            sesion.putExtra(Constantes.SESSION_EXTRA, sp_sessionType.getSelectedItem().toString());
                            if (cb_setTime.isChecked()) {
                                sesion.putExtra(Constantes.TIME_EXTRA, Integer.parseInt(et_settedTime.getText().toString()));
                            }
                            //sesion.putExtras(extras);
                            startActivityForResult(sesion, Constantes.REQUEST_SESSION);
                        }
                        else
                            Toast.makeText(MainActivity.this, "Selecciona un dispositivo del menú.", Toast.LENGTH_LONG).show();
                    }
            }
        });

        //Declaramos y registramos el listener que responderá ante eventos de cambio de estado de bluetooth.
        mBluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            tv_bluetoothState.setText(getString(R.string.bluetooth_state_not_available));
                            tv_bluetoothState.setTextColor(Color.RED);
                            bn_start.setEnabled(false);
                            btConectado = false;
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            tv_bluetoothState.setText(getString(R.string.bluetooth_state_turning_off));
                            tv_bluetoothState.setTextColor(Color.RED);
                            tv_deviceName.setText(getString(R.string.bluetooth_state_not_available));
                            bn_start.setEnabled(false);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            tv_bluetoothState.setText(getString(R.string.bluetooth_state_available));
                            tv_bluetoothState.setTextColor(Color.GREEN);
                            bn_start.setEnabled(true);
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            tv_bluetoothState.setText(getString(R.string.bluetooth_state_turning_on));
                            tv_bluetoothState.setTextColor(Color.BLACK);
                            bn_start.setEnabled(false);
                            break;
                        case BluetoothAdapter.ERROR:
                            tv_bluetoothState.setText(getString(R.string.bluetooth_state_error));
                            tv_bluetoothState.setTextColor(Color.RED);
                            bn_start.setEnabled(false);
                            btConectado = false;
                            break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, filter);
    }

    /**
     * Método que relaciona los elementos visuales con sus objetos.
     */
    private void relacionarViews() {
        mToolbar = findViewById(R.id.toolbar);
        tv_bluetoothState = findViewById(R.id.tv_bluetooth_state);
        tv_deviceName = findViewById(R.id.tv_device_name);
        tv_day = findViewById(R.id.tv_day);
        tv_totalSessions = findViewById(R.id.tv_total_sessions);
        sp_sessionType = findViewById(R.id.sp_session_type);
        bn_start = findViewById(R.id.bn_start);
        cb_setTime = findViewById(R.id.cb_set_time);
        et_settedTime = findViewById(R.id.et_setted_time);
    }

    /**
     * Asigna un layout al menú.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    /**
     * Acciones al pulsar sobre uno de los elementos del menú.
     *
     * @param item El elemento pulsado
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_item_mi_terapeuta:
                startActivity(new Intent(MainActivity.this, TerapeutaActivity.class));
                break;
            case R.id.menu_item_dispositivo:
                if (!btCompatible || !btConectado) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constantes.REQUEST_ENABLE_BT_DEVICE);
                } else {
                    Intent seleccionarDispositivo = new Intent(MainActivity.this, DispositivoActivity.class);
                    startActivityForResult(seleccionarDispositivo, Constantes.REQUEST_SELECT_DEVICE);
                }
                break;
            case R.id.menu_item_config:
                startActivity(new Intent(MainActivity.this,
                        PreferencesActivity.class));
                break;
            case R.id.menu_item_acercade:
                startActivity(new Intent(MainActivity.this, AcercaDeActivity.class));
                break;
            case R.id.menu_item_cerrar_sesion:
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            case R.id.menu_item_salir:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
        return true;
    }

    /**
     * Manejo de las acciones después de volver de una actividad.
     *
     * @param requestCode El tipo de acción de la que viene.
     * @param resultCode  Código que indica el resultado de la acción.
     * @param data        Datos provenientes de la acción.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Qué actividad es la que retorna?
        switch (requestCode) {
            //Petición de activación de bluetooth
            case Constantes.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    btConectado = true;
                    configBluetooth();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    btConectado = false;
                }
                break;

            //Petición de activación de bluetooth y abrir intent de dispositivo
            case Constantes.REQUEST_ENABLE_BT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    btConectado = true;
                    Intent seleccionarDispositivo = new Intent(MainActivity.this, DispositivoActivity.class);
                    startActivityForResult(seleccionarDispositivo, Constantes.REQUEST_SELECT_DEVICE);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    btConectado = false;
                }
                break;

            //Petición de selección de dispositivo
            case Constantes.REQUEST_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        dispositvo = data.getParcelableExtra(Constantes.DEVICE_EXTRA);
                        tv_deviceName.setText(dispositvo.getName()==null?"Desconocido":dispositvo.getName());
                    } else
                        tv_deviceName.setText(getString(R.string.bluetooth_state_not_available));
                } else
                    tv_deviceName.setText(getString(R.string.bluetooth_state_not_available));
                break;
            //Petición de intent de sesión
            case Constantes.REQUEST_SESSION:
                if (resultCode == Activity.RESULT_OK) {
                    if (data!=null){
                        TreeMap datos = new TreeMap((Map) data.getExtras().get(Constantes.DATA_EXTRA));
                        AccesoBD.guardaSesion(datos);
                        actualizaUI(null);
                        reiniciarOpciones();
                    }
                }
                break;
        }
    }

    /**
     * Reinicia las opciones de sesión.
     */
    public void reiniciarOpciones(){
        sp_sessionType.setSelection(0);
        cb_setTime.setChecked(false);
        et_settedTime.setText("");
    }

    /**
     * Acción al pulsar el botón volver del dispositivo.
     */
    @Override
    public void onBackPressed() {
        if (backButtonCount >= 1) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Pulsa otra vez para salir de la aplicacion.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backButtonCount=0;
        if (mBluetoothStateReceiver.isOrderedBroadcast())
            unregisterReceiver(mBluetoothStateReceiver);    }

    @Override
    protected void onPause() {
        super.onPause();
        backButtonCount=0;
        if (mBluetoothStateReceiver.isOrderedBroadcast())
            unregisterReceiver(mBluetoothStateReceiver);
    }

    /**
     * Guardo todos los datos necesarios para reconstruir el estado anterior
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (dispositvo!=null)
        outState.putParcelable(Constantes.DEVICE_EXTRA,dispositvo);
        if (!sp_sessionType.getSelectedItem().toString().equals(getApplicationContext().getResources().getStringArray(R.array.session_types)[0]))
            outState.putInt(Constantes.SESSION_POSITION_EXTRA,sp_sessionType.getSelectedItemPosition());
        if (cb_setTime.isChecked())
            outState.putInt(Constantes.TIME_EXTRA, Integer.parseInt(et_settedTime.getText().toString()));
        super.onSaveInstanceState(outState);
    }
}
