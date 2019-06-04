package com.jorgeblascoespinosa.ep_handle;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AcercaDeActivity extends AppCompatActivity {

    public static final String DEVICE_NAME = "device_name";
    Button bnVolver;
    TextView tvFecha, tvDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acercade);
        tvFecha = findViewById(R.id.tv_about_date);
        tvDispositivo = findViewById(R.id.tv_about_device_name);
        bnVolver = findViewById(R.id.bn_volver);
        bnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //TODO igual con esto peta. sólo tiene que volver a su actividad padre
            }
        });
        SharedPreferences prefs = getSharedPreferences(MainActivity.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        String fecha = prefs.getString(
                MainActivity.PREF_FECHA_REGISTRO,
                getResources().getString(R.string.about_register_date_not_found)
        );
        tvFecha.setText(fecha);
        tvDispositivo.setText(savedInstanceState.getString(DEVICE_NAME));
    }
}
