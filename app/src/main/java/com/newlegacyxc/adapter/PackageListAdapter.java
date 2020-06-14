package com.newlegacyxc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.newlegacyxc.R;

import java.util.List;

/**
 * Created by RST on 7/23/2017.
 */

public class PackageListAdapter extends BaseAdapter {

    Context context;
    List<String> datas;
    LayoutInflater inflater;

    public PackageListAdapter(Context context, List<String> datas) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.datas = datas;
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
            convertView = inflater.inflate(R.layout.item_pack_list, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.pack_list_item);
        textView.setText(datas.get(position));
        return convertView;
    }
}
