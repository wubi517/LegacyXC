package com.newlegacyxc.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newlegacyxc.R;
import com.newlegacyxc.models.CategoryModel;
import com.newlegacyxc.utils.Utils;

import java.util.List;

public class CategoryListMultiAdapter extends BaseAdapter {
    Context context;
    private List<CategoryModel> datas;
    private LayoutInflater inflater;
    private int selected_pos;
    private TextView title;
    private LinearLayout main_lay;
    private boolean is_blue_bg=false;
    public CategoryListMultiAdapter(Context context, List<CategoryModel> datas) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.datas = datas;
        this.is_blue_bg = is_blue_bg;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_category_list1, parent, false);
        }
        main_lay = (LinearLayout)convertView.findViewById(R.id.main_lay);
        title = (TextView)convertView.findViewById(R.id.categry_list_txt);
        title.setText(datas.get(position).getName());
        title.setOnFocusChangeListener((new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) title.setTypeface(null, Typeface.BOLD);
                else title.setTypeface(null, Typeface.NORMAL);
            }
        }));
        main_lay.setPadding(Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5));
        return convertView;
    }
    public void selectItem(int pos) {
        selected_pos = pos;
        notifyDataSetChanged();
    }
}
