package com.jorgeblascoespinosa.ep_handle;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private static final int RECUEST_SIGN_IN_WITH_GOOGLE = 12;
    private static final String TAG = "EP_HANDLE";
    public static final String USER_EXTRA = "user";

    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private Button bn_login, bn_login_g;
    private EditText et_email, et_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        bn_login = findViewById(R.id.btn_login);
        bn_login_g = findViewById(R.id.btn_login_google);
        et_email = findViewById(R.id.et_login_email);
        et_pass = findViewById(R.id.et_login_pass);

        //----------Listeners-------------
        bn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final String email = et_email.getText().toString().trim();
               String pass = et_pass.getText().toString().trim();
               if (email.isEmpty()){
                   Toast.makeText(LoginActivity.this,"Debe introducir el email.",Toast.LENGTH_LONG);
                   return;
               }
               if (pass.isEmpty()){
                   Toast.makeText(LoginActivity.this,"Debe introducir la contraseña.",Toast.LENGTH_LONG);
               }

               mAuth.signInWithEmailAndPassword(email,pass)
                       .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                               if (task.isSuccessful()){
                                   Log.d(TAG, "signInWithEmail:success");
                                   Toast.makeText(LoginActivity.this, "Bienvenid@ "+email, Toast.LENGTH_SHORT).show();
                                   Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                   intent.putExtra(USER_EXTRA,mAuth.getCurrentUser());
                                   startActivity(intent);
                               }
                               else {
                                   if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                       Toast.makeText(LoginActivity.this,"Los datos introducidos no son correctos",Toast.LENGTH_LONG);
                                   }
                                   else {
                                       Toast.makeText(LoginActivity.this,"El tipo de excepcion no coincide xD",Toast.LENGTH_LONG);
                                   }
                               }
                           }
                       });
            }
        });

        bn_login_g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RECUEST_SIGN_IN_WITH_GOOGLE);
            }
        });

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            //Si está logeado ya, ir a la actividad principal.
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            intent.putExtra(USER_EXTRA,currentUser);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RECUEST_SIGN_IN_WITH_GOOGLE:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try{
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    Log.w(TAG,"El inicio de sesión de google ha fallado");
                }
        }
    }

    /**
     * Método que lanza la actividad principal una vez que el usuario se ha logueado correctamente.
     * @param user
     */
    private void autenticacionValida(FirebaseUser user) {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra(USER_EXTRA,user);
        startActivity(intent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
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
    }

}


