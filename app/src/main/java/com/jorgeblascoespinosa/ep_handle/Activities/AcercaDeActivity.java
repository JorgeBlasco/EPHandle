package com.jorgeblascoespinosa.ep_handle.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jorgeblascoespinosa.ep_handle.Constantes;
import com.jorgeblascoespinosa.ep_handle.R;

public class AcercaDeActivity extends AppCompatActivity {

    Button bnVolver;
    TextView tvFecha, tvLicencia;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acercade);
        //Añadimos el toolbar con la acción de volver atrás
        toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //Relacionamos los componentes con sus vistas
        tvFecha = findViewById(R.id.tv_about_date);
        tvLicencia = findViewById(R.id.tv_about_license);
        bnVolver = findViewById(R.id.bn_volver);

        //Al pulsar sobre el botón esta actividad finaliza
        bnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Cargamos las preferencias para establecer la fecha de registro de la aplicación.
        SharedPreferences prefs = getSharedPreferences(Constantes.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        String fecha = prefs.getString(Constantes.PREF_FECHA_REGISTRO,
                getResources().getString(R.string.about_register_date_not_found)
        );
        tvFecha.setText(fecha);
        tvLicencia.setText(Constantes.UUID);
    }
}
