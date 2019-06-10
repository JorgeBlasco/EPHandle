package com.jorgeblascoespinosa.ep_handle.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.jorgeblascoespinosa.ep_handle.Constantes;
import com.jorgeblascoespinosa.ep_handle.R;

public class PreferencesActivity extends AppCompatActivity {


    Toolbar toolbar;
    SharedPreferences prefs;
    SeekBar sbVibracion, sbVolumen, sbBrillo;
    CheckBox cbVibracion, cbSonido, cbIluminacion, cbNotificaciones;
    FloatingActionButton bnGuardar;
    Button bn_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        asignarViews();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        cargaDatos();
        cargaListeners();

        //Desactivamos los controles que actualmente están en desuso.
        cbVibracion.setEnabled(false);
        sbVibracion.setEnabled(false);
        sbBrillo.setEnabled(false);
    }

    private void asignarViews() {
        prefs = getSharedPreferences(Constantes.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar_config);
        bnGuardar = findViewById(R.id.bn_pref_save);
        sbVibracion = findViewById(R.id.sb_pref_intensity);
        sbVolumen = findViewById(R.id.sb_pref_vol);
        sbBrillo = findViewById(R.id.sb_pref_brillo);
        cbIluminacion = findViewById(R.id.cb_pref_ilum);
        cbSonido = findViewById(R.id.cb_pref_sound);
        cbVibracion = findViewById(R.id.cb_pref_vib);
        cbNotificaciones = findViewById(R.id.cb_pref_notifications);
        bn_reset = findViewById(R.id.bn_reset);
    }


    /**
     * Mueve los controles para que coincidan con las preferencias guardadas
     */
    private void cargaDatos() {
        //Si no existen prefencias, se crean las de por defecto
        if (prefs.getBoolean(Constantes.LOAD_DEFAULT_SETTINGS,true)){
            cargaPreferenciasPorDefecto();
        }
        sbVibracion.setProgress(prefs.getInt(Constantes.PREF_VIBR_INTENSITY,49));
        sbVolumen.setProgress(prefs.getInt(Constantes.PREF_SOUND_VOLUME,1000));
        sbBrillo.setProgress(prefs.getInt(Constantes.PREF_LIGHT_BRIGHTNESS,3));
        cbVibracion.setChecked(prefs.getBoolean(Constantes.PREF_ENABLE_VIBRATION,false));
        cbSonido.setChecked(prefs.getBoolean(Constantes.PREF_ENABLE_SOUND,true));
        cbIluminacion.setChecked(prefs.getBoolean(Constantes.PREF_ENABLE_LIGHT,true));
        cbNotificaciones.setChecked(prefs.getBoolean(Constantes.PREF_ENABLE_NOTIFICATIONS,true));
    }

    /**
     * Carga unas preferencias por defecto
     */
    private void cargaPreferenciasPorDefecto() {
        SharedPreferences.Editor editor = prefs.edit();
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
     * Guarda las preferencias tal y como están configuradas en los controles
     */
    private void guardarDatos(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constantes.PREF_ENABLE_VIBRATION,cbVibracion.isChecked());
        editor.putBoolean(Constantes.PREF_ENABLE_SOUND,cbSonido.isChecked());
        editor.putBoolean(Constantes.PREF_ENABLE_LIGHT,cbIluminacion.isChecked());
        editor.putBoolean(Constantes.PREF_ENABLE_NOTIFICATIONS,cbNotificaciones.isChecked());
        editor.putInt(Constantes.PREF_VIBR_INTENSITY,sbVibracion.getProgress());
        editor.putInt(Constantes.PREF_SOUND_VOLUME,sbVolumen.getProgress());
        editor.putInt(Constantes.PREF_LIGHT_BRIGHTNESS,sbBrillo.getProgress());
        editor.apply();
    }

    /**
     * Carga los listeners para todos los controles
     */
    private void cargaListeners() {
        cbVibracion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sbVibracion.setEnabled(isChecked);
            }
        });
        cbSonido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sbVolumen.setEnabled(isChecked);
            }
        });
//        cbIluminacion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                sbBrillo.setEnabled(isChecked);
//            }
//        });
        bnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatos();
                finish();
            }
        });
        bn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargaPreferenciasPorDefecto();
                cargaDatos();
            }
        });
    }
}
