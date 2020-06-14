package com.newlegacyxc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.newlegacyxc.R;
import com.newlegacyxc.models.SideMenu;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.CategoryViewHolder> {
    List<SideMenu> list;
    Context context;
    Function2<SideMenu,Integer, Unit> clickeListenerFunction;
    int selected = 0;

    public MenuAdapter(List<SideMenu> list, Context context, Function2<SideMenu, Integer, Unit> clickeListenerFunction) {
        this.list = list;
        this.context = context;
        this.clickeListenerFunction = clickeListenerFunction;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item,parent,false));
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
                holder.itemView.setBackgroundColor(Color.parseColor("#2962FF"));
                holder.name.setScaleX(1.1f);
                holder.name.setScaleY(1.1f);
                holder.name.setTypeface(ResourcesCompat.getFont(context,R.font.montserrat_bold));
            }else{
                holder.itemView.setBackgroundColor(Color.parseColor("#002962FF"));
                holder.name.setScaleX(1.0f);
                holder.name.setScaleY(1.0f);
                holder.name.setTypeface(ResourcesCompat.getFont(context,R.font.montserrat));
            }
        });

        holder.itemView.setOnClickListener(v -> {
            clickeListenerFunction.invoke(list.get(position), position);
            int previouslySelected = selected;
            selected = position;
            notifyItemChanged(previouslySelected);
            notifyItemChanged(selected);
        });
        if(selected==position)
            holder.itemView.requestFocus();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }
}