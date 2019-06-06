package com.jorgeblascoespinosa.ep_handle;

import android.app.Activity;
import android.app.AlertDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private int backButtonCount = 0;
    private boolean btCompatible = true;
    private BroadcastReceiver mBluetoothStateReceiver;
    private Toast btDesconectado = Toast.makeText(this, "El bluetooth no está disponible.", Toast.LENGTH_SHORT);
    private BluetoothDevice dispositvo;

    private TextView tv_bluetoothState, tv_deviceName, tv_day, tv_totalSessions, tv_sessionObjective;
    private Spinner sp_sessionType;
    private Button bn_start;
    private CheckBox cb_setTime;
    private EditText et_settedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(Constantes.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        //Guardamos la fecha de registro en las preferencias (sólo la primera vez que se inicia la app)
        if (prefs.getString(Constantes.PREF_FECHA_REGISTRO, null) == null) {
            SharedPreferences.Editor editor = prefs.edit();
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            editor.putString(Constantes.PREF_FECHA_REGISTRO, df.format(c));
            editor.apply();
        }
        setContentView(R.layout.activity_main);
        relacionarViews();
        cb_setTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                et_settedTime.setEnabled(isChecked);
            }
        });
        setSupportActionBar(mToolbar);
        //Obtener la referencia a la base de datos Firebase
        //Obtener el adaptador bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            btCompatible = false;
        else
            configBluetooth();
        actualizaUI();


        //Para lanzar el intent  que muestra un dialogo de activación del bluetooth (sin salir de la aplicacion)
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constantes.REQUEST_ENABLE_BT);
        }
    }

    /**
     * Actualiza la vista con los datos de la base de datos.
     */
    private void actualizaUI() {
        if (!btCompatible) {
            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_not_connected));
            tv_bluetoothState.setTextColor(Color.RED);
        }
        //TODO IMPORTANTE recojo los datos de la base de datos y los muestro en la pantalla
    }

    /**
     * Configura lo perteneciente al bluetooth.
     */
    private void configBluetooth() {
        //El listener del botón no se asigna si no existe el adaptador bluetooth, o si el bluetooth no está activado.
        bn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sp_sessionType.getSelectedItem().toString().equals(getResources().getStringArray(R.array.session_types)[0])) {
                    Toast.makeText(MainActivity.this, "Selecciona un tipo de sesión.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent sesion = new Intent(MainActivity.this, SesionActivity.class);
                    Bundle extras = new Bundle();
                    extras.putParcelable(Constantes.DEVICE_EXTRA, dispositvo);
                    extras.putString(Constantes.SESSION_EXTRA, sp_sessionType.getSelectedItem().toString());
                    if (cb_setTime.isChecked()) {
                        extras.putInt(Constantes.TIME_EXTRA, Integer.parseInt(et_settedTime.getText().toString()));
                    }
                    sesion.putExtras(extras);
                    startActivityForResult(sesion, Constantes.REQUEST_SESSION);
                }
                //TODO abrir actividad de sesión, recogiendo el tipo de sesión, el tiempo fijado (si hay)
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
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_not_connected));
                            tv_bluetoothState.setTextColor(Color.RED);
                            bn_start.setEnabled(false);
                            btDesconectado.show();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_turning_off));
                            tv_bluetoothState.setTextColor(Color.RED);
                            bn_start.setEnabled(false);
                            btDesconectado.show();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_connected));
                            tv_bluetoothState.setTextColor(Color.GREEN);
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_turning_on));
                            tv_bluetoothState.setTextColor(Color.YELLOW);
                            bn_start.setEnabled(false);
                            btDesconectado.show();
                            break;
                        case BluetoothAdapter.ERROR:
                            tv_bluetoothState.setText(getResources().getText(R.string.bluetooth_state_error));
                            tv_bluetoothState.setTextColor(Color.RED);
                            bn_start.setEnabled(false);
                            btDesconectado.show();
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
        tv_sessionObjective = findViewById(R.id.tv_session_objective);
        sp_sessionType = findViewById(R.id.sp_session_type);
        bn_start = findViewById(R.id.bn_start);
        cb_setTime = findViewById(R.id.cb_set_time);
        et_settedTime = findViewById(R.id.et_setted_time);
    }

    /**
     * Asigna un layout al menú.
     *
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
                //TODO caso terapeuta
                break;
            case R.id.menu_item_dispositivo:
                if (!btCompatible) {
                    abrirDialogoNoBt();
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
                    //TODO si activa el bluetooth ante la petición
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    //TODO si no lo activa
                }
                break;

            //Petición de selección de dispositivo
            case Constantes.REQUEST_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        dispositvo = data.getParcelableExtra(Constantes.DEVICE_EXTRA);
                        tv_deviceName.setText(dispositvo.getName());
                    } else
                        tv_deviceName.setText(getString(R.string.bluetooth_state_not_connected));
                } else
                    tv_deviceName.setText(getString(R.string.bluetooth_state_not_connected));
                break;
            //Petición de sesión
            case Constantes.REQUEST_SESSION:
                if (resultCode == Activity.RESULT_OK) {
                    //TODO debe venirme un MAP de la sesion, que hay que subirlo a la nube.
                }
        }
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
        unregisterReceiver(mBluetoothStateReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBluetoothStateReceiver);
    }

    /**
     * Muestra un diálogo que informa de que el bluetooth no está disponible.
     */
    public void abrirDialogoNoBt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_no_bt_title))
                .setMessage(getString(R.string.dialog_no_bt_message));
        AlertDialog dialog = builder.create();

    }
}
