package com.newlegacyxc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.newlegacyxc.R;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.SeriesModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GridSeriesAdapter extends BaseAdapter {
    private Context mContext;
    private List<SeriesModel> seriesModels;
    private List<SeriesModel> originalData;
    private int row_index=-1;
    private int height,width;
    private ItemFilter mFilter = new ItemFilter();
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<SeriesModel> list = originalData;

            int count = list.size();
            final ArrayList<SeriesModel> nlist = new ArrayList<>(count);

            SeriesModel seriesModel ;

            for (int i = 0; i < count; i++) {
                seriesModel = list.get(i);
                if (seriesModel.getName().toLowerCase().contains(filterString)) {
                    nlist.add(seriesModel);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            seriesModels = (ArrayList<SeriesModel>) results.values;
            notifyDataSetChanged();
        }

    }

    public GridSeriesAdapter(Context c, List<SeriesModel> seriesModels) {
        mContext = c;
        this.seriesModels = seriesModels;
        originalData = seriesModels;
    }

    public int getCount() {
        return seriesModels.size();
    }

    public Object getItem(int position) {
        return seriesModels.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final SeriesModel seriesModel= seriesModels.get(position);
        if (convertView==null){
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.item_vod, null);
            width = (int)(1.2 * MyApp.SCREEN_WIDTH / mContext.getResources().getInteger(R.integer.span_count_1));
            height = (int)(width* 1.2);
//            convertView.setMinimumHeight(height);
        }
        ImageView movie_icon;
        TextView movie_name;
        RatingBar ratingBar;
//        ConstraintLayout lay_main = convertView.findViewById(R.id.lay_main);
//        if (Build.VERSION.SDK_INT<23){
//            lay_main.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (hasFocus) lay_main.setBackgroundResource(R.color.green_foreground);
//                    else lay_main.setBackgroundResource(R.color.trans_parent);
//                }
//            });
//        }
        movie_icon =convertView.findViewById(R.id.movie_image);
        movie_name =convertView.findViewById(R.id.movie_name);
        ratingBar=convertView.findViewById(R.id.ratingbar);
        movie_name.setText(seriesModel.getName());
        if (seriesModel.getStream_icon()!=null && !seriesModel.getStream_icon().equals(""))
            Picasso.with(mContext)
                    .load(seriesModel.getStream_icon())
                    .resize(width,height)
                    .placeholder(R.drawable.icon)
                    .centerCrop()
                    .into(movie_icon);
//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int prev_i=row_index;
//                row_index=i;
//                notifyItemChanged(prev_i);
//                notifyItemChanged(row_index);
//                viewHolderClicks.onClick(i,movieModel);
//            }
//        });
//        convertView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus){
//                    row_index=i;
//                    viewHolderClicks.onFocus(i,movieModel);
////                    itemView.setBackgroundColor(context.getResources().getColor(R.color.dark_pink));
//                    view.setVisibility(View.VISIBLE);
//                }else {
////                    itemView.setBackgroundColor(context.getResources().getColor(R.color.middle_blue));
//                    view.setVisibility(View.GONE);
//                }
//
//            }
//        });
//        if (row_index==i){
//            Log.e("requestfocus","true");
//            itemView.requestFocus();
//        }

//        if (is_over_lay) {
//            itemView.getBackground().setAlpha(Constants.opacity);
//            movie_icon.setAlpha(0.5f);
//        }else {
//            itemView.getBackground().setAlpha(Constants.opacity_full);
//            movie_icon.setAlpha(1.0f);
//        }

        ratingBar.setIsIndicator(true);
        ratingBar.setRating(Float.valueOf(seriesModel.getRating()));
        return convertView;
    }
}