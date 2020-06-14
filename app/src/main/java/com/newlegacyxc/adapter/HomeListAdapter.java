package com.newlegacyxc.adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.newlegacyxc.R;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.MovieModel;
import com.newlegacyxc.models.SeriesModel;

import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function5;

public class HomeListAdapter extends RecyclerView.Adapter<HomeListAdapter.HomeListViewHolder> {
    private Context context;
    private Function5<Integer, Integer, EPGChannel, MovieModel, SeriesModel, Unit> clickListenerFunction;
    private boolean IS_NULL_RECENT;
    public HomeListAdapter(Context context, boolean IS_NULL_RECENT, Function5<Integer,Integer, EPGChannel, MovieModel, SeriesModel, Unit> clickListenerFunction) {
        this.context = context;
        this.clickListenerFunction = clickListenerFunction;
        this.IS_NULL_RECENT = IS_NULL_RECENT;
    }

    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeListViewHolder holder, final int position) {
        holder.items_recyclerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false));
        switch (position){
            case 0:
                holder.name.setText(context.getResources().getString(R.string.featured_live_tv));
                break;
            case 1:
                holder.name.setText(context.getResources().getString(R.string.featured_movies));
                break;
            case 2:
                holder.name.setText(context.getResources().getString(R.string.featured_series));
                break;
        }
        holder.items_recyclerview.setAdapter(new HomeCategoryListAdapter(position, (pos, epgChannel, movieModel, seriesModel) -> {
            Log.e("Home","clicked "+position+" "+pos);
            clickListenerFunction.invoke(position,pos,epgChannel,movieModel,seriesModel);
            return null;
        }));
        if (position==0 && IS_NULL_RECENT){
            final int pos = 0;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (holder.items_recyclerview.findViewHolderForAdapterPosition(pos)!=null){
                        Objects.requireNonNull(holder.items_recyclerview.findViewHolderForAdapterPosition(pos)).itemView.performClick();
                        IS_NULL_RECENT = false;
                    }
                }
            },1);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    class HomeListViewHolder extends  RecyclerView.ViewHolder{
        TextView name;
        RecyclerView items_recyclerview;
        HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            items_recyclerview = itemView.findViewById(R.id.items_recyclerview);
        }
    }
}