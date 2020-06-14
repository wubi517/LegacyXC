package com.newlegacyxc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.newlegacyxc.R;

public class HideCategoryListAdapter extends BaseAdapter {
    Context context;
    String[] datas;
    LayoutInflater inflater;
    boolean[] checks;
    OnClick onClick;
    public HideCategoryListAdapter(Context context, String[]  data, final boolean[] checks, OnClick onClick) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.datas = data;
        this.checks = checks;
        this.onClick = onClick;
    }

    @Override
    public int getCount() {
        return datas.length;
    }

    @Override
    public Object getItem(int position) {
        return datas[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_hide_category, parent, false);
        }
        final CheckedTextView textView = (CheckedTextView) convertView.findViewById(R.id.checked_textview);
        textView.setText(datas[position]);
        textView.setChecked(checks[position]);
//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                checkBox.setChecked(!checkBox.isChecked());
//                onClick.onClick(position, checkBox.isChecked());
//                checks[position]=checkBox.isChecked();
//            }
//        });
        return convertView;
    }

    public void toggleChecked(int position){
        checks[position] = !checks[position];
        notifyDataSetChanged();
    }

    public interface OnClick{
        boolean onClick(int position, boolean checked);
    }
}
