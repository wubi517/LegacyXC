package com.newlegacyxc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.EPGEvent;
import com.newlegacyxc.utils.Utils;

import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class EpgListAdapter extends BaseAdapter{

    Context context;
    List<EPGEvent> datas;
    LayoutInflater inflater;
    int selected_pos;
    TextView title,time;
    LinearLayout main_lay;
    ImageView image_clock;
    public EpgListAdapter(Context context, List<EPGEvent> datas) {
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
            convertView = inflater.inflate(R.layout.item_epg_list, parent, false);
        }
        main_lay = (LinearLayout) convertView.findViewById(R.id.main_lay);
        title = (TextView) convertView.findViewById(R.id.main_epg_txt);
        time = (TextView)convertView.findViewById(R.id.main_date_num);
        image_clock = (ImageView)convertView.findViewById(R.id.image_clock);

        title.setText(datas.get(position).getTitle());
        time.setText(Constants.Offset(true,datas.get(position).getStartTime()));
        if(datas.get(position).getMark_archive()==1){
            image_clock.setVisibility(View.VISIBLE);
        }else {
            image_clock.setVisibility(View.GONE);
        }
        if (selected_pos == position) {
            main_lay.setBackgroundResource(R.drawable.list_yellow_bg);
            time.setBackgroundResource(R.drawable.white_btn_border);
            title.setTextColor(Color.BLACK);
        } else {
            title.setTextColor(context.getResources().getColor(R.color.num_color1));
            time.setTextColor(Color.BLACK);
            time.setBackgroundResource(R.drawable.yellow_btn_border);
            main_lay.setBackgroundResource(R.drawable.list_bg);
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
