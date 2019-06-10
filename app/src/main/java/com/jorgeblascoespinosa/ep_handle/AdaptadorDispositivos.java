package com.jorgeblascoespinosa.ep_handle;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jorgeblascoespinosa.ep_handle.Activities.DispositivoActivity;

import java.util.ArrayList;

/**
 * Adaptador personalizado para cargar los datos de los dispositivos bluetooth en los RecyclerView
 */
public class AdaptadorDispositivos extends RecyclerView.Adapter<AdaptadorDispositivos.MiViewHolder> {
    private ArrayList<String> mDataset;
    DispositivoActivity.OnItemClickListener listener;

    public AdaptadorDispositivos(ArrayList<String> dispositivos, DispositivoActivity.OnItemClickListener listener) {
        this.mDataset=dispositivos;
        this.listener = listener;
    }

    /**
     * Método que carga el layout de cada elemento en la lista y le añade un listener
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public MiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_element,parent,false);
        v.setOnClickListener(listener);
        return new MiViewHolder(v);
    }

    /**
     * Método que pinta cada elemento de la lista, y le asigna el texto correspondiente.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull MiViewHolder holder, int position) {
        //Los elementos pares los pintamos de gris claro
        if (position%2==0)
        holder.itemView.setBackgroundColor(Color.LTGRAY);
        //Obtenemos el textview y le asignamos el texto
        if (!mDataset.isEmpty()) {
            TextView textView = (TextView) holder.layout.getChildAt(0);
            textView.setText(mDataset.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * Método para añadir dinámicamente nuevos elementos
     * @param device El texto a mostrar en el elemento de la lista
     */
    public void addItem(String device){
        mDataset.add(device);
        this.notifyItemInserted(mDataset.size()-1);
    }


    /**
     * Método para limpiar la lista
     */
    public void clear(){
        mDataset.clear();
        this.notifyDataSetChanged();
    }

    public static class MiViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;
        public MiViewHolder(@NonNull LinearLayout l) {
            super(l);
            layout = l;
        }
    }
}
