package com.newlegacyxc.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.MovieModel;
import com.newlegacyxc.models.SeriesModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function4;

class HomeCategoryListAdapter extends RecyclerView.Adapter<HomeCategoryListAdapter.HomeListViewHolder> {

    private int type;
    private Function4<Integer, EPGChannel, MovieModel, SeriesModel, Unit> clickListenerFunction;
    private List<EPGChannel> epgChannels = new ArrayList<>();
    private List<MovieModel> movieModels = new ArrayList<>();
    private List<SeriesModel> seriesModels = new ArrayList<>();

    HomeCategoryListAdapter(int type, Function4<Integer, EPGChannel, MovieModel, SeriesModel, Unit> function) {
        clickListenerFunction = function;
        this.type = type;
        int sorting_id;
        if (MyApp.instance.getPreference().get(Constants.getSORT())!=null)
            sorting_id = (int) MyApp.instance.getPreference().get(Constants.getSORT());
        else sorting_id = 0;
        switch (sorting_id){
            case 0://last added
                switch (type){
                    case 0:
                        epgChannels = new ArrayList<>(Constants.getFavFullModel(MyApp.fullModels).getChannels());
                        Log.e(getClass().getSimpleName(),"all full model: "+epgChannels.size());
                        Collections.sort(epgChannels, (o1, o2) -> o2.getAdded().compareTo(o1.getAdded()));
                        break;
                    case 1:
                        movieModels = new ArrayList<>(MyApp.movieModels);
                        Collections.sort(movieModels, (o1, o2) -> o2.getAdded().compareTo(o1.getAdded()));
                        break;
                    case 2:
                        seriesModels = new ArrayList<>(MyApp.seriesModels);
                        Collections.sort(seriesModels, (o1, o2) -> o2.getLast_modified().compareTo(o1.getLast_modified()));
                        break;
                }
                break;
            case 1://top to bottom
                switch (type){
                    case 0:
                        epgChannels = new ArrayList<>(Constants.getFavFullModel(MyApp.fullModels).getChannels());
                        break;
                    case 1:
                        movieModels = new ArrayList<>(MyApp.movieModels);
                        break;
                    case 2:
                        seriesModels = new ArrayList<>(MyApp.seriesModels);
                        break;
                }
                break;
            case 2://bottom to top
                switch (type){
                    case 0:
                        epgChannels = new ArrayList<>(Constants.getFavFullModel(MyApp.fullModels).getChannels());
                        Collections.reverse(epgChannels);
                        break;
                    case 1:
                        movieModels = new ArrayList<>(MyApp.movieModels);
                        Collections.reverse(movieModels);
                        break;
                    case 2:
                        seriesModels = new ArrayList<>(MyApp.seriesModels);
                        Collections.reverse(seriesModels);
                        break;
                }
                break;
        }
    }

    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_category_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeListViewHolder holder, final int position) {
        switch (type){
            case 0:
                final EPGChannel epgChannel = epgChannels.get(position);
                if (epgChannel.getStream_icon()!=null && !epgChannel.getStream_icon().equals("")){
                    Picasso.with(holder.itemView.getContext())
                            .load(epgChannel.getStream_icon())
                            .placeholder(R.drawable.icon) // Equivalent of what ends up in onPrepareLoad
                            .error(R.drawable.icon) // Equivalent of what ends up in onBitmapFailed
                            .centerCrop()
                            .fit()
                            .into(holder.image);
                }else {
                    holder.image.setImageResource(R.drawable.icon);
                }
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(epgChannel.getName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListenerFunction.invoke(position,epgChannel, null, null);
                    }
                });
                break;
            case 1:
                final MovieModel movieModel = movieModels.get(position);
                if (movieModel.getStream_icon()!=null && !movieModel.getStream_icon().equals("")){
                    Picasso.with(holder.itemView.getContext())
                            .load(movieModel.getStream_icon())
                            .placeholder(R.drawable.icon) // Equivalent of what ends up in onPrepareLoad
                            .error(R.drawable.icon) // Equivalent of what ends up in onBitmapFailed
                            .centerCrop()
                            .fit()
                            .into(holder.image);
                }else {
                    holder.image.setImageResource(R.drawable.icon);
                }
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(movieModel.getName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListenerFunction.invoke(position, null, movieModel, null);
                    }
                });
                break;
            case 2:
                final SeriesModel seriesModel = seriesModels.get(position);
                if (seriesModel.getStream_icon()!=null && !seriesModel.getStream_icon().equals("")){
                    Picasso.with(holder.itemView.getContext())
                            .load(seriesModel.getStream_icon())
                            .placeholder(R.drawable.icon) // Equivalent of what ends up in onPrepareLoad
                            .error(R.drawable.icon) // Equivalent of what ends up in onBitmapFailed
                            .centerCrop()
                            .fit()
                            .into(holder.image);
                }else {
                    holder.image.setImageResource(R.drawable.icon);
                }
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(seriesModel.getName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListenerFunction.invoke(position, null, null,seriesModel);
                    }
                });
                break;
        }
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    holder.card.setCardElevation(10f);
                    holder.card.setCardBackgroundColor(Color.parseColor("#FFD600"));
                    holder.itemView.setScaleX(1f);
                    holder.itemView.setScaleY(1f);
                    holder.name.setTextColor(Color.parseColor("#212121"));
                }else{
                    holder.card.setCardElevation(1f);
                    holder.card.setCardBackgroundColor(Color.parseColor("#25ffffff"));
                    holder.itemView.setScaleX(0.95f);
                    holder.itemView.setScaleY(0.95f);
                    holder.name.setTextColor(Color.parseColor("#eeeeee"));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        switch (type){
            case 0:
                return epgChannels.size();
            case 1:
                return movieModels.size();
            case 2:
                return seriesModels.size();
        }
        return 0;
    }

    class HomeListViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        CardView card;
        TextView name;
        HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            card = itemView.findViewById(R.id.card);
            name = itemView.findViewById(R.id.name);
        }
    }
}