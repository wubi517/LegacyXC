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
import com.newlegacyxc.models.CategoryModel;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryModel> list;
    private int selected = 0;
    private Function3<CategoryModel,Integer,Boolean, Unit> categoryModelsFunction0;

    public CategoryAdapter(List<CategoryModel> list, Function3<CategoryModel,Integer,Boolean, Unit> categoryModelsFunction0) {
        this.list = list;
        this.categoryModelsFunction0=categoryModelsFunction0;
    }

    public void setSelected(int selected){
        this.selected = selected;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent,false));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, final int position) {
        holder.name.setText(list.get(position).getName());
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                holder.card.setCardElevation(8f);
                holder.itemView.setBackgroundColor(Color.parseColor("#F3DD1B"));
//                    categoryModelsFunction0.invoke(list.get(position),position,false);
            }else{
                holder.card.setCardElevation(0f);
                holder.itemView.setBackgroundColor(Color.parseColor("#000096a6"));
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if(selected!=position){
                //clickeListenerFunction(list[position])
                categoryModelsFunction0.invoke(list.get(position),position,true);
                int previouslySelected = selected;
                selected = position;
                notifyItemChanged(previouslySelected);
                notifyItemChanged(selected);
            }

        });

        if(position==selected) {
            holder.itemView.requestFocus();
//            holder.itemView.setBackgroundColor(Color.parseColor("#602962FF"));
        }
//        else holder.itemView.setBackgroundColor(Color.parseColor("#002962FF"));

    }

    class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        CardView card;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            card = itemView.findViewById(R.id.card);
        }
    }
}