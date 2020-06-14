package com.newlegacyxc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.newlegacyxc.R;
import com.newlegacyxc.models.MovieModel;

import java.util.List;

/**
 * Created by RST on 2/26/2017.
 */

public class EpisodeListAdapter extends BaseAdapter {

    Context context;
    List<MovieModel> datas;
    LayoutInflater inflater;
    int selected_pos;

    public EpisodeListAdapter(Context context, List<MovieModel> datas) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_episode_list, viewGroup, false);
        }
        TextView item = (TextView) view.findViewById(R.id.episode_item_txt);
        item.setText(datas.get(i).getName());
        return view;
    }
    public void selectItem(int pos) {
        selected_pos = pos;
        notifyDataSetChanged();
    }
}
