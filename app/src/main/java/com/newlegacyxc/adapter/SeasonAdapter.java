package com.newlegacyxc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.newlegacyxc.R;
import com.newlegacyxc.models.SeasonModel;
import com.squareup.picasso.Picasso;

import java.util.List;


public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.ViewHolder>{
    private Context context;
    private List<SeasonModel> seasonModels;
    private int row_index=0;
    private IMyViewHolderClicks viewHolderClicks;

    public SeasonAdapter(Context context, List<SeasonModel> seasonModels, IMyViewHolderClicks viewHolderClicks){
        this.seasonModels = seasonModels;
        this.context=context;
        this.viewHolderClicks = viewHolderClicks;
    }

    @NonNull
    @Override
    public SeasonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_season, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SeasonAdapter.ViewHolder viewHolder, @SuppressLint("RecyclerView") final int i) {
        final SeasonModel movieModel= seasonModels.get(i);
        viewHolder.movie_name.setText(movieModel.getName());
        if (movieModel.getIcon()!=null && !movieModel.getIcon().equals(""))
        Picasso.with(context)
                .load(movieModel.getIcon())
                .resize(170,250)
                .placeholder(R.drawable.icon)
                .centerCrop()
                .into(viewHolder.movie_icon);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int prev_i=row_index;
                row_index=i;
                Log.e("seasonAdapter","clicked "+i);
                notifyItemChanged(prev_i);
                notifyItemChanged(row_index);
                viewHolderClicks.onClick(i,movieModel);
            }
        });
        viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    row_index=i;
                    viewHolderClicks.onFocus(i,movieModel);
                    Log.e("seasonAdapter","focused "+i);
//                    viewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.dark_pink));
                    viewHolder.view.setVisibility(View.VISIBLE);
                }else {
//                    viewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.middle_blue));
                    viewHolder.view.setVisibility(View.GONE);
                }

            }
        });
        if (row_index==i){
            boolean result = viewHolder.itemView.requestFocus();
            Log.e("requestfocus","result "+result);
        }
    }

    @Override
    public int getItemCount() {
        return seasonModels.size();
    }

    public interface IMyViewHolderClicks {
        void onClick(int i, SeasonModel movieModel);
        void onFocus(int i, SeasonModel movieModel);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView movie_icon;
        TextView movie_name;
        View view;
        ViewHolder(View view){
            super(view);
            movie_icon =view.findViewById(R.id.movie_image);
            movie_name =view.findViewById(R.id.movie_name);
            this.view = view.findViewById(R.id.view);
        }
    }

    public void setSeasonModels(List<SeasonModel> seasonModels){
        this.seasonModels = seasonModels;
        row_index=0;
        notifyDataSetChanged();
    }

    public void setRow_index(int i){
        row_index=i;
        notifyItemChanged(row_index);
    }
}
