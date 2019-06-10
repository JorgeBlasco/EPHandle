package com.jorgeblascoespinosa.ep_handle.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jorgeblascoespinosa.ep_handle.Constantes;
import com.jorgeblascoespinosa.ep_handle.R;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button bn_login, bn_login_g;
    private EditText et_email, et_pass;
    private RelativeLayout loadingScreen;
    private ConstraintLayout loginLayout;
    private int backButtonCount = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        //Asignamos los coontroles//
        bn_login = findViewById(R.id.btn_login);
        bn_login_g = findViewById(R.id.btn_login_google);
        et_email = findViewById(R.id.et_login_email);
        et_pass = findViewById(R.id.et_login_pass);
        loginLayout = findViewById(R.id.login_layout);
        loadingScreen = findViewById(R.id.loadingPanel);
        loadingScreen.setVisibility(View.GONE);

        //----------Listeners-------------//
        loginLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Registramos las pulsaciones en cualquier parte de la pantalla,
                //y siempre que esas pulsaciones no sucedan en los campos de texto,
                //esconderemos el teclado.
                if (!(v.getId() == R.id.et_login_email || v.getId() == R.id.et_login_pass)) {
                    loginLayout.clearFocus();
                    hideKeyboard();
                    bn_login.requestFocus();
                }
                return true;
            }
        });
        bn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                final String email = et_email.getText().toString().trim();
                String pass = et_pass.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Debe introducir el email.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Debe introducir la contraseña.", Toast.LENGTH_LONG).show();
                    return;
                }
                bn_login.setVisibility(View.INVISIBLE);
                loadingScreen.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(Constantes.TAG, "signInWithEmail:success");
                                    loadingScreen.setVisibility(View.GONE);
                                    autenticacionValida(mAuth.getCurrentUser());
                                } else {
                                    bn_login.setVisibility(View.VISIBLE);
                                    loadingScreen.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Los datos introducidos no son correctos", Toast.LENGTH_LONG).show();
                                    et_pass.setText("");
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            //Si está logeado ya, ir a la actividad principal.
            autenticacionValida(currentUser);
        }
    }

    /**
     * Método que lanza la actividad principal una vez que el usuario se ha logueado correctamente.
     * @param user
     */
    private void autenticacionValida(FirebaseUser user) {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra(Constantes.USER_EXTRA,user);
        startActivity(intent);
    }

    /**
     * Método para esconder el teclado cuando es llamado.
     */
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        else {
            view = loginLayout;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * Método para controlar qué se hace cuando se pulsa la tecla atrás del dispositivo.
     */
    @Override
    public void onBackPressed() {
        if (backButtonCount >= 1) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Pulsa otra vez para salir de la aplicación.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }
}


