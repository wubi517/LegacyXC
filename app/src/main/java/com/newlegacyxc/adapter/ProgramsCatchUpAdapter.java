package com.newlegacyxc.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.newlegacyxc.R;
import com.newlegacyxc.models.EPGEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class ProgramsCatchUpAdapter extends RecyclerView.Adapter<ProgramsCatchUpAdapter.CategoryViewHolder> {

    private List<EPGEvent> epgModels=new ArrayList<>();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
    private Function2<Integer, EPGEvent, Unit> onClickListener;
    private Function2<Integer, EPGEvent, Unit> onFocusListener;
    public ProgramsCatchUpAdapter(List<EPGEvent> epgModels, Function2<Integer, EPGEvent, Unit> onClickListener, Function2<Integer, EPGEvent, Unit> onFocusListener){
        this.epgModels=epgModels;
        this.onClickListener = onClickListener;
        this.onFocusListener = onFocusListener;
    }
    public void setEpgModels(List<EPGEvent> epgModels){
        this.epgModels=epgModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.livetv_program_item_catchup,parent,false));
    }

    @Override
    public int getItemCount() {
        return epgModels.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, final int position) {
        final EPGEvent epgEvent=epgModels.get(position);
//        holder.name.setText(epgEvent.getTitle());
//        holder.time.setText(dateFormat.format(epgEvent.getStartTime()));
        holder.name.setText(epgEvent.getTitle());
        Calendar that_date = Calendar.getInstance();
        that_date.setTime(epgEvent.getStartTime());
        holder.time.setText(dateFormat.format(that_date.getTime()));
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    holder.name.setTextColor(Color.parseColor("#ffaa3f"));
                    holder.time.setTextColor(Color.parseColor("#ffaa3f"));
                    onFocusListener.invoke(position,epgEvent);
                }else {
                    holder.name.setTextColor(Color.parseColor("#ffffff"));
                    holder.time.setTextColor(Color.parseColor("#ffffff"));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.invoke(position, epgEvent);
            }
        });
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView name,time;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            time = itemView.findViewById(R.id.time);
        }
    }
}