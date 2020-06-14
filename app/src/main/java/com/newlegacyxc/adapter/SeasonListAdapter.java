package com.newlegacyxc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newlegacyxc.R;
import com.newlegacyxc.models.CategoryModelSeries;
import com.newlegacyxc.utils.Utils;

import java.util.List;

public class SeasonListAdapter extends BaseAdapter {

    Context context;
    List<CategoryModelSeries> datas;
    LayoutInflater inflater;
    int selected_pos;

    public SeasonListAdapter(Context context, List<CategoryModelSeries> datas) {
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
            view = inflater.inflate(R.layout.item_cat_list, viewGroup, false);
        }
        LinearLayout main_lay = (LinearLayout) view.findViewById(R.id.main_lay);
        TextView name_txt = (TextView) view.findViewById(R.id.cat_item_txt);
        name_txt.setText(datas.get(i).getName());
        if (selected_pos == i) {
            main_lay.setBackgroundResource(R.drawable.lang_item_high);
        } else {
            main_lay.setBackgroundColor(Color.TRANSPARENT);
        }
        main_lay.setPadding(Utils.dp2px(context, 10), Utils.dp2px(context, 5), Utils.dp2px(context, 10), Utils.dp2px(context, 5));
        return view;
    }

    public void selectItem(int pos) {
        selected_pos = pos;
        notifyDataSetChanged();
    }
}
