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
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class DateListAdapter extends BaseAdapter{

    private Context context;
    private List<String > datas;
    private LayoutInflater inflater;
    private int selected_pos;
    private TextView txt_date;
    private LinearLayout main_lay;
    public DateListAdapter(Context context, List<String> datas) {
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
            convertView = inflater.inflate(R.layout.item_date_list, parent, false);
        }
        main_lay = (LinearLayout) convertView.findViewById(R.id.main_lay);
        txt_date = (TextView)convertView.findViewById(R.id.txt_date);
        try {
            Date date=new SimpleDateFormat("yyyy-MM-dd").parse(datas.get(position));
            txt_date.setText(new SimpleDateFormat("EEE, dd MMM").format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (selected_pos == position) {
            main_lay.setBackgroundResource(R.drawable.list_yellow_epg_bg);
            txt_date.setTextColor(Color.BLACK);
        } else {
            txt_date.setTextColor(context.getResources().getColor(R.color.date_color));
            main_lay.setBackgroundResource(R.drawable.list_item_epg_draw);
        }

        if(MyApp.instance.getPreference().get(Constants.IS_PHONE)!=null){
            main_lay.setPadding(Utils.dp2px(context, 3), Utils.dp2px(context, 3), Utils.dp2px(context, 3), Utils.dp2px(context, 3));
        }else {
            main_lay.setPadding(Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5));
        }

        return convertView;
    }

    public void selectItem(int pos) {
        selected_pos = pos;
        notifyDataSetChanged();
    }
}
