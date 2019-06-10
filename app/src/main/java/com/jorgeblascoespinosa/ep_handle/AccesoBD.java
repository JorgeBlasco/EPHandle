package com.jorgeblascoespinosa.ep_handle;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

public class AccesoBD {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void guardaSesion(TreeMap datos) {
        //Usamos el UID como nodo base para separar los datos de cada usuario.
        String uid = auth.getUid();
        //Obtenemos la fecha para crear una ruta ordenada en la base de datos.
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        //Añadimos los datos.
        db.collection(uid).document("sesiones").collection(df.format(c)).add(datos);
    }
}