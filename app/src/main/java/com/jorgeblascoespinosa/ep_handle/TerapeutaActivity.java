package com.jorgeblascoespinosa.ep_handle;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class TerapeutaActivity extends AppCompatActivity {

    Toolbar toolbar;
    Button bnEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terapeuta);
        toolbar = findViewById(R.id.toolbar_terapeuta);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        bnEmail = findViewById(R.id.bn_Email);
        bnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","emsapena@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Concertar cita");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Enviar email..."));
            }
        });
    }
}
