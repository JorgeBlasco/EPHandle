package com.jorgeblascoespinosa.ep_handle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private Button bn_login, bn_login_g;
    private EditText et_email, et_pass;
    private RelativeLayout loadingScreen;
    private ConstraintLayout loginLayout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_client_id))
                .requestEmail()
                .build();
        bn_login = findViewById(R.id.btn_login);
        bn_login_g = findViewById(R.id.btn_login_google);
        et_email = findViewById(R.id.et_login_email);
        et_pass = findViewById(R.id.et_login_pass);
        loginLayout = findViewById(R.id.login_layout);
        loadingScreen = findViewById(R.id.loadingPanel);
        loadingScreen.setVisibility(View.GONE);

        //----------Listeners-------------
        loginLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!(v.getId()==R.id.et_login_email || v.getId()==R.id.et_login_pass)){
                    loginLayout.clearFocus();
                    hideKeyboard();
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
               if (email.isEmpty()){
                   Toast.makeText(LoginActivity.this,"Debe introducir el email.",Toast.LENGTH_LONG).show();
                   return;
               }
               if (pass.isEmpty()){
                   Toast.makeText(LoginActivity.this,"Debe introducir la contraseña.",Toast.LENGTH_LONG).show();
                   return;
               }
               bn_login.setVisibility(View.INVISIBLE);
               loadingScreen.setVisibility(View.VISIBLE);
               mAuth.signInWithEmailAndPassword(email,pass)
                       .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                               if (task.isSuccessful()){
                                   Log.d(Constantes.TAG, "signInWithEmail:success");
                                   loadingScreen.setVisibility(View.GONE);
                                   autenticacionValida(mAuth.getCurrentUser());
                               }
                               else {
                                   bn_login.setVisibility(View.VISIBLE);
                                   loadingScreen.setVisibility(View.GONE);
                                   Toast.makeText(LoginActivity.this,"Los datos introducidos no son correctos",Toast.LENGTH_LONG).show();
                                   et_pass.setText("");
                               }
                           }
                       });
            }
        });

        /*bn_login_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingScreen.setVisibility(View.VISIBLE);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RECUEST_SIGN_IN_WITH_GOOGLE);
            }
        });

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);*/
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Constantes.REQUEST_SIGN_IN_WITH_GOOGLE:
                /*Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try{
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                    loadingScreen.setVisibility(View.GONE);
                    autenticacionValida(mAuth.getCurrentUser());
                } catch (ApiException e) {
                    Log.w(TAG,"El inicio de sesión de google ha fallado");
                }*/
                break;
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

   /* private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            autenticacionValida(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.login_layout),"Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //TODO limpiar los textFields?
//                            et_email.setText("");
//                            et_pass.setText("");
                        }
                    }
                });
    }*/

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
}


