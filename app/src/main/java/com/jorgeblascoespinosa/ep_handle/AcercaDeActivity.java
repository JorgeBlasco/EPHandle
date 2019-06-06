package com.jorgeblascoespinosa.ep_handle;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AcercaDeActivity extends AppCompatActivity {

    Button bnVolver;
    TextView tvFecha, tvDispositivo;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acercade);
        toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tvFecha = findViewById(R.id.tv_about_date);
        tvDispositivo = findViewById(R.id.tv_about_device_name);
        bnVolver = findViewById(R.id.bn_volver);
        bnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SharedPreferences prefs = getSharedPreferences(Constantes.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        String fecha = prefs.getString(
                Constantes.PREF_FECHA_REGISTRO,
                getResources().getString(R.string.about_register_date_not_found)
        );
        tvFecha.setText(fecha);
        if (savedInstanceState != null)
        tvDispositivo.setText(savedInstanceState.getString(Constantes.DEVICE_NAME));
    }
}
