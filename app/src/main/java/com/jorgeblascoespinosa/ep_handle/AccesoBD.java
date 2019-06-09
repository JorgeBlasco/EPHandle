package com.jorgeblascoespinosa.ep_handle;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class AccesoBD {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void guardaSesion(TreeMap datos) {
        String uid = auth.getUid();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        db.collection(uid).document("sesiones").collection(df.format(c)).add(datos);
    }

    private void prueba() {
        db.collection("users").whereEqualTo("primero", "Ada").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // datos.setText((String)document.get("primero"));
                            }
                    }
                });
    }
}


//Insertar datos en la base de datos
       /* Map<String, Object> user = new HashMap<>();
        user.put("primero","Ada");
        user.put("segundo","Lovelace");
        user.put("tercero",1815);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        etiqueta.setText("Base de datos actualizada");
                    }
                });

        user.clear();
        user.put("primero","Pepe");
        user.put("segundo","Navarro");
        user.put("tercero",1815);

        db.collection("users")
                .add(user);*/

