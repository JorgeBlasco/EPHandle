package com.jorgeblascoespinosa.ep_handle;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class PreferencesActivity extends AppCompatActivity {

    Toolbar toolbar;
    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        prefs = getSharedPreferences(Constantes.EP_HANDLE_PREFS, Context.MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar_config);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
