package com.jorgeblascoespinosa.ep_handle;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AccesoBD {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private void prueba(){
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
                .add(user);

                //-----------bloque separado----------------------

                */

    //   bt.setText("Hay bluetooth? " + (btCompatible?"Si":"No"));

}
