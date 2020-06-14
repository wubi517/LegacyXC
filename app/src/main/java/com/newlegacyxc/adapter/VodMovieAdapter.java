package com.newlegacyxc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.newlegacyxc.R;
import com.newlegacyxc.models.MovieModel;
import com.squareup.picasso.Picasso;

import java.util.List;


public class VodMovieAdapter extends RecyclerView.Adapter<VodMovieAdapter.ViewHolder>{
    private Context context;
    private List<MovieModel> movieModels;
    private int row_index=-1;
    private IMyViewHolderClicks viewHolderClicks;
    private boolean is_over_lay=false;
    private int height,width;
    public VodMovieAdapter(Context context, List<MovieModel> movieModels, IMyViewHolderClicks viewHolderClicks){
        this.movieModels = movieModels;
        this.context=context;
        this.viewHolderClicks = viewHolderClicks;
    }

    public void setIs_over_lay(boolean is_over_lay) {
        this.is_over_lay = is_over_lay;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VodMovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_vod, viewGroup, false);
        width = viewGroup.getMeasuredWidth() / itemView.getContext().getResources().getInteger(R.integer.span_count_1);
        height = (int)(width* 1.2);
        itemView.setMinimumHeight(height);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final VodMovieAdapter.ViewHolder viewHolder, @SuppressLint("RecyclerView") final int i) {
        final MovieModel movieModel= movieModels.get(i);
        viewHolder.movie_name.setText(movieModel.getName());
        if (movieModel.getStream_icon()!=null && !movieModel.getStream_icon().equals(""))
        Picasso.with(context)
                .load(movieModel.getStream_icon())
                .resize(width,height)
                .placeholder(R.drawable.icon)
                .centerCrop()
                .into(viewHolder.movie_icon);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int prev_i=row_index;
                row_index=i;
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
//                    viewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.dark_pink));
                    viewHolder.view.setVisibility(View.VISIBLE);
                }else {
//                    viewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.middle_blue));
                    viewHolder.view.setVisibility(View.GONE);
                }

            }
        });
//        if (row_index==i){
//            Log.e("requestfocus","true");
//            viewHolder.itemView.requestFocus();
//        }

//        if (is_over_lay) {
//            viewHolder.itemView.getBackground().setAlpha(Constants.opacity);
//            viewHolder.movie_icon.setAlpha(0.5f);
//        }else {
//            viewHolder.itemView.getBackground().setAlpha(Constants.opacity_full);
//            viewHolder.movie_icon.setAlpha(1.0f);
//        }

        viewHolder.ratingBar.setIsIndicator(true);
        viewHolder.ratingBar.setRating((float)(movieModel.getRating()));
    }

    @Override
    public int getItemCount() {
        return movieModels.size();
    }

    public interface IMyViewHolderClicks {
        void onClick(int i, MovieModel movieModel);
        void onFocus(int i, MovieModel movieModel);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView movie_icon;
        TextView movie_name;
        RatingBar ratingBar;
        View view;
        ViewHolder(View view){
            super(view);
            movie_icon =view.findViewById(R.id.movie_image);
            movie_name =view.findViewById(R.id.movie_name);
            ratingBar=view.findViewById(R.id.ratingbar);
            this.view = view.findViewById(R.id.view);
        }
    }

    public void setMovieModels(List<MovieModel> movieModels){
        this.movieModels=movieModels;
        row_index=-1;
        notifyDataSetChanged();
    }

    public void setRow_index(int i){
        row_index=i;
        notifyItemChanged(row_index);
    }
}
