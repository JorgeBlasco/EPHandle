package com.jorgeblascoespinosa.ep_handle;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorDispositivos extends RecyclerView.Adapter<AdaptadorDispositivos.MiViewHolder> {
    private ArrayList<String> mDataset;
    DispositivoActivity.OnItemClickListener listener;

    public AdaptadorDispositivos(ArrayList<String> dispositivos, DispositivoActivity.OnItemClickListener listener) {
        this.mDataset=dispositivos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_element,parent,false);
        v.setOnClickListener(listener);
        return new MiViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MiViewHolder holder, int position) {
        if (position%2==0)
        holder.itemView.setBackgroundColor(Color.LTGRAY);
        if (!mDataset.isEmpty()) {
            TextView textView = (TextView) holder.layout.getChildAt(0);
            textView.setText(mDataset.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addItem(String device){
        mDataset.add(device);
        this.notifyItemInserted(mDataset.size()-1);
    }

    public String getItem(int index){
        return mDataset.get(index);
    }

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
