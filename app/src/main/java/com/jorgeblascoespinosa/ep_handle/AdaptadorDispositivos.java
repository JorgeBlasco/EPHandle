package com.jorgeblascoespinosa.ep_handle;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorDispositivos extends RecyclerView.Adapter<AdaptadorDispositivos.MiViewHolder> {
    private ArrayList<String> mDataset;

    public AdaptadorDispositivos(ArrayList<String> dispositivos) {
        this.mDataset=dispositivos;
    }

    @NonNull
    @Override
    public MiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_element,parent,false);
        MiViewHolder vh = new MiViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MiViewHolder holder, int position) {
        holder.textView.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(String device){
        mDataset.add(device);
        this.notifyItemInserted(mDataset.size()-1);
    }

    public static class MiViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public MiViewHolder(@NonNull TextView v) {
            super(v);
            textView = v;
        }
    }
}
