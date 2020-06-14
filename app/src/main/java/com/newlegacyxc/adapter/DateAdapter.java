package com.newlegacyxc.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.newlegacyxc.R;
import com.newlegacyxc.models.CatchupModel;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.CategoryViewHolder> {
    private List<CatchupModel> list;
    private int selected = 0;
    private Function2<CatchupModel,Integer, Unit> categoryModelsFunction0;

    public DateAdapter(List<CatchupModel> list, Function2<CatchupModel, Integer, Unit> categoryModelsFunction0) {
        this.list = list;
        this.categoryModelsFunction0=categoryModelsFunction0;
    }

    public void setList(List<CatchupModel> list){
        this.list = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_catchup,parent,false));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, final int position) {
        holder.name.setText(list.get(position).getName());
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    categoryModelsFunction0.invoke(list.get(position),position);
                    holder.card.setCardElevation(8f);
                    holder.itemView.setBackgroundColor(Color.parseColor("#602962FF"));
                    holder.name.setTextColor(Color.parseColor("#64DD17"));
                }else{
                    holder.card.setCardElevation(0f);
                    holder.itemView.setBackgroundColor(Color.parseColor("#002962FF"));
                    holder.name.setTextColor(Color.parseColor("#ffffff"));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selected!=position){
                    categoryModelsFunction0.invoke(list.get(position),position);
                    int previouslySelected = selected;
                    selected = position;
                    notifyItemChanged(previouslySelected);
                    notifyItemChanged(selected);
                }
            }
        });

        if(position==0) holder.itemView.requestFocus();
        if(selected==position) {
            holder.card.setCardElevation(8f);
            holder.name.setTextColor(Color.parseColor("#64DD17"));
            holder.itemView.setBackgroundColor(Color.parseColor("#602962FF"));
        }
        else {
            holder.card.setCardElevation(0f);
            holder.name.setTextColor(Color.parseColor("#ffffff"));
            holder.itemView.setBackgroundColor(Color.parseColor("#002962FF"));
        }
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        CardView card;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            card = itemView.findViewById(R.id.card);
        }
    }
}